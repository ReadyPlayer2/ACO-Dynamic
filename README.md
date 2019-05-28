# ACO-Dynamic

JavaFX based GUI for Implementation of Ant Colony Optimisation as a simulation for point-to-point shortest path problem.

Uses Ant System and Max-Min Ant System algorithms (in basic form). Allows for nodes to be generated using Java's Random class and uses a proximity based system for connecting nodes with edges.

ACODynamic currently does the heavy lifting and can be vastly improved and modularised. 

## maven build & run
- Download maven and add to PATH.  
- `mvn clean`  
- `mvn compile `  
- `mvn package`  
- `java -cp target\ACO-Dynamic-1.0-SNAPSHOT.jar com.readyplayer2.aco.dynamic.ACODynamic`