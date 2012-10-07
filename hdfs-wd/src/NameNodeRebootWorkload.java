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


// This workload could cover several workloads:

// 1) Test crash during reboot:
//       1 storage dir at namenode, 1 datanode
//       filter: crash, at namenode

// 2) 



public class NameNodeRebootWorkload implements Workload {

  private Driver     d;
  private Hdfs       hdfs;
  private Utility    u;
  private Experiment exp;

  // *******************************************
  public NameNodeRebootWorkload(Driver driver, Experiment exp) {
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
    
    // reboot NN
    u.createNewFile(d.EXPERIMENT_RUN_FLAG);
    d.recordExpState("runFirstRestartNn");
    d.copyOutputLogsToPerExpInfoDir("beforeRunFirstRestartNn"); 
    d.restartDeadNameNode();
    d.waitForEitherNnReadyOrDead(exp, "RUNFIRSTRESTART");
    

    if (exp.isFail()) {
      u.deleteFile(d.EXPERIMENT_RUN_FLAG);
      return;
    }

     /*****    
    //if(Parameters.MAX_FSN >= 2){
	    // restart the node for two failures ... (if not dead), pallavi-12
	    d.recordExpState("runSecondRestartNn");   
	    d.copyOutputLogsToPerExpInfoDir("beforeRunSecondRestartNn"); 
	    d.restartDeadNameNode();
	    //d.waitForNnLeaveSafeMode(exp, "RUNSECONDRESTART");
	    d.waitForEitherNnReadyOrDead(exp, "RUNSECONDRESTART");
    //}
    
    d.recordExpState("runThirdRestartNn");   
    d.copyOutputLogsToPerExpInfoDir("beforeRunThirdRestartNn"); 
    d.restartDeadNameNode();
    d.waitForEitherNnReadyOrDead(exp, "RUNTHIRDRESTART");
    ****/

    u.deleteFile(d.EXPERIMENT_RUN_FLAG);
    
  }

  // *******************************************
  public void postSetup() {

    // stop the failure
    d.disableFailureManager();
    d.disableClientOptimizer();


    String dir1, dir3;
    dir1 = String.format("dir1-%03d", exp.getExpNum());
    dir3 = String.format("dir3-%03d", exp.getExpNum());
    

    // if fail, clean up and return
    if (exp.isFail()) {
      d.recordExpState("postCleanUp-1");
      d.copyOutputLogsToPerExpInfoDir("beforePostCleanUp-1"); 
      d.cleanAndRestartHdfs();
      d.recordExpState("postEnd");    
      return;
    }
    
    // restart the node ... (if not dead)
    d.recordExpState("postRestartNn");   
    d.copyOutputLogsToPerExpInfoDir("beforePostRestartNn"); 
    d.restartDeadNameNode();
    d.waitForNnLeaveSafeMode(exp, "POSTRESTART");
    
    
    // if fail, clean up and return
    if (exp.isFail()) {
      d.recordExpState("postCleanUp-2");
      d.copyOutputLogsToPerExpInfoDir("beforePostCleanUp-2"); 
      d.cleanAndRestartHdfs();
      d.recordExpState("postEnd");          
      return;
    }
    
    // connect again
    d.recordExpState("postConnect");
    hdfs.connect();
    
    // check dirs
    d.recordExpState("postIsDir1"); hdfs.checkIsDirectory(dir1, exp);
    d.recordExpState("postIsDir3"); hdfs.checkIsDirectory(dir3, exp);
    
    // post delete 
    d.recordExpState("postDelete1"); hdfs.delete(dir1, exp);
    d.recordExpState("postDelete3"); hdfs.delete(dir3, exp);
    
    d.recordExpState("postEnd");    
  }
  
  

  // *******************************************
  public void preSetup() {


    String dir1, dir3;
    dir1 = String.format("dir1-%03d", exp.getExpNum());
    dir3 = String.format("dir3-%03d", exp.getExpNum());
    
    // connect
    d.recordExpState("preConnect1"); hdfs.connect();

    // mkdirs
    d.recordExpState("preMkDir1");  hdfs.mkdirs(dir1, exp);
    
    // kill NN
    d.recordExpState("preKillNn1"); d.killNameNode(); d.checkDeadNodes();
    
    // restart NN
    d.recordExpState("preRestartNn"); 
    d.copyOutputLogsToPerExpInfoDir("beforePreRestartNn"); 
    d.restartDeadNameNode(); 
    d.waitForNnLeaveSafeMode(exp, "PRERESTART");


    // connect 
    d.recordExpState("preConnect2"); hdfs.connect();

    // mkdirs
    d.recordExpState("preMkDir3"); hdfs.mkdirs(dir3, exp);

    // kill NN 
    d.recordExpState("preKillNn2"); d.killNameNode(); d.checkDeadNodes();

    
    // enable failures (and optimizer)
    d.enableFailureManager();
    d.enableClientOptimizer();
    d.enableCoverage();

  }

}
