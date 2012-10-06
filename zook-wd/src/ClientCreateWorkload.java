/**
 * Copyright (c) 2012,
 * Thanh Do  <thanhdo@cs.wisc.edu>
 * Haryadi S. Gunawi  <haryadi@cs.uchicago.edu>
 * Pallavi Joshi  <pallavi@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


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
