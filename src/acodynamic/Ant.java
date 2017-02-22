package acodynamic;

import java.util.Random;
import java.util.Stack;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * @author Jack Dunning 11013982
 */
public class Ant extends Rectangle {    
    private final int antID;
    private final static double width = 10.0;
    private final static double height = 10.0;
    private Node currentNode;
    private Edge currentEdge;
    private Node nextNode;
    private Edge prevEdge;
    private boolean outwardBound;
    private Stack<Edge> edgesTaken;
    private Stack<Node> nodesTaken;
    
    private double prevRouteLength;
    
    public Ant(int antID, Node spawnLocation) {
        super(spawnLocation.getCenterX() - (width/2), spawnLocation.getCenterY() - (height/2), width, height);
        super.setFill(Color.CRIMSON);
        this.antID = antID;
        this.currentNode = spawnLocation;
        this.outwardBound = true;
        this.edgesTaken = new Stack<>();
        this.nodesTaken = new Stack<>();
    }
    
    /**
     * Gets the ants id
     * @return antID
     */
    public int getAntID() {
        return antID;
    }
    
    /**
     * Gets the center x
     * @return x
     */
    public double getCenterX() {
        return getX() + getWidth() / 2;
    }
    
    /**
     * Gets the center y
     * @return y
     */
    public double getCenterY() {
        return getY() + getHeight() / 2;
    }
    
    /**
     * Gets the current node
     * @return currentNode
     */
    public Node getCurrentNode() {
        return currentNode;
    }

    /**
     * Sets the current node
     * @param currentNode the current node
     */
    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    /**
     * Sets the current edge
     * @param currentEdge the current edge
     */
    public void setCurrentEdge(Edge currentEdge) {
        this.currentEdge = currentEdge;
    }

    /**
     * Gets the next node
     * @return nextNode
     */
    public Node getNextNode() {
        return nextNode;
    }

    /**
     * Sets the next node
     * @param nextNode the next node
     */
    public void setNextNode(Node nextNode) {
        this.nextNode = nextNode;
    }

    /**
     * Gets if the ant is outward bound
     * @return outwardBound
     */
    public boolean isOutwardBound() {
        return outwardBound;
    }

    /**
     * Sets the outwardBound switch and changes ant colour
     * @param outwardBound true if travelling to destination
     */
    public void setOutwardBound(boolean outwardBound) {
        this.outwardBound = outwardBound;
        if (!this.outwardBound) {
            super.setFill(Color.GREEN);
        } else {
            super.setFill(Color.CRIMSON);
        }
    }

    /**
     * Gets the edges taken
     * @return edgesTaken
     */
    public Stack<Edge> getEdgesTaken() {
        return edgesTaken;
    }

    /**
     * Pushes to the edges taken stack
     * @param e edge to push
     */
    public void pushEdgeTaken(Edge e) {
        this.edgesTaken.push(e);   
    }
    
    /**
     * Pops from the edges taken stack
     * @return Edge
     */
    public Edge popEdgeTaken() {
        return this.edgesTaken.pop();
    }
    
    /**
     * Peeks at the edges taken stack
     * @return Edge
     */
    public Edge peekEdgeTaken() {
        return this.edgesTaken.peek();
    }

    /**
     * Gets the nodes taken
     * @return nodesTaken
     */
    public Stack<Node> getNodesTaken() {
        return nodesTaken;
    }

    
    /**
     * Pushes to the node taken stack
     * @param n node to push
     */
    public void pushNodeTaken(Node n) {
        nodesTaken.push(n);
    }
    
    /**
     * Pops from the node taken stack
     * @return Node
     */
    public Node popNodeTaken() {
        return this.nodesTaken.pop();
    }
    
    /**
     * Peeks at the node taken stack
     * @return Node
     */
    public Node peekNodeTaken() {
        return this.nodesTaken.peek();
    }

    /**
     * Gets the previous route length (cost)
     * @return prevRouteLength
     */
    public double getPrevRouteLength() {
        return prevRouteLength;
    }

    /**
     * Sets the previous route length (cost)
     * @param prevRouteLength the previous route cost
     */
    public void setPrevRouteLength(double prevRouteLength) {
        this.prevRouteLength = prevRouteLength;
    }
    
    /**
     * Chooses an edge to take using probability based upon pheromone values. If
     * only one edge is available it is returned instantly. Should check for 
     * destination node being an option before calling this function!
     * @return edge chosen
     */
    public Edge chooseEdge() {
        int maxNumGen = 0;
        
        if (currentNode.getConnectedEdges().size() == 2 && !currentNode.isSource()) {
            // Two options, but one is previous so only 1 valid option
            for (Edge e : currentNode.getConnectedEdges()) {
                if (!e.equals(prevEdge)) {

                    for (Node n : e.getConnectedNodes()) {
                        if (!n.equals(currentNode)) {
                            nextNode = n;
                            prevEdge = e;
                            
                            // Remove loops in the route
                            Edge replacementEdge = removeLoopsTaken(n);
                            
                            if (replacementEdge != null) {
                                e = replacementEdge;
                            }
                            
                            pushNodeTaken(n);
                            pushEdgeTaken(e);
                            currentEdge = e;
                            return e;
                        }
                    }
                    
                }
            }
            
        } else if (currentNode.getConnectedEdges().size() > 1) {
            // Choose randomly
            for (Edge e : currentNode.getConnectedEdges()) {
                // Previous edge is not valid if more options are available
                if (!e.equals(prevEdge)) {
                    maxNumGen += e.getPheromone();
                }  
            }

            Random r = new Random();
            int selected = r.nextInt(maxNumGen+1); // Choose a random value in range

            // Keep adding pheromone from edges in order until the selected
            // value is reached or exceeded, in which case return that edge
            int sum = 0;
            for (Edge e : currentNode.getConnectedEdges()) {
                // Previous edge is not valid if more options are available
                    if (!e.equals(prevEdge)) {
                        sum += e.getPheromone();
                        if (sum >= selected) {

                            // Set the next node as the one chosen and prev as current
                            for (Node n : e.getConnectedNodes()) {
                                if (!n.equals(currentNode)) {
                                    nextNode = n;
                                    prevEdge = e;
                                    
                                    // Remove loops in the route
                                    Edge replacementEdge = removeLoopsTaken(n);
                                    
                                    if (replacementEdge != null) {
                                        e = replacementEdge;
                                    }
                                    
                                    pushNodeTaken(n);
                                    pushEdgeTaken(e);
                                    currentEdge = e;
                                    return e;
                                }
                            }
                            
                        }
                    }
            }
        } else {
            // Dead end or only 1 option, take it!
            nextNode = currentNode.getConnectedNodes().get(0);

            // Remove loops in the route
            removeLoopsTaken(nextNode);
            
            pushNodeTaken(nextNode);
            pushEdgeTaken(currentNode.getConnectedEdges().get(0));
            currentEdge = currentNode.getConnectedEdges().get(0);
            prevEdge = currentNode.getConnectedEdges().get(0);
            return currentNode.getConnectedEdges().get(0);
        }
        return null;
    }
    
    /**
     * Before a new node is pushed on the stack, this function checks if it is 
     * already in the stack. If it is, the node stack is popped until the loop
     * has been removed. The edge stack is popped the same number of times, but
     * returns the last edge so that it can be re-added in chooseEdge().
     * 
     * @param nodeAdded - the node to check for
     * @return edge to add back onto the stack
     */
    public Edge removeLoopsTaken(Node nodeAdded) {
        // Remove node loops
        while (nodesTaken.contains(nodeAdded)) {
            // Pop nodes until repeated node is gone
            this.popNodeTaken().getNodeID();
            
            if (nodesTaken.contains(nodeAdded)) {
                this.popEdgeTaken().getEdgeID(); 
            } else {
                // Return this edge so it can be added again
                return this.popEdgeTaken();
            }
            
        }
        return null;
    }
    
    /**
     * Updates the ants x and y position so that it travels towards the next node
     */
    public void travel() {
        // Change this to increase distance travelled per location update
        // NOTE: Max 10.0 or nodes will be missed!
        double pixelsPerStep = 5.0;
        
        // Travel towards the next node
        double currentNodeX = this.getCurrentNode().getCenterX();
        double currentNodeY = this.getCurrentNode().getCenterY();
        double nextNodeX = this.getNextNode().getCenterX();
        double nextNodeY = this.getNextNode().getCenterY();

        // Difference in x/y position
        double xDiff = Math.abs(currentNodeX - nextNodeX);
        double yDiff = Math.abs(currentNodeY - nextNodeY);
        
        // Pythagoras
        double magnitude = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        
        // Calculates the size of step + apply traffic
        double xStepSize = ((xDiff/magnitude) * pixelsPerStep) / currentEdge.getTraffic();
        double yStepSize = ((yDiff/magnitude) * pixelsPerStep)/ currentEdge.getTraffic();

        
        // Move x direction
        if (currentNodeX > nextNodeX) {
            this.setX(this.getX() - xStepSize);
        } else if (currentNodeX < nextNodeX) {
            this.setX(this.getX() + xStepSize);
        }

        // Move y direction
        if (currentNodeY > nextNodeY) {
            this.setY(this.getY() - yStepSize);
        } else if (currentNodeY < nextNodeY) {
            this.setY(this.getY() + yStepSize);
        }
    }
    
    /**
     * Resets all the routing data - used when looped and at source node
     */
    public void reset() {
        this.nodesTaken = new Stack<>();
        this.edgesTaken = new Stack<>();
        this.prevEdge = null;
        this.nextNode = null;
    }
}
