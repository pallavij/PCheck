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

import org.fi.*;
import org.fi.FMServer.FailType;
import org.fi.FMJoinPoint.*;

import java.io.*;
import java.util.*;



// hdfs specifics


public class FMLogic {


  private static int cachedMaxFsn = 0; // cache, don't update directly

  // public static final String HADOOP_USERNAME = "hadoop-haryadi";
  public static final String HADOOP_USERNAME = "hadoop-" + System.getenv("USER");

  public static final String TMPFI = "/tmp/fi/";
  public static final String HADOOP_STORAGE_DIR = TMPFI + HADOOP_USERNAME + "/";
  
  public static final String FAIL_HISTORY_DIR      = TMPFI + "failHistory/";
  public static final String FLAGS_FAILURE_DIR     = TMPFI + "flagsFailure/";
  public static final String EXP_RESULT_DIR        = TMPFI + "expResult/";
  public static final String RPC_FILES_DIR         = TMPFI + "rpcFiles/";
  public static final String SOCKET_HISTORY_DIR    = TMPFI + "socketHistory/";
  public static final String COVERAGE_COMPLETE_DIR = TMPFI + "coverageComplete/";
  public static final String COVERAGE_STATIC_DIR   = TMPFI + "coverageStatic/";
  
  public static final String ENABLE_FAILURE_FLAG   = TMPFI + "enableFailureFlag";
  public static final String CLIENT_OPTIMIZE_FLAG  = TMPFI + "clientOptimizeFlag";
  public static final String ENABLE_COVERAGE_FLAG  = TMPFI + "enableCoverageFlag";
  
  public static final String EVT_ORDER_HISTORY_DIR  = TMPFI + "evtOrderHistory/";

  public static final String PFX_ORDER_HISTORY_DIR = EVT_ORDER_HISTORY_DIR + "pfxOrders/";
  public static final String EVT_ORDER_HISTORY_FILE = EVT_ORDER_HISTORY_DIR + "history";
  public static final String EVT_ORDER_PFX_TO_TRY = EVT_ORDER_HISTORY_DIR + "orderPfx";
  public static final String EVT_DEP_TO_TRY = EVT_ORDER_HISTORY_DIR + "evtDep";

  public static final String N_PREV_PFXS = EVT_ORDER_HISTORY_DIR + "nPrevPfxs";
  public static final String PREV_PFX = EVT_ORDER_HISTORY_DIR + "prevPfx";

  public static final String EVTS_WOKEN = EVT_ORDER_HISTORY_DIR + "wokenUpEvts";
  public static final String EVTS_WOKEN_DET = EVT_ORDER_HISTORY_DIR + "wokenUpEvtsDet";
  public static final String EVTS_WOKEN_WO_NODE = EVT_ORDER_HISTORY_DIR + "wokenUpEvtsWONode";
 
  public static final String EXEC_STATE = EVT_ORDER_HISTORY_DIR + "execState";
  public static final String QUEUE_STATE = EVT_ORDER_HISTORY_DIR + "queueState";
 
  public static final String WIPE_OUT_FLAG = EVT_ORDER_HISTORY_DIR + "wipeOut";
  public static final String LAST_EXP_WIPE_OUT_FLAG = EVT_ORDER_HISTORY_DIR + "lExpWipeOut";
  public static final String START_FLAG = EVT_ORDER_HISTORY_DIR + "start";
  public static final String EXP_START_FLAG = EVT_ORDER_HISTORY_DIR + "expStart";
  public static final String EXP_RUN_FLAG = TMPFI + "expRunning";
  public static final String ELECTION_OVER = TMPFI + "electionOver";
  public static final String WRITE_OVER = TMPFI + "writeOver";
 
  public static boolean NORMAL_EXEC = false;
  //public static final int nZkServers = 5;
  public static final int nZkServers = 3;
 
  public static Map<String, Set<String>> netReads = new HashMap<String, Set<String>>();
  public static Set<String> corruptedFiles = new HashSet<String>();
  public static Set<String> filesOnFailedDisks = new HashSet<String>();
  public static Set<String> crashedNodes = new HashSet<String>();
 

  // ########################################################################
  // ########################################################################
  // ##                                                                    ##
  // ##                            S E T U P S                             ##
  // ##                                                                    ##
  // ########################################################################
  // ########################################################################


  // *********************************************
  public FMLogic() {  }


  // *********************************************
  // the brain of fm logic begins. have fun!
  // *********************************************
  public static FailType run(FMAllContext fac) {

    FailType ft;

    // check if we need to reset anything?
    checkResetExperiment();

    //recordCounters(fac);
    System.out.println(">> FMLogic.run ... ");

    // generate all possible failures
    FailType [] failures = listPossibleFailures(fac);
    ft = tryTheseFailures(fac, failures);

    // DEPRECATED ... we are calling this at the client side
    // check if we have persistent failure, see the function's comment
    // ft = checkPersistentFailure(fac, ft);

    return ft;
  }

    public static void recordCounters(FMAllContext fac){
       String targetIO = fac.ctx.getTargetIO();
       boolean rdOrWr = (fac.fjp.getJoinIot() == JoinIot.READ ||
                                       fac.fjp.getJoinIot() == JoinIot.WRITE);

        if ((Util.isNetIO(targetIO) || Util.isDiskIO(targetIO)) && rdOrWr) {
                FMFilter.filterCrash(fac);
        }

       if (Util.isNetIO(targetIO) && rdOrWr &&
             fac.fjp.getJoinPlc() == JoinPlc.BEFORE) {
               FMFilter.filterNetFail(fac);

       }

       //if (fac.ctx.getTargetIO().contains("hadoop") &&
       if (Util.isDiskIO(targetIO) && rdOrWr &&
                fac.fjp.getJoinPlc() == JoinPlc.BEFORE) {
               FMFilter.filterDiskFail(fac, FailType.BADDISK);
       }

       //if (fac.fjp.getJoinExc() != JoinExc.NONE &&
       //                           fac.fjp.getJoinPlc() == JoinPlc.BEFORE) {
       //       FMFilter.filterDiskFail(fac, FailType.EXCEPTION);
       //}

       if (Util.isDiskIO(targetIO) && fac.fjp.getJoinIot() == JoinIot.READ &&
                                  fac.fjp.getJoinPlc() == JoinPlc.AFTER) {
               FMFilter.filterCorruption(fac);
       }

  }


  // *********************************************
  // do we need to reset anything? signaled by WorkloadDriver
  // *********************************************
  private static void checkResetExperiment() {

  }

  // *********************************************
  // List all possible failures, given the information about this
  // pointcut. It's up to the FIState model-checker and the
  // server filter to decide which failures that we want to exercise
  // later. All we want to do here is simply list all possible failures.
  // *********************************************
  private static FailType[] listPossibleFailures(FMAllContext fac) {

    List<FailType> list = new ArrayList<FailType>();

    boolean crash = false;
    boolean exception = false;
    boolean corruption = false;
    boolean baddisk = false;
    boolean retfalse = false;
    boolean netfailure = false;


    // throw exception if it's possible (must before)
    if (fac.fjp.getJoinExc() != JoinExc.NONE &&
        fac.fjp.getJoinPlc() == JoinPlc.BEFORE) {
      exception = true;
    }

    // special case, if it's an FNF exception, it's okay
    // that we throw this FNF exception after -- because
    // in 'before' we haven't known the context yet
    if (fac.fjp.getJoinExc() == JoinExc.FNF) {
      exception = true;
    }

    // false bool if operation JoinRbl is yes (must before)
    if (fac.fjp.getJoinRbl() == JoinRbl.YES &&
        fac.fjp.getJoinPlc() == JoinPlc.BEFORE) {
      retfalse = true;
    }

    // corruption only if iot is read (must after)
    if (fac.fjp.getJoinIot() == JoinIot.READ &&
        fac.fjp.getJoinPlc() == JoinPlc.AFTER) {
      corruption = true;
    }

    // crash for read and write (before or after is fine)
    if (fac.fjp.getJoinIot() == JoinIot.READ ||
        fac.fjp.getJoinIot() == JoinIot.WRITE) {
      crash = true;
    }
  
     
    if (fac.ctx.getTargetIO().contains("NetIO") ||
        fac.ctx.getTargetIO().contains("badnetio")) {
	netfailure = true;
    }
    

    // baddisk if targetIO is disk (must be before)
    if (fac.ctx.getTargetIO().contains("tmp") &&
        fac.fjp.getJoinPlc() == JoinPlc.BEFORE) {

      // but we only want to insert baddisk if
      // we haven't failed this disk
      if (!isTargetIOaBadDisk(fac.ctx))  {
        baddisk = true;
      }
    }


    // now let's add possible failures
    if (crash)      list.add(FailType.CRASH);
    if (exception)  list.add(FailType.EXCEPTION);
    if (corruption) list.add(FailType.CORRUPTION);
    if (baddisk)    list.add(FailType.BADDISK);
    if (retfalse)   list.add(FailType.RETFALSE);
    if (netfailure) list.add(FailType.NETFAILURE);


    if (list.size() == 0)
      return null;

    FailType[] failures = list.toArray(new FailType[list.size()]);

    /*****
    if (false) {
      System.out.print("  Possible failures: ");
      for (int i = 0; i < failures.length; i++) {
        System.out.print(failures[i].toString() + ", ");
      }
      System.out.println("\n");
    }
    *****/

    return failures;

  }


  // *********************************************
  // At this point, we might have a failure to exercise
  // or we don't, we'll just return this to the FMClient
  // but before that, we always need to check for
  // persistent failures (e.g. baddisk), because even
  // though we don't have a failure at this point, we
  // might have exercised a persistent failure (e.g. baddisk)
  // before.  Hence, we want to check if the persistent
  // failure overwrites non-failure.
  // If we should exercise a failure, we just return the failure
  // this function only conversts FailType.NONE to
  // FailType.BADDISK if it's appropriate

  // *********************************************
  public static FailType checkPersistentFailure(FMAllContext fac, FailType ft) {

    if (ft != FailType.NONE) {
      return ft;
    }

    if (isTargetIOaBadDisk(fac.ctx)) {
      return FailType.BADDISK;
    }

    return FailType.NONE;

  }


  // *********************************************
  // from the ctx, is target an already bad disk?
  // if so, return true, else return false
  // *********************************************
  private static boolean isTargetIOaBadDisk(FMContext ctx) {

    // if this is not a disk io return false
    if (!Util.isDiskIO(ctx.getTargetIO())) {
      return false;
    }

    // let's get the nodeId and diskId for this ctx
    String nodeId = ctx.getNodeId();
    String diskId = Util.getDiskIdFromTargetIO(ctx.getTargetIO());

    // something wrong
    if (diskId.equals("DiskUnknown"))
      return false;

    // check the flag file
    File flagFile = getBadDiskFlagFile(nodeId, diskId);
    if (flagFile.exists()) {
      return true;
    }

    return false;
  }




  // *********************************************
  // for each possible failure, we want to try if we
  // so do the failure or not.
  // if ft is approved, then we should break
  // if not, we should continue to the next failure
  // *********************************************
  private static FailType tryTheseFailures(FMAllContext fac, FailType[] failures) {
    FailType ft = FailType.NONE;
    if (failures == null)
      return ft;
    for (int i = 0; i < failures.length; i++) {
      ft = tryThisFailure(fac, failures[i]);
      if (ft != FailType.NONE) {
        break;
      }
    }
    return ft;
  }


  // *********************************************
  // Before we insert the failure, we want to filter this
  // first.  So check the filter.
  // *********************************************
  private static FailType tryThisFailure(FMAllContext fac, FailType ft) {

    // let's build the FIState based on the failure
    // build the FIState
    FIState fis = new FIState(fac, ft);

    if (FMFilter.passServerFilter(fac, ft, fis)) {
      
      // if pass the server filter, we want to measure the stats
      // that have been filtered ..
      Coverage.recordStatAfterFilter(fac, ft, fis);


      // for the sake of recording stat, we're done ..
      // so no need to continue ...
      // just check if
      if (isEnableFailureFlagExist()) {

        FailType retFt = runFailLogic(fac, ft, fis);
        return retFt;

      }
    }

    return FailType.NONE;
  }


  // ########################################################################
  // ########################################################################
  // ##                                                                    ##
  // ##                       C O R E    L O G I C                         ##
  // ##                                                                    ##
  // ########################################################################
  // ########################################################################




  // *************************************************
  // This is the fail logic: logics for single crash,
  // multiple crashes, remembering failure history
  // should all go in this place
  // *************************************************
  private static FailType runFailLogic(FMAllContext fac, FailType ft, FIState fis) {


    // if i have reached max fsn ...
    // just continue ...
    if (hasReachedMaxFsn()) {
      return FailType.NONE;
    }

    // let's get current failure number
    int fsn = getCurrentFsn();

    // check the logic
    if (!shouldFail(fsn, fis)) {
      return FailType.NONE;
    }


    // if we reach this point we're doing failure ..
    recordFailure(fac, ft, fis, fsn);


    return ft;

  }



  // ********************************************
  // Given a fsn and a hash, this is the logic:
  //  - first we check if the fsn is locked or not
  //    if it is locked then the hash must match
  //    with the hash specified by the locked fsn.
  //    otherwise, we shouldn't fail this.
  //  - else, if fsn is not locked, then we go
  //    to normal mode, where we check if we fail
  //    this failure before or not
  // ********************************************
  private static boolean shouldFail(int fsn, FIState fis) {

    // String tmp = String.format("fsn-%d hash-%s", fsn, hash);

    boolean shouldFail;
    if (isFsnLocked(fsn)) {
      if (isFsnAndHashMatched(fsn, fis.getHashId())) {
        shouldFail = true;
      }
      else {
        shouldFail = false;
      }
    }
    else {
      if (isInFailHistory(fsn, fis.getHashId())) {
        // System.out.format("_We have injected %d in the past_\n",
        // fis.getHashId());
        shouldFail = false;
      }
      else  {
        shouldFail = true;
      }
    }
    return shouldFail;
  }


  // ********************************************
  // record the failure
  // ********************************************
  private static void recordFailure(FMAllContext fac, FailType ft,
                                    FIState fis, int fsn) {

    recordInjectedFsn(fsn);
    recordFailHistory(fac, ft, fis, fsn);
    recordFailureToExperiment(fac, ft, fis, fsn);
    recordLatestHistory(fsn, fis);

    // special treatment for bad disk
    recordBadDisk(fac, ft);

  }



  // ########################################################################
  // ########################################################################
  // ##                                                                    ##
  // ##                        U T I L I T Y                               ##
  // ##                                                                    ##
  // ########################################################################
  // ########################################################################



  // ********************************************
  // This algorithm is easy ... we're just failing
  // whatever we have
  // ********************************************
  public static boolean hasReachedMaxFsn() {

    int maxFsn = getMaxFsn();
    if (isFsnInjected(maxFsn))
      return true;
    return false;
  }

  // *******************************************
  public static int getMaxFsn() {
    if (cachedMaxFsn != 0)
      return cachedMaxFsn;
    String path = FLAGS_FAILURE_DIR + "/maxFsn";
    String tmp1 = Util.fileContentToString(path);

    if (tmp1 == null) {
      Util.FATAL("maxFsn is unknown");
      return 0;
    }
    tmp1 = tmp1.replaceAll("\n", "");

    Integer tmp;
    try {
      tmp = new Integer(tmp1);
    } catch(NumberFormatException nfe) {
      Util.FATAL("There is no maxFsn file?");
      return 0;
    }

    int maxFsn = tmp.intValue();
    if (maxFsn < 1 || maxFsn > 100) {
      Util.FATAL("weird maxFsn " + maxFsn);
    }
    cachedMaxFsn = maxFsn;
    return cachedMaxFsn;
  }

  // *******************************************
  private static boolean isFsnInjected(int fsn) {
    File f = getInjectedFsnFile(fsn);
    if (f.exists())
      return true;
    return false;
  }

  // *******************************************
  private static File getInjectedFsnFile(int fsn) {
    String path = String.format("%s/injected-fsn-%d", FLAGS_FAILURE_DIR, fsn);
    File f = new File(path);
    return f;
  }

  // ********************************************
  public static int getCurrentFsn() {
    int fsn = 1;
    while (true) {
      if (!isFsnInjected(fsn))
        return fsn;
      fsn++;
    }
  }

  // ********************************************
  // if fsn is locked it means this fsn has a
  // specific hash that we must follow
  // ********************************************
  private static boolean isFsnLocked(int fsn) {
    File f = getFsnLockFile(fsn);
    if (f.exists())
      return true;
    return false;
  }

  // ********************************************
  // filename: locked-fsn-#
  // ********************************************
  private static File getFsnLockFile(int fsn) {
    String path = String.format("%s/locked-fsn-%d", FLAGS_FAILURE_DIR, fsn);
    File f = new File(path);
    return f;
  }


  // ********************************************
  //
  // ********************************************
  private static boolean isFsnAndHashMatched(int fsn, int hashId) {
    File f = getFsnAndHashFile(fsn, hashId);
    if (f.exists())
      return true;
    return false;
  }

  // ********************************************
  // filename: hash-for-fsn-%d-is-
  // ********************************************
  private static File getFsnAndHashFile(int fsn, int hashId) {
    String path = String.format("%s/hash-for-fsn-%d-is-h%s.txt",
                                FLAGS_FAILURE_DIR,
                                fsn, hashId);
    File f = new File(path);
    return f;
  }


  // ********************************************
  //
  // ********************************************
  private static boolean isInFailHistory(int fsn, int hashId) {
    File f = getFailHistoryFile(fsn, hashId);
    if (f.exists())
      return true;
    return false;
  }


  // ********************************************
  // fail history file: .../failHistory/fsn-1/h-d989.txt
  // ********************************************
  private static File getFailHistoryFile(int fsn, int hashId) {

    String dir = String.format("%s/fsn-%d", FAIL_HISTORY_DIR, fsn);
    File d = new File(dir);
    if (!d.exists()) {
      Util.mkDir(d);
    }

    String file = getHashFileName(hashId);
    File f = new File(d, file);
    return f;
  }


  // ********************************************
  public static String getHashFileName(int hashId) {
    return String.format("h%d.txt", hashId);
  }

  // *******************************************
  private static void recordInjectedFsn(int fsn) {
    File f = getInjectedFsnFile(fsn);
    Util.createNewFile(f);
  }

  // *************************************************
  private static void recordBadDisk(FMAllContext fac, FailType ft) {

    // no need to do anything if it's not a baddisk
    if (ft != FailType.BADDISK)
      return;

    // if it's a bad disk .... need to remember what node and what disk ..
    String nodeId = fac.ctx.getNodeId();
    String diskId = Util.getDiskIdFromTargetIO(fac.ctx.getTargetIO());
    File flagFile = getBadDiskFlagFile(nodeId, diskId);
    Util.createNewFile(flagFile);
  }

  // *******************************************
  private static File getBadDiskFlagFile(String nodeId, String diskId) {
    String fname =
      String.format("%s/BadDisk_%s_%s",
                    FLAGS_FAILURE_DIR, nodeId,  diskId);
    File f = new File(fname);
    return f;
  }

  // ********************************************
  private static void recordFailHistory(FMAllContext fac, FailType ft,
                                        FIState fis, int fsn) {

    File f = getFailHistoryFile(fsn, fis.getHashId());
    if (f.exists()) {
      // it's okay that a fail history already there
      // for example, in the case where fail history is locked ..
      return;
    }


    String buf = getFailHistoryContent(fac, ft, fis, fsn);
    Util.stringToFileContent(buf, f, true, false);
  }


  // ********************************************
  public static String getFailHistoryContent(FMAllContext fac, FailType ft,
                                             FIState fis, int fsn) {

    String buf = "";

    // print hash id first
    buf += "\n";
    buf += "The hash ID string is: \n";
    buf += "## [" + fis.getHashIdStr() + "] \n";
    buf += "\n";
    buf += "The hash ID is: \n";
    buf += "[[" + fis.getHashId() + "]] \n";
    buf += "\n";

    // print all context
    buf += String.format("Receive sendContext: [" +
                         fac.ctx.getCutpointRandomId() + "]\n");

    buf += "\n";


    if (ft != null) {
      buf += "FailType: **" + ft.toString() + "**\n\n";
    }


    buf += fac.ctx + "\n";
    buf += fac.fjp + "\n";
    buf += fac.fst + "\n";

    buf += "\n";

    return buf;
  }


  // ********************************************
  // we must do this because "ls -t" is not precise
  // ********************************************
  private static void recordLatestHistory(int fsn, FIState fis) {
    File f = getLatestHistoryFile(fsn);
    Util.stringToFileContent(fis.getHashId() + "\n", f);
  }

  // ********************************************
  // return the latest history file for this fsn
  // ********************************************
  private static File getLatestHistoryFile(int fsn) {
    String path = String.format("%s/latest-for-fsn-%d", FAIL_HISTORY_DIR, fsn);
    File f = new File(path);
    return f;
  }


  // ********************************************
  // this is the function that is dependent on workload driver
  // however, if expNumStr
  // ********************************************
  private static void recordFailureToExperiment(FMAllContext fac, FailType ft,
                                                FIState fis, int fsn) {

    String path = FLAGS_FAILURE_DIR + "/currentExpNumber";
    String expNumStr = Util.fileContentToString(path);
    if (expNumStr == null) {
      Util.WARNING("No info on experiment number, continuing");
      return;
    }
    expNumStr = expNumStr.replaceAll("\n", "");
    Integer expNum;
    try {
      expNum = new Integer(expNumStr);
    } catch(NumberFormatException nfe) {
      Util.EXCEPTION("Can't convert exp# ", nfe);
      return;
    }


    String expNumDirName = getExpNumDirName(expNum.intValue());
    Util.mkDir(expNumDirName);

    path = String.format("%s/fsn%d-%s",
                         expNumDirName,
                         fsn, getHashFileName(fis.getHashId()));

    String buf = getFailHistoryContent(fac, ft, fis, fsn);

    Util.stringToFileContent(buf, path);

  }


  // ********************************************
  private static String getExpNumDirName(int expNum) {
    return String.format("%s/exp-%05d", EXP_RESULT_DIR, expNum);
  }


  // ***********************************************************
  public static boolean isEnableFailureFlagExist() {
    File f = new File(ENABLE_FAILURE_FLAG);
    if (f.exists())
      return true;
    return false;
  }

   public static void recordReadFromBadNode(FMAllContext fac, FailType retFt, FIState fis){
    boolean isNetFail = (retFt == FailType.NETFAILURE);
    boolean readIO = (fac.fjp.getJoinIot() == JoinIot.READ);
    boolean writeIO = (fac.fjp.getJoinIot() == JoinIot.WRITE);

    if(readIO && isNetFail){
      if(fis != null){
        String nodeId = fis.getNodeId();
        String targetNodeId = fis.getTargetNode();

        if(notUnknown(nodeId) && notUnknown(targetNodeId)){
          Set<String> nodesReadingFrom = (Set<String>)netReads.get(nodeId);
          if(nodesReadingFrom == null){
            nodesReadingFrom = new HashSet<String>();
            netReads.put(nodeId, nodesReadingFrom);
          }
          nodesReadingFrom.add(targetNodeId);
        }
      }
    }

    if(writeIO && isNetFail){
      if(fis != null){
        String nodeId = fis.getNodeId();
        String targetNodeId = fis.getTargetNode();
        if(notUnknown(nodeId) && notUnknown(targetNodeId)){
          Set<String> nodesReadingFrom = (Set<String>)netReads.get(nodeId);
          if(nodesReadingFrom != null){
            nodesReadingFrom.remove(targetNodeId);
          }
        }
      }
    }
  }


  private static boolean notUnknown(String s){
    return !(s.contains("Unknown") || s.contains("UnKnown") || s.contains("unknown"));
  }

  public static void recordCorruptedFile(FMAllContext fac, FailType retFt, FIState fis){
    boolean isCorrupt = (retFt == FailType.CORRUPTION);
    boolean readIO = (fac.fjp.getJoinIot() == JoinIot.READ);

    if (readIO && isCorrupt){
      if (fis != null){
        String fType = fis.getFileType();
        if(!fType.contains("Unknown")){
          corruptedFiles.add(fType);
        }
      }
    }
  }

   public static void recordFileOnFailedDisk(FMAllContext fac, FailType retFt, FIState fis){
    boolean isDiskFail = (retFt == FailType.BADDISK || retFt == FailType.EXCEPTION);
    boolean readIO = (fac.fjp.getJoinIot() == JoinIot.READ);

    if (readIO && isDiskFail){
      if (fis != null){
        String fType = fis.getFileType();
        if(!fType.contains("Unknown")){
          filesOnFailedDisks.add(fType);
        }
      }
    }
  }
  

}
