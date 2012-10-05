package org.fi;

public class Parameters{
	public final static boolean enableFailure = Boolean.parseBoolean(System.getProperty("enableFailure", "false"));
	public final static boolean enableOptimizer = Boolean.parseBoolean(System.getProperty("enableOptimizer", "false"));
	public final static boolean enableCoverage = Boolean.parseBoolean(System.getProperty("enableCoverage", "true"));
	public final static boolean debug = Boolean.parseBoolean(System.getProperty("debug", "true"));
	public final static boolean enableFrog = Boolean.parseBoolean(System.getProperty("enableFrog", "false"));

	public final static String workload = System.getProperty("workload", "leaderElect");
	//public final static String workload = System.getProperty("workload", "create");

	public final static int NUM_OF_ZK_NODES = Integer.parseInt(System.getProperty("ZK_NODES", "3"));
	public final static int MAX_FSN = Integer.parseInt(System.getProperty("MAX_FSN", "1"));
	//public final static int BREAK_EXP_NUMBER = Integer.parseInt(System.getProperty("BREAK_EXP_NUMBER", "500000"));
	public final static int BREAK_EXP_NUMBER = Integer.parseInt(System.getProperty("BREAK_EXP_NUMBER", "2"));
}
