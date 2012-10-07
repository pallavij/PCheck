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
import java.util.HashMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.LinkedList;

import org.apache.hadoop.util.FIUtil;


public class Driver {

  // ########################################################################
  // ########################################################################
  // ##                                                                    ##
  // ##                  E X P   P A R A M E T E S                         ##
  // ##                                                                    ##
  // ########################################################################
  // ########################################################################


  public String curHdfsDir = "/Users/pallavi/Research/faultInjection/hdfs-wd/";

  public static int BREAK_EXP_NUMBER = Parameters.BREAK_EXP_NUMBER;
  
  public static final int MAX_FSN = Parameters.MAX_FSN;

  public static boolean 
    
   enableFailure = Parameters.enableFailure,
    
   enableCoverage = Parameters.enableCoverage,
    
   debug = Parameters.debug,
    
   junk;
  
  // ########################################################################
  // ########################################################################
  
  static boolean enableOptimizer = false;
  static boolean enableFrog = false;

  public static int VIRTUAL_WAIT_TIME_THRESHOLD = 3000; // 5 seconds

  // some locations
  public static final String HADOOP_TMP      = "/tmp/";
  public static final String HADOOP_USERNAME = "hadoop-" + System.getenv("USER");
  public static final String TMPFI           = "/tmp/fi/";
  public static final String FAIL_PTS_INFO_DIR = TMPFI + "/failPts/";
  
  // dirs
  public static final String HADOOP_STORAGE_DIR = TMPFI + HADOOP_USERNAME + "/";
  public static final String PIDS_DIR           = TMPFI + "pids/";
  public static final String FAIL_HISTORY_DIR   = TMPFI + "failHistory/";
  public static final String RPC_FILES_DIR      = TMPFI + "rpcFiles/";
  public static final String FLAGS_FAILURE_DIR  = TMPFI + "flagsFailure/";
  public static final String EXP_RESULT_DIR     = TMPFI + "expResult/";
  public static final String SOCKET_HISTORY_DIR = TMPFI + "socketHistory/";
  public static final String HADOOP_LOGS_DIR    = TMPFI + "logs/";
  public static final String CONTROL_DIR        = TMPFI + "control/";

  public static final String COVERAGE_COMPLETE_DIR = TMPFI + "coverageComplete/";
  public static final String COVERAGE_STATIC_DIR   = TMPFI + "coverageStatic/";

  public static final String CUR_EXP_STATE_FILE = FLAGS_FAILURE_DIR + "/currentExpState";

  public static final String PER_EXP_INFO_DIR = TMPFI + "perExpInfo/";


  public static final String REP_MONITOR_FLAG = CONTROL_DIR + "runRepMonitorFlag";
    
  // files and flags
  public static final String FROG_OUTPUT_FILE     = TMPFI + "frogOutput.txt";
  public static final String RESET_FROG_FLAG      = TMPFI + "resetFrogFlag";
  public static final String ENABLE_FAILURE_FLAG  = TMPFI + "enableFailureFlag";
  public static final String ENABLE_FROG_FLAG     = TMPFI + "enableFrogFlag";
  public static final String CLIENT_OPTIMIZE_FLAG = TMPFI + "clientOptimizeFlag";
  public static final String ENABLE_COVERAGE_FLAG = TMPFI + "enableCoverageFlag";
  public static final String EXPERIMENT_RUN_FLAG  = TMPFI + "experimentRunning";
  public static final String POST_SETUP_FLAG      = TMPFI + "postsetup";
  public static final String CONTEXT_FILE         = TMPFI + "context";

  // tell NN to start checking for stable state
  // stable state = 0 block under replicated, 0 pending replicated
  // (may be more, say now LEASE HOLDER other than NN_Recovery
  public static final String CHECK_STABLE_FLAG   = CONTROL_DIR + "checkStable";

  // NameNode is at stable state
  public static final String IS_STABLE_FLAG      = CONTROL_DIR + "isStable";
  
  // vars
  private static Utility u;
  private static Hdfs hdfs;
  private static int expNum = 1;
  private static int wipedOutNum = 1; // # of wiped out experiments

  private static final int MAX_REGISTRATION_RETRY = 100000;


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
  public static final String WRITE_OVER = TMPFI + "writeOver";
  
  
  public static final String PID_FILE_FOR_CLIENT = PIDS_DIR + HADOOP_USERNAME + "-client.pid";



  // ########################################################################
  // ########################################################################
  // ##                                                                    ##
  // ##                            S E T U P S                             ##
  // ##                                                                    ##
  // ########################################################################
  // ########################################################################



  // *******************************************
  // general setup
  // *******************************************
  public Driver() {

    // setup where you want your output to be sent
    // all messages you see on the screen will be
    // put in this file too
    u = new Utility();
    u.setupPrintStream("/tmp/workloadOut.txt");
    org.apache.hadoop.util.FIUtil.setPrintStream(u.getPrintStream());
    
    // don't forget to run make kill
    printReminder();

    // connect to hdfs ... workload can use this
    hdfs = new Hdfs(this);

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
    setupBeforeEverything();

    writePidForClient();

    // begin recursive fsn with 1
    //recursiveFsn(1);
    testEvtOrders();

    
    // final message
    setupAfterEverything();

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
  // prepare general stuffs .. this takes a while
  // *******************************************
  public void setupBeforeEverything() {

    // 0 ... disable failure manager
    disableCoverage();
    disableFailureManager();
    disableClientOptimizer();
    disableFrog();


    // 1. make sure current working directory is correct
    printCwd();
    
    // ------------------------------ clean up stuffs

    // 2. kill any HDFS processes (this not always work)
    // so that's why we call "make kill" before running this app
    //killHDFS();

    rmPerExpInfoDir();

    rmImages();    // 3. remove all HDFS metadata images
    rmTmps();     // 4. remove all HDFS temporary files
    rmExpResult();    // .. remove previous experiment result
    rmSocketHistory();    // .. remove previous experiment result
    rmRpcFiles();    // rm all rpc files
    rmCoverageFiles(); // rm all coverage files
    rmFmStat();
    rmContextFile(); 
    rmControlDir();


    rmEvtOrders();

    // 5. clear any failure flags
    clearAllFlagsFailure();
    
    // 6. clear all fail history (the directory that contains
    //    failure hash files
    clearAllFailHistory();

    rmLogs();
    rmPids();

    createAllDirectories();
    u.mkDir(CONTROL_DIR);
    u.mkDir(PER_EXP_INFO_DIR);
    u.createNewFile(START_FLAG);

    startFailureManager();
    
    // 7. format the hdfs (via shell)
    formatHDFS();

    startHDFS();

    hdfs.mkdirViaShell();
  }


  // *******************************************
  public void setupAfterEverything() {

    printAllExperimentsFinish();
    
    hdfs.lsViaShell();

    u.deleteFile(Driver.START_FLAG);
    
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
    
    // setup after
    setupAfterEachWorkload(exp);

  }

  // *******************************************
  public void setupBeforeEachWorkload(Experiment exp) {

    u.print("- Prepare before each workload ... \n");    
  
    //u.stringToFileContent("********************Experiment Starting***********************\n",new File("/tmp/debug.txt"), false, true);

    recordCurrentExpNumber(exp.getExpNum());    

    
    // must be after restarting dead datanodes !!
    rmAllBlocks();
    
    // clear all control flags
    clearAllControlFlags();

    // rmSocketHistory();

    rmPerExpInfoDir();

    u.deleteFile(WIPE_OUT_FLAG);

    u.createNewFile(EXP_START_FLAG);
    u.createNewFile(EVTS_WOKEN);
    u.createNewFile(EVTS_WOKEN_DET);
    u.createNewFile(EVTS_WOKEN_WO_NODE);
    u.createNewFile(EXEC_STATE);
    u.createNewFile(QUEUE_STATE);

    u.mkDir(EXP_RESULT_DIR + exp.getExpNumShortDirName() + "/");

  }


  // *******************************************
  public void setupAfterEachWorkload(Experiment exp) {

    u.deleteFile(EXP_START_FLAG);

    if(!((new File(WIPE_OUT_FLAG)).exists())){
      exp.writeEvtIdsInExp();
    }

    String expDirName = exp.getExpNumShortDirName();
    u.runCommand("cp " + EVTS_WOKEN + " " + TMPFI + "eWoken/" + expDirName);
    u.runCommand("cp " + EVTS_WOKEN_WO_NODE + " " + TMPFI + "eWokenWONode/" + expDirName);
    u.runCommand("cp " + EVTS_WOKEN_DET + " " + TMPFI + "eWokenDet/" + expDirName);
    u.deleteFile(EVTS_WOKEN);
    u.deleteFile(EVTS_WOKEN_DET);
    u.deleteFile(EVTS_WOKEN_WO_NODE);
    u.runCommand("cp " + EXEC_STATE + " " + TMPFI + "eStates/" + expDirName);
    u.deleteFile(EXEC_STATE);
    u.deleteFile(QUEUE_STATE);

    //u.runCommand(curHdfsDir + "scripts/moveCovFiles.py");


    recordExpState(null);

    // done
    exp.printEnd();

    exp.checkFailExperiment();
    
    exp.printFailHistorySummary();    
    if (debug) {
      exp.printFailHistory();
    }

    // increment the experiment number
    incrementExpNum();          

    // and remove all ports otherwise the directory gets too big !!!
    // and it could contain thousands of files
    //rmSocketHistory();

  }



  // *******************************************
  // check the algorithm below
  // *******************************************
  public void runWorkload(Experiment exp) {

    // the experiment runs here ...
    // here, we can write whatever experiments we want
    // so now, I've created the workload client write class
    // which will run the client write workloads
    
    //ClientWriteWorkload cww = new ClientWriteWorkload(this, exp);
    //cww.run();
    
    HaryadiAppendWorkload haw = new HaryadiAppendWorkload(this, exp);
    haw.run();
    
    //NameNodeRebootWorkload nnrw = new NameNodeRebootWorkload(this, exp);
    //nnrw.run();
   
    //FsTimeWorkload fsw = new FsTimeWorkload(this, exp);
    //fsw.run();

    //ClientReadWorkload crw = new ClientReadWorkload(this, exp);
    //crw.run();

  }



  // *******************************************
  // clear all blockReport* 
  public void clearAllControlFlags() {
    u.print("- Clearing all control flags \n");
    u.deleteDirContent(CONTROL_DIR);
  }


  // *******************************************
  private void recordCurrentExpNumber(int expNum) {
    String path = FLAGS_FAILURE_DIR + "/currentExpNumber";
    String tmp = String.format("%d", expNum);
    u.stringToFileContent(tmp, path);
  }

  // *******************************************
  // if null, means delete the file
  // *******************************************
  String latestState = "none";
  public void recordExpState(String curState) {
    if (curState == null) {
      u.println("\n- Leaving latest state: " + latestState + "\n");
      u.deleteFile(CUR_EXP_STATE_FILE);
    }
    else  {
      u.println("\n- Entering exp state: " + curState + "\n");
      u.stringToFileContent(curState, CUR_EXP_STATE_FILE);
      latestState = curState;
    }
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



  // ********************************************************
  // jjust print our current working directory
  // should be in the hadoop root dir
  // *******************************************
  public void printCwd() {
    String curDir = System.getProperty("user.dir");
    u.print(String.format("- Current directory is [%s] \n", curDir));
  }

  // *******************************************
  // remove all files recursively starting from HADOOP_STORAGE_DIR
  // *******************************************
  public void rmImages() {
    u.print("- Removing images ...\n");
    if (!u.deleteDir(HADOOP_STORAGE_DIR)) {
      u.ERROR("Can't delete " + HADOOP_STORAGE_DIR);
    }
  }

  public void rmControlDir() {
    u.print("- Removing control dir ...\n");
    if (!u.deleteDir(CONTROL_DIR)) {
      u.ERROR("Can't delete " + CONTROL_DIR);
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
  public void rmPerExpInfoDir() {
    u.print("- Removing per experiment info dir ...\n");
    u.deleteDirFiles(PER_EXP_INFO_DIR);
  }


  // *******************************************
  // remove all tmp files, i.e. all files in
  // /tmp that contains HADOOP_USERNAME string
  // *******************************************
  public void rmTmps() {
    u.print("- Removing tmps ...\n");
    if (!u.deleteDirContent(HADOOP_TMP, HADOOP_USERNAME)) {
      u.ERROR("Can't delete " + HADOOP_TMP);
    }
  }

  // *******************************************
  // clear failure flags flags
  // *******************************************
  public void clearAllFlagsFailure() {
    u.print("- Clearing all failure flags ...\n");
    if (!u.deleteDirContent(FLAGS_FAILURE_DIR)) {
      u.ERROR("Can't delete " + HADOOP_TMP);
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
  // let's format the HDFS via shell
  // *******************************************
  public void formatHDFS() {
    u.print("- Formating HDFS ...\n");
    String cmdout = u.runCommand("bin/hadoop namenode -format");
    u.print(cmdout);
    u.print("\n\n");
  }


  // *******************************************
  // rm all logs file ..
  // *******************************************
  public void rmLogs() {
    u.print("- Removing logs ...\n");
    if (!u.deleteDirContent(HADOOP_LOGS_DIR)) {
      u.ERROR("Can't delete " + HADOOP_LOGS_DIR);
    }
  }

  // *******************************************
  // rm all logs file ..
  // *******************************************
  public void rmPids() {
    u.print("- Removing pids ...\n");
    if (!u.deleteDirContent(PIDS_DIR)) {
      u.ERROR("Can't delete " + PIDS_DIR);
    }
  }

  // *******************************************
  // start failure manager
  // *******************************************
  public void startFailureManager() {

    u.print("- Starting Failure Manager ...\n");
    String cmdout = u.runCommand("bin/start-fi.sh");
    u.print(cmdout);
    u.print("\n\n");
  }


  // *******************************************
  // enable failure manager via the fmadmin command
  // see my bin/hadoop to find what this is
  public static void enableFailureManager() {
    
    if (!Driver.enableFailure) 
      return;
    
    u.print("- Enabling Failure Manager ...\n");
    // String cmdout = u.runCommand("bin/hadoop fmadmin -enable");
    // u.print(cmdout);
    // u.print("\n\n");
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
  public void rmContextFile() {
    u.deleteFile(CONTEXT_FILE);
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
  // just call start-dfs (NOTE that start-dfs does not
  // call the dfs)
  public void startHDFS() {

    u.print("- Starting HDFS ...\n");
    String cmdout = u.runCommand("bin/start-dfs.sh");
    u.print(cmdout);
    u.print("\n\n");

    String slaveFile = u.fileContentToString("conf/slaves");
    String []slaves = slaveFile.split("\n");
    int numDataNodes = 0;
    for (String s : slaves) {
      if (s.contains("localhost")) numDataNodes++;
    }
    u.println("- Waiting for " + numDataNodes + " to register ... ");
    for (int i = 1; i <= numDataNodes; i++) {
      waitForDnRegistration(String.format("%d",i));
    }
    u.println("- HDFS is started \n");

  }


  // *******************************************
  // killall hdfs processes (not always work)
  public void killHDFS() {
    u.print("- Killing HDFS ...\n");
    NodeProcess[] nps = getNodeProcesses();
    if (nps == null) return;
    for (int i = 0; i < nps.length; i++) {
      if (nps[i].isClient() || nps[i].isFI()) {
	      continue;
      }
      String cmd = String.format("kill -s KILL %5s", nps[i].getPid());
      u.print(String.format("   %s, %s \n", cmd, nps[i].getName()));
      String cmdout = u.runCommand(cmd);
    }
    u.print("\n\n");
  }

  // *******************************************
  // kill the name node (not always work)
  public void killNameNode() {
    u.print("- Killing the NameNode ...\n");
    NodeProcess[] nps = getNodeProcesses();
    if (nps == null) return;
    for (int i = 0; i < nps.length; i++) {
      
      if (nps[i].isNameNode()) {
	String cmd = String.format("kill -s KILL %5s", nps[i].getPid());
	u.print(String.format("   %s, %s \n", cmd, nps[i].getName()));
	String cmdout = u.runCommand(cmd);
      }
    }
    u.print("\n\n");
  }

  // *******************************************                                                                         
  public void killDataNodes() {
    u.print("- Killing all the DataNodes ...\n");
    NodeProcess[] nps = getNodeProcesses();
    if (nps == null) return;
    for (int i = 0; i < nps.length; i++) {
      if (nps[i].isDataNode()) {
	String cmd = String.format("kill -s KILL %5s", nps[i].getPid());
        u.print(String.format("   %s, %s \n", cmd, nps[i].getName()));
	String cmdout = u.runCommand(cmd);
      }
    }
    u.print("\n\n");
  }


  // *******************************************
  // a bit stupid method to find out if a datanode is dead or not
  // just do ps -p pid .. then search if there is the word java in it or not
  public void checkDeadNodes() {
    u.print("- Checking dead nodes ...\n");
    NodeProcess[] nps = getNodeProcesses();
    if (nps == null) return;
    for (int i = 0; i < nps.length; i++) {
      boolean isAlive = u.isPidAlive(nps[i].getPid());
      u.print(String.format("   %-5s  %-15s ", nps[i].getPid(), nps[i].getName()));
      if (isAlive) { u.print("ok   \n"); }
      else         { u.print("DEAD \n"); }
    }
    u.print("\n\n");
  }
  
  // *******************************************
  // run rep monitor flag
  // *******************************************
  public void runReplicationMonitor() {
    u.print("- Run replication monitor ... \n");
    u.createNewFile(REP_MONITOR_FLAG);
    while (u.exists(REP_MONITOR_FLAG)) {
      u.print("   Rep monitor hasn't run yet .. ");
      u.sleep(500);
    }
    u.print("\n\n");
  }


  // ************************************************
  public void restartDeadNameNode() {

    u.print("- Restarting dead namenode ...\n");
    NodeProcess[] nps = getNodeProcesses();
    if (nps == null) return;
    for (int i = 0; i < nps.length; i++) {

      if (nps[i].isNameNode()) {

        boolean isAlive = u.isPidAlive(nps[i].getPid());


	if (isAlive) {
	  continue;
	}
	

        String s = String.format("    Restarting NameNode %-15s \n", nps[i].getName());
        u.print(s);

        // before restarting, make sure we remove
        // all stuffs that relate to this dead node
        // such as the pid file, and log files

        // first I need to remove the tmp pid file
        u.deleteFile(PIDS_DIR, nps[i].getTmpPidFile());
	
        // then I need to remevove the logs
        rmNameNodeLogFile();
	
        // let's resetart the datanode
        restartNameNode();

	u.print("   NameNode Restarted !!!!!!!!!!!!!! \n");

      }
      u.print("\n\n");
    }

  }

  // *******************************************
  // pallavi-07, upgrade
  // *******************************************
  private String nameNodeCmdArgs = "";
  public void restartDeadNameNodeWithUpgrade() {
    nameNodeCmdArgs = "-upgrade";
    restartDeadNameNode();
    nameNodeCmdArgs = "";
  }

  // *******************************************
  public void restartDeadNameNodeWithRollBack() {
    nameNodeCmdArgs = "-rollback";
    restartDeadNameNode();
    nameNodeCmdArgs = "";
  }

  // *******************************************
  public void restartDeadNameNodeWithFinalize() {
    nameNodeCmdArgs = "-finalize";
    restartDeadNameNode();
    nameNodeCmdArgs = "";
  }

  // *******************************************
  private void restartNameNode() {

    u.print("- Restarting NameNode \n");

    // or, bin/hd.sh --config conf start pdatanode -1
    // pallavi-07, upgrade, add nameNodeCmdArgs
    String cmd = String.format
      ("bin/hadoop-daemon.sh --config conf start namenode " + nameNodeCmdArgs);
    String cmdout = u.runCommand(cmd);

  
    // waitForNnLeaveSafeMode(exp);

  }

  // *******************************************
  public boolean isDataNodeAlive(String dnId) {
    u.print("- Checking if datanode " + dnId + " is alive ...\n");
    NodeProcess[] nps = getNodeProcesses();
    if (nps == null) return false;
    for (int i = 0; i < nps.length; i++) {
      if (nps[i].getName().equals("pdatanode" + dnId)) {
        boolean isAlive = u.isPidAlive(nps[i].getPid());
        return isAlive;
      }
    }
    return false; // no datanode is found then it's dead
  }


  // *******************************************
  // pallavi-08: isNameNodeAlive
  public boolean isNameNodeAlive() {
    u.print("- Checking if namenode is alive ...\n");
    NodeProcess[] nps = getNodeProcesses();
    if (nps == null) return false;
    for (int i = 0; i < nps.length; i++) {
      if (nps[i].isNameNode()) {
        boolean isAlive = u.isPidAlive(nps[i].getPid());
        return isAlive;
      }
    }
    return false;
  }
  
  // *******************************************
  // create flag that force datanode to send blockReport
  // once datanode send blockReport, it should delete the flag
  public static void sendBlockReport() {
    u.print("- Checking dead nodes to create blockReportFlag...\n");
    NodeProcess[] nps = getNodeProcesses();
    if (nps == null) return;
    for (int i = 0; i < nps.length; i++) {
      boolean isAlive = u.isPidAlive(nps[i].getPid());
      if (isAlive && nps[i].isDataNode()) { 
	String flag = "blockReport_" + nps[i].getName();
	u.print("   create blockReportFlag for " + nps[i].getName() + "\n");
	u.createNewFile(CONTROL_DIR + flag);      
      }
    }
  }

  // *******************************************
  // wait for all alive dns send blockReport
  // and create a flags for NameNode to check for stable state
  // check for LocatedBlock
  public static void waitForStableState(Experiment exp) {
    u.print("- Wait for stable state ... \n");

    // how long i should wait here?
    u.sleep(1000);
    // then, i need to create a flag to tell NN to check for stable state
    createCheckStableFlag();
   
    // finally, wait for stable state
    // TODO: what if NN never create IS_STABLE flag?
    // this will spin forever, hence, we need to
    // to timeout here
    long maxWaitTime = 4000;
    long realWaitTime = 0;
    u.print("- check if namenode is stable ... \n");
    while ( !u.exists(IS_STABLE_FLAG)) {
      u.print(" namenode is not stable yet\n");
      u.sleep(200);
      realWaitTime += 200;
      if (realWaitTime > maxWaitTime) {
	exp.markFailFromStableCheck();
	break;
      }
    }
    
  }

 // **************************************************
 //wait until there is no garbage in the file system
  
 public static void waitForStableState(){
    while ( u.exists(CONTROL_DIR, "blockReport")) {
      u.print("  all dns have not gotten their blockReports processed yet\n");
      u.sleep(200);
    }
    u.print("\n\n");
    //while(!isStableStateReached()){
    //	try{Thread.sleep(2000);}catch(Exception e){}	
    //}	

 }

 public static boolean isStableStateReached(){
	Map<String,String> blkIdToGs = new HashMap<String,String>();
 	String storageDirName = HADOOP_STORAGE_DIR + "dfs";
	File storageDir = new File(storageDirName);
	File[] dataDirs = storageDir.listFiles();

	for(File dataDir: dataDirs){
		if(dataDir.getName().contains("dataOne")){
			File[] dataOneDirs = dataDir.listFiles();
			for(File dataOneDir: dataOneDirs){
				if(dataOneDir.getName().equals("tmp")){
					File[] tmpFiles = dataOneDir.listFiles();
					for(File tmpFile : tmpFiles){
						String tmpFName = tmpFile.getName();
						if(tmpFName.startsWith("blk_") && tmpFName.contains(".meta")){
							return false;
						}
					}
				}
				if(dataOneDir.getName().equals("current")){
					File[] curFiles = dataOneDir.listFiles();
					for(File curFile : curFiles){
						String curFName = curFile.getName();
						if(curFName.startsWith("blk_") && curFName.contains(".meta")){
							String[] curFNameParts = curFName.split(".");
							String curFNameWOMeta = curFNameParts[0];

							String blkIdGS = curFNameWOMeta.substring(4);
							String[] blkIdGSParts = blkIdGS.split("_");
							String blkId = blkIdGSParts[0];
							String GS = blkIdGSParts[1];
							String GSInMap = blkIdToGs.get(blkId);
							
							if(GSInMap == null){
								blkIdToGs.put(blkId, GS);
							}
							else{
								if(!GSInMap.equals(GS)){
									return false;
								}
							}
						}	
					}
				}
			}
		}
	}	
	return true;
 } 



  public static void createCheckStableFlag() {
    u.print("- Create checkStableFlag ... \n");
    //    u.createNewFile(CONTROL_DIR + "checkStable");
    u.createNewFile(CHECK_STABLE_FLAG);
  }
  
  
  // *******************************************
  // restart dead datanodes ...
  // go through each pid in /tmp/hadoop..pid
  // and find which pid is dead
  public void restartDeadDataNodes() {
   // *********************************
   // haryadi, pallavi-10, if datanode cannot restart properly
   boolean foundDeadDataNode = true;
   while (foundDeadDataNode) {
     foundDeadDataNode = false;
   // *********************************


    u.print("- Restarting dead datanodes ...\n");
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
      u.deleteFile(PIDS_DIR, nps[i].getTmpPidFile());

      // then I need to remevove the logs
      rmDataNodeLogFile(nps[i].getDnId());

      // let's resetart the datanode
      restartDataNode(nps[i].getDnId());

      // *********************************
      // haryadi, pallavi-10, if datanode cannot restart properly
      // it's possible that restart datanode, restart the whole
      // cluster ...
      // so let's just break here ... and let's make sure
      // we call getNodeProcesses again !
      // *********************************
      foundDeadDataNode = true;
      break;
      // *********************************

      //PALLAVI: seems like the following code should not be there 
      //after applying the above pallavi-10 fix
      /******
      // okay so we must wait until that datanode is registered
      u.print("    Waiting for registration ...\n");
      waitForDnRegistration(nps[i].getDnId());
      *******/
     }

    }
    u.print("\n\n");
  }

  // *******************************************
  // restart the datanode with dnId
  // in my case I could do this by callng
  // e.g. "pdatanode -3" .. in thanh's case you
  // must set the conf folder properly
  // ********************************************
  
  public void restartDataNode(String dnId) {

    u.print("- Restarting DataNode-" + dnId + "\n");

    // or, bin/hd.sh --config conf start pdatanode -1
    String pureDnId = dnId.replaceAll("-", "");
    String cmd = String.format
      ("bin/hadoop-daemon.sh --config conf/dnconf%s start datanode \n", pureDnId);
    String cmdout = u.runCommand(cmd);

    // okay so we must wait until that datanode is registered
    u.print("\n\n    Waiting for registration ...\n\n");
    waitForDnRegistration(dnId);

  }


  // *******************************************
  // a stupid but working method to find out if
  // a datanode has been registered or not.
  // we can detect successful registration by checking that
  // "dnRegistration = " exists in the log file
  public void waitForDnRegistration(String dnId) {
   
    String registeredTag = "dnRegistration";
    int i = 0;

    while (true) {

      // this must be inside .. just incase the log file does not
      // exist yet, want to get this again
      String dnLogFile = getDnLogFileFromDnId(dnId);

      // sanity check
      if (dnLogFile == null || !(new File(HADOOP_LOGS_DIR + dnLogFile)).exists()) {
        u.println("- There is no dnLogFile for DataNode" + dnId + ", sleeping");
        u.sleep(500); // sleep after ..
        continue;
      }

      // file exist let's check
      String cmd = String.format
        ("grep %s %s/%s", registeredTag, HADOOP_LOGS_DIR, dnLogFile);
      // u.print(cmd + "\n"); // if you want to debug only
      String cmdout = u.runCommand(cmd);
      if (cmdout.contains(registeredTag)) {
        u.print("- Dn " + dnId + " is registered, Yah!! ...\n\n");
        break;
      }

      // u.print(cmdout); // for debugging
      i++;
      u.print("- Dn " + dnId + " is not registered, sleeping ..., " +
	      "try=" + i + " out of max " + MAX_REGISTRATION_RETRY + "\n");
      
      u.sleep(1000); // sleep after ..
      

      // ******************************************************
      // haryadi, pallavi-10, if datanode cannot restart properly
      // we cannot restart properly after 30 seconds .. let's redo again ..
      // but kill this node
      // ******************************************************
      if (i > MAX_REGISTRATION_RETRY) {
        u.println("- DANGER: cannot restart " + dnId + " properly, let's restart the whole cluster .. ");
        cleanAndRestartHdfs();
        break;
      }
      // ******************************************************



    }

    /*******
    String registeredTag = "dnRegistration";

    while (true) {

      // this must be inside .. just incase the log file does not
      // exist yet, want to get this again
      String dnLogFile = getDnLogFileFromDnId(dnId);
      if (dnLogFile != null) {
        String cmd = String.format
          ("grep %s %s/%s", registeredTag, HADOOP_LOGS_DIR, dnLogFile);
        u.print(cmd + "\n");
        String cmdout = u.runCommand(cmd);
        if (cmdout.contains(registeredTag))
          break;
        u.print(cmdout);
        u.print("    Dn " + dnId + " is not registered, sleeping ...\n");
      }
      u.sleep(200);
    }
    *******/
  }




  // *******************************************
  // for this workload, we need to kill the namenode
  // and then format the namenode
  // *******************************************
  public void cleanAndRestartHdfs() {

    u.println("- Cleaning up and restarting hdfs");
    killDataNodes();
    killNameNode();
    waitForNnToDie(); // pallavi-08, pallavi-10
    u.sleep(2000);
    rmImages();
    rmLogs();
    rmPids();
    formatHDFS();
    rmLogs();
    startHDFS();
  }

  // *******************************************
  // pallavi-08, wait until namenode is dead
  // *******************************************
  public void waitForNnToDie() {
    u.println("- Waiting for NN to die ... ");
    while (isNameNodeAlive()) {
      u.println("- NN is still alive");
      u.sleep(1000);
    }
    u.println("- NN is dead");
  }

  // *******************************************
  // pallavi-08, wait for nn finalize upgrade
  // *******************************************
  public void waitForNnFinalizeUpgrade(Experiment exp, String failCode) {
    u.println("- waiting for NN to finalize .... ");
    for (int i = 0; i < 20; i++) {
      if (isNameNodeFinalizeUpgrade()) { return; }
      u.print("    Nn has not finalized upgrade ...\n");
      u.sleep(500);
    }
    exp.markFail(failCode, null, "NameNode does not finalize after 10 seconds");
  }

  // *******************************************
  // pallavi-08: finalize
  // the string:  INFO .. Finalize upgrade for ... is complete
  // *******************************************
  public boolean isNameNodeFinalizeUpgrade() {
    String nnLogFile = getSpecificLogFile("-namenode-", ".log");
    if (nnLogFile != null) {
      String cmd = String.format
        ("grep %s %s/%s", "complete", HADOOP_LOGS_DIR, nnLogFile);
      u.print(cmd + "\n");
      String cmdout = u.runCommand(cmd);
      if (cmdout.contains("complete")) {
        u.println("- is Nn finalize upgrade? yes !!");
        return true;
      }
    }
    u.println("- is Nn finalize upgrade? no !!");
    return false;
  }


  // *******************************************
  // a stupid but working method to find out if
  // a datanode has been registered or not.
  // we can detect successful registration by checking that
  // "dnRegistration = " exists in the log file
  // pallavi-04: I refactor this function
  // pallavi-07: add failCode, andd shut down
  public void waitForNnLeaveSafeMode(Experiment exp, String failCode) {
    u.println("- waiting for NN to be ready (leave safe mode) .... ");
    for (int i = 0; i < 20; i++) {

      if (isNameNodeLeaveSafeMode()) { return; }
      
      //if (isNameNodeShutDownItself()) {
      //exp.markFail(failCode, null, "NameNode shuts down itself");
      //return;
      //}
      
      u.print("    Nn is not leaving safe mode, sleeping ...\n");
      u.sleep(500);
    }
    exp.markFail(failCode, null, "NameNode cannot start after 10 seconds");
  }

  // *******************************************
  // pallavi-04: RebootNameNode workload
  public boolean isNameNodeLeaveSafeMode() {
    String nnLogFile = getSpecificLogFile("-namenode-", ".log");
    if (nnLogFile != null) {
      String cmd = String.format
        ("grep %s %s/%s", "Leaving", HADOOP_LOGS_DIR, nnLogFile);
      u.print(cmd + "\n");
      String cmdout = u.runCommand(cmd);
      if (cmdout.contains("Leaving")) {
        u.println("- is Nn leave safe mode? yes !!");
        return true;
      }
    }
    u.println("- is Nn leave safe mode? no !!");
    return false;
  }

  // *******************************************
  // pallavi-07: RebootNameNode workload
  public boolean isNameNodeShutDownItself() {
    String nnLogFile = getSpecificLogFile("-namenode-", ".log");
    if (nnLogFile != null) {
      String cmd = String.format
        ("grep %s %s/%s", "SHUTDOWN_MSG", HADOOP_LOGS_DIR, nnLogFile);
      u.print(cmd + "\n");
      String cmdout = u.runCommand(cmd);
      if (cmdout.contains("SHUTDOWN_MSG")) {
        u.println("- is Nn shut down itself? yes !!");
        return true;
      }
    }
    u.println("- is Nn shut down itself? no !!");
    return false;
  }



  // *******************************************
  // pallavi-04: RebootNameNode workload
  // pallavi-07: add failCode
  public void waitForEitherNnReadyOrDead(Experiment exp, String failCode) {
    u.println("- wait for either NN ready (leave safe mode) or dead  ");
    for (int i = 0; i < 20; i++) {
      if (isNameNodeLeaveSafeMode() || 
	  isNameNodeDead()) {
	  //isNameNodeShutDownItself()) {
        return;
      }
      
      u.sleep(500);
    }
    exp.markFail(failCode, null, "NameNode not safe/not dead");
  }

  // *******************************************
  // pallavi-04: RebootNameNode workload
  public boolean isNameNodeDead() {
    NodeProcess[] nps = getNodeProcesses();
    if (nps == null) return false;  // can't tell let's just return false
    for (int i = 0; i < nps.length; i++) {
      if (nps[i].isNameNode()) {
        boolean isAlive = u.isPidAlive(nps[i].getPid());
        if (!isAlive) {
          u.println("   - is namenode dead? yes !! ...");
          return true;
        }
      }
    }
    u.print("- is namenode dead? no ...\n");
    return false;
  }






  // *******************************************
  // rm all log files related to this datanode
  public static void rmDataNodeLogFile(String dnId) {
    String dnLogFile = getDnLogFileFromDnId(dnId);
    if (dnLogFile != null)
      u.deleteFile(HADOOP_LOGS_DIR, dnLogFile);
    String dnOutFile = getDnOutFileFromDnId(dnId);
    if (dnOutFile != null)
      u.deleteFile(HADOOP_LOGS_DIR, dnOutFile);
  }


  // *******************************************
  // get the log file for this datanode
  public static String getDnLogFileFromDnId(String dnId) {
    File dir = new File(HADOOP_LOGS_DIR);
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
  public static String getDnOutFileFromDnId(String dnId) {
    File dir = new File(HADOOP_LOGS_DIR);
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
  public static NodeProcess[] getNodeProcesses() {

    LinkedList<NodeProcess> list = new LinkedList<NodeProcess>();

    File dir = new File(PIDS_DIR);
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
  }



  // *******************************************
  // is this a tmp-pid file?
  public static boolean isTmpPid(String fname) {
    if (fname.contains(HADOOP_USERNAME) && fname.contains(".pid"))
      return true;
    return false;
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
  public static void rmAllBlocks() {

    u.print("- Removing all blocks ...\n");

    String [] dfsData = { "dataOne", "dataTwo", "dataThree" };

    // support upto 3 storage dirs
    for (int d = 0; d < dfsData.length; d++) {


      for (int i = 1; i < 10; i++) {

        String dnDir = HADOOP_STORAGE_DIR + "/dfs/" + dfsData[d] + i;

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
  // a stupid but working method to find out if
  // a datanode has been registered or not.
  // we can detect successful registration by checking that
  // "dnRegistration = " exists in the log file
  public void waitForNnLeaveSafeMode(Experiment exp) {

    // while (true) {
    for (int i = 0; i < 50; i++) {

      // this must be inside .. just incase the log file does not
      // exist yet, want to get this again
      String nnLogFile = getSpecificLogFile("-namenode-", ".log");
      if (nnLogFile != null) {
        String cmd = String.format
          ("grep %s %s/%s", "Leaving", HADOOP_LOGS_DIR, nnLogFile);
        u.print(cmd + "\n");
        String cmdout = u.runCommand(cmd);
        if (cmdout.contains("Leaving")) {
	  u.print("    Nn is Leaving safe mode, sleeping ...\n");
          return;
	}
        u.print(cmdout);
        u.print("    Nn is not leaving safe mode, sleeping ...\n");
      }
      u.sleep(200);
    }
    
    exp.markFailFromNonFrog();
    exp.addNonFrogReport("NameNode cannot start after 10 seconds");
    
  }
  
  // *******************************************
  // rm all log files related to this datanode
  public void rmNameNodeLogFile() {

    String fname;

    fname = getSpecificLogFile("secondary", ".log");
    if (fname != null)  u.deleteFile(HADOOP_LOGS_DIR, fname);

    fname = getSpecificLogFile("secondary", ".out");
    if (fname != null)  u.deleteFile(HADOOP_LOGS_DIR, fname);

    fname = getSpecificLogFile("namenode", ".log");
    if (fname != null)  u.deleteFile(HADOOP_LOGS_DIR, fname);

    fname = getSpecificLogFile("namenode", ".out");
    if (fname != null)  u.deleteFile(HADOOP_LOGS_DIR, fname);

  }
  // *******************************************
  // crate perExpInfo/logs-1 or logs-2 .. etc.  
  // so that later it will be moved to the per experiment directory
  // allow at most 9 logs dir
  // pallavi-07
  // *******************************************
  public void copyOutputLogsToPerExpInfoDir(String tagFileName) {

    if (!WDebug.copyLogs) return;

    u.println("- Copying logs to per exp info dir ...");

    final int MAX_LOGS_DIR = 9;
    for (int i = 1;  i <= MAX_LOGS_DIR; i++) {
      File destDir = new File(String.format("%s/logs-%d", PER_EXP_INFO_DIR, i));
      if (destDir.exists()) continue; // continue if exists
      else                  u.mkDir(destDir);

      File sourceDir = new File(HADOOP_LOGS_DIR);
      String[] logFiles = sourceDir.list();
      if (logFiles == null) break;

      for (int j=0; j < logFiles.length; j++) {
        File sourceFile = new File(sourceDir, logFiles[j]);
        File destFile = new File(destDir, logFiles[j]);
        u.copyFile(sourceFile, destFile);
      }

      File tagFile =  new File(destDir, tagFileName);
      u.createNewFile(tagFile);

      break;
    }
  }

  // *******************************************
  // crate perExpInfo/dfs-1 or dfs-2 .. etc.
  // so that later it will be moved to the per experiment directory
  // allow at most 9 dirs
  // pallavi-07
  // *******************************************
  public void copyDfsFilesToPerExpInfoDir(String tagFileName) {

    if (!WDebug.copyDfsFiles) return;

    u.println("- Copying dfs files to per exp info dir ...");

    final int MAX_DFS_DIR = 9;
    for (int i = 1;  i <= MAX_DFS_DIR; i++) {
      File destDir = new File(String.format("%s/dfs-%d", PER_EXP_INFO_DIR, i));
      if (destDir.exists()) continue; // continue if exists
      else                  u.mkDir(destDir);

      String source = HADOOP_STORAGE_DIR + "/dfs";
      String dest   = destDir + "/dfs";

      u.copyDir(source, dest);

      File tagFile =  new File(destDir, tagFileName);
      u.createNewFile(tagFile);

      break;
    }
  }


  // *******************************************
  // get the log file for this datanode
  public String getSpecificLogFile(String x, String y) {
    File dir = new File(HADOOP_LOGS_DIR);
    String[] c = dir.list();
    for (int i=0; i< c.length; i++) {
      if (c[i].contains(x) && c[i].contains(y)) {
        return c[i];
      }
    }
    return null;
  }


  // *******************************************
  Utility getUtility() {
    return u;
  }

  // *******************************************
  Hdfs getHdfs() {
    return hdfs;
  }


  // *******************************************
  // print done ...
  public void printAllExperimentsFinish() {

    String full =
      ("## ################################################# ##\n");
    String side =
      ("##                                                   ##\n");
    String middle = String.format
      ("## A L L    E X P E R I M E N T S    F I N I S H !!! ##\n");

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


  // *******************************************
  public void createAllDirectories() {
    u.mkDir(TMPFI);
    u.mkDir(PIDS_DIR);
    u.mkDir(FAIL_PTS_INFO_DIR);
    u.mkDir(EXP_RESULT_DIR);      
    u.mkDir(FAIL_HISTORY_DIR);
    u.mkDir(COVERAGE_COMPLETE_DIR);
    u.mkDir(COVERAGE_STATIC_DIR);
    u.mkDir(FLAGS_FAILURE_DIR);;
    u.mkDir(HADOOP_LOGS_DIR);
    u.mkDir(RPC_FILES_DIR);
    u.mkDir(SOCKET_HISTORY_DIR);

    //for event re-ordering
    u.mkDir(TMPFI + "eStates");
    u.mkDir(TMPFI + "eWoken");
    u.mkDir(TMPFI + "eWokenWONode");
    u.mkDir(TMPFI + "eWokenDet");

    u.mkDir(EVT_ORDER_HISTORY_DIR);
    u.mkDir(PFX_ORDER_HISTORY_DIR);

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

  public static void writePidForClient(){
        String res = u.runCommand("jps");	  
	String[] pidInfos = res.split("\\n");
	for(String pidInfo : pidInfos){
		if(pidInfo.contains("Main")){
			String[] mPidInfo = pidInfo.split(" ");
			String pid = mPidInfo[0];
		        try{
			    FileWriter fw = new FileWriter(new File(Driver.PID_FILE_FOR_CLIENT));
			    fw.write(pid+"\n");
			    fw.close();
		        }
		        catch(Exception e){
			    System.err.println("Exception while writing client pid");
			    e.printStackTrace();
		        }
		}
	}
  }

}


