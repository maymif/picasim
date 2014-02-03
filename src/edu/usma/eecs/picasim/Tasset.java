package edu.usma.eecs.picasim;

//import java.util.UUID;
import java.util.Vector;

public class Tasset {
	private int src;
	private int dst;
	private int delay;
	private Vector<Requisition> payload = new Vector<Requisition>();
	
	
	public Tasset() {
	}

	public int getSrc() {
		return src;
	}

	public void setSrc(int src) {
		if (Node.getNode(src) != null)
			this.src = src;
		else
			System.err.println("Tasset source ID invalid.");
	}

	public int getDst() {
		return dst;
	}

	public void setDst(int dst) {
		if (Node.getNode(dst) != null)
			this.dst = dst;
		else
			System.err.println("Tasset destination ID invalid.");
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		if (delay >= 0)
			this.delay = delay;
		else
			System.err.println("Tasset delay must be non-negative");
	}


	public Vector<Requisition> getPayload() {
		return payload;
	}
	
	public void addPaylod(Requisition r) {
		payload.add(r);
	}
	
}
