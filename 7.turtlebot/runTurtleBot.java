//package turtlebot;

import java.util.Arrays;
import java.util.Scanner;

public class runTurtleBot {

	String[] actions = {"\"drive\" at a set speed in range [-1, 1] m/s, e.g. drive 0.2", 
						"\"turnright\" or \"turnleft\" by a given amount of steps otherwise default amount is used, e.g. turnleft 5, turnright (default amount)",
						"\"read\" sensor data", "\"move\" to certain (x,y) location relative to bot's current position, e.g. move (-2,4)", 
						"\"return\" to starting position","\"reset\" starting location)"};
		
	private TurtleBotHighlevel turtlebot = new TurtleBotHighlevel("./demo_kobuki_initialisation");
	private Scanner sc = new Scanner(System.in);
	private String userInput;
	

	public void parse(String currLine) {
		String[] currAction = currLine.split(" ",2);
		String x, y;
					
		switch (currAction[0]) {
		case "drive":
			if (currAction.length == 1) 
				System.out.println("Drive speed needed.");
			else 
				//double linear_velocity = Double.parseDouble(currAction[1]);
				turtlebot.drive(Double.parseDouble(currAction[1]));
			break;
			
		case "turnleft":
			if (currAction.length == 1) /* turn by fixed amount */
				turtlebot.turnLeft();
			else  /* turn by programmable amount */  
				turtlebot.turn(Double.parseDouble(currAction[1]));
			break;
				
		case "turnright":
			if (currAction.length == 1)  /* turn by fixed amount */ 
				turtlebot.turnRight();
			else  /* turn by programmable amount */  
				turtlebot.turn(-Double.parseDouble(currAction[1]));
			break;
			
		case "read":
			CoreSensorData sensorData = turtlebot.readSensorData();
			System.out.println(sensorData);
			break;
			
		case "move":
			if (currAction.length == 1)
				System.out.println("Move location needed.");
			else{	
				x = currAction[1].substring(currAction[1].indexOf("(") + 1, currAction[1].indexOf(","));
				y = currAction[1].substring(currAction[1].indexOf(",") + 1, currAction[1].indexOf(")"));
System.err.println("move to ("+Double.parseDouble(x)+","+Double.parseDouble(y)+")");
				turtlebot.goTo(Double.parseDouble(x),Double.parseDouble(y));
			}
			break;
				
		case "return":
			turtlebot.goBack();
			break;
				
		case "reset":
			turtlebot.resetLocation();
			break;
			
		default:
			System.out.println("Invalid command");
		}	
	} 

	
	public static void main(String[] args) {
		runTurtleBot rtb = new runTurtleBot();
		rtb.runInstance();
	}
	public void runInstance(){
		try{
		System.out.println("Enter a command to move the robot. Enter separate commands on new lines. The robot accepts the following commands:");
		for (int i = 0; i < actions.length; i++) {
			System.out.println(actions[i]);
		}
		
		do {
			userInput = sc.nextLine();
			parse(userInput);
		}
		while (!userInput.equals("exit"));
		}
		catch(Exception e){
			if(turtlebot!=null)
				turtlebot.close();
		}
	
		
	}
	
}

