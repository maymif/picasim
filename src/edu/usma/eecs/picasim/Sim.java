package edu.usma.eecs.picasim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
*/

public class Sim {

	public static final int SIM_LENGTH = 360;
	public static final int NUM_CHILDREN = 3; 
	public static final int NUM_TSC_TASSETS = 120;
	public static final int NUM_CSSB_TASSETS = 80;
	public static final int NUM_BSB_TASSETS = 20;
	public static final int CSSB_STOCK = 0;
	public static final int BSB_STOCK = 0;
	public static final int FSC_STOCK = 135;
	public static final int DIST_TSC_CSSB = 175;
	public static final int DIST_CSSB_BSB = 75;
	public static final int DIST_BSB_FSC = 75;
	public static final double SPEED_MEAN_TSC_CSSB = 40;
	public static final double SPEED_MEAN_CSSB_BSB = 35;
	public static final double SPEED_MEAN_BSB_FSC = 25;
	public static final double SPEED_STDEV_TSC_CSSB = 15.21;
	public static final double SPEED_STDEV_CSSB_BSB = 11.56;
	public static final double SPEED_STDEV_BSB_FSC = 5.76;

	private static final String INVENTORY_FILENAME = "inventories.txt";
	private static final String OPEN_REQS_FILENAME = "open_reqs.txt";
	private static final String TASSETS_FREE_FILENAME = "tassets_free.txt";
	private static final String TASSETS_IN_MAINT_FILENAME = "tassets_in_maint.txt";
	private static final String TASSETS_IN_USE_FILENAME = "tassets_in_use.txt";
	private static final String DEMAND_FILENAME = "demand.txt";
	private static final String TIME_IN_SYSTEM_FILENAME = "tis.txt";

	
	/** configuration file name specifying node names and locations */
//	private static String configFileName = "network.txt";
	private static int turnNum;
	private static ArrayList<Integer> timeInSystem;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Initialize the network
		//configure();
		createNodeTree(0, Node.NODE_TYPE_TSC);
		timeInSystem = new ArrayList<Integer>();
		
		int [][]demand = new int[Node.getNumNodes()][SIM_LENGTH];
		int [][]inventory = new int[Node.getNumNodes()][SIM_LENGTH];
		int [][]requisitions = new int[Node.getNumNodes()][SIM_LENGTH];
		int [][]freeTassets = new int[Node.getNumNodes()][SIM_LENGTH];
		int [][]inUseTassets = new int[Node.getNumNodes()][SIM_LENGTH];
		int [][]inMaintTassets = new int[Node.getNumNodes()][SIM_LENGTH];
		// Run the simulation
		Node thisNode;
		for (int i=0; i<SIM_LENGTH; i++) {
			turnNum = i;
			for (int n=0; n<Node.getNumNodes(); n++) {
				thisNode = Node.getNode(n);
				thisNode.checkDelivery();
				thisNode.checkDemand();
				thisNode.pushTasset();
				demand[n][i] = thisNode.getDemand();
				inventory[n][i] = thisNode.getInventory();
				requisitions[n][i] = thisNode.getOpenReqs().size();
				freeTassets[n][i] = thisNode.getTassetFreeQueue().size();
				inUseTassets[n][i] = thisNode.getTassetInUseQueue().size();
				inMaintTassets[n][i] = thisNode.getTassetMaintQueue().size();
			}
		}
		// Simulation complete
		
		// Write the inventories file
    	try{
    		File file =new File(INVENTORY_FILENAME);
    		//if file doesnt exists, then create it
    		if(!file.exists())
    			file.createNewFile();
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			for (int n=1; n<Node.getNumNodes(); n++) {
				thisNode = Node.getNode(n);
				// print inventories only for nodes with set stock levels
				if (thisNode.getStockLevel()>0) {
					//System.out.print(thisNode.getId() + ":  ");
					for (int i=0; i<SIM_LENGTH-1; i++) {
						bufferWritter.write(inventory[n][i] + ", ");
					}
					bufferWritter.write(inventory[n][SIM_LENGTH-1] + "\n");
				}
			}
    	    bufferWritter.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}

		// Write the demand file
    	try{
    		File file =new File(DEMAND_FILENAME);
    		//if file doesnt exists, then create it
    		if(!file.exists())
    			file.createNewFile();
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			for (int n=1; n<Node.getNumNodes(); n++) {
				thisNode = Node.getNode(n);
				// print demand only for nodes with set stock levels
				if (thisNode.getStockLevel()>0) {
					for (int i=0; i<SIM_LENGTH-1; i++) {
						bufferWritter.write(demand[n][i] + ", ");
					}
					bufferWritter.write(demand[n][SIM_LENGTH-1] + "\n");
				}
			}
    	    bufferWritter.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	
		// Write the open requisitions file
    	try{
    		File file =new File(OPEN_REQS_FILENAME);
    		//if file doesnt exists, then create it
    		if(!file.exists())
    			file.createNewFile();
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			for (int n=1; n<Node.getNumNodes(); n++) {
				thisNode = Node.getNode(n);
				// print open requisitions only for nodes with set stock levels
				if (thisNode.getStockLevel()>0) {
					for (int i=0; i<SIM_LENGTH-1; i++) {
						bufferWritter.write(requisitions[n][i] + ", ");
					}
					bufferWritter.write(requisitions[n][SIM_LENGTH-1] + "\n");
				}
			}
    	    bufferWritter.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	
		// Write the free tassets file
    	try{
    		File file =new File(TASSETS_FREE_FILENAME);
    		//if file doesnt exists, then create it
    		if(!file.exists())
    			file.createNewFile();
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			for (int n=1; n<Node.getNumNodes(); n++) {
				thisNode = Node.getNode(n);
				// print free tassets only for nodes with tassets
				if (thisNode.getNodeType() != Node.NODE_TYPE_FSC) {
					for (int i=0; i<SIM_LENGTH-1; i++) {
						bufferWritter.write(freeTassets[n][i] + ", ");
					}
					bufferWritter.write(freeTassets[n][SIM_LENGTH-1] + "\n");
				}
			}
    	    bufferWritter.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	
		// Write the in-use tassets file
    	try{
    		File file =new File(TASSETS_IN_USE_FILENAME);
    		//if file doesnt exists, then create it
    		if(!file.exists())
    			file.createNewFile();
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			for (int n=1; n<Node.getNumNodes(); n++) {
				thisNode = Node.getNode(n);
				// print in-use tassets only for nodes with tassets
				if (thisNode.getNodeType() != Node.NODE_TYPE_FSC) {
					for (int i=0; i<SIM_LENGTH-1; i++) {
						bufferWritter.write(inUseTassets[n][i] + ", ");
					}
					bufferWritter.write(inUseTassets[n][SIM_LENGTH-1] + "\n");
				}
			}
    	    bufferWritter.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	
		// Write the in-maintenance tassets file
    	try{
    		File file =new File(TASSETS_IN_MAINT_FILENAME);
    		//if file doesnt exists, then create it
    		if(!file.exists())
    			file.createNewFile();
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			for (int n=1; n<Node.getNumNodes(); n++) {
				thisNode = Node.getNode(n);
				// print in-maintenance tassets only for nodes with tassets
				if (thisNode.getNodeType() != Node.NODE_TYPE_FSC) {
					for (int i=0; i<SIM_LENGTH-1; i++) {
						bufferWritter.write(inMaintTassets[n][i] + ", ");
					}
					bufferWritter.write(inMaintTassets[n][SIM_LENGTH-1] + "\n");
				}
			}
    	    bufferWritter.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	
		// Write the time-in-system file
    	try{
    		File file =new File(TIME_IN_SYSTEM_FILENAME);
    		//if file doesnt exists, then create it
    		if(!file.exists())
    			file.createNewFile();
    		//true = append file
    		FileWriter fileWritter = new FileWriter(file.getName(),true);
    	    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
    	    int count = timeInSystem.size();
			for (int n=0; n<count-1; n++) {
				bufferWritter.write(timeInSystem.remove(0) + ",");
			}
			bufferWritter.write(timeInSystem.remove(0) + "\n");
    	    bufferWritter.close();
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    	
		System.out.println("Sim completed.");
	}

/*	
	private static void configure() {
		int nodeId;
		int parentId;
		int S;
		int numTassets;
		int numLines = 0;
		String line;
		// read the configuration file and create the entities
		try {
			FileReader fread = new FileReader (configFileName);
			BufferedReader reader = new BufferedReader(fread);
			line = reader.readLine();
			// while there are lines to read
			while (line != null) {
				// if the line is valid and not a comment
				if (line.length() > 1 && !line.startsWith("#")) {
					numLines++;
					// tokenize the line contents
					WStringTokenizer tokenizer = new WStringTokenizer(line, ",");
					if (tokenizer.countTokens() < 5) {
						System.err.println("Ignoring configuration line " + 
								numLines + " (wrong # of parameters)");
					}
					else {
						// read the tokens and create the node
						nodeId = Integer.parseInt(tokenizer.nextToken());
						S = Integer.parseInt(tokenizer.nextToken());
						numTassets = Integer.parseInt(tokenizer.nextToken());
						parentId = Integer.parseInt(tokenizer.nextToken());
						// ignore the number and IDs of children
						Node n = new Node(nodeId);
						n.setStockLevel(S);
						n.setInventory(S);
						n.setParentId(parentId);
						n.setNumTassets(numTassets);
					}
				}
				line = reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException fnfe) {
			System.out.println("File not found.");
		} catch (IOException ioe) {
			System.out.println("Error reading the file: " + ioe.getMessage());
		} 
		System.out.println(numLines + " configuration statements processed.");
	}
*/
	
	/**
	 * Creates a subtree of the given node type, rooted under the node with the
	 * given ID. Note that the first (root) node is always numbered 0.
	 * @param parentId
	 * @param nodeType
	 */
	private static void createNodeTree(int parentId, int nodeType) {
		int nodeId = 0;
		int numNodes = Node.getNumNodes();
		if (numNodes > 0)
			nodeId =  numNodes;
		Node n = new Node(nodeId, nodeType);
		n.setParentId(parentId);
		switch(nodeType) {
		case Node.NODE_TYPE_FSC:	n.setStockLevel(FSC_STOCK);
									n.setInventory(FSC_STOCK);
									n.setNumTassets(0);
									//System.out.println("FSC with ID " + n.getId() + " created.\n");
									break;
		case Node.NODE_TYPE_BSB:	n.setStockLevel(BSB_STOCK);
									n.setInventory(BSB_STOCK);
									n.setNumTassets(NUM_BSB_TASSETS);
									for (int i=0; i<NUM_CHILDREN; i++)
										createNodeTree(n.getId(), Node.NODE_TYPE_FSC);
									//System.out.println("BSB with ID " + n.getId() + " created.\n");
									break;
		case Node.NODE_TYPE_CSSB:	n.setStockLevel(CSSB_STOCK);
									n.setInventory(CSSB_STOCK);
									n.setNumTassets(NUM_CSSB_TASSETS);
									for (int i=0; i<NUM_CHILDREN; i++)
										createNodeTree(n.getId(), Node.NODE_TYPE_BSB);
									//System.out.println("CSSB with ID " + n.getId() + " created.\n");
									break;
		case Node.NODE_TYPE_TSC:	n.setStockLevel(Integer.MAX_VALUE);
									n.setInventory(Integer.MAX_VALUE);
									n.setNumTassets(NUM_TSC_TASSETS);
									for (int i=0; i<NUM_CHILDREN; i++)
										createNodeTree(n.getId(), Node.NODE_TYPE_CSSB);
									//System.out.println("TSC with ID " + n.getId() + " created.\n");
		}
	}
	
	public static int getTurnNum() {
		return turnNum;
	}
	
	public static void addTimeInSystem(int tos) {
		timeInSystem.add(tos);
	}
}
