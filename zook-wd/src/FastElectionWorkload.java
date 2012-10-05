
package org.fi;

import java.io.*;
import java.nio.channels.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;



public class FastElectionWorkload implements Workload {
  
  private Driver     driver;
  private Zook       zook;
  private Utility    u;
  private Experiment exp;



  // *******************************************
  public FastElectionWorkload(Driver driver, Experiment exp) {
    this.driver = driver;
    this.zook = driver.getZook();
    this.u = driver.getUtility();
    this.exp = exp;
  }
  

  // *******************************************
  // the algorithm
  // *******************************************
  public void run() {

    // 1. setup for this specific workload
    preSetup();

    // 3. the exact workload where we want to run with failure
    runWithFailure();
    
    // 5. run post setup 
    postSetup();
  
  }
  
  
  // *******************************************
  public void runWithFailure() {

    u.createNewFile(Driver.EXPERIMENT_RUN_FLAG);

    Driver.startZooKeeperNodes();
  
    //zook.reconnectToZooKeeper();
    
    while((new File(Driver.EXPERIMENT_RUN_FLAG)).exists())
	    u.sleep(500);
    //u.deleteFile(Driver.EXPERIMENT_RUN_FLAG);    
    
  }


  // *******************************************
  public void preSetup() {
    
    Driver.enableFailureManager();
    Driver.enableClientOptimizer();
    Driver.enableCoverage();
    
  }

  // *******************************************
  public void postSetup() {    

    Driver.disableFailureManager();
    Driver.disableClientOptimizer();
    Driver.disableCoverage();
    
  }

  
}
