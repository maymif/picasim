package edu.usma.eecs.picasim;

import java.util.Vector;

public class Requisition {

	private int id;
	private int src;
	private int dst;
	private int qty;
	private Vector<Integer> route;
	private int dtg;
	
	private static int numReqs;
	
	public Requisition() {
		id = numReqs++;
		route = new Vector<Integer>();
	}
	
	public int getSrc() {
		return src;
	}
	public void setSrc(int src) {
		if (Node.getNode(src) != null)
			this.src = src;
		else
			System.err.println("Requisition source node ID invalid.");
	}
	public int getDst() {
		return dst;
	}
	public void setDst(int dst) {
		if (Node.getNode(dst) != null)
			this.dst = dst;
		else
			System.err.println("Requisition destination node ID invalid.");
	}
	public int getQty() {
		return qty;
	}
	public void setQty(int qty) {
		if (qty > 0)
			this.qty = qty;
		else
			System.err.println("Requisition quantity must be greater than zero.");
	}

	public int getDtg() {
		return dtg;
	}
	public void setDtg(int dtg) {
		this.dtg = dtg;
	}

	public int getId() {
		return id;
	}
	
	/**
	 * Appends a node id to the route stack for a requisition.
	 * @param nid the node that is forwarding this requisition upstream
	 */
	public void pushNode(int nid) {
		if (Node.getNode(nid) != null) {
			if (this.route == null)
				this.route = new Vector<Integer>();
			route.add(new Integer(nid));
		}
		else
			System.err.println("Requisition route node ID invalid.");
	}
	
	/**
	 * Returns tne ID of the next node towards the source of the req
	 * @return the ID of the downstream node
	 */
	public int popNode() {
		int nid = -1;
		int routeLength = route.size();
		if (routeLength > 0)
			nid = route.remove(routeLength - 1);
		else
			nid = this.src;
		return nid;
	}
	
}
