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
import java.util.*;

public class Driver {

  // ########################################################################
  // ########################################################################
  // ##                                                                    ##
  // ##                  E X P   P A R A M E T E R S                       ##
  // ##                                                                    ##
  // ########################################################################
  // ########################################################################


  public String curZkDir = "/home/st/pallavi/Research/PCheck/zook-wd/";

  public String workload = Parameters.workload;

  public static int NUM_OF_ZK_NODES = Parameters.NUM_OF_ZK_NODES;
  
  public static int BREAK_EXP_NUMBER = Parameters.BREAK_EXP_NUMBER;

  public static final int MAX_FSN = Parameters.MAX_FSN;


  public static boolean

    enableFailure = Parameters.enableFailure,

    enableOptimizer = Parameters.enableOptimizer,

    enableCoverage = Parameters.enableCoverage,

    debug = Parameters.debug,

    enableFrog = Parameters.enableFrog,

    junk;


  // ########################################################################
  // ########################################################################

  public static final String CMTLOGS_DIR = "commitLogs";
  public static final String DATA_DIR = "data";
  public static final String LOGS_DIR = "logs";

  // some locations
  public static final String TMPFI           = "/tmp/fi/";

  // dirs
  public static final String ZK_STORAGE_DIR        = TMPFI + "zk/";
  public static final String FAIL_HISTORY_DIR      = TMPFI + "failHistory/";
  public static final String RPC_FILES_DIR         = TMPFI + "rpcFiles/";
  public static final String FLAGS_FAILURE_DIR     = TMPFI + "flagsFailure/";
  public static final String EXP_RESULT_DIR        = TMPFI + "expResult/";
  public static final String SOCKET_HISTORY_DIR    = TMPFI + "socketHistory/";
  public static final String ZK_LOGS_DIR           = TMPFI + "logs/";
  public static final String ZK_PIDS_DIR           = TMPFI + "pids/";

  public static final String COVERAGE_COMPLETE_DIR = TMPFI + "coverageComplete/";
  public static final String COVERAGE_STATIC_DIR   = TMPFI + "coverageStatic/";

  // files and flags
  public static final String FROG_OUTPUT_FILE     = TMPFI + "frogOutput.txt";
  public static final String RESET_FROG_FLAG      = TMPFI + "resetFrogFlag";
  public static final String ENABLE_FAILURE_FLAG  = TMPFI + "enableFailureFlag";
  public static final String ENABLE_FROG_FLAG     = TMPFI + "enableFrogFlag";
  public static final String CLIENT_OPTIMIZE_FLAG = TMPFI + "clientOptimizeFlag";
  public static final String ENABLE_COVERAGE_FLAG = TMPFI + "enableCoverageFlag";

  // JASON ..
  public static final String EXPERIMENT_RUN_FLAG  = TMPFI + "expRunning";
  
  public static final String WRITE_OVER_FLAG  = TMPFI + "writeOver";

  //for message re-ordering
  public static final String EVT_ORDER_HISTORY_DIR  = TMPFI + "evtOrderHistory/";
  public static final String PFX_ORDER_HISTORY_DIR = EVT_ORDER_HISTORY_DIR + "pfxOrders/";
  public static final String EVT_ORDER_HISTORY_FILE = EVT_ORDER_HISTORY_DIR + "history";
  public static final String EVT_ORDER_PFX_TO_TRY = EVT_ORDER_HISTORY_DIR + "orderPfx";
  public static final String EVTS_WOKEN = EVT_ORDER_HISTORY_DIR + "wokenUpEvts";
  public static final String EVTS_WOKEN_DET = EVT_ORDER_HISTORY_DIR + "wokenUpEvtsDet";
  public static final String EVTS_WOKEN_WO_NODE = EVT_ORDER_HISTORY_DIR + "wokenUpEvtsWONode";
  public static final String EXEC_STATE = EVT_ORDER_HISTORY_DIR + "execState";
  public static final String QUEUE_STATE = EVT_ORDER_HISTORY_DIR + "queueState";
  public static boolean NORMAL_EXEC = false;
  public static final String WIPE_OUT_FLAG = EVT_ORDER_HISTORY_DIR + "wipeOut";
  public static final String LAST_EXP_WIPE_OUT_FLAG = EVT_ORDER_HISTORY_DIR + "lExpWipeOut";
  public static final String START_FLAG = EVT_ORDER_HISTORY_DIR + "start";
  public static final String EXP_START_FLAG = EVT_ORDER_HISTORY_DIR + "expStart";

  // vars
  private static Utility u;
  private static Zook zook;
  private static int expNum = 1;
  private static int wipedOutNum = 1; // # of wiped out experiments

  public Driver() {

    u = new Utility();
    u.setupPrintStream("/tmp/workloadOut.txt");

    // don't forget to run make kill
    printReminder();

    zook = new Zook(this);

  }

  // *******************************************
  // 1. prepare everthing
  // 2. decide if we want to enable failure or not
  // 3. run the recursive fsn ...
  // 4. final message or check
  // *******************************************
  public void run() {

    // a big preparation state before we
    // run a set of long experiments
    setUpBeforeEverything();

    // begin recursive fsn
    //recursiveFsn(1);
    testEvtOrders();

    // final message
    setUpAfterEverything();
  }


  // *******************************************
  // prepare general stuffs .. this takes a while
  // *******************************************
  public void setUpBeforeEverything() {

    createAllDirectories();

    // 0. disable failure manager
    disableCoverage();
    disableFailureManager();
    disableClientOptimizer();
    disableFrog();

    // 1. make sure current working directory is correct
    printCwd();


    // ------------------------------ clean up stuffs


    // killZook();

    // clear all
    rmPids();
    rmLogs();
    rmStorageFiles();
    rmExpResult();    // .. remove previous experiment result
    rmSocketHistory();    // .. remove previous experiment result
    rmRpcFiles();    // rm all rpc files
    rmCoverageFiles();
    rmFmStat();
    rmFlags(); // JASON ..
    rmEvtOrders();

    // 5. clear any failure flags
    clearAllFlagsFailure();

    // 6. clear all fail history (the directory that contains
    //    failure hash files)
    clearAllFailHistory();

    // ------------------------------ setup stuffs
    
    // 8. format zookeeper
    // 9. start the failure manager
    //10. start the server
    
    startFailureManager();
    if(!workload.equals("leaderElect")){ 
    	    formatZooKeeper();
    	    startZooKeeperNodes();
    }

    // record max fsn
    recordMaxFsn();
    u.createNewFile(START_FLAG);
    
    u.mkDir(PFX_ORDER_HISTORY_DIR);

  }

  // *******************************************
  // final
  // *******************************************
  public void setUpAfterEverything() {
    if(!workload.equals("leaderElect")){ 
	    u.print("===================Stopping ZooKeeper Nodes==================\n");
	    stopZooKeeperNodes();
	    rmStorageFiles();
    }

    printAllExperimentsFinish();

  }



  // ########################################################################
  // ########################################################################
  // ##                                                                    ##
  // ##                       C O R E    L O G I C                         ##
  // ##                                                                    ##
  // ########################################################################
  // ########################################################################



  // *******************************************
  // the recursive logic of multiple failures ... not documented right now

  // - unlockFsn: we want to unlock the failure sequence number for this
  //   fsn so we can insert failures
  // - clearFailHistoryOfPostFsns: if we are at failures A-C-?
  //   we want to exercise A-C-D, A-C-E ... but D and E at fsn-3 might
  //   have been exercised before (e.g. A-B-D, A-B-E). so we want to
  //   clear all post fsns so that we can try D and E again at fsn-3.
  // - if (fsn != MAX_FSN) recursiveFsn(fsn + 1)
  //   we only want to run the workload if we get to final fsn .. because
  //   we are indeed trying MAX_FSN of failures.
  // - prepareToRunWorkload .. see there
  // - currentFhf and lastFhf: check if we're at fsn-X, do we see
  //   new failures or not ... if not, break .. so we go back to fsn-X-1
  //   and recursive since then
  // - lock pre-fsns ... say if we are at failures A-B-C, basically
  //   what we want to say is in the next experiment we want to
  //   exercise A-B-D .. so it means we must lock fsn-1 and fsn-2
  //   such that they will go fail A and B
  // *******************************************
  public void recursiveFsn(int fsn) {

    String lastFhf = "LAST";


    // keep doing until there is no new failure for this
    // failure sequence number
    while(true) {

      // see if we can break
      if (expNum > BREAK_EXP_NUMBER)
        break;

      //u.print("\n\n------------------------ fsn-%d \n\n", fsn));

      // ...
      unlockFsn(fsn);
      clearFailHistoryOfPostFsns(fsn);

      // ...
      if (fsn != MAX_FSN) {
        recursiveFsn(fsn+1);
      }
      else {
        prepareToRunWorkload();
      }

      // should we move on ?
      lastFhf = compareAndGetNewFhf(lastFhf, fsn);
      if (lastFhf == null)
        break;

      // lock pre fsns ..
      lockPreFsns(fsn);

    }
  }

  
  public void testEvtOrders() {

    int count = 0;	  

    while(true) {

      count++;	    

      if (expNum > BREAK_EXP_NUMBER)
        break;

      //prepareToRunWorkload();

      //if the last experiment was wiped out
      if((new File(WIPE_OUT_FLAG)).exists()){
      	      (new File(EVT_ORDER_PFX_TO_TRY)).delete();
	      u.createNewFile(LAST_EXP_WIPE_OUT_FLAG);
      }	      

      if((count == 1) || (new File(WIPE_OUT_FLAG)).exists() 
		      || (new File(EVT_ORDER_PFX_TO_TRY)).exists()){
	      prepareToRunWorkload(); 
      }	  
      else{
	      break;
      }   

    }

  }


  // *******************************************
  // if current is a new one, returns current
  // else, return null
  // *******************************************
  public String compareAndGetNewFhf(String lastFhf, int fsn) {

    String currentFhf = getLatestFhf(fsn);

    // no failure condition
    if (currentFhf == null)
      return null;

    // last failure is the same as current failure
    // u.print(String.format("- Comparing fsn-%d  last=%s  current=%s \n",
    // fsn, currentFhf, lastFhf));
    if (lastFhf.equals(currentFhf))
      return null;

    // return the new fhf
    return currentFhf;

  }


  // *******************************************
  public void prepareToRunWorkload() {

    // create experiment
    Experiment exp = new Experiment(this, expNum);
    exp.printBegin();

    // prepare to run workload
    setupBeforeEachWorkload(exp);

    // run the actual workload
    runWorkload(exp);
    /****
    Thread t = new WThread(exp);
    t.start();
    try{
      //t.join(900000);
      t.join();
    }
    catch(Exception e){
    }
    ****/

    //PALLAVI: recording coverage information
    u.runCommand("java -cp ../zook-fi/lib/emma.jar emma ctl -connect localhost:5001 -command coverage.get,../zook-fi/emma/coverage1.emma,true,true -command coverage.reset");
    u.runCommand("java -cp ../zook-fi/lib/emma.jar emma ctl -connect localhost:5002 -command coverage.get,../zook-fi/emma/coverage2.emma,true,true -command coverage.reset");
    u.runCommand("java -cp ../zook-fi/lib/emma.jar emma ctl -connect localhost:5003 -command coverage.get,../zook-fi/emma/coverage3.emma,true,true -command coverage.reset");

    // setup after
    setupAfterEachWorkload(exp);

  }

  
  class WThread extends Thread{
	  Experiment exp;

	  public WThread(Experiment exp){
		  this.exp = exp;
	  }

	  public void run(){
		  runWorkload(exp);
	  }
  }



  // *******************************************
  public void setupBeforeEachWorkload(Experiment exp) {

    u.print("- Prepare before each workload ... \n");

    if(workload.equals("leaderElect")){ 
    	formatZooKeeper();
    }

    recordCurrentExpNumber(exp.getExpNum());
    //clearAllInjectedFsn();
    //clearAllBadDiskFlags();

    u.deleteFile(WIPE_OUT_FLAG);

    u.createNewFile(EXP_START_FLAG);
    u.createNewFile(EVTS_WOKEN);
    u.createNewFile(EVTS_WOKEN_DET);
    u.createNewFile(EVTS_WOKEN_WO_NODE);
    u.createNewFile(EXEC_STATE);
    u.createNewFile(QUEUE_STATE);

    u.mkDir(EXP_RESULT_DIR + exp.getExpNumShortDirName() + "/");

    //checkDeadNodes();
    //restartDeadDataNodes();
    //resetFrog();

    // must be after restarting dead datanodes !!
    //rmAllBlocks();
  }


  // *******************************************
  public void setupAfterEachWorkload(Experiment exp) {

    //u.deleteFile(EVT_ORDER_PFX_TO_TRY);
    if(!((new File(WIPE_OUT_FLAG)).exists())){ 
	    exp.writeEvtIdsInExp();
	    //exp.writeOrderPfxToTry();   
    }
    //u.stringToFileContent("===================================================\n", EVTS_WOKEN, false, true);
    
    String lgDir = curZkDir + "logs/";
    String expDirName = exp.getExpNumShortDirName();
    String lgForExpDir = lgDir + expDirName;
    u.mkDir(lgForExpDir);
    u.runCommand("cp " + TMPFI + "logs/node1.out " + lgForExpDir + "/node1.out");    
    u.runCommand("cp " + TMPFI + "logs/node2.out " + lgForExpDir + "/node2.out");    
    u.runCommand("cp " + TMPFI + "logs/node3.out " + lgForExpDir + "/node3.out");    
    
    
    u.runCommand("cp " + EVTS_WOKEN + " " + TMPFI + "eWoken/" + expDirName);
    u.runCommand("cp " + EVTS_WOKEN_WO_NODE + " " + TMPFI + "eWokenWONode/" + expDirName);
    u.runCommand("cp " + EVTS_WOKEN_DET + " " + TMPFI + "eWokenDet/" + expDirName);
    u.deleteFile(EVTS_WOKEN);
    u.deleteFile(EVTS_WOKEN_DET);
    u.deleteFile(EVTS_WOKEN_WO_NODE);
    u.runCommand("cp " + EXEC_STATE + " " + TMPFI + "eStates/" + expDirName);
    u.deleteFile(EXEC_STATE);
    u.deleteFile(QUEUE_STATE);

    if(workload.equals("leaderElect")){ 
	    u.print("===================Closing ZooKeeper Client==================\n");
	    if(zook.zk != null){
		try{
		    zook.zk.close();
		}
		catch(Exception e){
		    u.ERROR("Exception while closing the Zookeeper client");	
		}
	    } 

	    u.print("===================Stopping ZooKeeper Nodes==================\n");
	    stopZooKeeperNodes();
	    rmStorageFiles();
    }
   
    u.runCommand(curZkDir + "scripts/moveCovFiles.py");

    // done
    exp.printEnd();
    
    /***********************************************
    // now check if this experiment fails
    exp.checkFailExperiment();

    exp.printFailHistorySummary();
    if (debug) {
      exp.printFailHistory();
    }

    // this experiment does not reach the max fsn
    if (!exp.reachMaxFsn()) {
      // must be after checking max fsn ...
      // just delete the content of the experiment dir
      if (!debug) {
        exp.rmExpDirContent();
      }

      exp.wipeOutThisExperiment();
    }
    else {

      // delete the dir content if not fail
      if (!exp.isFail() && !debug) {
        exp.rmExpDirContent();
      }
    }
    ***********************************************/

    if((new File(WIPE_OUT_FLAG)).exists()){
	exp.wipeOutThisExperiment();
    }

    // increment the experiment number
    incrementExpNum();

    // and remove all ports otherwise the directory gets too big !!!
    // and it could contain thousands of files
    rmSocketHistory();

    //u.deleteFile(Driver.MSGS_WOKEN);
    
    //process coverage info file

    u.runCommand("../zook-fi/scripts/distinctBranches.py");

  }


  // *******************************************
  // check the algorithm below
  // *******************************************
  public void runWorkload(Experiment exp) {

    // the experiment runs here ...
    // here, we can write whatever experiments we want
    // so now, I've created the workload client write class
    // which will run the client write workloads

    if (workload.contains("create")) {
      ClientCreateWorkload ccw = new ClientCreateWorkload(this, exp);
      ccw.run();
    }

    if (workload.contains("leaderElect")) {
      FastElectionWorkload few = new FastElectionWorkload(this, exp);
      few.run();
    }

  }

  // *******************************************
  // unlock this fsn so the hash for this fsn is free
  // unlock this and aosl delete the fsn hash file
  // *******************************************
  public void unlockFsn(int fsn) {

    u.print("- Unlocking fsn-" + fsn + "\n");

    File f = getFsnLockFile(fsn);
    u.deleteFile(f);

    String hashPrefix = "hash-for-fsn-" + fsn;
    u.deleteDirContent(FLAGS_FAILURE_DIR, hashPrefix);
  }

  // *******************************************
  // this is optional if we want to repeat a failure
  // but in difference zone ... try to disable this
  // and see what will happen
  // *******************************************
  public void clearFailHistoryOfPostFsns(int currentFsn) {
    u.print(String.format
            ("- Clearing fail history of post fsns %d - %d \n" ,
             currentFsn+1, MAX_FSN));
    for (int i = currentFsn+1; i <= MAX_FSN; i++) {
      clearFailHistory(i);
    }
  }

  // *******************************************
  // .../failHistory/fsn-1/
  // if we clear fail history we must also clear the latest history
  // of this fsn
  // ../failHistory/latest-for-fsn-1
  // *******************************************
  public void clearFailHistory(int fsn) {
    u.print("- Clearing fail history of fsn-" + fsn + "\n");

    String path = String.format("%s/fsn-%d",
                                FAIL_HISTORY_DIR, fsn);
    if (!u.deleteDir(path)) {
      u.ERROR("Can't delete " + path);
    }

    File f = getLatestHistoryFile(fsn);
    u.deleteFile(f);

  }


  // *******************************************
  // .../flagsFailure/injected-fsn-*
  // *******************************************
  public void clearAllInjectedFsn() {
    u.print("- Clearing all injected fsns ...\n");
    u.deleteDirContent(FLAGS_FAILURE_DIR, "injected-fsn-");
  }

  // *******************************************
  // .../flagsFailure/BadDisk_..
  // *******************************************
  public void clearAllBadDiskFlags() {
    u.print("- Clearing all injected fsns ...\n");
    u.deleteDirContent(FLAGS_FAILURE_DIR, "BadDisk");
  }


  // *******************************************
  // check if pre fsns are not locked then we want to
  // lock them
  // *******************************************
  public void lockPreFsns(int currentFsn) {
    u.print(String.format("- Locking pre fsns %d - %d \n", 1, currentFsn));
    for (int i = 1; i < currentFsn; i++) {
      lockFsn(i);
    }
  }

  // *******************************************
  // lockFsn comprises of adding the flag,
  // and also adding the latest failure hash id
  // *******************************************
  public void lockFsn(int fsn) {
    u.print("- Locking fsn-" + fsn + "\n");

    File f = getFsnLockFile(fsn);
    u.createNewFile(f);

    String latestFhf = getLatestFhf(fsn);
    if (latestFhf == null) {
      u.FATAL("lockFsn logic error");
    }

    f = getFsnAndHashFile(fsn, latestFhf);
    u.createNewFile(f);
  }


  // *******************************************
  private void recordCurrentExpNumber(int expNum) {
    String path = FLAGS_FAILURE_DIR + "/currentExpNumber";
    String tmp = String.format("%d", expNum);
    u.stringToFileContent(tmp, path);
  }



  // ########################################################################
  // ########################################################################
  // ##                                                                    ##
  // ##                        U T I L I T Y                               ##
  // ##                                                                    ##
  // ########################################################################
  // ########################################################################


  // *******************************************
  private static void incrementExpNum() {
    expNum++;
  }

  // *******************************************
  public static int getWipedOutNum() {
    return wipedOutNum;
  }

  // *******************************************
  public static void incrementWipedOutNum() {
    wipedOutNum++;
  }

  // ********************************************
  // filename: locked-fsn-#
  // ********************************************
  private File getFsnLockFile(int fsn) {
    String path = String.format("%s/locked-fsn-%d",
                                FLAGS_FAILURE_DIR,
                                fsn);
    File f = new File(path);
    return f;
  }

  // ********************************************
  // return the latest history file for this fsn
  private File getLatestHistoryFile(int fsn) {
    String path = String.format("%s/latest-for-fsn-%d", FAIL_HISTORY_DIR, fsn);
    File f = new File(path);
    return f;
  }


  // ***************************************************
  public String getLatestFhf(int fsn) {

    File f = getLatestHistoryFile(fsn);
    if (!f.exists()) {
      return null;
    }
    String tmp = u.fileContentToString(f);
    if (tmp == null)
      return tmp;
    tmp = tmp.replaceAll("\n", "");
    return tmp;
  }


  // ********************************************
  // filename: hash-for-fsn-%d-is-
  // ********************************************
  private File getFsnAndHashFile(int fsn, String hash) {
    String path = String.format("%s/hash-for-fsn-%d-is-h%s.txt",
                                FLAGS_FAILURE_DIR,
                                fsn, hash);
    File f = new File(path);
    return f;
  }

  public void printCwd() {
    String curDir = System.getProperty("user.dir");
    u.println(String.format("- Current directory is " + curDir));
  }


  // *******************************************
  public void rmCoverageFiles() {
    u.print("- Removing Coverage files ...\n");
    if (!u.deleteDirContent(COVERAGE_COMPLETE_DIR)) {
      u.ERROR("Can't delete " + COVERAGE_COMPLETE_DIR);
    }
    if (!u.deleteDirContent(COVERAGE_STATIC_DIR)) {
      u.ERROR("Can't delete " + COVERAGE_COMPLETE_DIR);
    }
  }


  // *******************************************
  // remove all files recursively starting from 
  // *******************************************
  public void rmStorageFiles() {
    u.print("- Removing images ...\n");
    if (!u.deleteDir(ZK_STORAGE_DIR)) {
      u.ERROR("Can't delete " + ZK_STORAGE_DIR);
    }
  }

  // *******************************************
  // remove all files recursively
  // *******************************************
  public void rmExpResult() {
    u.print("- Removing previous experiment results ...\n");
    if (!u.deleteDirContent(EXP_RESULT_DIR)) {
      u.ERROR("Can't delete " + EXP_RESULT_DIR);
    }
  }

  // *******************************************
  // remove sockethistory
  // *******************************************
  public void rmSocketHistory() {
    u.print("- Removing socket history ...\n");
    if (!u.deleteDirContent(SOCKET_HISTORY_DIR)) {
      u.ERROR("Can't delete " + SOCKET_HISTORY_DIR);
    }
  }

  // *******************************************
  // remove rpc files
  // *******************************************
  public void rmRpcFiles() {
    u.print("- Removing RPC files ...\n");
    if (!u.deleteDirContent(RPC_FILES_DIR)) {
      u.ERROR("Can't delete " + RPC_FILES_DIR);
    }
  }



  // *******************************************
  // clear failure flags flags
  // *******************************************
  public void clearAllFlagsFailure() {
    u.print("- Clearing all failure flags ...\n");
    if (!u.deleteDirContent(FLAGS_FAILURE_DIR)) {
      u.ERROR("Can't delete " + FLAGS_FAILURE_DIR);
    }
  }

  // *******************************************
  // reset frog
  // this is a stupid but fast way to reset frog
  // just create a new flag /tmp
  // *******************************************
  public void resetFrog() {
    u.print("- Resetting frog ...\n");
    File f = new File(RESET_FROG_FLAG);
    try {
      f.createNewFile();
    } catch (Exception e) {
      u.ERROR("can't create " + f.getAbsolutePath());
    }

  }


  // *******************************************
  // remove all files inside the FAIL_HISTORY_DIR dir
  // *******************************************
  public void clearAllFailHistory() {
    u.print("- Removing all fail history ...\n");
    if (!u.deleteDirContent(FAIL_HISTORY_DIR)) {
      u.ERROR("Can't delete " + FAIL_HISTORY_DIR);
    }
  }


  // *******************************************
  // rm all logs file ..
  // *******************************************
  public void rmLogs() {
    u.print("- Removing logs ...\n");
    if (!u.deleteDirContent(ZK_LOGS_DIR)) {
      u.ERROR("Can't delete " + ZK_LOGS_DIR);
    }
  }


  // *******************************************
  // rm all logs file ..
  // *******************************************
  public void rmPids() {
    u.print("- Removing logs ...\n");
    if (!u.deleteDirContent(ZK_PIDS_DIR)) {
      u.ERROR("Can't delete " + ZK_PIDS_DIR);
    }
  }

  // *******************************************
  // rm all evt ordering files ..
  // *******************************************
  public void rmEvtOrders() {
    u.print("- Removing event ordering files ...\n");
    if (!u.deleteDirContent(EVT_ORDER_HISTORY_DIR)) {
      u.ERROR("Can't delete " + EVT_ORDER_HISTORY_DIR);
    }
  }

  // *******************************************
  // start failure manager
  // *******************************************
  public void startFailureManager() {
    
    u.print("- Starting Failure Manager ...\n");
    String cmdout = u.runCommand("bin/zkServer.sh startfi");
    u.print(cmdout);
    u.print("\n\n");
  }

  // *******************************************
  // record max fsn, so the fm knows the max fsn
  // *******************************************
  public void recordMaxFsn() {
    String path = FLAGS_FAILURE_DIR + "/maxFsn";
    String tmp = String.format("%d", MAX_FSN);
    u.stringToFileContent(tmp, path);
  }

  // *******************************************
  // enable failure manager via the fmadmin command
  // see my bin/hadoop to find what this is
  public static void enableFailureManager() {

    if (!Driver.enableFailure)
      return;

    u.print("- Enabling Failure Manager ...\n");
    u.createNewFile(ENABLE_FAILURE_FLAG);
  }


  // *******************************************
  public static void enableFrog() {

    if (!Driver.enableFrog)
      return;

    u.print("- Enabling Frog ...\n");
    u.createNewFile(ENABLE_FROG_FLAG);
  }


  // *******************************************
  public static void disableFrog() {
    u.print("- Disabling Frog ...\n");
    u.deleteFile(ENABLE_FROG_FLAG);
  }


  // *******************************************
  public static void enableCoverage() {

    if (!Driver.enableCoverage)
      return;

    u.print("- Enabling Coverage ...\n");
    u.createNewFile(ENABLE_COVERAGE_FLAG);
  }

  // *******************************************
  public static void disableCoverage() {
    u.print("- Disabling Coverage...\n");
    u.deleteFile(ENABLE_COVERAGE_FLAG);
  }


  // *******************************************
  public static void enableClientOptimizer() {

    if (!Driver.enableOptimizer)
      return;

    u.print("- Optimizing FM Client ...\n");
    u.createNewFile(CLIENT_OPTIMIZE_FLAG);
  }


  // *******************************************
  public static void disableClientOptimizer() {
    u.print("- unOptimizing FM Client ...\n");
    u.deleteFile(CLIENT_OPTIMIZE_FLAG);
  }

  // *******************************************
  public static void disableFailureManager() {
    u.print("- Disabling Failure Manager ...\n");
    u.deleteFile(ENABLE_FAILURE_FLAG);
  }

  // *******************************************
  public void rmFmStat() {
    u.deleteFile("/tmp/fmStat.txt");
  }

  // *******************************************
  // JASON ..
  public void rmFlags() {
    u.print("- Deleting flags ...\n");
    u.deleteFile(Driver.EXPERIMENT_RUN_FLAG);
  }  

  // *******************************************
  public static void startZooKeeperNodes() {
    
    u.print("- Starting ZooKeeper ...\n");
    
    for (int i = 1; i <= NUM_OF_ZK_NODES; i++) {
	  System.out.flush();

          String cmd = String.format("bin/zkServer.sh start zoo%d.cfg %d",
				 i, i);
      
          String cmdout = u.runCommand(cmd);
          u.print(cmdout);
          u.print("\n\n");
	  u.sleep(1000);
    }

    u.print("This line is in startZooKeeperNodes(), after FOR LOOP");

  }


  // *******************************************
  public static void stopZooKeeperNodes() {
    
    u.print("- Stopping ZooKeeper ...\n");
    
    for (int i = 1; i <= NUM_OF_ZK_NODES; i++) {
	
          String cmd = String.format("bin/zkServer.sh stop zoo%d.cfg %d",
				 i, i);
      
          String cmdout = u.runCommand(cmd);
          u.print(cmdout);
          u.print("\n\n");
	  u.sleep(1000);
    }

    u.print("This line is in stopZooKeeperNodes(), after FOR LOOP");

  }
  
  
  // *******************************************
  // not alway work
  public void killZook() {

    u.print("- Killing Zookeeper nodes ...\n");
    /*
      NodeProcess[] nps = getNodeProcesses();
      if (nps == null) return;
      for (int i = 0; i < nps.length; i++) {
      String cmd = String.format("kill -s KILL %5s", nps[i].getPid());
      u.print(String.format("   %s, %s \n", cmd, nps[i].getName()));
      String cmdout = u.runCommand(cmd);
      }
    */
    u.print("\n\n");
  }


  // *******************************************
  // a bit stupid method to find out if a datanode is dead or not
  // just do ps -p pid .. then search if there is the word java in it or not
  public void checkDeadNodes() {
    u.print("- Checking dead nodes ...\n");
    u.MESSAGE(" FIXME, getNodeProcesses \n");

    /*
      NodeProcess[] nps = getNodeProcesses();
      if (nps == null) return;
      for (int i = 0; i < nps.length; i++) {
      boolean isAlive = u.isPidAlive(nps[i].getPid());
      u.print(String.format("   %-5s  %-15s ", nps[i].getPid(), nps[i].getName()));
      if (isAlive) { u.print("ok   \n"); }
      else         { u.print("DEAD \n"); }
      }
    */
    u.print("\n\n");
  }


  // *******************************************
  // restart dead datanodes ...
  // go through each pid in /tmp/hadoop..pid
  // and find which pid is dead
  // FIXME
  // *******************************************
  public void restartDeadDataNodes() {
    u.print("- Restarting dead datanodes ...\n");

    u.MESSAGE("FIXME: restart dead datanodes \n");

    /*
      NodeProcess[] nps = getNodeProcesses();
      if (nps == null) return;
      for (int i = 0; i < nps.length; i++) {
      boolean isAlive = u.isPidAlive(nps[i].getPid());
      if (isAlive || !nps[i].isDataNode())
      continue; // continue if it's alive or it's not a datanode

      String s = String.format("    Restarting %-15s %s \n",
      nps[i].getName(), nps[i].getDnId());
      u.print(s);

      // before restarting, make sure we remove
      // all stuffs that relate to this dead datanode
      // such as the pid file, and log files

      // first I need to remove the tmp pid file
      u.deleteFile(ZK_DATA, nps[i].getTmpPidFile());

      // then I need to remevove the logs
      rmDataNodeLogFile(nps[i].getDnId());

      // let's resetart the datanode
      restartDataNode(nps[i].getDnId());

      // okay so we must wait until that datanode is registered
      u.print("    Waiting for registration ...\n");
      waitForDnRegistration(nps[i].getDnId());

      }
    */
    u.print("\n\n");
  }

  // *******************************************
  // restart the datanode with dnId
  // in my case I could do this by callng
  // e.g. "pdatanode -3" .. in thanh's case you
  // must set the conf folder properly
  public void restartDataNode(String dnId) {
    // or, bin/hd.sh --config conf start pdatanode -1

    //ZK_DATA + "node" + dnId
    String cmd = String.format
      ("bin/zk -p %s \n", dnId);
    String cmdout = u.runCommand(cmd);
  }


  // *******************************************
  // a stupid but working method to find out if
  // a datanode has been registered or not.
  // we can detect successful registration by checking that
  // "dnRegistration = " exists in the log file
  public void waitForDnRegistration(String dnId) {

    String registeredTag = "dnRegistration";

    while (true) {

      // this must be inside .. just incase the log file does not
      // exist yet, want to get this again
      String dnLogFile = getDnLogFileFromDnId(dnId);
      if (dnLogFile != null) {
        String cmd = String.format
          ("grep %s %s/%s", registeredTag, ZK_LOGS_DIR, dnLogFile);
        u.print(cmd + "\n");
        String cmdout = u.runCommand(cmd);
        if (cmdout.contains(registeredTag))
          break;
        u.print(cmdout);
        u.print("    Dn " + dnId + " is not registered, sleeping ...\n");
      }
      u.sleep(200);
    }
  }

  // *******************************************
  // rm all log files related to this datanode
  public void rmDataNodeLogFile(String dnId) {
    String dnLogFile = getDnLogFileFromDnId(dnId);
    if (dnLogFile != null)
      u.deleteFile(ZK_LOGS_DIR, dnLogFile);
    String dnOutFile = getDnOutFileFromDnId(dnId);
    if (dnOutFile != null)
      u.deleteFile(ZK_LOGS_DIR, dnOutFile);
  }


  // *******************************************
  // get the log file for this datanode
  public String getDnLogFileFromDnId(String dnId) {
    File dir = new File(ZK_LOGS_DIR);
    String[] c = dir.list();
    for (int i=0; i< c.length; i++) {
      if (c[i].contains(dnId + ".log")) {
        return c[i];
      }
    }
    return null;
  }

  // *******************************************
  // get the output file for this datanode
  public String getDnOutFileFromDnId(String dnId) {
    File dir = new File(ZK_LOGS_DIR);
    String[] c = dir.list();
    for (int i=0; i< c.length; i++) {
      if (c[i].contains(dnId + ".out"))
        return c[i];
    }
    return null;
  }

  // *******************************************
  // get a list of node processes from tmp-pids
  // see the NodeProcess class
  // *******************************************
  public NodeProcess[] getNodeProcesses() {

    u.MESSAGE("FIXME getNodeProcesses \n");

    /*
      LinkedList<NodeProcess> list = new LinkedList<NodeProcess>();

      File dir = new File(ZK_DATA);
      String[] c = dir.list();
      for (int i=0; i< c.length; i++) {

      if (!isTmpPid(c[i])) continue;

      File f = new File(dir, c[i]);
      String pid = u.getPidFromTmpPid(f);
      if (pid == null) continue;

      NodeProcess np = new NodeProcess(c[i], pid);

      list.add(np);
      }

      return (NodeProcess[]) list.toArray(new NodeProcess[list.size()]);
    */
    return null;
  }



  // *******************************************
  // is this a tmp-pid file?
  public boolean isTmpPid(String fname) {
    u.MESSAGE("FIXME isTmpPid \n");
    return false;
    //if (fname.contains(ZK_USERNAME) && fname.contains(".pid"))
    //return true;
    //return false;
  }



  // *******************************************
  // current failure hash file is the most recent
  // fail history (use "ls -t" to grep the file)
  public String getCurrentFailureHashFile() {

    String cmd = String.format("ls -t %s", FAIL_HISTORY_DIR);
    String cmdout = u.runCommand(cmd);

    String [] split = cmdout.split("\n", 2);
    String latest = split[0];
    String dotTxt = ".txt";

    // sanity check
    if (latest.indexOf("h") == 0 &&
        latest.indexOf(dotTxt) == latest.length() - dotTxt.length()) {
      return latest;
    }

    // it's possible that if we don't inject failure
    // there is no has failure, let's just put this to something
    u.WARNING("getLatestHashedFailure returns " +  latest);

    return null;

  }




  // *******************************************
  // IMPORTANT !!!!
  // the reason we want to remove all blocks is because
  // we don't want to have other background traffics
  // this is  a bad hack .. but let's do it for now
  public void rmAllBlocks() {

    u.print("- Removing all blocks ...\n");

    String [] dfsData = { "dataOne", "dataTwo", "dataThree" };

    // support upto 3 storage dirs
    for (int d = 0; d < dfsData.length; d++) {


      for (int i = 1; i < 10; i++) {

        String dnDir = ZK_STORAGE_DIR + "/dfs/" + dfsData[d] + i;

        String currentDir = dnDir + "/current";
        // u.print("    Removing " + currentDir + "\n");
        u.deleteDirContent(currentDir, "blk_");

        String tmpDir = dnDir + "/tmp";
        // u.print("    Removing " + tmpDir + "\n");
        u.deleteDirContent(tmpDir, "blk_");

        String detachDir = dnDir + "/detach";
        // u.print("    Removing " + detachDir + "\n");
        u.deleteDirContent(detachDir, "blk_");


      }
    }
    u.print("\n\n");
  }

  // *******************************************
  Utility getUtility() {
    return u;
  }

  // *******************************************
  Zook getZook() {
    return zook;
  }


  // *******************************************
  // print done ...
  public void printAllExperimentsFinish() {

    String full =
      ("## ################################################# ##\n");
    String side =
      ("##                                                   ##\n");
    String middle = String.format
      ("## A L L    E X P E R I M E N T S    F I N I S H E D !!! ##\n");

    u.print("\n\n");
    u.print(full);
    u.print(full);
    u.print(side);
    u.print(middle);
    u.print(side);
    u.print(full);
    u.print(full);
    u.print("\n\n");
  }

  public static void waiting (int n){

    long t0, t1;

    t0 =  System.currentTimeMillis();

    do{
      t1 = System.currentTimeMillis();
    }
    while ((t1 - t0) < (n * 1000));
  }

  // *******************************************
  public void createAllDirectories() {
    u.mkDir(TMPFI);
    u.mkDir(TMPFI + "eStates");
    u.mkDir(TMPFI + "eWoken");
    u.mkDir(TMPFI + "eWokenWONode");
    u.mkDir(TMPFI + "eWokenDet");
    u.mkDir(EXP_RESULT_DIR);
    u.mkDir(FAIL_HISTORY_DIR);
    u.mkDir(COVERAGE_COMPLETE_DIR);
    u.mkDir(COVERAGE_STATIC_DIR);
    u.mkDir(FLAGS_FAILURE_DIR);;
    u.mkDir(ZK_LOGS_DIR);
    u.mkDir(RPC_FILES_DIR);
    u.mkDir(SOCKET_HISTORY_DIR);
    u.mkDir(EVT_ORDER_HISTORY_DIR);
  }

  // *******************************************
  public void formatZooKeeper() {
    
    u.print("- Formatting zookeeper \n");
    
    String zkdir = ZK_STORAGE_DIR;
    u.mkDir(zkdir);

    for (int i = 1; i <= NUM_OF_ZK_NODES; i++) {
      
      String server = ZK_STORAGE_DIR + "/server" + i;
      String myid = server + "/myid";
      String id = String.format("%d", i);
      
      u.mkDir(server);
      u.stringToFileContent(id, myid, true);

    }

  }


  // *******************************************
  public void printReminder() {


    u.print("## ############################################# ## \n");
    u.print("## ############################################# ## \n");
    u.print("##                                               ## \n");
    u.print("##    DON'T FORGET TO RUN: make kill             ## \n");
    u.print("##                                               ## \n");
    u.print("## ############################################# ## \n");
    u.print("## ############################################# ## \n");

    // don't forget to run make kill (just make sure no java process
    // before this)


  }
}
