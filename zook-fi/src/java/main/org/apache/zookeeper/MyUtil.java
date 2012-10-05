
package org.apache.zookeeper;


import java.lang.Thread;
import java.io.*;
import java.lang.*;
import java.lang.management.ManagementFactory;

// added by JASON for printing system Date/Time.
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;


public final class MyUtil {


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

    // only print if experiment is running
    File f = new File("/tmp/fi/expRunning");
    if (f.exists()) {
      msg = getTime() + " " + msg + " **POW**";
    }

    debug(pps, msg);
  }


  // **************************************
  public static void debug(PrintStream ps, String msg) {
    boolean debug = true;
    //boolean debug = false;
    if (debug) {
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
  public static void setPrintStream(String fname) {
    try{
	 FileOutputStream fos = new FileOutputStream(fname, true);
    	 PrintStream ps = new PrintStream(fos);
    	 setPrintStream(ps);
    }
    catch(FileNotFoundException e){
	 
    }
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
    System.out.flush(); // ??
    if (ps != null) {
      ps.print(msg);
    }


    else {
      String pid = getPid();
      String cmd = String.format("touch /tmp/fi/pids/" + pid);
      runCommand(cmd);
    }


  }

  // **************************************
  public static void println(PrintStream ps, String msg) {
    print(ps, msg + "\n");
  }

  // *******************************************
  public static int getIntPid() {
    String pidStr = getPid();
    return (new Integer(pidStr)).intValue();
  }

  // *******************************************
  public static String getPid() {
    String fullPid = ManagementFactory.getRuntimeMXBean().getName();
    String [] split = fullPid.split("@", 2);
    String pid = split[0];
    return pid;
  }


  // *****************************************
  // a tool to run the command
  public static String runCommand(String cmd) {

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
      System.out.println("runcmd!!!");
      System.err.println("runcmd!!!");
    }

    return msg;
  }


  // **************************************
  public static boolean stringToFileContent(String buf, String path) {
    return stringToFileContent(buf, new File(path), false, false);
  }

  
  // **************************************
  public static boolean stringToFileContent(String buf, String path, boolean append) {
    return stringToFileContent(buf, new File(path), false, append);
  }


  // **************************************
  public static boolean stringToFileContent(String buf, File f) {
    return stringToFileContent(buf, f, false, false);
  }


  // **************************************
  public static boolean stringToFileContent(String buf, File f, boolean flush, boolean append) {

    //assertSafeFile(f);

    try {
      
      FileWriter fw;	    
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

      return false;
    }
    return true;
  }

  // **************************************
  public static void assertSafeFile(File f) {
    assertSafeFile(f.getAbsolutePath());
  }

  // **************************************
  public static void assertSafeFile(String fullPath) {
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
      System.out.println("Dangerous!!!");
      System.err.println("Dangerous!!!");
      System.out.flush();
      System.exit(-1);
    }
  }


  // **************************************
  public static String getThreadName() {
    Thread t = Thread.currentThread();
    String name = t.getName();
    return name;
  }


  // *****************************************
  public static void sleep(int ms) {
    try {
      Thread.sleep(ms);
    } catch (Exception e) {}

  }

  // *****************************************
  // added by JASON for printing system 
  public static String getTime() {
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSSS");
	Date date = new Date();
	return dateFormat.format(date);
  }

}




