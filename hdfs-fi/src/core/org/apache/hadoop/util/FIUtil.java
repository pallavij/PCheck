/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.util;

import java.lang.Thread;
import java.io.PrintStream;
import java.lang.*;
import java.io.*;

import org.apache.hadoop.conf.Configuration;


public final class FIUtil {


  private static PrintStream pps;
  private static MyLong theLastTime = new MyLong(0);
  private static String lastWarning = "";

  // **********************************
  public static class MyLong {
    long l;
    public MyLong() { l = 0; }
    public MyLong(long x) { l = x; }
    public long longValue() { return l;    }
    public void setValue(long x) { l = x; }
  }

  // **********************************
  public static long now() {
    return System.currentTimeMillis();
  }

  // **********************************
  public static String diff() {
    return diff(theLastTime);
  }


  // **********************************
  public static void printDiffLongOnly(String msg, MyLong lastTime) {
    String d = diff(lastTime);
    if (lastWarning.contains("LONG")) {
      println(pps, msg + " " + d);
      // printStackTrace("pdl");
    }
  }

  // **********************************
  public static String diff(MyLong lastTime) {
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

      warning += ("\n" + getStackTrace());
    }

    if (diff > 30) {
      warning = "*LONG LONG LONG!!*";

      warning += ("\n" + getStackTrace());

    }

    lastWarning = warning;

    long ct = (currentTime / 1000) % 1000;

    long tid = (Thread.currentThread().getId()) % 100;

    return String.format("[hd-diff][%5.3f secs] [%d] (t-%d) %s",
                         diff, (int)ct, (int)tid, warning);

  }


  // **************************************
  public static void debug(String msg) {

    File f = new File("/tmp/fi/experimentRunning");
    if (f.exists()) {
      if (!msg.equals("\n") && !msg.equals("") && !msg.equals(" ")) {
        msg = msg + " (PartOfWorkload)";
      }
    }

    debug(pps, msg);
  }


  // **************************************
  public static void debug(PrintStream ps, String msg) {
    boolean debug = true;
    //boolean debug = false;


    boolean includeExpState = true;


    if (debug) {

      if (includeExpState) {
        String state = fileContentToString("/tmp/fi/flagsFailure/currentExpState");
        if (state == null) state = "null";
        else state = state.replaceAll("\n","");
        msg = ">> " + state + " " + msg;
      }

      println(ps, msg);
    }


  }


  // **************************************
  public static void printStackTrace(Exception e) {
    if (e == null)
      print(getStackTrace());
    else
      print(stackTraceToString(e.getStackTrace()));
  }

  // **************************************
  public static void printStackTrace() {
    printStackTrace(null);
  }

  // **************************************
  public static String getStackTrace() {
    Thread t = Thread.currentThread();
    StackTraceElement[] ste = t.getStackTrace();
    return stackTraceToString(ste);
  }

  // **************************************
  public static String stackTraceToString(StackTraceElement[] ste) {
    String str = "";
    for (int i = 0; i < ste.length ; i++) {
      str += String.format("    [%02d] %s \n", i, ste[i].toString());
    }
    return str;
  }


  // **************************************
  public static void setPrintStream(PrintStream ppsArg) {
    pps = ppsArg;
  }


  // **************************************
  public static void print(String msg) {
    print(pps, msg);
  }

  // **************************************
  public static void print(PrintStream ps, String msg) {
    System.out.print(msg);
    if (ps != null) {
      ps.print(msg);
    }
  }


  // **************************************
  public static void println(PrintStream ps, String msg) {
    print(ps, msg + "\n");
  }


  // **************************************
  public static void sleep(int ms) {
    try { Thread.sleep(ms); } catch (Exception e) { }
  }

  // **************************************
  public static boolean isExperimentRunning() {
    File tmp = new File("/tmp/fi/experimentRunning");
    return tmp.exists();
  }

  // **************************************
  public static String fileContentToString(String path) {
    return fileContentToString(new File(path));
  }

  // **************************************
  public static String fileContentToString(File f) {
    String buf = "";
    if (!f.exists()) {
      return null;
    }
    try {
      BufferedReader in = new BufferedReader(new FileReader(f));
      String line;
      while ((line = in.readLine()) != null) {
        buf = buf + line + "\n";
      }
      in.close();
    } catch (Exception e) {
      return null;
    }
    return buf;
  }



}