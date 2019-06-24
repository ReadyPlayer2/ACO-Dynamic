package com.readyplayer2.aco.dynamic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * @author ReadyPlayer2
 */
public class ACODynamic extends Application implements Serializable {
    // Serialize ID
    private static final long serialVersionUID = 6529685098267757690L;
    
    private Stage window; // The entire window
    private BorderPane layout;
    private Pane simulationArea; // Pane to hold nodes, edges, ants
    private Scene scene;
    
    // Get screen size
    private final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
    private final double SCREENWIDTH = screenBounds.getWidth();
    private final double SCREENHEIGHT = screenBounds.getHeight();
    
    // MenuBar, Menus, MenuItems
    private final MenuBar menuBar = new MenuBar();
    // File menu
    private final Menu file = new Menu("File");
    private final MenuItem newFile = new MenuItem("New");
    private final MenuItem openEnvironment = new MenuItem("Open Environment...");
    private final MenuItem saveEnvironment = new MenuItem("Save Environment...");
    private final SeparatorMenuItem separator = new SeparatorMenuItem();
    private final MenuItem exit = new MenuItem("Exit");
    // Edit menu
    private final Menu edit = new Menu("Edit");
    private final MenuItem settings = new MenuItem("Settings...");
    // Help menu
    private final Menu help = new Menu("Help");
    private final MenuItem userGuide = new MenuItem("User Guide...");
    // CSS menu
    private final Menu css = new Menu("CSS");
    private final MenuItem defaultCSS = new MenuItem("Default");
    private final MenuItem mapCSS = new MenuItem("Map");
    private final MenuItem darkCSS = new MenuItem("Dark");
    
    // MenuBar height, used to offset positions correctly
    private final double MENUBARHEIGHT = 25;
    // Multipliers used to ensure correct proportions (dynamic sizing)
    private final double SIMULATIONMULTIPLIER = 0.8;
    private final double LOGMULTIPLIER = 0.2;
    
    // Right section
    private TextArea log;
    private ScrollPane scrollPane;
    private final VBox rightVBox = new VBox();
    private final HBox tier1HBox = new HBox(); // Generate Buttons
    private final HBox tier2Hbox = new HBox(); // Start/stop, reset Buttons
    private final HBox tier3HBox = new HBox(); // Toggle for traffic
    private final HBox tier4HBox = new HBox(); // Algorithm
    private final HBox tier5HBox = new HBox(); // Runtime
    private final HBox tier6HBox = new HBox(); // Best route cost
    private final HBox tier7HBox = new HBox(); // Automated Simulations
    
    // Buttons
    private final Button generateNodes = new Button("Generate Nodes");
    private final Button generateEdges = new Button("Generate Edges");
    private final Button startStop = new Button("Start");
    private final Button reset = new Button("Reset");
    private final Button automatedSimulation = new Button("Run Automated Simulations");
    
    // Time 
    private final Label runtimeLabel = new Label("Simulation runtime:");
    private final Label runtimeValueLabel = new Label("0s");
    
    // Best Route Label
    private final Label bestRouteCostLabel = new Label("Best Route Cost:");
    private final Label bestRouteCostValueLabel = new Label("----");
    
    // Choice Box
    private final Label mouseModeLabel = new Label("Mouse Mode:");
    private ChoiceBox<String> mouseModeChoiceBox;
    private final Label algorithmLabel = new Label("Algorithm:");
    private ChoiceBox<String> algorithmChoiceBox;
    
    // Enum to determine algorithm being used
    private enum ALGORITHM {AS, MMAS};
    private ALGORITHM algorithm = ALGORITHM.AS;
    
    // Settings window variables
    private Stage settingsStage;
    private GridPane settingsGrid;
    // MAXANTS
    private final Label maxAntsLabel = new Label("Max ants: ");
    private Slider maxAntsSlider;
    private Label maxAntsValue;
    // MAXNODES
    private final Label maxNodesLabel = new Label("Max nodes: ");
    private Slider maxNodesSlider;
    private Label maxNodesValue;
    // Pheromone multiplier
    private final Label pherMultiplierLabel = new Label("Pheromone multiplier: ");
    private Slider pherMultiplierSlider;
    private Label pherMultiplierValue;
    // Save button
    private final Button saveSettingsButton = new Button("Save and Close");
    
    // Automated Simulation variables
    private Stage automatedSimStage;
    private GridPane automatedSimGrid;
    // Iterations (number of times to run sims)
    private int maxIterations = 1;
    private final Label maxIterationsLabel = new Label("Simulations per run: ");
    private Slider maxIterationsSlider;
    private Label maxIterationsValue;
    // Runtime of sims
    private long maxRuntime = 30;
    private final Label maxRuntimeLabel = new Label("Runtime per simulation (s): ");
    private Slider maxRuntimeSlider;
    private Label maxRuntimeValue;
    // Run button
    private final Button runAutomatedSimButton = new Button("Run");
    // Switch
    private boolean automatedSim = false;
    
    // Max parameters
    private int MAXNODES = 200;
    private final int MAXCONNECTIONS = 5;
    private int MAXANTS = 100; 
    
    // Incrementable counters for unique IDs
    private int uniqueNodeID = 0;
    private int uniqueEdgeID = 0;
    private int uniqueAntID = 0;
    
    // List to hold nodes, edges, ants, best route edges
    private List<Node> nodeList = new ArrayList();
    private List<Edge> edgeList = new ArrayList();
    private List<Ant> antList = new ArrayList();
    private List<Edge> bestRouteEdgeList = new ArrayList();
    
    // Pointers to source and destination nodes
    private Node sourceNode = null;
    private Node destNode = null;
    
    // Game loop
    private AnimationTimer simulationLoop;
    
    // Runtime
    long simulationRuntime = 0;
    int simulationsRun = 0;
    
    // Shortest route pointer
    private double shortestRoute = Double.MAX_VALUE;
    
    private final double evaporation = 0.9;
    private final double pheromoneConstant = 10000;
    private double PHERMULTIPLIER = 1;
    private int stagnationCounter = 0;
    private final int stagnationSecondLimit = 20;
    
    // Switch to determine mouse click function
    private boolean trafficMode = false;
    
    // Simulation logging to file
    private Handler fileHandler;
    private static final Logger simLogger = Logger.getLogger("NewLog");
    
    /**
     * Overrides Application start() method
     * 
     * @param primaryStage - the primary stage
     */
    @Override
    public void start(Stage primaryStage) {
        // Change the default logging format so only required info is output (Level + message)
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$td/%1$tm/%1$tY %1$tl:%1$tM:%1$tS] %4$s: %5$s%n");
        
        // Initialise and name the window
        window = primaryStage;
        window.setTitle("ACODynamic");
        window.setFullScreen(false);
        window.setMaximized(true);
        window.setFullScreenExitHint("Press ESC to close");
        window.setResizable(true);
        
        // Menus and MenuItems
        file.getItems().addAll(newFile, openEnvironment, saveEnvironment, separator, exit);
        edit.getItems().addAll(settings);
        help.getItems().addAll(userGuide);
        css.getItems().addAll(defaultCSS, mapCSS, darkCSS);

        // Add the Menus to the MenuBar
        menuBar.getMenus().addAll(file, edit, help, css);
        
        // Setup right section
        setupRightPane();
        
        // Pane to hold nodes, edges, ants
        simulationArea = new Pane();
        
        layout = new BorderPane();
        layout.setTop(menuBar);
        layout.setRight(rightVBox);
        layout.setCenter(simulationArea);
        
        scene = new Scene(layout);
        //scene.getStylesheets().add(getClass().getResource("maptheme.css").toExternalForm());
        
        // Setup actions for each MenuItem
        setupMenuItemActions();
        // Setup actions to be taken relating to scene events
        setupSceneEventHandlers();
        // Setup actions to be taken relating to button events
        setupButtonHandlers();
        // Setup file logger
        setupLogging();
        
        // Simulation Loop - called at every frame
        simulationLoop = new AnimationTimer() {
            
            long antUpdateTimer = System.currentTimeMillis();
            long iterationTimer = System.currentTimeMillis();
            
            @Override
            public void handle(long now) {
                
                // Update every millisecond
                if (System.currentTimeMillis() - antUpdateTimer > 1) {
                    
                    updateDrawAnts();
                    // Reset update timer
                    antUpdateTimer = System.currentTimeMillis();
                }
                
                // Update every second
                if (System.currentTimeMillis() - iterationTimer > 1000) {
                    
                    updatePheromone();
                    // Reset iteration timer
                    iterationTimer = System.currentTimeMillis();
                    
                    // Increment runtime by 1 second
                    updateRuntime();
                    
                    if (!bestRouteEdgeList.isEmpty()) {
                        // Display best cost (cost can change due to traffic)
                        bestRouteCostValueLabel.setText(String.valueOf(Math.round(getRouteCost(bestRouteEdgeList))));
                    }
                }
                
                // Logic for automated simulations
                if (automatedSim) {
                    if (simulationsRun < maxIterations) {
                        if (simulationRuntime > maxRuntime) {
                            // Stop simulation
                            startStop.fire();
                            // Increment counter
                            simulationsRun++;
                            // Reset simulation
                            reset.fire();
                            // Restart simulation
                            startStop.fire();
                        }
                    } else {
                        // Stop simulation
                        simulationLoop.stop();
                        reset.fire();
                        
                        resetAutomatedSim();
                        
                        log.appendText("End of automated simulations - see log file for results!\n");
                    }
                }
            }
        };
        
        // Set the scene for the window and show it
        window.setScene(scene);
        window.show();
    }
    
    /**
     * Constructs all objects in the right pane: log, buttons, etc.
     */
    private void setupRightPane() {
        // Log for simulation messages + debugging
        log = new TextArea();
        log.setWrapText(true);
        log.setEditable(false);
        log.setMaxHeight(0.6 * SCREENHEIGHT);
        
        // ScrollPane to hold log
        scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);
        scrollPane.setMinHeight(0.6 * SCREENHEIGHT);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxWidth(LOGMULTIPLIER * SCREENWIDTH);
        scrollPane.setContent(log);
        
        // Set the padding and spacing for HBox
        tier1HBox.setPadding(new Insets(5,20,5,20));
        tier1HBox.setSpacing(10);
        tier2Hbox.setPadding(new Insets(5,20,5,20));
        tier2Hbox.setSpacing(10);
        tier3HBox.setPadding(new Insets(5,20,5,20));
        tier3HBox.setSpacing(10);
        tier4HBox.setPadding(new Insets(5,20,5,20));
        tier4HBox.setSpacing(10);
        tier5HBox.setPadding(new Insets(5,20,5,20));
        tier5HBox.setSpacing(10);
        tier6HBox.setPadding(new Insets(5,20,5,20));
        tier6HBox.setSpacing(10);
        tier7HBox.setPadding(new Insets(5,20,5,20));
        tier7HBox.setSpacing(10);
        
        // Mouse mode
        mouseModeChoiceBox = new ChoiceBox();
        mouseModeChoiceBox.getItems().addAll("Normal", "Add Traffic", "Remove Traffic");
        mouseModeChoiceBox.setValue("Normal");
        
        // Algorithm
        algorithmChoiceBox = new ChoiceBox();
        algorithmChoiceBox.getItems().addAll("AS", "MMAS");
        algorithmChoiceBox.setValue("AS");
        
        // Populate right side containers
        tier1HBox.getChildren().addAll(generateNodes, generateEdges);
        tier2Hbox.getChildren().addAll(startStop, reset);
        tier3HBox.getChildren().addAll(mouseModeLabel, mouseModeChoiceBox);
        tier4HBox.getChildren().addAll(algorithmLabel, algorithmChoiceBox);
        tier5HBox.getChildren().addAll(runtimeLabel, runtimeValueLabel);
        tier6HBox.getChildren().addAll(bestRouteCostLabel, bestRouteCostValueLabel);
        tier7HBox.getChildren().add(automatedSimulation);
        rightVBox.getChildren().addAll(scrollPane, tier1HBox, tier2Hbox, tier3HBox, tier4HBox, tier5HBox, tier6HBox, tier7HBox);
    }
    
    /**
     * Sets the actions to be taken when MenuItems are clicked on
     */
    private void setupMenuItemActions() {
        
        newFile.setOnAction(e -> {
            
            // Stop simulation if running
            if (startStop.getText().equalsIgnoreCase("Stop")) { 
                startStop.setText("Start");
                simulationLoop.stop();
            }
            
            // Remove all nodes, edges, ants
            simulationArea.getChildren().removeAll(nodeList);
            simulationArea.getChildren().removeAll(edgeList);
            simulationArea.getChildren().removeAll(antList);
            
            // Reset lists
            nodeList = new ArrayList();
            edgeList = new ArrayList();
            antList = new ArrayList();
            bestRouteEdgeList = new ArrayList();
            bestRouteCostValueLabel.setText("----");
            
            // Reset source + destination
            sourceNode = null;
            destNode = null;
            
            // Reset ID counters
            uniqueNodeID = 0;
            uniqueEdgeID = 0;
            uniqueAntID = 0;
            
            // Reset shortest route
            shortestRoute = Double.MAX_VALUE;
            
            // Reset runtime
            simulationRuntime = 0;
            runtimeValueLabel.setText(String.valueOf(simulationRuntime)+"s");
            
            // Clear the log
            log.clear();
            
            e.consume();
        });
        
        openEnvironment.setOnAction(e -> {
            // Stop sim if running
            startStop.setText("Start");
            simulationLoop.stop();
            
            FileChooser fc = new FileChooser();
            fc.setTitle("Open Environment");
            fc.setInitialDirectory(new File(System.getProperty("user.dir")));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".txt", "*.txt"));
            File fileChosen = fc.showOpenDialog(window);
            
            // return if no file chosen
            if (fileChosen == null) {
                return;
            }
            
            // Check file is readable
            if (!fileChosen.canRead()) {
                log.appendText("[E] - File not chosen or not readable!\n");
                e.consume();
                return;
            }
            
            // Reset the environment before populating
            newFile.fire();
            
            try {
                FileInputStream fis = new FileInputStream(fileChosen);
                ObjectInputStream ois = new ObjectInputStream(fis);
                
                // Read screen dimensions used when creating this environment
                double inputScreenWidth = ois.readDouble();
                double inputScreenHeight = ois.readDouble();
                // Read node and edge lists
                List<Node> tempNodeList = (List<Node>) ois.readObject();
                List<Edge> tempEdgeList = (List<Edge>) ois.readObject();
                
                // Close streams
                fis.close();
                ois.close();
                
                // Check for content
                if (tempNodeList != null) {
                    // Construct nodes from tempNodeList
                    for (Node n : tempNodeList) {
                        // Scale x and y positions for this screen size
                        n.setCenterX(Math.round((SCREENWIDTH/inputScreenWidth) * n.getCenterX()));
                        n.setCenterY(Math.round((SCREENHEIGHT/inputScreenHeight) * n.getCenterY()));
                        
                        simulationArea.getChildren().add(n);
                        nodeList.add(n);
                        
                        if (n.isSource()) {
                            sourceNode = n;
                        } else if (n.isDestination()) {
                            destNode = n;
                        }
                    }
                    
                    // Construct edges from tempEdgeList
                    for (Edge edge : tempEdgeList) {
                        // Scale x and y positions for this screen size
                        edge.setStartX(Math.round((SCREENWIDTH/inputScreenWidth) * edge.getStartX()));
                        edge.setStartY(Math.round((SCREENHEIGHT/inputScreenHeight) * edge.getStartY()));
                        edge.setEndX(Math.round((SCREENWIDTH/inputScreenWidth) * edge.getEndX()));
                        edge.setEndY(Math.round((SCREENHEIGHT/inputScreenHeight) * edge.getEndY()));
                        
                        simulationArea.getChildren().add(edge);
                        edgeList.add(edge);
                    }
                    
                    // Make sure the node ID counter is correct
                    if (!tempNodeList.isEmpty()) {
                        
                        uniqueNodeID = tempNodeList.get(tempNodeList.size() -1).getNodeID() + 1;
                        
                        if (!tempEdgeList.isEmpty()) {
                            uniqueEdgeID = tempEdgeList.get(tempEdgeList.size() - 1).getEdgeID() + 1;
                        }
                    }
                    

                    log.appendText("Environment loaded from file: " + fileChosen.getAbsolutePath() + "\n");
                } else {
                    log.appendText("[E]: File chosen is empty!\n");
                }
                
            } catch (FileNotFoundException fnfe) {
                log.appendText("[E]: FileNotFoundException: Choose a valid .txt file \n");
            } catch (IOException ioe) {
                log.appendText("[E]: IOException: Choose a valid .txt file \n");
            } catch (ClassNotFoundException cnfe) {
                log.appendText("[E]: ClassNotFoundException: Choose a valid .txt file \n");
            }
            
            e.consume();
        });
           
        saveEnvironment.setOnAction(e -> {
            // Stop sim if running
            startStop.setText("Start");
            simulationLoop.stop();
            
            FileChooser fc = new FileChooser();
            fc.setTitle("Save Environment");
            fc.setInitialDirectory(new File(System.getProperty("user.dir")));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".txt", "*.txt"));
            File fileChosen = fc.showSaveDialog(window);
            
            // return if no file chosen
            if (fileChosen == null) {
                return;
            }
            
            // Create file if it does not exist
            if (!fileChosen.exists()) {
                try {
                    fileChosen.createNewFile();
                } catch (IOException ioe) {
                    log.appendText("[E]: File could not be created!\n");
                    return;
                }
            }
            
            // Check file has been chosen and is writeable
            if (!fileChosen.canWrite()) {
                log.appendText("[E]: File not chosen or not writeable!\n");
                e.consume();
                return;
            }
                
            try {                
                FileOutputStream fos = new FileOutputStream(fileChosen);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                
                // Write screen dimensions so x and y positions can be scaled
                oos.writeDouble(SCREENWIDTH);
                oos.writeDouble(SCREENHEIGHT);
                // Write node and edge list
                oos.writeObject(nodeList);
                oos.writeObject(edgeList);
                
                // Close streams
                fos.close();
                oos.close();
                
                log.appendText("Environment saved to file: " + fileChosen.getAbsolutePath() + "\n");
                
            } catch (FileNotFoundException fnfe) {
                log.appendText("[E]: FileNotFoundException: Choose a valid .txt file \n");
            } catch (IOException ioe) {
                log.appendText("[E]: IOException: Choose a valid .txt file \n");
            }
            
            e.consume();
        });  
        
        exit.setOnAction(e -> {
            e.consume();
            // Add end log message
            simLogger.info("--- END LOG ---");
            // Close application
            window.close();
        });
                
        settings.setOnAction(e -> {
            startStop.setText("Start");
            simulationLoop.stop();
            
            openSettingsWindow();
            
            e.consume();
        });
                
        userGuide.setOnAction(e -> {
            // As the user guide is within the jar file, it must be extracted to be read
            InputStream is = getClass().getResourceAsStream("resources/USERGUIDE.pdf");
            Path tempUserGuideFilePath = null;
            try {
                // Attempt to create a temp file and copy input stream contents to it
                tempUserGuideFilePath = Files.createTempFile("USERGUIDE", ".pdf");
                Files.copy(is, tempUserGuideFilePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ioe) {
                log.appendText("[E]: Could not extract user guide from jar file!\n");
            }
            
            if (tempUserGuideFilePath != null) {
                // Use HostServices to open the pdf - will open in Adobe Acrobat Reader or web browser
                HostServices hostServices = getHostServices();
                hostServices.showDocument(tempUserGuideFilePath.toAbsolutePath().toString());
            }
            e.consume();
        });
        
        
        defaultCSS.setOnAction(e -> { 
            if (scene.getStylesheets().size() != 0) {
                scene.getStylesheets().remove(0);
            }
        });

        mapCSS.setOnAction(e -> {
            if (scene.getStylesheets().size() != 0) {
                scene.getStylesheets().remove(0);
            }
            scene.getStylesheets().add(getClass().getResource("maptheme.css").toExternalForm());
        });
        
        darkCSS.setOnAction(e -> {
            if (scene.getStylesheets().size() != 0) {
                scene.getStylesheets().remove(0);
            }
            scene.getStylesheets().add(getClass().getResource("darktheme.css").toExternalForm());
        });
        
    }
    
    private void setupButtonHandlers() {
        generateNodes.setOnAction(e -> {
            if (nodeList.size() != MAXNODES) {
                while (nodeList.size() < MAXNODES) {
                    addRandomNode();
                }
                log.appendText("Random nodes added!\n");
            } else {
                log.appendText("[E]: Node limit reached!\n");
            }
        });
        
        generateEdges.setOnAction(e -> { 
            // For all nodes
            Iterator<Node> nodeIter = nodeList.iterator();
            while (nodeIter.hasNext()) {
                Node element = nodeIter.next();
                
                // List to hold the closest 3 valid nodes
                List<Node> closestNodeList = new ArrayList<>(2);
                
                // If the element does not already have MAXCONNECTIONS
                if (element.getConnectionsCount() != MAXCONNECTIONS) {
                    // Create edges to the 3 closest nodes which have less than MAXCONNECTIONS
                    for (int i = 0; i < 3; i++) {
                        double shortest = 9999;
                        for (Node n: nodeList) {
                            // If n is not itself & n is not already on the closestNodeList & n has not got MAXCONNECTIONS
                            if (!element.equals(n)  && !closestNodeList.contains(n) && n.getConnectionsCount() != MAXCONNECTIONS) {
                                if (getEuclideanDistance(element, n.getX(), n.getY()) < shortest) {
                                    // New closest node for this round
                                    shortest = getEuclideanDistance(element, n.getX(), n.getY());
                                    // Remove the previous closest node before adding, if it exists
                                    if (closestNodeList.size() == i + 1) {
                                        closestNodeList.remove(i);
                                        closestNodeList.add(n);
                                    } else {
                                        closestNodeList.add(n);
                                    }
                                }
                            }
                        }
                    }
                    // Create the edges between the element and the nodes in the list
                    for (Node closeNode: closestNodeList) {
                        addEdge(element, closeNode, false);
                    }
                }
            }
           
            log.appendText("Closest edges added!\n");
            e.consume();
        });
        
        startStop.setOnAction(e -> { 
            // Check setup is ok before starting simulation
            if (getSourceNode() != null && getDestinationNode() != null) {
                
                // Check there are edges for ants to traverse
                if (edgeList.isEmpty()) {
                    log.appendText("[E]: Ants requires edges to walk down!\n");
                } else if (sourceNode.getConnectedEdges().isEmpty() || destNode.getConnectedEdges().isEmpty()) {
                    log.appendText("[E]: Source and/or destination node has no connected edges!\n");
                } else {
                    if (startStop.getText().equals("Start")) {
                        startStop.setText("Stop");
                        log.appendText("Simulation started!\n");
                        
                        // Spawn ants
                        while (antList.size() < MAXANTS) {
                            spawnAnt();
                        }

                        simulationLoop.start();
                    } else {
                        startStop.setText("Start");
                        log.appendText("Simulation stopped!\n");
                        simulationLoop.stop();
                    }
                }
            } else {
                log.appendText("[E]: Set source and destination node before starting simulation!\n");
            }
            e.consume();
        });
        
        reset.setOnAction(e -> { 
            // Reset the simulation
            // Stop simulation if running
            if (startStop.getText().equalsIgnoreCase("Stop")) { 
                startStop.setText("Start");
                simulationLoop.stop();
            }
            
            // Remove all nodes, edges, ants
            simulationArea.getChildren().removeAll(antList);
            
            // Reset lists
            antList = new ArrayList();
            
            // Remove highlighting from all edges
            if (!bestRouteEdgeList.isEmpty()) {
                for (Edge edge : edgeList) {
                    edge.setStroke(Color.BLACK);
                    edge.setStrokeWidth(1.0);
                }
            }
            
            // Remove pheromone & traffic
            for (Edge edge : edgeList) {
                edge.setPheromone(1.0, false);
                edge.setTraffic(1.0);
            }
            
            // Reset best route details
            bestRouteEdgeList = new ArrayList();
            bestRouteCostValueLabel.setText("----");
            
            // Reset ID counters
            uniqueAntID = 0;
            
            // Reset shortest route
            shortestRoute = Double.MAX_VALUE;
            
            // Reset runtime
            simulationRuntime = 0;
            runtimeValueLabel.setText(String.valueOf(simulationRuntime)+"s");
            
            // Clear log
            log.clear();
            
            // Write reset to simulation log
            simLogger.log(Level.INFO, "Simulation reset");
            
            e.consume();
        });
        
        algorithmChoiceBox.setOnAction(e -> { 
            switch(algorithmChoiceBox.getValue()) {
                case "AS":
                    reset.fire(); // Reset the simulation
                    algorithm = ALGORITHM.AS;
                    log.appendText("Algorithm: " + algorithmChoiceBox.getValue() + "\n");
                    break;
                case "MMAS":
                    reset.fire(); // Reset the simulation
                    algorithm = ALGORITHM.MMAS; 
                    log.appendText("Algorithm: " + algorithmChoiceBox.getValue() + "\n");
                    break;
            }
        });
        
        mouseModeChoiceBox.setOnAction(e -> { 
            switch(mouseModeChoiceBox.getValue()) {
                case "Normal":
                    trafficMode = false;
                    break;
                case "Add Traffic":
                    trafficMode = true;
                    break;
                case "Remove Traffic":
                    trafficMode = true;
                    break;
            }
        });
        
        automatedSimulation.setOnAction(e -> {
            switch (automatedSimulation.getText()) {
                case "End Automated Simulations":
                    // Reset default simulation settings
                    reset.fire();
                    // Reset automated simulation settings
                    resetAutomatedSim();
                    automatedSimulation.setText("Run Automated Simulations");
                    break;
                case "Run Automated Simulations":
                    openAutomatedSimWindow();
                    break;
            }
        });
    }
    
    /**
     * Sets actions to be taken for scene related events
     */
    private void setupSceneEventHandlers() {
        // KeyReleased events
        scene.setOnKeyReleased((KeyEvent ke) -> {
            if (ke.getCode() == KeyCode.ESCAPE) {
                // Close the application
                ke.consume();
                // Add end log message
                simLogger.info("--- END LOG ---");
                window.close();
            } else if (ke.getCode() == KeyCode.F1) {
                // If a node is highlighted
                if (getHighlightedNode() != null) {
                    // Switch if this node is the source
                    if (getHighlightedNode().isSource()) {
                        getHighlightedNode().setSource(false);
                        sourceNode = null;
                        log.appendText("Node " + getHighlightedNode().getNodeID() + " removed as source!\n");
                    } else if (getHighlightedNode().isDestination()) {
                        log.appendText("[E]: Node is already set as destination!\n");
                    } else {
                        // Remove the current source if it exists
                        if (getSourceNode() != null) {
                            getSourceNode().setSource(false);
                            sourceNode = null;
                        }
                        getHighlightedNode().setSource(true);
                        sourceNode = getHighlightedNode();
                        log.appendText("Node " + getHighlightedNode().getNodeID() + " set as source!\n");
                    }
                    
                    removeHighlight();
                }
                ke.consume();
            } else if (ke.getCode() == KeyCode.F2) {
                // If a node is highlighted
                if (getHighlightedNode() != null) {
                    // Switch if this node is the destinaton
                    if (getHighlightedNode().isDestination()) {
                        getHighlightedNode().setDestination(false);
                        destNode = null;
                        log.appendText("Node " + getHighlightedNode().getNodeID() + " removed as destination!\n");
                    } else if (getHighlightedNode().isSource()) {
                        log.appendText("[E]: Node is already the source!\n");
                    } else {
                        // Remove the currest destination if it exists
                        if (getDestinationNode() != null) {
                            getDestinationNode().setDestination(false);
                            destNode = null;
                        }
                        getHighlightedNode().setDestination(true);
                        destNode = getHighlightedNode();
                        log.appendText("Node " + getHighlightedNode().getNodeID() + " set as destination!\n");
                    }
                    
                    removeHighlight();
                }
                ke.consume();
            } else if (ke.getCode() == KeyCode.H) {
                // Toggle ants visibility
                log.appendText("Ant visibility switched!\n");
                for (Ant a : antList) {
                    a.setVisible(!a.isVisible());
                }
            }
        });
        
        // MousePressed events
        scene.setOnMousePressed((MouseEvent me) -> {
            boolean existingNodeSelected = false;
            // Traffic mode disables other actions so traffic can be added or removed
            if (trafficMode) {
                // Find the edge and add traffic
                for (Edge e: edgeList) {
                    if (me.getTarget().equals(e)) {
                        // Add or remove traffic
                        switch (mouseModeChoiceBox.getValue()) {
                            case "Add Traffic":
                                e.addTraffic(1.0);
                                log.appendText("Traffic added to edge " + e.getEdgeID() + ". Value: " + e.getTraffic() + "\n");
                                simLogger.log(Level.INFO, "Runtime:{0}s - Traffic added to edge {1}. Value: {2}", new Object[]{simulationRuntime, e.getEdgeID(), e.getTraffic()});
                                break;
                            case "Remove Traffic":
                                e.reduceTraffic(1.0);
                                log.appendText("Traffic reduced on edge " + e.getEdgeID() + ". Value: " + e.getTraffic() + "\n");
                                simLogger.log(Level.INFO, "Runtime:{0}s - Traffic removed from edge {1}. Value: {2}", new Object[]{simulationRuntime, e.getEdgeID(), e.getTraffic()});
                                break;
                        }
                    }
                }
            } else if (me.isPrimaryButtonDown() && me.isControlDown()) {
                /**
                 * CTRL is down so in delete mode:
                 * Iterate through all nodes/edges and if a match is found remove the 
                 * node/edge
                 */
                removeNodeIfFound(me);
                removeEdgeIfFound(me);
                removeHighlight();
                
            } else if (me.isPrimaryButtonDown()) {
                Iterator<Node> nodeIter = nodeList.iterator();
                while (nodeIter.hasNext()) {
                    Node element = nodeIter.next();
                    if (element.equals(me.getTarget())) {
                        // An existing node has been selected
                        existingNodeSelected = true;
                        
                        
                        Node highlightedElement = getHighlightedNode();
                        if (!element.equals(highlightedElement)) {
                            if (getHighlightedNode() != null) {
                                // Create an edge between the highlighted node and this node
                                addEdge(highlightedElement, element, true);

                                // Remove highlighting
                                removeHighlight();
                            } else {
                                // Highlight the selected node
                                element.setHighlighted(true);
                            }
                        } else {
                            removeHighlight();
                        }
                    }
                }
                
                if (!existingNodeSelected) {
                    if (getHighlightedNode() != null) {
                        // Clicked to disable
                        removeHighlight();
                    } else {
                        addNodeMouseClick(me);
                    }
                }                
            } else if (me.isSecondaryButtonDown()) {
                // Add a random node
                addRandomNode();
            }
        });
    }
    
    /**
     * Attempts to create/open a given logging file, then sets the level of logging,
     * and writes a begin log info message
     */
    private void setupLogging() {
        try {
            // Create log file in user.dir, max size 50kb, 10 versions
            fileHandler = new FileHandler(System.getProperty("user.dir")+"\\simulationLog.log", 50000, 2);
            simLogger.addHandler(fileHandler);
            
            // Format is too long by default so just use the standard
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);
            
        } catch (IOException | SecurityException ioe) {
            log.appendText("[E]: Error initialising file handler!\n");
        }

        // Log all
        simLogger.setLevel(Level.ALL);
        // Don't use the default console log
        simLogger.setUseParentHandlers(false);
        
        simLogger.info("--- BEGIN LOG ---");
    }
    
    /**
     * Adds a node at the location where a left click took place provided the
     * location is within the simulation area
     * @param me 
     */
    private void addNodeMouseClick(MouseEvent me) {
        // Must be within the simulation area
        if (nodeList.size() < MAXNODES) {
            if (me.getX() < (SCREENWIDTH * SIMULATIONMULTIPLIER) && me.getY() > MENUBARHEIGHT) {
                // Create a new node and add to the simulation area
                Node n = new Node(uniqueNodeID, me.getX(), me.getY() - MENUBARHEIGHT);
                uniqueNodeID++;
                simulationArea.getChildren().add(n);
                
                log.appendText(getNewNodeString(n));
                nodeList.add(n);
            } else {
                log.appendText("[E]: Invalid node location - must be within simulation area!\n");
            }
        } else {
            log.appendText("[E]: Node limit reached!\n");
        }
    }

    /**
     * Randomly places a new node if the node limit has not been reached
     */
    private void addRandomNode() {
        if (nodeList.size() < MAXNODES) {
            double x, y;
            // Get a random valid position           
            do {
                // Overlapping another node
                x = getDoubleInRange((SCREENWIDTH * SIMULATIONMULTIPLIER));
                y = getDoubleInRange(SCREENHEIGHT - MENUBARHEIGHT);
            } while (!checkValidPosition(x, y));
            
            Node n = new Node(uniqueNodeID, Math.round(x), Math.round(y));
            uniqueNodeID++;
            simulationArea.getChildren().add(n);

            log.appendText(getNewNodeString(n));
            nodeList.add(n);
            
        } else {
            log.appendText("[E]: Node limit reached!\n");
        }
        
        removeHighlight();
    }
    
    /**
     * Iterates through all nodes and removes the node if a match is found with 
     * the MouseEvent.target()
     * @param me - MouseEvent
     */
    private void removeNodeIfFound(MouseEvent me) {
        Iterator<Node> nodeIter = nodeList.iterator();
        while (nodeIter.hasNext()) {
            Node element = nodeIter.next(); // Store the current element
            if (element.equals(me.getTarget())) {
                log.appendText("Node " + element.getNodeID() + " removed!\n");
                
                // Remove all the connected edges as well
                Iterator<Edge> edgeIter = element.getConnectedEdges().iterator();
                while (edgeIter.hasNext()) {
                    Edge elementEdge = edgeIter.next();
                    
                    // Remove this edge from simulation area and the connected nodes lists
                    log.appendText("Edge " + elementEdge.getEdgeID() + " removed!\n");
                    
                    // Find the node that isn't the one clicked and remove the node and edge connection
                    for (Node n :elementEdge.getConnectedNodes()) {
                        if (!n.equals(element)) {
                            n.removeConnectedNode(element);
                            n.removeConnectedEdge(elementEdge);
                        }
                    }
                    
                    simulationArea.getChildren().remove(elementEdge);
                    edgeIter.remove(); // Removes from iterator and underlying collection
                }
                
                simulationArea.getChildren().remove(element);
                nodeIter.remove(); // Removes from the iterator and underlying collection
            }
        }
    }
    
    /**
     * Searches through all nodes and returns the node which is highlighted,
     * else returns a null node
     * @return Node 
     */
    private Node getHighlightedNode() {
        Iterator<Node> nodeIter = nodeList.iterator();
        while (nodeIter.hasNext()) {
            Node element = nodeIter.next(); // Store the current element
            if (element.isHighlighted()) {
                return element;
            }
        }
        // Return null if no highlighted node
        return null;
    }
    
    /**
     * Returns the source node pointer
     * @return Node
     */
    private Node getSourceNode() {
        return sourceNode;
    }
    
    /**
     * Returns the destination node pointer
     * @return Node 
     */
    private Node getDestinationNode() {
        return destNode;
    }
    
    /**
     * Add an edge between the source and destination and creates a connection 
     * between the source and destination nodes. The edge also adds the connected
     * nodes. Verbose determines log output
     * @param source
     * @param destination 
     * @param verbose
     */
    private void addEdge(Node source, Node destination, boolean verbose) {
        // Only add the edge if an edge is not already present between the two nodes
        if (source.getConnectedNodes().contains(destination)) {
            if (verbose) {
                log.appendText("[E]: Edge already present!\n");
            }
        } else if (source.getConnectionsCount() == MAXCONNECTIONS 
                || destination.getConnectionsCount() == MAXCONNECTIONS) {
            if (verbose) {
                log.appendText("[E]: Max connections reached for source or target node!\n");
            }
        } else {
            // Create a new edge and add to the simulation area
            Edge e = new Edge(uniqueEdgeID, source.getX(), source.getY(), destination.getX(), destination.getY());
            uniqueEdgeID++;

            simulationArea.getChildren().add(e);
            
            // Mutally add the node connections
            destination.addConnectedNode(source);
            // Add the edge to both nodes + add nodes to edges
            source.addConnectedEdge(e);
            destination.addConnectedEdge(e);
            // Add the connected nodes to the edge
            e.addConnectedNode(source);
            e.addConnectedNode(destination);

            log.appendText(getNewEdgeString(e));
            edgeList.add(e);
        } 
    }
    
    /**
     * Iterates through all edges and removes the edge if a match is found with 
     * the MouseEvent.target()
     * @param me - MouseEvent
     */
    private void removeEdgeIfFound(MouseEvent me) {
        // Find the edge clicked       
        Iterator<Edge> edgeIter = edgeList.iterator();
        while (edgeIter.hasNext()) {
            Edge element = edgeIter.next();
            if (element.equals(me.getTarget())) {
                log.appendText("Edge " + element.getEdgeID() + " removed!\n");
                
                // Get the two nodes connected to this edge
                Node nodeOne = element.getConnectedNodes().get(0);
                Node nodeTwo = element.getConnectedNodes().get(1);
                
                // Remove the edge and each other from connections
                nodeOne.removeConnectedNode(nodeTwo);
                nodeOne.removeConnectedEdge(element);
                nodeTwo.removeConnectedNode(nodeOne);
                nodeTwo.removeConnectedEdge(element);
                
                simulationArea.getChildren().remove(element);
                
                // Reset best route if an edge within it is removed
                if (bestRouteEdgeList.contains(element)) {
                    for (Edge edge : edgeList) {
                        edge.setStroke(Color.BLACK);
                        edge.setStrokeWidth(1.0);
                    }
                    bestRouteEdgeList = new ArrayList();
                }
                
                edgeIter.remove(); // Removes the edge from the list
            }
        }
    }
    
    /**
     * Removes highlighting from highlighted node
     */
    private void removeHighlight() {
        if (getHighlightedNode() != null) {
            getHighlightedNode().setHighlighted(false);
        }
    }
    
    /**
     * Gets a string message for a new node
     * @param n
     * @return string
     */
    private String getNewNodeString(Node n) {
        StringBuilder sb = new StringBuilder("Node added! (ID: " + n.getNodeID());
        sb.append(" x: ").append(n.getX());
        sb.append(" y: ").append(n.getY());
        sb.append(")\n");
        return sb.toString();
    }
    
    /**
     * Gets a string message for a new edge
     * @param e
     * @return string
     */
    private String getNewEdgeString(Edge e) {
        StringBuilder sb = new StringBuilder("Edge added! (ID: " + e.getEdgeID());
        sb.append(" (x: ").append(e.getStartX());
        sb.append(" y: ").append(e.getStartY());
        sb.append(") (x: ").append(e.getEndX());
        sb.append(" y: ").append(e.getEndY());
        sb.append("))\n");
        return sb.toString();
    }
    
    /**
     * Gets a random double up to a max value
     * @param max
     * @return double
     */
    private Double getDoubleInRange(double max) {
        Random r = new Random();
        return r.nextDouble() * max;    
    }
    
    /**
     * Gets the Euclidean distance between two nodes using Pythagoras theorem
     * @param n
     * @return 
     */
    private double getEuclideanDistance(Node n, double x, double y) {
        // h = sqrt(dx * dx + dy * dy)
        return Math.sqrt(Math.pow(Math.abs((n.getX() - x)), 2) + Math.pow(Math.abs((n.getY() - y)), 2));
    }
    
    /**
     * Checks if the node is in a valid position (i.e. not on another node)
     * @param x
     * @param y
     * @return true if valid
     */
    private Boolean checkValidPosition(double x, double y) {        
        Iterator<Node> nodeIter = nodeList.iterator();
        while (nodeIter.hasNext()) {
            Node element = nodeIter.next(); // Store the current element
            if (getEuclideanDistance(element, x, y) < 4.0) {
                return false;                
            }
        }
        return true;
    }
    
    /**
     * Adds a single ant to the simulation area at the source node, if MAXANTS 
     * is not yet reached.
     */
    private void spawnAnt() {
        if (antList.size() < MAXANTS) {
            Ant a = new Ant(uniqueAntID, getSourceNode());
            simulationArea.getChildren().add(a);
            antList.add(a);
            uniqueAntID++;
        }
    }
    
    /**
     * Calculates the cost of a route and sets best route if a new one is found.
     * Also writes additional debugging information to the log.
     * @param routeEdges
     * @return cost
     */
    private Double calculateRouteCost(List<Edge> routeEdges) {
        double tempCost = getRouteCost(routeEdges);
        
        String mes = "";
        for (Edge e : routeEdges) {
            mes += e.getEdgeID() + ",";
        }
        
        if (bestRouteEdgeList.isEmpty()) {
            // First solution so save it
            shortestRoute = Math.round(tempCost);
            
            // Save and highlight the new best route
            saveAndHighlightNewBestRoute(routeEdges);
            
            // Reset stagnation counter
            stagnationCounter = 0;
            log.appendText("New best route: " + shortestRoute + "!\n");
            
            // Write to file log
            simLogger.log(Level.INFO, "Runtime:{0}s - New best route: {1}! Edges[{2}]", new Object[]{simulationRuntime, shortestRoute, mes.substring(0, mes.length() - 1)});
            
        } else if (Math.round(tempCost) < Math.round(getRouteCost(bestRouteEdgeList))) {
            shortestRoute = Math.round(tempCost);
            
            // Save and highlight the new best route
            saveAndHighlightNewBestRoute(routeEdges);
            
            // Reset stagnation counter
            stagnationCounter = 0;

            log.appendText("New best route: " + shortestRoute + "!\n");
            
            // Write to file log
            simLogger.log(Level.INFO, "Runtime:{0}s - New best route: {1}! Edges[{2}]", new Object[]{simulationRuntime, shortestRoute, mes.substring(0, mes.length() - 1)});
            
        } else if (Math.round(tempCost) == Math.round(getRouteCost(bestRouteEdgeList))) {
            log.appendText("Following best route\n");
        }
        
        return tempCost;
        
    }
    
    /**
     * Returns the sum of the costs for each edge in the route
     * @param route
     * @return cost
     */
    private double getRouteCost(List<Edge> route) {
        double cost = 0;
        for (Edge e : route) {
            cost += e.getCost();
        }
        return cost;
    }
    
    /**
     * Stores the new best route in list globalBestRoute. Removes highlighting
     * from the old best route if it exists, and then highlights the new best 
     * route.
     * @param newBestEdges 
     */
    private void saveAndHighlightNewBestRoute(List<Edge> newBestEdges) {
        // Remove highlighting from all edges
        if (!bestRouteEdgeList.isEmpty()) {
            for (Edge e : edgeList) {
                e.setStroke(Color.BLACK);
                e.setStrokeWidth(1.0);
            }
        }

        // Update route
        bestRouteEdgeList = new ArrayList(newBestEdges);

        // Highlight edges
        for (Edge e : bestRouteEdgeList) {
            e.setStroke(Color.BLUE);
            e.setStrokeWidth(2.0);
        }
    }
    
    /**
     * Updates ants location and draws
     */
    private void updateDrawAnts() {
        // Manage ants movements
        for (Ant a : antList) {
            if (a.isOutwardBound()) {                
                // Searching for destination
                if (a.getNextNode() == null) {
                    // Choose an edge if ant doesn't have a nextNode
                    a.chooseEdge();

                } else if (Math.abs(a.getCenterX() - a.getNextNode().getCenterX()) < 5 && 
                        Math.abs(a.getCenterY() - a.getNextNode().getCenterY()) < 5) {
                    // Close enough to the next node so jump to it and choose again or complete
                    a.setX(a.getNextNode().getX() - 5.0);
                    a.setY(a.getNextNode().getY() - 5.0);

                     // Keep track of the last node in case we need to find the edge between current and last
                    Node tempNodeForPrevCheck = a.getCurrentNode();

                    a.setCurrentNode(a.getNextNode());

                    // If at destination, return to source
                    if (a.getCurrentNode().equals(getDestinationNode())) { 

                        // Push the node
                        a.pushNodeTaken(getDestinationNode());
                        // Push the edge
                        for (Edge e : getDestinationNode().getConnectedEdges()) {
                            if (e.getConnectedNodes().contains(tempNodeForPrevCheck)) {
                                a.pushEdgeTaken(e);
                            }
                        }

                        // Store route cost for pheromone update and output route details
                        a.setPrevRouteLength(calculateRouteCost(a.getEdgesTaken()));

                        // Reset for return
                        a.setNextNode(null);
                        // Return to source
                        a.setOutwardBound(false);
                    } else if (a.getCurrentNode().getConnectedNodes().contains(getDestinationNode())) {
                        // Choose the destination node
                        a.setNextNode(getDestinationNode());
                        
                        // Work out and set current edge
                        for (Edge e : getDestinationNode().getConnectedEdges()) {
                            if (e.getConnectedNodes().contains(a.getCurrentNode())) {
                                a.setCurrentEdge(e);
                            }
                        }
                        
                    } else {
                        // If ant is back at source, reset it
                        if (a.getCurrentNode().equals(getSourceNode())) {
                            a.reset();
                        }

                        // Choose an edge if we are not 
                        a.chooseEdge();
                    }

                } else {
                    // Move
                    a.travel();
                }
            } else {
                // Returning to source
                if (a.getNextNode() == null) {                               
                    a.setNextNode(a.peekNodeTaken());
                    // Set current edge
                    a.setCurrentEdge(a.peekEdgeTaken());
                } else if (Math.abs(a.getCenterX() - a.getNextNode().getCenterX()) < 5 && 
                        Math.abs(a.getCenterY() - a.getNextNode().getCenterY()) < 5) {
                    // Close enough to next node
                    a.setX(a.getNextNode().getX() - 5.0);
                    a.setY(a.getNextNode().getY() - 5.0);
                    a.setCurrentNode(a.getNextNode());


                    // If at source, search for destination
                    if (a.getCurrentNode().equals(getSourceNode())) {

                        // Reset for return
                        a.setNextNode(null);
                        // Return to destination
                        a.setOutwardBound(true);
                    } else if (a.getNodesTaken().size() == 0 && a.getEdgesTaken().size() == 0) {
                        // Check for empty stack, if so we are next to source
                        a.setNextNode(getSourceNode());
                        
                        // Work out and set current edge
                        for (Edge e : getSourceNode().getConnectedEdges()) {
                            if (e.getConnectedNodes().contains(a.getCurrentNode())) {
                                a.setCurrentEdge(e);
                            }
                        }
                    } else {
                        // Pop node and edge
                        a.popNodeTaken();
                        if (algorithm == ALGORITHM.AS) {
                            // Set current edge
                            a.setCurrentEdge(a.peekEdgeTaken());
                            // Pop and add pheromone
                            a.popEdgeTaken().addPheromone(pheromoneConstant/a.getPrevRouteLength() * PHERMULTIPLIER, false);
                            
                        } else {
                            // Set current edge
                            a.setCurrentEdge(a.peekEdgeTaken());
                            // Pop
                            a.popEdgeTaken();
                        }

                        // Set the next node as the top of the node stack
                        if (!a.getNodesTaken().empty()) {
                            a.setNextNode(a.peekNodeTaken());
                        }
                    }
                } else {
                    // Move
                    a.travel();
                }
            }
        }
    }
    
    /**
     * Updates pheromone
     * Note:AS adds pheromone within updateDrawAnts()
     */
    private void updatePheromone() {                   
        // Switch on algorithm so correct pheromone update is used.
        if (algorithm == ALGORITHM.AS) {
            //Pheromone decay
            for (Edge e : edgeList) {
                e.setPheromone(e.getPheromone() * evaporation, false);
            }
        } else if (algorithm == ALGORITHM.MMAS) {
            // Deposit on best route and check for stagnation
            if (!bestRouteEdgeList.isEmpty()) {
                double routeLength = getRouteCost(bestRouteEdgeList);

                for (Edge e : bestRouteEdgeList) {
                    e.addPheromone((pheromoneConstant/routeLength) * PHERMULTIPLIER, true);
                }

                stagnationCounter++;
                // If route best route is stagnent for 20 iterations refresh all edges
                if (stagnationCounter == stagnationSecondLimit) {
                    log.appendText("\tStagnant for " + stagnationSecondLimit + " seconds, refresing pheromone!\n");
                    simLogger.log(Level.INFO, "Runtime:{0}s - Stagnant for {1} seconds, refreshing pheromone!", new Object[]{simulationRuntime, stagnationSecondLimit});
                    for (Edge e : edgeList) {
                        if (!bestRouteEdgeList.contains(e)) {
                            e.addPheromone(10, true);
                        }
                    }
                    stagnationCounter = 0;
                }
            }
            
            //Pheromone decay
            for (Edge e : edgeList) {
                e.setPheromone(e.getPheromone() * evaporation, true);
            }
        }
    }
    
    /**
     * Updates the runtime value display by adding 1 second
     */
    private void updateRuntime() {
        simulationRuntime += 1;
        runtimeValueLabel.setText(String.valueOf(simulationRuntime)+"s");
    }
    
    /**
     * Creates and opens the settings window
     */
    private void openSettingsWindow() {
        // MAXANTS
        maxAntsSlider = new Slider(1, 1000, MAXANTS);
        maxAntsSlider.setMajorTickUnit(100); // Every 10
        maxAntsSlider.setBlockIncrement(1);
        maxAntsSlider.setSnapToPixel(true);
        maxAntsSlider.setShowTickMarks(true);
        maxAntsSlider.setPrefWidth(200);
        maxAntsValue = new Label(String.valueOf(MAXANTS));
        // Listener to update value label
        maxAntsSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldVale, Number newValue) -> {
            maxAntsValue.setText(String.valueOf(newValue.intValue()));
        });

        // MAXNODES
        maxNodesSlider = new Slider(50, 1000, MAXNODES);
        maxNodesSlider.setMajorTickUnit(100); // Every 100
        maxNodesSlider.setMinorTickCount(3); // Every 25
        maxNodesSlider.setSnapToTicks(true);
        maxNodesSlider.setShowTickMarks(true);
        maxNodesSlider.setPrefWidth(200);
        maxNodesValue = new Label(String.valueOf(MAXNODES));
        // Listener to update value label
        maxNodesSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            maxNodesValue.setText(String.valueOf(newValue.intValue()));
        });
        
        // Pheromone multiplier
        pherMultiplierSlider = new Slider(1, 5, PHERMULTIPLIER);
        pherMultiplierSlider.setMajorTickUnit(1);
        pherMultiplierSlider.setBlockIncrement(1);
        pherMultiplierSlider.setSnapToTicks(true);
        pherMultiplierSlider.setShowTickMarks(true);
        pherMultiplierSlider.setPrefWidth(200);
        pherMultiplierValue = new Label(String.valueOf(PHERMULTIPLIER));
        // Listener to update value label
        pherMultiplierSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            pherMultiplierValue.setText(String.valueOf(newValue.intValue()));
        });

        // Save and close
        saveSettingsButton.setOnAction(action -> {
            MAXANTS = (int) maxAntsSlider.getValue();
            MAXNODES = (int) maxNodesSlider.getValue();
            PHERMULTIPLIER = (double) pherMultiplierSlider.getValue();
            log.appendText("Settings changed:\nMax ants: " + String.valueOf(MAXANTS) + 
                    "\nMax nodes: " + String.valueOf(MAXNODES) + 
                    "\nPheromone multiplier: " + String.valueOf(PHERMULTIPLIER) + "\n");
            
            // Close settings window
            settingsStage.close();
        });

        
        settingsGrid = new GridPane();
        // Pads the edge of the screen
        settingsGrid.setPadding(new Insets(10, 10, 10, 10));
        // Pads content cells
        settingsGrid.setVgap(8);
        settingsGrid.setHgap(10);
        
        // Populate the grid
        GridPane.setConstraints(maxAntsLabel, 0, 0);
        GridPane.setConstraints(maxAntsSlider, 1, 0);
        GridPane.setConstraints(maxAntsValue, 2, 0);
        GridPane.setConstraints(maxNodesLabel, 0, 1);
        GridPane.setConstraints(maxNodesSlider, 1, 1);
        GridPane.setConstraints(maxNodesValue, 2, 1);
        GridPane.setConstraints(pherMultiplierLabel, 0, 2);
        GridPane.setConstraints(pherMultiplierSlider, 1, 2);
        GridPane.setConstraints(pherMultiplierValue, 2, 2);
        GridPane.setConstraints(saveSettingsButton, 2, 3);
        
        // Add to grid (show layout with format)
        settingsGrid.getChildren().addAll(maxAntsLabel, maxAntsSlider, maxAntsValue,
                                maxNodesLabel, maxNodesSlider, maxNodesValue,
                                pherMultiplierLabel, pherMultiplierSlider, pherMultiplierValue,
                                                                            saveSettingsButton);

        settingsStage = new Stage();
        settingsStage.setTitle("Settings");
        settingsStage.setScene(new Scene(settingsGrid));
        // Don't allow input to the window until this is closed
        settingsStage.initModality(Modality.WINDOW_MODAL);
        settingsStage.initOwner(window);
        settingsStage.setResizable(false);
        settingsStage.show();      
    }
    
    /**
     * Opens a setup window to start a series of automated simulations
     */
    private void openAutomatedSimWindow() {
        // Reset the simulation if it has been started
        if (simulationRuntime != 0) {
            reset.fire();
        }
        
        // Max iterations
        maxIterationsSlider = new Slider(1, 10, maxIterations);
        maxIterationsSlider.setMajorTickUnit(1); // Every 1
        maxIterationsSlider.setBlockIncrement(1);
        maxIterationsSlider.setSnapToPixel(true);
        maxIterationsSlider.setShowTickMarks(true);
        maxIterationsSlider.setPrefWidth(200);
        maxIterationsValue = new Label(String.valueOf(maxIterations));
        // Listener to update value label
        maxIterationsSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldVale, Number newValue) -> {
            maxIterationsValue.setText(String.valueOf(newValue.intValue()));
        });
        
                // Max iterations
        maxRuntimeSlider = new Slider(1, 600, maxRuntime);
        maxRuntimeSlider.setMajorTickUnit(60); // every 60
        maxRuntimeSlider.setBlockIncrement(1);
        maxRuntimeSlider.setSnapToPixel(true);
        maxRuntimeSlider.setShowTickMarks(true);
        maxRuntimeSlider.setPrefWidth(200);
        maxRuntimeValue = new Label(String.valueOf(maxRuntime));
        // Listener to update value label
        maxRuntimeSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldVale, Number newValue) -> {
            maxRuntimeValue.setText(String.valueOf(newValue.intValue()));
        });
        
        // Run
        runAutomatedSimButton.setOnAction(action -> {
            maxIterations = (int) maxIterationsSlider.getValue();
            maxRuntime = (int) maxRuntimeSlider.getValue();
            
            // Start Sims
            runAutomatedSims();
            
            // Close automated sim window
            automatedSimStage.close();
        });
        
        automatedSimGrid = new GridPane();
        // Pads the edge of the screen
        automatedSimGrid.setPadding(new Insets(10, 10, 10, 10));
        // Pads content cells
        automatedSimGrid.setVgap(8);
        automatedSimGrid.setHgap(10);
        
        // Populate the grid
        GridPane.setConstraints(maxIterationsLabel, 0, 0);
        GridPane.setConstraints(maxIterationsSlider, 1, 0);
        GridPane.setConstraints(maxIterationsValue, 2, 0);
        GridPane.setConstraints(maxRuntimeLabel, 0, 1);
        GridPane.setConstraints(maxRuntimeSlider, 1, 1);
        GridPane.setConstraints(maxRuntimeValue, 2, 1);
        GridPane.setConstraints(runAutomatedSimButton, 2, 2);
        
        // Add to grid (show layout with format)
        automatedSimGrid.getChildren().addAll(maxIterationsLabel, maxIterationsSlider, maxIterationsValue,
                                maxRuntimeLabel, maxRuntimeSlider, maxRuntimeValue,
                                                                    runAutomatedSimButton);

        automatedSimStage = new Stage();
        automatedSimStage.setTitle("Run Automated Simulations");
        automatedSimStage.setScene(new Scene(automatedSimGrid));
        // Don't allow input to the window until this is closed
        automatedSimStage.initModality(Modality.WINDOW_MODAL);
        automatedSimStage.initOwner(window);
        automatedSimStage.setResizable(false);
        automatedSimStage.show();
    }
    
    /**
     * Outputs simulation settings: max ants, max nodes, pheromone multiplier, algorithm
     */
    private void outputSimSettings() {
        simLogger.log(Level.INFO, "Sim Params: Max ants:{0}   Max nodes:{1}   Pheromone multiplier:{2}   Sim iterations:{3}   Sim runtime:{4}",
                                    new Object[]{MAXANTS, MAXNODES, PHERMULTIPLIER, maxIterations, maxRuntime});
    }
    
    /**
     * Start off an automated simulation run
     */
    private void runAutomatedSims() {
        outputSimSettings();
        // Update button text
        automatedSimulation.setText("End Automated Simulations");

        // Set switch true
        automatedSim = true;

        // Start simulation
        startStop.fire();
    }
    
    /**
     * Resets settings related to automated simulation
     */
    private void resetAutomatedSim() {
        // Reset variables
        automatedSim = false;
        simulationsRun = 0;

        // Reset button text
        automatedSimulation.setText("Run Automated Simulations");
    }
    
    /**
     * Launches the application
     */
    public static void main(String[] args) {
        launch();
    }  
}
