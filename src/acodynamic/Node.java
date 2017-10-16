package acodynamic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * @author ReadyPlayer2
 */
public class Node extends Circle implements Serializable {
    // Serialize ID
    private static final long serialVersionUID = 6529685098267757690L;
    
    private int nodeID;
    private static final double SIZE = 4;
    private int MAXCONNECTIONS = 5;
    private boolean highlighted = false; // Purple
    private boolean source = false; // Green
    private boolean destination = false; // Red
    private List<Node> connectedNodes = new ArrayList<>(MAXCONNECTIONS);
    private List<Edge> connectedEdges = new ArrayList<>(MAXCONNECTIONS);
    
    /**
     * Constructor for Nodes
     * @param id - Node id
     * @param x - x position
     * @param y - y position
     */
    public Node(int id, double x, double y) {
        super(x, y, SIZE, Color.BLACK);
        this.nodeID = id;
    }

    public int getNodeID() {
        return nodeID;
    }

    public double getX() {
        return super.getCenterX();
    }

    public double getY() {
        return super.getCenterY();
    }

    public int getConnectionsCount() {
        return connectedNodes.size();
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        
        if (highlighted) {
            super.setFill(Color.DARKMAGENTA);
        } else {
            // Set the node to its original colour
            if (this.source) {
                super.setFill(Color.GREEN);
            } else if (this.destination) {
                super.setFill(Color.RED);
            } else {
                super.setFill(Color.BLACK);    
            }
        }
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;

        if (source) {
            super.setFill(Color.GREEN);
            super.setRadius(SIZE*2);
        } else {
            super.setFill(Color.BLACK);
            super.setRadius(SIZE);
        }
    }

    public boolean isDestination() {
        return destination;
    }

    public void setDestination(boolean destination) {
        this.destination = destination;

        if (destination) {
            super.setFill(Color.RED);
            super.setRadius(SIZE*2);
        } else {
            super.setFill(Color.BLACK);
            super.setRadius(SIZE);
        }
    }

    public List<Node> getConnectedNodes() {
        return connectedNodes;
    }
    
    /**
     * Mutually add the connection if not already connected
     * @param node - the connected node to add
     */
    public void addConnectedNode(Node node) {
        if (connectedNodes.size() != MAXCONNECTIONS) {
            if (!connectedNodes.contains(node)) {
                // Mutually add the connection
                connectedNodes.add(node);
                node.addConnectedNode(this);
            }
        }
    }
    
    /**
     * Mutually remove the connection if not already connected
     * @param node - the connected node to remove
     */
    public void removeConnectedNode(Node node) {
        if (connectedNodes.size() > 0) {
            if (connectedNodes.contains(node)) {
                // Mutually removes the connection
                connectedNodes.remove(node);
                node.removeConnectedNode(this);
            }
        }
    }

    public List<Edge> getConnectedEdges() {
        return connectedEdges;
    }
    
    public void addConnectedEdge(Edge e) {
        connectedEdges.add(e);
    }
    
    public void removeConnectedEdge(Edge e) {
        connectedEdges.remove(e);
    }
    
    /**
     * Custom serialization
     * @param oos ObjectOutputStream
     * @throws IOException required throw
     */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeInt(nodeID);
        oos.writeInt(MAXCONNECTIONS);
        oos.writeBoolean(highlighted);
        oos.writeBoolean(source);
        oos.writeBoolean(destination);
        
        // Avoid StackOverflowException from deeply nested structures
        oos.writeInt(connectedNodes.size());
        for (Node n: connectedNodes) {
            oos.writeObject(n);
        }
        
        // Avoid StackOverflowException from deeply nested structures
        oos.writeInt(connectedEdges.size());
        for (Edge e: connectedEdges) {
            oos.writeObject(e);
        }
        
        oos.writeDouble(super.getCenterX());
        oos.writeDouble(super.getCenterY());
    }
    
    /**
     * Custom de-serialization
     * @param ois ObjectInputStream
     * @throws IOException required throw
     * @throws ClassNotFoundException required throw
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        this.nodeID = ois.readInt();
        this.MAXCONNECTIONS = ois.readInt();
        this.highlighted = ois.readBoolean();
        this.source = ois.readBoolean();
        this.destination = ois.readBoolean();
        
        // Avoid StackOverflowException from deeply nested structures
        int size = ois.readInt();
        this.connectedNodes = new ArrayList<>(this.MAXCONNECTIONS);
        for (int i = 0; i < size; i++) {
            this.connectedNodes.add((Node) ois.readObject());
        }
        
        // Avoid StackOverflowException from deeply nested structures
        size = ois.readInt();
        this.connectedEdges = new ArrayList<>(this.MAXCONNECTIONS);
        for (int i = 0; i < size; i++) {
            this.connectedEdges.add((Edge) ois.readObject());
        }
        
        super.setCenterX(ois.readDouble());
        super.setCenterY(ois.readDouble());
        super.setRadius(SIZE); // Default
        
        // Correct colour and size
        if (source) {
            super.setFill(Color.GREEN);
            super.setRadius(SIZE*2);
        } else if (destination) {
            super.setFill(Color.RED);
            super.setRadius(SIZE*2);
        } else if (highlighted) {
            super.setFill(Color.DARKMAGENTA);
        } else {
            super.setFill(Color.BLACK);
        }
    }
    
}
