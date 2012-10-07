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
import java.util.*;
import java.nio.*;

import java.lang.Thread;
import java.lang.StackTraceElement;
import java.lang.Math;


public class Utility {

  // private static Map<String,String> pidCacheMap = new TreeMap<String, String>();

  public static PrintStream ps = null;

  public Utility() {

  }

  // ############################################################
  // ##                 GENERAL UTILITY                        ##
  // ############################################################

  private Random random = new Random();

  public int r() { return Math.abs(random.nextInt()); }

  // random to 4th digit
  public int r4() { return (Math.abs(random.nextInt()) % 10000); }

  // random to 8th digit
  public int r8() { return (Math.abs(random.nextInt()) % 100000000); }


  public static class UtilLong {
    long l;
    public UtilLong() {  l = 0;  }
    public UtilLong(long x) {  l = x;  }
    public long longValue() {  return l;   }
    public void setValue(long x) {   l = x;   }
  }

  public long now() {
    return System.currentTimeMillis();
  }

  // haryadi ...
  private UtilLong theLastTime = new UtilLong();
  private String lastWarning = "";

  public String diff() {
    return diff(theLastTime);
  }

  public String diff(UtilLong lastTime) {
    long currentTime = System.currentTimeMillis();
    double diff = 0.0;
    if (lastTime.longValue() == 0) {
      lastTime.setValue(currentTime);
    }
    else {
      diff = (double)(currentTime - lastTime.longValue()) / 1000;
      lastTime.setValue(currentTime);
    }
    String warning = "";

    if (diff > 1) {
      warning = "*LONG!!*";
    }

    if (diff > 5) {
      warning = "*LONG LONG!!*";

    }

    if (diff > 30) {
      warning = "*LONG LONG LONG!!*";

    }

    lastWarning = warning;

    long ct = (currentTime / 1000) % 1000;

    long tid = (Thread.currentThread().getId()) % 100;

    return String.format("[hd-diff][%5.3f secs] [%d] (t-%d) %s",
                         diff, (int)ct, (int)tid, warning);

  }


  // ############################################################
  // ##               PRINTING UTILITY                         ##
  // ############################################################
  public void setupPrintStream(String filename) {
    try {
      FileOutputStream fos = new FileOutputStream(filename);
      ps = new PrintStream(fos);
    } catch (Exception e) {
      EXCEPTION("Can't open " + filename, e);
      System.exit(-1);
    }
  }

  // *****************************************
  public PrintStream getPrintStream() {
    return ps;
  }

  // *****************************************
  // print to both output shell and to the print stream
  public void print(String buf) {
    System.out.print(buf);
    ps.print(buf);
  }

  // *****************************************
  // print to both output shell and to the print stream
  public void println(String buf) {
    System.out.println(buf);
    ps.println(buf);
  }

  // ############################################################
  // ##                   HDFS UTILITY                         ##
  // ############################################################


  // *****************************************
  // given an tmp-pid file, get the pid by reading the file
  public String getPidFromTmpPid(File f) {
    String pid = null;
    if (!f.exists())
      return pid;
    try {
      BufferedReader in = new BufferedReader(new FileReader(f));
      pid = in.readLine();
      if (pid == null) {
        WARNING("pid is null");
        return pid;
      }
      in.close();
    } catch (Exception e) {
      EXCEPTION("getPidFromTmpPid ", e);
    }
    return pid;
  }


  // ############################################################
  // ##                   OS UTILITY                           ##
  // ############################################################


  // *****************************************
  public void sleep(int ms) {
    try {
      Thread.sleep(ms);
    } catch (Exception e) {}

  }


  // *****************************************
  // find out if pid is a java alive, by calling the shell
  // ps -c -p pid , and then grep the output and see
  // if "java" is in there or not
  public boolean isPidAlive(String pid) {
    String cmdout = runCommand ("ps -c -p " +  pid);
    if (cmdout.contains("java"))
      return true;
    return false;
  }


  // *****************************************
  // a tool to run the command
  public String runCommand(String cmd) {

    String msg = "";
    try {
      String line;
      Process p = Runtime.getRuntime().exec(cmd);
      BufferedReader input =new BufferedReader
        (new InputStreamReader(p.getInputStream()));
      while ((line = input.readLine()) != null) {
        msg = msg + line + "\n";
      }
      input.close();
    } catch (Exception e) {
      EXCEPTION("runCommand", e);
    }

    return msg;
  }



  // ############################################################
  // ##                   FILE UTILITY                         ##
  // ############################################################

  // **************************************
  public void copyDir(String from, String to) {
    File f = new File(from);
    File t = new File(to);
    copyDir(f, t);
  }

  // **************************************
  public void copyDir(File from, File to) {
    println("- Copying dir " + from + " to " + to);
    if (!from.exists()) {
      ERROR("in copyDir, source dir does not exist: " + from);
      return;
    }
    if (to.exists()) {
      ERROR("in copyDir, dest dir exist: " + to);
      return;
    }
    String cmd = String.format("cp -R %s %s",
                               from.getAbsolutePath(),
                               to.getAbsolutePath());
    println("- shellcmd: " + cmd);
    String cmdout = runCommand (cmd);
    println("- shellret: " + cmdout);
  }

  // **************************************
  public void copyFile(String from, String to) {

    File ffrom = new File(from);
    File fto = new File(to);

    copyFile(ffrom, fto);
  }

  // **************************************
  public void copyFile(File ffrom, File fto) {

    if (!ffrom.exists())
      WARNING("in copyFile: from does not exist: " + ffrom.getAbsolutePath());
    if (fto.exists())
      WARNING("in copyFile: to exist: " + fto.getAbsolutePath());

    String cmdout = runCommand ("cp " +  ffrom.getAbsolutePath() + " " + fto.getAbsolutePath());
    print(cmdout + "\n");
  }


  // **************************************
  // mkDir
  public boolean mkDir(String dirname) {
    return mkDir(new File(dirname));
  }

  // **************************************
  // mkDir
  public boolean mkDir(File dir) {
    try {
      dir.mkdir();
    } catch (Exception e) {
      WARNING("can't mkdir " + dir);
      return false;
    }
    return true;
  }


  // **************************************
  // convert string to file, and delete
  public boolean deleteDir(String dirname) {
    return deleteDir(new File(dirname));
  }



  // **************************************                                                               
  public boolean rename(String f, String t) {
    assertSafeFile(f);
    assertSafeFile(t);
    File from = new File(f);
    File to = new File(t);
    return rename (from, to);
  }

  // **************************************                                                               
  public boolean rename(File f, File t) {
    if (!f.exists())
      return false;
    print(String.format("   Moving '%s' to '%s' \n",
                        f.getAbsolutePath(), t.getAbsolutePath()));
    boolean success = f.renameTo(t);
    return success;
  }


  // **************************************
  public void assertSafeFile(String fullPath) {

    boolean safe = false;

    // we only allow removal of anything that is in /tmp/*  !!
    if (fullPath.indexOf("/tmp") == 0) {
      safe = true;
    }
    else  {
      safe = false;
    }

    // if not safe, don't proceed
    if (safe == false) {
      ERROR("You're trying to remove UNSAFE directories!!" + fullPath);
      ERROR("I only allow files under /tmp/** to be removed");
      ERROR("Are you sure?? Exiting");
      System.exit(-1);
    }
  }

  // **************************************
  public void assertSafeFile(File f) {
    assertSafeFile(f.getAbsolutePath());
  }

  // **************************************
  public boolean createNewFile(String path) {
    return createNewFile(new File(path));
  }

  // **************************************
  public boolean createNewFile(File f) {
    try {
      boolean b = f.createNewFile();
      if (b) {
        print(String.format("- Created new file '%s' \n", f.getAbsolutePath()));
      }
      return b;
    } catch (Exception e) {
      print(e.getMessage());	    
      return false;
    }
  }


  // **************************************
  // do the actual deletion, CAREFUL !!!
  // I only allow deletion if the file is under /tmp/
  // if your files are under different folder
  // just change the flags here
  public boolean doDelete(File f) {


    assertSafeFile(f);

    // if not exist, we're fine, just return true
    if (!f.exists())
      return true;

    // do the actual deletion
    if (f.isDirectory()) {
      if (Driver.debug)
	print(String.format("   Deleting dir '%s' \n", f.getName()));
    }
    else {
      if (Driver.debug)
	print(String.format("   Deleting file '%s' \n", f.getName()));
    }
    return f.delete();
  }


  // **************************************
  // delete the file
  public boolean deleteFile(String path) {
    return doDelete(new File(path));
  }


  // **************************************
  public boolean deleteFile(File f) {
    return doDelete(f);
  }


  // **************************************
  public boolean deleteFile(String parent, String fname) {
    return doDelete(new File(parent, fname));
  }


  // **************************************
  // recursively delete the content of a directory AND this directory
  public boolean deleteDir(File dir) {


    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i=0; i<children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }
    // The directory is now empty so delete it
    return doDelete(dir);
  }


  // **************************************                                                               
  // recursively delete all the files only !!!                                                            
  // **************************************                                                               
  public boolean deleteDirFiles(String dirname) {
    File dir = new File(dirname);
    return deleteDirFiles(dir);
  }

  // **************************************                                                               
  public boolean deleteDirFiles(File dir) {

    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i=0; i< children.length; i++) {
        File f = new File(dir, children[i]);
        if (f.isFile())  doDelete(f);
        else             deleteDirFiles(f);
      }
    }
    return true;
  }


  // **************************************
  // just delete the content of the directory BUT NOT
  // the directory
  public boolean deleteDirContent(String dirname) {
    File dir = new File(dirname);
    return deleteDirContent(dir);
  }

  // **************************************
  public boolean deleteDirContent(File dir) {

    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i=0; i< children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }
    return true;
  }


  // **************************************
  // just delete the content of the directory THAT
  // CONTAINS the substring
  public boolean deleteDirContent(String dirname, String substring) {
    File dir = new File(dirname);
    return deleteDirContent(dir, substring);
  }

  // **************************************
  public boolean deleteDirContent(File dir, String substring) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      if (children == null)
	return true;
      for (int i=0; i< children.length; i++) {
	File f = new File(dir, children[i]);
	if (f.isFile()) {
	  if (children[i].contains(substring)) {
	    doDelete(f);
	  }
	}
	else if (f.isDirectory()) {
	  deleteDirContent(f, substring);
	}
      }
    }
    return true;
  }

  // **************************************
  public boolean exists(String filename) {
    File f = new File(filename);
    return exists(f);
  }

  public boolean exists(File f) {
    return f.exists();
  }

  // **************************************
  // check if the directory contains has any content
  // that CONTAINS the substring
  public boolean exists(String dirname, String substring) {
    File dir = new File(dirname);
    return exists(dir, substring);
  }

  public boolean exists(File dir, String substring) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      if (children == null)
	return false;
      for (int i=0; i< children.length; i++) {
	File f = new File(dir, children[i]);
	if (f.isFile() && children[i].contains(substring)) {
	  return true;
	}
      }
    }
    return false;
  }

  // **************************************
  public String fileContentToString(String path) {
    File f = new File(path);
    return fileContentToString(f);
  }


  // **************************************
  public String fileContentToString(File f) {
    String buf = "";

    if (!f.exists()) {
      ERROR("not exist " + f);
      return null;
    }

    // read the file
    try {
      BufferedReader in = new BufferedReader(new FileReader(f));
      String line;
      while ((line = in.readLine()) != null) {
        buf = buf + line + "\n";
      }
      in.close();
    } catch (Exception e) {
      EXCEPTION("fileContentToString " + f, e);
      return null;
    }
    return buf;
  }

  
  // **************************************
  public List<String> fileContentToList(String path) {
    return fileContentToList(new File(path));
  }


  // **************************************
  public List<String> fileContentToList(File f) {
    if (!f.exists()) {
      ERROR("Not exist " + f);
      return null;
    }

    List<String> content = new LinkedList<String>();

    try{
      BufferedReader in = new BufferedReader(new FileReader(f));
      String line;
      while ((line = in.readLine()) != null) {
        content.add(line);
      }
      in.close();
    }
    catch(Exception e){
      EXCEPTION("fileContentToList " + f, e);
      return null;
    }

    return content;
  }



  // **************************************
  public boolean stringToFileContent(String buf, String path) {
    return stringToFileContent(buf, new File(path), false, false);
  }


  // **************************************
  public boolean stringToFileContent(String buf, File f) {
    return stringToFileContent(buf, f, false, false);
  }


  // **************************************
  public boolean stringToFileContent(String buf, File f, boolean flush, boolean append) {
    assertSafeFile(f);

    try {
      FileWriter fw = null;
      if(append){
	fw = new FileWriter(f, true);
      }
      else{
	fw = new FileWriter(f);
      }
      fw.write(buf);
      if (flush)
        fw.flush();
      fw.close();
    } catch (IOException e) {
      EXCEPTION("stringToFileContent", e);
      ERROR("should not throw exception here");
      return false;
    }
    return true;
  }

  // ************************************** pallavi-14, all findAndCount functions
  public int findAndCountFile(String dname, String fname) {
    File dir = new File(dname);
    return findAndCountFile(dir, fname);
  }

  // **************************************
  public int findAndCountFile(File dir, String fname) {

    int cnt = 0;
    if (dir.isDirectory()) {

      String[] children = dir.list();

      if (children == null)
        return cnt;

      for (int i=0; i< children.length; i++) {

        if (fname.equals(children[i]))
          cnt++;

        File f = new File(dir, children[i]);
        if (f.isDirectory()) {
          cnt += findAndCountFile(f, fname);
        }
      }
    }
    return cnt;
  }

  // ############################################################
  // ##               ERROR MESSAGING UTILITY                  ##
  // ############################################################


  // **************************************
  public void pre() {
    print("\n");
    print("## ############################################\n");
  }


  // **************************************
  public void post() {
    print("## ############################################\n");
    print("\n");
  }


  // **************************************
  public void ERROR(String msg) {
    pre();
    print("## ERROR: " + msg + "\n");
    printStackTrace();
    post();
  }

  // **************************************
  public void FATAL(String msg) {
    pre();
    print("## FATAL: " + msg + "\n");
    printStackTrace();
    post();
    System.exit(-1);
  }


  // **************************************
  public void killAllJava() {
    print("should kill all java now\n");
  }


  // **************************************
  public void WARNING(String msg) {
    pre();
    print("## WARNING: " + msg + "\n");
    post();
  }


  // **************************************
  public void WARNING_ONELINE(String msg) {
    print("## WARNING: " + msg);
  }


  // **************************************
  public void EXCEPTION(String msg, Exception e) {
    pre();
    print("## EXCEPTION: " + msg + "\n");
    print("## ---------------------- Exception:\n");
    print(e.toString() + "\n");
    if (e.getCause() != null) {
      print("## ---------------------- Cause: \n");
      print(e.getCause().toString() + "\n");
    }
    print("## ---------------------- Trace: \n");
    printStackTrace(e);
    post();
  }


  // **************************************
  public void printStackTrace(Exception e) {
    if (e == null)
      print(getStackTrace());
    else
      print(stackTraceToString(e.getStackTrace()));
  }

  // **************************************
  public void printStackTrace() {
    printStackTrace(null);
  }

  // **************************************
  public String getStackTrace() {
    Thread t = Thread.currentThread();
    StackTraceElement[] ste = t.getStackTrace();
    return stackTraceToString(ste);
  }

  // **************************************
  public String stackTraceToString(StackTraceElement[] ste) {
    String str = "";
    for (int i = 0; i < ste.length ; i++) {
      str += String.format("    [%02d] %s \n", i, ste[i].toString());
    }
    return str;
  }

  // **************************************
  public void MESSAGE(String msg) {
    pre();
    print("## MESSAGE: " + msg);
    post();
  }

}
