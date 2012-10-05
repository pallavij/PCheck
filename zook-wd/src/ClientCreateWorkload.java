
package org.fi;

import java.io.*;
import java.nio.channels.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.zookeeper.KeeperException;


public class ClientCreateWorkload implements Workload {
  
  private Driver     driver;
  private Zook       zook1;
  private Zook       zook2;
  private Utility    u;
  private Experiment exp;



  // *******************************************
  public ClientCreateWorkload(Driver driver, Experiment exp) {
    this.driver = driver;
    //this.zook = driver.getZook();
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
    u.sleep(500); //uncomment this line only if there is a race condition.

    CThread1 t1 = new CThread1();
    CThread2 t2 = new CThread2();

    t1.start();
    t2.start();
    try{t1.join();}catch(Exception e){}
    try{t2.join();}catch(Exception e){}

    u.createNewFile(Driver.WRITE_OVER_FLAG);

    while((new File(Driver.EXPERIMENT_RUN_FLAG)).exists())
		u.sleep(500);
  }


   class CThread1 extends Thread{

      public void run(){
	    String path = String.format("/file-%03d", exp.getExpNum());
	    byte[] data = {-20, -10, 10, 20};
	    zook1.setdata(path, data, -1);
      }

    }


   class CThread2 extends Thread{

      public void run(){
	    String path = String.format("/file-%03d", exp.getExpNum());
	    byte[] data = {-30, -20, 20, 10};
	    zook2.setdata(path, data, -1);
      }

    }

  // *******************************************
  public void preSetup() {

    zook1 = new Zook(driver);	  
    zook2 = new Zook(driver);	  
      	  
    zook1.assertConnection(1);
    zook2.assertConnection(3);
    
    String path = String.format("/file-%03d", exp.getExpNum());
    zook1.create(path, exp);
    
    // 2. enable failures (and optimizer)
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
    
    // get entry
    //String path = String.format("/file-%03d", exp.getExpNum());
    //zook1.exists(path, exp);
    //zook.areServersConsistent(Driver.NUM_OF_ZK_NODES, 2180, path);

    //zook.delete(path);
    
    // then delete the file
    // zook.delete(path);
    
    zook1.close();	  
    zook2.close();	  
    
  }

  
}
