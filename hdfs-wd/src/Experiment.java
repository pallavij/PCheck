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

import java.util.List;
import java.util.LinkedList;
import org.fi.Utility.UtilLong;

public class Experiment {

  Driver driver;
  private String type = "UNKNOWN";
  LinkedList<String> ops = new LinkedList<String>();
  private String nonFrogReport = "";
  private String frogReport = "";

  String failCode = "";

  boolean failFromNonFrog = false;
  boolean failFromFrog = false;
  
  boolean failFromStableCheck = false;

  int expNum;
  Utility u;

  UtilLong mytime;
  Hdfs hdfs;

  static final String allFsnsFile = Driver.FAIL_PTS_INFO_DIR + "coverageInfoAllFsnsPExp";  
  static final String subqFsnsFile = Driver.FAIL_PTS_INFO_DIR + "coverageInfoSubqFsnsPExp";  
  static final String corruptInfoFile = Driver.FAIL_PTS_INFO_DIR + "coverageInfoCorruptInfoPExp";
  static final String unOptimNetInfoFile = Driver.FAIL_PTS_INFO_DIR + "unOptimNetIOsPExp";

  static final String globAllFsnsFile = Driver.FAIL_PTS_INFO_DIR + "coverageInfoAllFsns";
  static final String globSubqFsnsFile = Driver.FAIL_PTS_INFO_DIR + "coverageInfoSubqFsns";
  static final String globCorruptInfoFile = Driver.FAIL_PTS_INFO_DIR + "coverageInfoSubqFsnsWCorruptInfo";
  static final String globUnOptimNetInfoFile = Driver.FAIL_PTS_INFO_DIR + "unOptimNetIOs";

  // *******************************************
  public Experiment(Driver driver, int expNum) {
    u = driver.getUtility();
    this.driver = driver;
    this.expNum = expNum;
    this.hdfs = driver.getHdfs();
    mytime = new UtilLong(0);
  }


  // *******************************************
  public void setType(String type) {
    this.type = type;
  }


  // *******************************************
  public int getExpNum() {
    return expNum;
  }

  // *******************************************
  public void addOps(String op) {
    ops.add(op);
  }


  // *******************************************                                 
  // fail experiment that                                                        
  // *******************************************                                 
  public void markFailCode(String code) {
    u.print("- Marking failure...\n");
    for (int i = 0; i < 10; i++)
       u.println("          [FAILCODE][" + code + "]");
    u.println("");

    failFromNonFrog = true;
    failCode += ("-" + code);
  }

  // *******************************************                                 
  public void markFail(String code, Exception e, String report) {
    markFailCode(code);
    addReport(report);
    if (e != null)
      addExceptionToReport(e);
  }

  // *******************************************
  // pallavi-08: disable failure
  //public void clearFail() {
  //  fail = false;
  //}

  // *******************************************
  // fail experiment that we catch outside frog spec
  public void markFailFromNonFrog() {
    u.print("- Marking failure from non frog ...\n");
    failFromNonFrog = true;
  }

  // *******************************************
  // fail experiment that we catch outside frog spec
  public void markFailFromStableCheck() {
    u.print("- Marking failure from stable check ...\n");
    failFromStableCheck = true;
  }

  // *******************************************
  public void addReport(String newReport) {
    addNonFrogReport(newReport);
  }

  public void addNonFrogReport(String newReport) {
    nonFrogReport += (newReport + "\n");
  }





  // *******************************************
  // Print the current stack trace and also the exception stack trace
  public void addExceptionToReport(Exception e) {
    addExceptionToNonFrogReport(e);
  }

  public void addExceptionToNonFrogReport(Exception e) {

    addNonFrogReport("The experiment stack trace is: ");
    addNonFrogReport(u.getStackTrace());

    addNonFrogReport("The Exception stack trace is: ");
    addNonFrogReport(u.stackTraceToString(e.getStackTrace()));

  }


  // ********************************************************
  public void checkFailExperiment() {

    u.print("\n\nChecking if experiment fails or not ... \n\n");

    // check if there is failure from frog
    failFromFrog = isThereErrorFromFrog();

    // if failure
    if (failFromFrog || failFromNonFrog || failFromStableCheck) {
      recordFailExperiment();
    }

  }

  // *******************************************
  public boolean isThereErrorFromFrog() {
    String failedSpec = "Failed Specification";
    String cmd = String.format
      ("grep %s %s", failedSpec, Driver.FROG_OUTPUT_FILE);
    u.print(cmd + "\n");
    String cmdout = u.runCommand(cmd);
    if (cmdout.contains(failedSpec))
      return true;
    return false;
  }


  // ********************************************************
  public boolean isFail() {
    if (failFromFrog || failFromNonFrog) {
      return true;
    }
    return false;
  }


  // *******************************************
  public void printFailHistory() {

    u.print("\nThe sequence of failures are: \n");
    u.print("----------------------------------\n\n");

    for (int fsn = 1; fsn <= Driver.MAX_FSN; fsn++) {
      u.print("\n# Failure Sequence # " + fsn + " :\n\n");
      String buf = getFailHistory(fsn);
      u.print(buf);
    }
  }

  // *******************************************
  public void printFailHistorySummary() {
    
    String buf = "\n";
    buf += String.format("summary %05d : ", expNum);
    
    File expNumDir = getExpNumDir();
    if (!expNumDir.exists() || !expNumDir.isDirectory()) {
      return;
    }
    
    for (int fsn = 1; fsn <= Driver.MAX_FSN; fsn++) {
      String[] children = expNumDir.list();
      for (int i = 0; i < children.length; i++) {
	String tmp = String.format("fsn%d-", fsn);
	if (children[i].contains(tmp)) {
	  buf += String.format("%-18s",
			       children[i].replaceAll(tmp, ""));
	}
      }
    }
    if (isFail()) {
      buf += "  FAIL";
    }
    buf += "\n";
    u.print(buf);
  }

  // *******************************************
  // this experiment does not reach the full max fsn
  // so just rename this experiment folder to another folder
  // *******************************************
  public void wipeOutThisExperiment() {

    u.print(String.format
	    ("\n\n- Experiment %d does not reach maxFsn %d, wiping out ... \n\n",
	     expNum, Driver.MAX_FSN));
    
    String newDirName = String.format("%s/wiped-%05d-%s",
				      Driver.EXP_RESULT_DIR,
				      Driver.getWipedOutNum(),
				      getExpNumShortDirName());
    File newDir = new File(newDirName);
    
    File oldDir = getExpNumDir();
    
    try {
      boolean ok = oldDir.renameTo(newDir);
      if (!ok) {
	u.ERROR("wipe out fail");
      }
    } catch (Exception e) {
      u.EXCEPTION("wipe out fail", e);
    }

    
    Driver.incrementWipedOutNum();

  }


  // *******************************************
  // if the experiment reaches max fsn, then we'll
  // all exp-000x/fsn1-... to fsnMaxFsn is there
  // *******************************************
  public boolean reachMaxFsn() {

    u.print("- reachMaxFsn??\n");

    File expNumDir = getExpNumDir();
    if (!expNumDir.exists() || !expNumDir.isDirectory()) {
      return false;
    }

    int count = 0;
    String[] children = expNumDir.list();
    for (int i=0; i<children.length; i++) {
      for (int fsn = 1; fsn <= Driver.MAX_FSN; fsn++) {
	String tmp = String.format("fsn%d-", fsn);
	// u.print("    " + tmp + " " + children[i] + "\n");
	if (children[i].contains(tmp)) {
	  count++;
	}
      }
    }
    
    if (count != Driver.MAX_FSN)
      return false;
    return true;
  }

  // *******************************************
  public void rmExpDirContent() {
    u.print("- Deleting dir content " + getExpNumDir() + "\n");
    u.deleteDirContent(getExpNumDir());
  }


  // ********************************************                                                         
  // mv perExpInfoDir/* to exp-xyz/                                                                       
  // mv perExpInfoDir/clientRawIoId to exp-xyz/clientRawIoId
  // mv perExpInfoDir/...           to exp-xyz/...
  // ********************************************                                                         
  public void obtainPerExpInfoDir() {
    u.println("- Obtaining perExpInfoDir ... ");

    File dir = new File(Driver.PER_EXP_INFO_DIR);

    File[] children = dir.listFiles();
    if (children == null) return;
    for (int i=0; i< children.length; i++) {
      File from = children[i];
      File to = new File(getExpNumDirName() + "/" + from.getName());
      u.rename(from, to);
    }
  }



  // *******************************************
  public String getFailHistory(int fsn) {

    File expNumDir = getExpNumDir();
    if (!expNumDir.exists() || !expNumDir.isDirectory()) {
      u.WARNING("not exist " + expNumDir);
      return "";
    }

    String[] children = expNumDir.list();
    for (int i=0; i<children.length; i++) {
      String tmp = String.format("fsn%d-", fsn);
      if (children[i].contains(tmp)) {
        return getFailHistoryFileContent(fsn, children[i]);
      }
    }
    return "";
  }

  // *******************************************
  public String getFailHistoryFileContent(int fsn, String fname) {
    File f = new File(getExpNumDir(), fname);
    String buf = u.fileContentToString(f);
    return buf;
  }

  // ********************************************
  private String getExpNumDirName() {
    return String.format("%s/%s", Driver.EXP_RESULT_DIR, getExpNumShortDirName());
  }

  // ********************************************
  // If you change the name of the exp dir, 
  // DON'T FORGET TO CHANGE THE FORMAT AT THE FM SERVER TOO !!!
  // ********************************************
  public String getExpNumShortDirName() {
    return String.format("exp-%05d", expNum);
  }


  // ********************************************
  public File getExpNumDir() {
    assertExpNumDirExists();
    return new File(getExpNumDirName());
  }

  // ********************************************
  private void assertExpNumDirExists() {
    File d = new File(getExpNumDirName());
    if (!d.exists()) {
      u.mkDir(d);
    }
  }


  // *******************************************
  // chmod the expdir to 777 so it's bold yellow,
  // and also mark the experiment fail with 
  private void markFailExperiment() {

    // mark this in the directory
    // if we get here .. we have failure, so let's mark this
    File f = new File(getExpNumDir(), "EXPERIMENT-FAIL");
    u.createNewFile(f);

    File f2 = new File(getExpNumDir(), "FAILCODE." + failCode);
    u.createNewFile(f2);


    //
    String cmd = String.format("chmod 777 %s %s",
			       f.getAbsolutePath(),
			       getExpNumDirName());

    u.runCommand(cmd);

  }

  // *******************************************
  private void recordFailExperiment() {

    u.print("\n");
    u.print("============================================\n");
    u.print("REPORTING FAIL EXPERIMENT # " + expNum + "\n");
    u.print("============================================\n\n");

    markFailExperiment();


    // print non frog report
    u.print("From nonFrog report: \n");
    u.print("--------------------\n\n");
    u.print(nonFrogReport);
    u.print("\n\n");

    // print frog report
    u.print("From frog report: \n");
    u.print("--------------------\n\n");
    String frogContent = u.fileContentToString(Driver.FROG_OUTPUT_FILE);
    u.print(frogContent);
    u.print("\n\n");


    // ... copy files
    u.print("\n\nCopying files now ........ \n");

    // copy non frog report
    String nonFrogFile = getExpNumDirName() + "/" + "nonFrogReport.txt";
    u.stringToFileContent(nonFrogReport, nonFrogFile);

    // copy frog report
    String frogFile = getExpNumDirName() + "/" + "frogReport.txt";
    u.copyFile(Driver.FROG_OUTPUT_FILE, frogFile);

  }



  // *******************************************
  // print the experiment number ...
  public void printBegin() {

    


    String full =
      ("## ################################################# ##\n");
    String side =
      ("##                                                   ##\n");
    String middle = String.format
      ("##         E X P E R I M E N T   #  %05d            ##\n", expNum);

    u.print("\n\n");
    u.print(full);
    u.print(full);
    u.print(side);
    u.print(middle);
    u.print(side);
    u.print(full);
    u.print(full);
    u.print("\n\n");


    u.print("- Experiment " + expNum + " begins " + u.diff(mytime) + "\n");

  }

  // *******************************************
  // print the experiment number ...
  public void printEnd() {

    u.print("- Experiment " + expNum + " ends " + u.diff(mytime) + "\n");

  }

  public void recordAllFsns(){
     String fsnsInjected = getStrFsnsInjected();
     String allFsns = u.fileContentToString(allFsnsFile);
     allFsns = allFsns.replaceAll("\n"," ");
     allFsns = allFsns.substring(0, allFsns.length()-1);
     u.stringToFileContent(fsnsInjected+":"+allFsns+"\n", new File(globAllFsnsFile), false, true);
  }

  public void recordSubqFsns(){
     String fsnsInjected = getStrFsnsInjected();
     
     File f = new File(subqFsnsFile);
     if(!f.exists()){
	return;
     }

     String subqFsnsStr = u.fileContentToString(subqFsnsFile);
     String lastFsnsCorruptInfoStr = u.fileContentToString(corruptInfoFile);
     
     String subqFsnsToWr = "";
     String subqFsnsWCorruptInfoToWr = "";
     String[] subqFsns = subqFsnsStr.split("\\n");
     String[] lastFsnsCorruptInfo = lastFsnsCorruptInfoStr.split("\\n"); 
     
     for(int i = 0; i < subqFsns.length; i++){
	String subqFsn = subqFsns[i];
        String lastFsnCorruptInfo = lastFsnsCorruptInfo[i];	
	subqFsnsToWr += fsnsInjected + " " + subqFsn + "\n";
	
	subqFsnsWCorruptInfoToWr += fsnsInjected + " " + subqFsn;
	if(!lastFsnCorruptInfo.contains("NOT_APPLICABLE")){
		subqFsnsWCorruptInfoToWr += " (" + lastFsnCorruptInfo + ")\n"; 
	}
	else{
		subqFsnsWCorruptInfoToWr += "\n";
	}
     }  

    u.stringToFileContent(subqFsnsToWr, new File(globSubqFsnsFile), false, true);   
    u.stringToFileContent(subqFsnsWCorruptInfoToWr, new File(globCorruptInfoFile), false, true);   

  }

  public void recordUnOptimNetFsns(){
     String fsnsInjected = getStrFsnsInjected();
     
     File f = new File(unOptimNetInfoFile);
     if(!f.exists()){
	return;
     }

     String unOptimNetFsnsStr = u.fileContentToString(unOptimNetInfoFile);
     String unOptimNetFsnsToWr = "";
     String[] unOptimNetFsns = unOptimNetFsnsStr.split("\\n");

     for(int i = 0; i < unOptimNetFsns.length; i++){
	String unOptimFsn = unOptimNetFsns[i];
	unOptimNetFsnsToWr += fsnsInjected + " " + unOptimFsn + "\n";
     } 

     u.stringToFileContent(unOptimNetFsnsToWr, new File(globUnOptimNetInfoFile), false, true);   
  }

  private String getStrFsnsInjected(){
     File expDir = new File(getExpNumDirName());
     String fsnsInjected = "";
     if(expDir.exists()){
     	  String[] files = expDir.list();
	  for(String fName : files){
		if(fName.startsWith("fsn")){
			String[] fNameParts = fName.split("h");
			String hId = fNameParts[1];
			fsnsInjected += hId.substring(0,hId.length()-4) + " ";
		}
	  }
     }

     int fsnsInjLen = fsnsInjected.length();

     if(fsnsInjLen > 0){
		fsnsInjected = fsnsInjected.substring(0, fsnsInjLen - 1);
     }

     return fsnsInjected;
  }


  //The following functions are for message re-ordering

  private String curMsgOrder = null;

  public String getCurMsgOrder(){
    List<String> curMsgs = u.fileContentToList(Driver.EVTS_WOKEN);
    if(curMsgs != null){
      String order = "";
      for(int i = 0; i < curMsgs.size(); i++){
	String s = curMsgs.get(i);
	//u.MESSAGE("s in MSGS_WOKEN is " + s + "\n");
	String[] sParts = s.split(":");
	String msgId = (sParts[0]).trim();
	order += msgId + " ";
      }
      order = order.trim();
      order += "\n";
      curMsgOrder = order;
    }
    return curMsgOrder;
  }


  public void writeEvtIdsInExp(){
    String order = getCurMsgOrder();
    if(order != null){
      String[] evtIds = order.split("\\s+");
      for(int i = 0; i < evtIds.length; i++){
	File f = new File(Driver.EXP_RESULT_DIR + getExpNumShortDirName() + "/" + "e" + (i+1));
	u.stringToFileContent(evtIds[i] + "\n", f, false, false);
	File descrF = new File(Driver.COVERAGE_COMPLETE_DIR + "h" + evtIds[i] + ".txt");
	if(descrF.exists()){
	  String descr = u.fileContentToString(descrF);
	  if(descr != null){
	    u.stringToFileContent(descr, f, false, true);
	  }
	}
      }
    }
  }


}
