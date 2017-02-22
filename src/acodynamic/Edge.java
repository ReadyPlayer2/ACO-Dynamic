package acodynamic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.shape.Line;

/**
 * @author Jack Dunning 11013982
 */
public class Edge extends Line implements Serializable {
    // Serialize ID
    private static final long serialVersionUID = 6529685098267757690L;
    
    private int edgeID;
    private List<Node> connectedNodes = new ArrayList<>(1); // Edge is between 2 nodes
    private double pheromone;
    private final double minPheromone = 1.0;
    private final double maxPheromone = 1000.0;
    private double cost;
    private double traffic;
    
    /**
     * Constructor for edges
     * @param id - edge id
     * @param sourceX - start x
     * @param sourceY - start y
     * @param destX - end x
     * @param destY - end y
     */
    public Edge(int id, double sourceX, double sourceY, double destX, double destY) {
        super(sourceX, sourceY, destX, destY);
        this.edgeID = id;
        this.cost = getEdgeLength();
        this.pheromone = minPheromone;
        this.traffic = 1.0;
    }

    /**
     * Gets the edge id
     * @return edgeID
     */
    public int getEdgeID() {
        return edgeID;
    }

    /**
     * Gets the list of connected nodes (max 2)
     * @return connectedNodes
     */
    public List<Node> getConnectedNodes() {
        return connectedNodes;
    }

    /**
     * Adds a connected node
     * @param n - the node to be added
     */
    public void addConnectedNode(Node n) {
        connectedNodes.add(n);
    }

    /**
     * Gets the quantity of pheromone on this edge
     * @return pheromone - the pheromone value
     */
    public double getPheromone() {
        return Math.round(pheromone);
    }

    /**
     * Sets the quantity of pheromone on this edge as long as it is within
     * min and max pheromone bounds
     * @param pheromone - the pheromone value
     * @param isLimited - is there min/max limits for this edge
     */
    public void setPheromone(double pheromone, boolean isLimited) {
        if (isLimited) {
            if (pheromone > minPheromone && pheromone < maxPheromone) {
                this.pheromone = pheromone;
            } else if (pheromone > maxPheromone) {
                this.pheromone = maxPheromone;
            } else if (pheromone < minPheromone) {
                this.pheromone = minPheromone;
            } 
        } else {
            if (pheromone < minPheromone) {
                this.pheromone = minPheromone;
            } else {
                this.pheromone = pheromone;
            }
        }
    }
    
    /**
     * Adds the amount to the pheromone on this edge
     * @param pheromone - the pheromone value
     * @param isLimited - is there min/max limits for this edge
     */
    public void addPheromone(double pheromone, boolean isLimited) {
        if (isLimited) {
            if (this.pheromone + pheromone < maxPheromone) {
                this.pheromone += pheromone;
            } else {
                System.out.println("Hitting max for edge:"+this.getEdgeID());
                this.pheromone = maxPheromone;
            }
        } else {
            this.pheromone += pheromone;
        }
    }

    /**
     * Gets the cost to traverse the edge
     * @return cost
     */
    public double getCost() {
        return cost * getTraffic();
    }    

    /**
     * Gets the traffic for this edge
     * @return traffic
     */
    public double getTraffic() {
        return traffic;
    }
    
    /**
     * Sets the traffic for this edge
     * @param traffic the traffic value
     */
    public void setTraffic(double traffic) {
        this.traffic = traffic;
    }
    
    /**
     * Adds traffic to this edge
     * @param amount the value to increase traffic
     */
    public void addTraffic(double amount) {
        this.traffic += amount;
    }
    
    /**
     * Removes traffic from this edge
     * @param amount the value to decrease traffic
     */
    public void reduceTraffic(double amount) {
        if (this.traffic - amount > 1.0) {
            this.traffic -= amount;
        } else {
            this.traffic = 1.0;
        }
    }
    
     /**
     * Gets the Euclidean distance (length) between the edges start and end 
     * using Pythagoras theorem
     * @param n
     * @return euclidian length of edge
     */
    private double getEdgeLength() {
        // h = sqrt(dx * dx + dy * dy)        
        double dx = Math.abs((super.getStartX() - super.getEndX()));
        double dy = Math.abs((super.getStartY() - super.getEndY()));

        return Math.sqrt((dx * dx) + (dy * dy));
    }
    
    /**
     * Custom serialization
     * @param oos ObjectOutputStream
     * @throws IOException required throw
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeInt(edgeID);
        
        // Avoid StackOverflowException from deeply nested structures
        oos.writeInt(connectedNodes.size());
        for (Node n: connectedNodes) {
            oos.writeObject(n);
        }

        oos.writeDouble(pheromone);
        oos.writeDouble(cost);
        oos.writeDouble(traffic);
        oos.writeDouble(super.getStartX());
        oos.writeDouble(super.getStartY());
        oos.writeDouble(super.getEndX());
        oos.writeDouble(super.getEndY());
    }
    
    /**
     * Custom de-serialization
     * @param ois ObjectInputStream
     * @throws IOException required throw
     * @throws ClassNotFoundException required throw
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.edgeID = ois.readInt();
        
        // Avoid StackOverflowException from deeply nested structures
        int size = ois.readInt();
        this.connectedNodes = new ArrayList<>(1);
        for (int i = 0; i < size; i++) {
            this.connectedNodes.add((Node) ois.readObject());
        }
        
        this.pheromone = ois.readDouble();
        this.cost = ois.readDouble();
        this.traffic = ois.readDouble();
        super.setStartX(ois.readDouble());
        super.setStartY(ois.readDouble());
        super.setEndX(ois.readDouble());
        super.setEndY(ois.readDouble());
    }
}

