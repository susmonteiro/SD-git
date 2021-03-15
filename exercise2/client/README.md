# REST Tic Tac Toe client

This is a client for the REST implementation of the Tic Tac Toe game.

The client depends on the contract module, where the messages shared between server and client are defined. 

Numbers 1-9 are used to choose where to play.
Number 0 is used to reset the board and number 10 is used to get the number of the opponent's plays.

## Instructions for using Maven

Make sure that you installed the contract module first.

To compile and run the client:

```
mvn compile exec:java [-DpostPlay]
```


## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

[SD Faculty](mailto:leic-sod@disciplinas.tecnico.ulisboa.pt)
