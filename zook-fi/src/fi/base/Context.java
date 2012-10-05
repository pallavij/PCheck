
package org.fi;

import java.io.*;
import java.lang.*;


// this context must be pure and have no dependence at all

public class Context {
  
  private String targetIO = "";
  
  private int port = 0;
  
  private Object extraContext = null;
  
  private final String TMPFI = "/tmp/fi/";
  
  //********************************************
  // rest
  //********************************************

  public Context() {
    
  }

  public Context(String s) { 
    targetIO = new String(s);
  }

  public Context(int port) {
    this.port = port;
  }
  
  public void setTargetIO(String f) {
    targetIO = new String (f);
  }
  
  public String getTargetIO() {
    return targetIO;
  }
  
  public void setPort(int port) {
    this.port = port;
  }
  
  public int getPort() {
    return port;
  }

  public void setExtraContext(Object extra) {
    this.extraContext = extra;
  }
  
  public Object getExtraContext() {
    return extraContext;
  }
  
  public String toString() {
    String tmp = targetIO;
    return tmp;
  }
  
}

