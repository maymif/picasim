package edu.usma.eecs.picasim;

import java.util.Hashtable;
import java.util.Vector;

public class Node {
	
	public static final int NODE_TYPE_TSC = 1;
	public static final int NODE_TYPE_CSSB = 2;
	public static final int NODE_TYPE_BSB = 3;
	public static final int NODE_TYPE_FSC = 4;
	
	private static Hashtable<Integer, Node> nodes = new Hashtable<Integer, Node>();

	public static Node getNode(int i) {
		return nodes.get(i);
	}
	
	public static int getNumNodes() {
		return nodes.size();
	}
	
	private int id;
	private int parentId;
	private int lastDemand;
	private int nodeType;
	private boolean isLeafNode;
	private int stockLevel;
	private int inventory;
	private Vector<Requisition> openReqs;
	private Vector<Requisition> inQueue;
	private Vector<Requisition> outQueue;
	private Vector<Tasset> tassetFreeQueue;
	private Vector<Tasset> tassetInUseQueue;
	private Vector<Tasset> tassetMaintQueue;
	
	public Node(int id) {
		this.id = id;
		parentId = -1;
		isLeafNode = true;
		inventory = stockLevel;
		openReqs = new Vector<Requisition>();
		inQueue = new Vector<Requisition>();
		outQueue = new Vector<Requisition>();
		tassetFreeQueue = new Vector<Tasset>();
		tassetInUseQueue = new Vector<Tasset>();
		tassetMaintQueue = new Vector<Tasset>();
		nodes.put(id, this);
	}
	
	public Node(int id, int type) {
		this(id);
		this.setNodeType(type);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		//ensure the ID is unique
		if (getNode(id) == null)
			this.id = id;
		else
			System.err.println("Node IDs must be unique");
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int pid) {
		// Ensure the parent ID is valid
		Node parentNode = getNode(pid);
		if (parentNode != null) {
			this.parentId = pid;
			// If a node has a child, it cannot be a leaf
			parentNode.isLeafNode = false;
		}
		else
			System.err.println("Parent node cannot be null");
	}
	public int getDemand() {
		return this.lastDemand;
	}
	public int getNodeType() {
		return this.nodeType;
	}
	public void setNodeType(int t) {
		if (t >= NODE_TYPE_TSC && t <= NODE_TYPE_FSC)
			this.nodeType = t;
	}
	public int getStockLevel() {
		return stockLevel;
	}
	public void setStockLevel(int stockLevel) {
		if (stockLevel >= 0)
			this.stockLevel = stockLevel;
		else
			System.err.println("Stock level cannot be negative");
	}
	public int getInventory() {
		return inventory;
	}
	public void setInventory(int inventory) {
		if (inventory >= 0)
			this.inventory = inventory;
		else
			System.err.println("Inventory level cannot be negative");
	}
	public Vector<Requisition> getOpenReqs() {
		return openReqs;
	}
	public void setOpenReqs(Vector<Requisition> openReqs) {
		this.openReqs = openReqs;
	}
	public Vector<Requisition> getInQueue() {
		return inQueue;
	}
	public Vector<Requisition> getOutQueue() {
		return outQueue;
	}
	public Vector<Tasset> getTassetFreeQueue() {
		return tassetFreeQueue;
	}
	public Vector<Tasset> getTassetInUseQueue() {
		return tassetInUseQueue;
	}
	public Vector<Tasset> getTassetMaintQueue() {
		return tassetMaintQueue;
	}
	
	public void addTasset() {
		tassetFreeQueue.add(new Tasset());
	}

	public void setNumTassets(int n) {
//System.out.println("Node " + this.id + " gets " + n + " tassets");
		tassetFreeQueue = new Vector<Tasset>();
		tassetInUseQueue = new Vector<Tasset>();
		tassetMaintQueue = new Vector<Tasset>();
		for (int i=0; i<n; i++)
			tassetFreeQueue.add(new Tasset());
	}
	/**
	 * Checks to see if any requisitions have arrived at the local node. If so,
	 * they are closed (if meant for this node) or relayed (if for other node).
	 */
	public void checkDelivery() {
		Requisition r;
		// inQueue is where other nodes drop off requisitions sent to this node
		while (inQueue.size() > 0) {
			r = inQueue.remove(0);
			// See if this is something this node ordered
			if (r.getSrc() == this.id) {
				inventory += r.getQty();
				// When a node creates a requisition, it is stored locally in 
				// openReqs and passed by reference to other nodes for full-
				// filment. Thus, when the requisition is filled, the owning
				// node just has to destroy the original object.
				boolean success = false;
				int deliveryId = r.getId();
				int openReqId;
				for (int i=0; i<openReqs.size(); i++) {
					openReqId = openReqs.get(i).getId();
					if (deliveryId == openReqId) {
						openReqs.remove(i);
						success = true;
						break;
					}
				}
				if (success) {
					int ordered = r.getDtg();
					int received = Sim.getTurnNum();
					Sim.addTimeInSystem(received - ordered);
				}
				else
					System.err.println("Failed to remove open requisition " + deliveryId);
			}
			else
				// If the requisition is for another node, we put it in the 
				// outbound queue.
				outQueue.add(r);
		}
	}
	
	/**
	 * Processes a demand placed on the local node. Demands can be extrinsic
	 * (i.e., from a firing battery) or intrinsic (i.e., from another node in
	 * the system). An extrinsic demand occurs when a round is consumed by a
	 * fire mission. An intrinsic demand occurs when a downstream node places
	 * a demand on the local node.
	 */
	public void checkDemand() {
		// Nodes with no successors are leaf nodes representing fire
		// support companies
		if (this.isLeafNode) {
			// Extrinsic demand is a random function with range [0,1]
			lastDemand = Util.getDemand();
			if (lastDemand > 0) {
				// if we can satisfy the demand, we do and decrement the inventory
				if (lastDemand <= inventory)
					inventory -= lastDemand;
				// either way, we order more ammo
				orderAmmo(lastDemand);
			}
		}
		// Internal nodes attempt to fill requests from child nodes using
		// organic stocks (if any), and then create a new requisition to
		// replenish their stocks. Otherwise, they relay the requisition
		// upstream.
		else {
			Requisition r;
			int numReqs = openReqs.size();
			for (int i=0; i < numReqs; i++) {
				r = openReqs.remove(0);
				// The root node has no predecessors and fulfills requisitions
				// from an infinite local stock.
				if (this.parentId == this.id) {
					outQueue.add(r);
				}
				// Non-root nodes either fill the requisition or relay it
				else {
					lastDemand = r.getQty();
					// If the node has sufficient inventory, it fulfills the 
					// requisition and then restocks from higher using a new
					// requisition
					if (lastDemand <= inventory) {
						inventory -= lastDemand;
						outQueue.add(r);
						orderAmmo(lastDemand);
					}
					// If the node has insufficient inventory, it relays the
					// requisition upstream
					else {
						// Push this node's ID into the routing stack for the req
						r.pushNode(this.id);
						Node p = getNode(this.parentId);
						p.openReqs.add(r);
					}
				}
			}
		}
	}
	
	/**
	 * Creates a requisition for ammo and adds it to the upstream node's inQueue
	 * @param d the amount of ammo being requested
	 */
	private void orderAmmo(int d) {
		Requisition r = new Requisition();
		r.setSrc(this.id);
		r.setQty(d);
		r.setDtg(Sim.getTurnNum());
		openReqs.add(r);
		Node parent = Node.getNode(parentId);
		parent.openReqs.add(r);
	}
	
	/**
	 * Dispatches transportation assets to deliver all requisitions that are
	 * ready to be filled.
	 */
	public void pushTasset() {
		int len = this.tassetMaintQueue.size();
		Tasset t;
		// Flush out the maintenance queue, move ready tassets to free queue
		for (int i=0; i<len; i++) {
			t = this.tassetMaintQueue.remove(0);
			if (t.getDelay() > 0)
				t.setDelay(t.getDelay() - 1);
			// If the maintenance delay expires, move the tasset to the free queue
			if (t.getDelay() < 1)
				tassetFreeQueue.add(t);
			// If maintenance continues, put the tasset back in the maint queue
			else
				tassetMaintQueue.add(t);
		}
		// Flush out the in-use queue
		len = tassetInUseQueue.size();
		for (int i=0; i<len; i++) {
			t = tassetInUseQueue.remove(0);
			if (t.getDelay() > 0)
				t.setDelay(t.getDelay() - 1);
			// If transportation delay expires, tasset reached a destination
			if (t.getDelay() < 1) {
				// See if the load arrived at its destination. Tassets only run
				// between adjacent nodes, so if the destination ID is not this
				// node's ID that means the load reached the distant node.
				if (t.getDst() != this.id) {
					Node n = getNode(t.getDst());
					// Tassets can have multiple (requisition) payloads for 
					// future use. Currently, we assume only one load, so we 
					// add it to the destination node's inQueue.
					n.inQueue.add(t.getPayload().remove(0));
					// Return the tasset to base (this node)
					t.setSrc(t.getDst());
					t.setDst(this.id);
					t.setDelay(Util.getTransDelay(t.getSrc(), t.getDst()));
					tassetInUseQueue.add(t);
				}
				// Otherwise, the tasset returned to base so send to maintenance
				else {
					t.setDelay(Util.getMaintDelay());
					tassetMaintQueue.add(t);
				}
			}
			// Otherwise put the tasset back in the queue
			else {
				tassetInUseQueue.add(t);
			}
		}
		// Source new delivery missions while we have both free tassets and 
		// outbound requisitions
		while (tassetFreeQueue.size() > 0 && outQueue.size() > 0) {
			t = tassetFreeQueue.remove(0);
			Requisition r = outQueue.remove(0);
			t.setSrc(this.id);
			t.setDst(r.popNode());
			t.addPaylod(r);
			t.setDelay(Util.getTransDelay(t.getSrc(), t.getDst()));
			tassetInUseQueue.add(t);
		}
	}
	
}
