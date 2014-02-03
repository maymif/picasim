package edu.usma.eecs.picasim;

import java.util.Random;

public class Util {

//	public static final double MEAN_DEMAND = 0.3;
	public static final double MEAN_DEMAND = 1.8;
	public static final double MEAN_MAINT = 6.0;
	
	private static Random rng = new Random();

	private static int poisson(double mean) {
	    int r = 0;
	    double a = rng.nextDouble();
	    double p = Math.exp(-mean);

	    while (a > p) {
	        r++;
	        a = a - p;
	        p = p * mean / r;
	    }
	    return r;
	}
	
	public static int getDemand() {
		return poisson(MEAN_DEMAND);
	}
	
	public static int getMaintDelay() {
		return poisson(MEAN_MAINT);
	}
	
	public static int getTransDelay(int src, int dst) {
		double mean = 0.0;
		double stdv = 0.0;
		double dist = 0.0;
		int delay = 0;
		// The supporting node (i.e., the higher headquarters in the sustainment
		// chain is the one that dictates the road distance and type. For a
		// supply push, the supporting node is the source node, but on the 
		// backhauls this is inverted. In order to get the greatest distance
		// we need to choose the lesser of the types of src and dst nodes.
		Node sn = Node.getNode(src);
		int supportingNodeType = sn.getNodeType();
		Node dn = Node.getNode(dst);
		int otherNodeType = dn.getNodeType();
		if (supportingNodeType > otherNodeType)
			supportingNodeType = otherNodeType;
		// Determine the correct mean and standard deviation based on the type
		// of supporting unit.
		switch (supportingNodeType) {
		case Node.NODE_TYPE_TSC :	mean = Sim.SPEED_MEAN_TSC_CSSB;
									stdv = Sim.SPEED_STDEV_TSC_CSSB;
									dist = Sim.DIST_TSC_CSSB;
									break;
		case Node.NODE_TYPE_CSSB :	mean = Sim.SPEED_MEAN_CSSB_BSB;
									stdv = Sim.SPEED_STDEV_CSSB_BSB;
									dist = Sim.DIST_CSSB_BSB;
									break;
		case Node.NODE_TYPE_BSB :	mean = Sim.SPEED_MEAN_BSB_FSC;
									stdv = Sim.SPEED_STDEV_BSB_FSC;
									dist = Sim.DIST_BSB_FSC;
									break;
		}
		delay = (int)(dist / (rng.nextGaussian() * stdv + mean));
		// There is a small chance that the delay is negative, so we fix it
		if (delay < 0)
			delay = 0;
		return delay;
	}
	
}
