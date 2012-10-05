
package org.fi;

import java.io.*;
import java.nio.channels.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;



public class LeaderElectionWorkload implements Workload {
  
  private Driver     driver;
  private Utility    u;
  private Experiment exp;


  // *******************************************
  public LeaderElectionWorkload(Driver driver, Experiment exp) {
  
    this.driver = driver;
    this.u = driver.getUtility();
    this.exp = exp;
  
  }
  

  // *******************************************
  // the algorithm
  // *******************************************
  public void run() {

    // 1. setup for this specific workload
    preSetup();

    // 2. the exact workload where we want to run with failure
    runWithFailure();
    
    // 3. run post setup 
    postSetup();
  
  }
  
  
  // *******************************************
  public void runWithFailure() {

    u.createNewFile(Driver.EXPERIMENT_RUN_FLAG);

    u.runCommand("./bin/run-test.sh");

    u.deleteFile(Driver.EXPERIMENT_RUN_FLAG);    
    
  }


  // *******************************************
  public void preSetup() {
    
    Driver.enableFailureManager();
    Driver.enableClientOptimizer();
    Driver.enableFrog();
    Driver.enableCoverage();
    
  }

  // *******************************************
  public void postSetup() {    

    // 4. stop the failure
    Driver.disableCoverage();
    Driver.disableFrog();
    Driver.disableFailureManager();
    Driver.disableClientOptimizer();
    
  }

  
}
