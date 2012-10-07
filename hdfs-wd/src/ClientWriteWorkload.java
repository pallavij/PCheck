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



public class ClientWriteWorkload implements Workload {

  private Driver     d;
  private Hdfs       hdfs;
  private Utility    u;
  private Experiment exp;

  // *******************************************
  public ClientWriteWorkload(Driver driver, Experiment exp) {
    this.d = driver;
    this.hdfs = d.getHdfs();
    this.u = d.getUtility();
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

    String dest = String.format("file-%03d", exp.getExpNum());
    
    // write ......
    d.recordExpState("runConnect");
    hdfs.connect();
    u.createNewFile(d.EXPERIMENT_RUN_FLAG);
    d.recordExpState("runWrite");
    hdfs.putFile(dest, exp);
    d.recordExpState("runEnd");
    u.deleteFile(d.EXPERIMENT_RUN_FLAG);

  }

  // *******************************************
  public void postSetup() {


    String dest = String.format("file-%03d", exp.getExpNum());
    
    // 0. stop the failure
    d.disableFailureManager();
    d.disableClientOptimizer();
   
    // pallavi-14, extra check ..
    d.recordExpState("postBlockCount");
    hdfs.checkBlockCount(dest, exp);

    postCleanupIfFail();
    d.recordExpState("postEnd");
    
    /***
    if (exp.isFail()) {
      postCleanupIfFail();
      d.recordExpState("postEnd");
      return;
    }
    

    // 1. connect again
    d.recordExpState("postConnect");
    hdfs.connect();
    
    // 2. re-read with different client
    d.recordExpState("postGet");
    hdfs.getFileWDC(dest, exp);
    

    // 3. re-append with different client
    d.recordExpState("postAppend");
    hdfs.appendFileWDC(dest, exp); 

    // d.waitForLeaseRecovery();   // ???????????
    
    // 4. restart dead datanodes
    d.recordExpState("postRestart");
    d.restartDeadDataNodes();


    // 5. send block report
    d.recordExpState("postRec");
    d.sendBlockReport();


    // 6. run replication monitor 
    // this is the missing piece
    d.recordExpState("postRepMon");
    d.runReplicationMonitor();


    // 7. run block report again 
    d.recordExpState("postRec");
    d.sendBlockReport();

    // 6. delete, ideally shoud run delete, run rep mon, send heart beat
    d.recordExpState("postDelete");
    hdfs.delete(dest, exp);
    d.runReplicationMonitor();    
    d.sendBlockReport();
    
    // 8. done
    d.recordExpState("postEnd");
    ***/
  }


  // *******************************************
  public void postCleanupIfFail() {
    String dest = String.format("file-%03d", exp.getExpNum());

    d.recordExpState("postCleanUpIfFail");
    hdfs.delete(dest, exp);
    hdfs.close();
    //d.runReplicationMonitor();    
    //d.sendBlockReport();
  }


  // *******************************************
  public void preSetup() {
    // 1. enable failures (and optimizer)
    d.enableFailureManager();
    d.enableClientOptimizer();
    d.enableCoverage();
  }

}
