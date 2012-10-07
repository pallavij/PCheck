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



// this bug is to catch the fstime corruption bug

public class FsTimeWorkload implements Workload {

  private Driver     driver;
  private Hdfs       hdfs;
  private Utility    u;
  private Experiment exp;


  private String dir;
  private boolean mkdirSuccess = false;

  // *******************************************
  public FsTimeWorkload(Driver driver, Experiment exp) {
    this.driver = driver;
    this.hdfs = driver.getHdfs();
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
  public void preSetup() {
    
    // 3. let's make sure, we setup the connection before we go
    // into the run with failure
    hdfs.assertConnection();
    
    // not needed
    //hdfs.mkdirs("preSetup1", exp);
    //driver.killNameNode();
    // driver.restartDeadNameNode(exp);
    //hdfs.mkdirs("preSetup2", exp);

    
    // 2. enable failures (and optimizer)
    Driver.enableFailureManager();
    Driver.enableClientOptimizer();
    Driver.enableFrog(); // make sure it's enabled ...
    Driver.enableCoverage();
  }


  // *******************************************
  public void runWithFailure() {
    u.createNewFile(Driver.EXPERIMENT_RUN_FLAG);

    // transient failure should happen here ...
    dir = String.format("dir-%03d", exp.getExpNum());
    mkdirSuccess = false;
    mkdirSuccess = hdfs.mkdirs(dir, exp);
    
    // crash and corruption should happen here
    driver.killNameNode();
    driver.checkDeadNodes();
    driver.restartDeadNameNode();
    driver.waitForNnLeaveSafeMode(exp, "run-fstime");

    // hdfs.testClientNameNode();
    u.deleteFile(Driver.EXPERIMENT_RUN_FLAG);
  }

  // *******************************************
  public void postSetup() {
    u.createNewFile(Driver.POST_SETUP_FLAG);

    // 4. stop the failure
    Driver.disableCoverage();
    // Driver.disableFrog(); // don't do it here
    Driver.disableFailureManager();
    Driver.disableClientOptimizer();

    // if experiment already fails, something wrong with the namenode
    // reboot .. so we're gonna kill everything and start from fresh
    if (exp.isFail()) {
      
      u.WARNING("Dead/Fail Reboot: Experiment fails .. start fresh ...");
      
      driver.killHDFS();
      driver.rmImages();
      driver.rmTmps();
      driver.formatHDFS();
      driver.rmLogs();
      driver.startHDFS();
      hdfs.clearFs();
      hdfs.mkdirViaShell();
      u.deleteFile(Driver.POST_SETUP_FLAG);
      return;
    }

    // isDirectory();
    if (mkdirSuccess) {
      hdfs.checkIsDirectory(dir, exp);
      hdfs.delete(dir, exp);
    }

    u.deleteFile(Driver.POST_SETUP_FLAG);
  }

}
