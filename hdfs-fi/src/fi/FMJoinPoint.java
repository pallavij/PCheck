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
import java.lang.*;

import org.aspectj.lang.Signature; // include this for Signature, etc!
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.SourceLocation;


import java.lang.Package;
import java.lang.Class;

// public class FMJoinPoint implements Writable {
public class FMJoinPoint {

  public static enum JoinPlc { BEFORE, AFTER, UNKNOWN; }
  public static enum JoinExc { IO, FNF, NONE; }      // throw exception?
  public static enum JoinRbl { YES, NO; }            // return boolean?
  public static enum JoinIot { READ, WRITE, NONE; }  // io type?


  // ******************************************************
  // variables that are passed to FMServer
  // ******************************************************
  private String joinPointStr = "";
  private String filename = "";
  private int line = 0;
  private String shortName = "";

  private JoinPlc jplc = JoinPlc.UNKNOWN;
  private JoinExc jexc = JoinExc.NONE;
  private JoinRbl jrbl = JoinRbl.NO;
  private JoinIot jiot = JoinIot.NONE;


  // ******************************************************
  // variables used by FMClient only (not passed to FMServer)
  // ******************************************************
  private ClassWC cwc = null;
  private Object jrov = null;// join point returned object value
  private JoinPoint jp = null;
  private IOException iox = null;
  private FileNotFoundException fnfx = null;


  //public int counterAsPort = 0;

  //public void setCounterAsPort(int x) {
  //  counterAsPort = x;
  //}


  //********************************************
  // writable interface
  // this is serialization (see DatanodeID.java for example)
  // need to decide how to serialize the RPC
  //********************************************
  public void write(DataOutput out) throws IOException {

    // xml rpc
    //out.writeInt(counterAsPort);
    out.writeUTF(joinPointStr);
    out.writeUTF(filename);
    out.writeInt(line);

    // hadoop rpc
    // UTF8.writeString(out, joinPointStr);
    // UTF8.writeString(out, filename);
    // out.writeInt(line);

    if      (jplc == JoinPlc.BEFORE) out.writeShort(1);
    else if (jplc == JoinPlc.AFTER)  out.writeShort(2);
    else                             out.writeShort(3);

    if      (jexc == JoinExc.IO)  out.writeShort(1);
    else if (jexc == JoinExc.FNF) out.writeShort(2);
    else                          out.writeShort(3);

    if      (jrbl == JoinRbl.YES) out.writeShort(1);
    else                          out.writeShort(2);

    if      (jiot == JoinIot.READ)  out.writeShort(1);
    else if (jiot == JoinIot.WRITE) out.writeShort(2);
    else                            out.writeShort(3);


  }

  public void readFields(DataInput in) throws IOException {

    // xml rpc
    //counterAsPort = in.readInt();
    joinPointStr = in.readUTF();
    filename = in.readUTF(); 
    line = in.readInt();

    // hadoop rpc
    // joinPointStr = UTF8.readString(in);
    // filename = UTF8.readString(in);
    // line = in.readInt();


    int tmp = in.readShort();
    switch(tmp) {
    case 1  : jplc = JoinPlc.BEFORE;  break;
    case 2  : jplc = JoinPlc.AFTER;   break;
    default : jplc = JoinPlc.UNKNOWN; break;
    }

    tmp = in.readShort();
    switch(tmp) {
    case 1  : jexc = JoinExc.IO;   break;
    case 2  : jexc = JoinExc.FNF;  break;
    default : jexc = JoinExc.NONE; break;
    }

    tmp = in.readShort();
    switch(tmp) {
    case 1  : jrbl = JoinRbl.YES; break;
    default : jrbl = JoinRbl.NO;  break;
    }

    tmp = in.readShort();
    switch(tmp) {
    case 1  : jiot = JoinIot.READ;  break;
    case 2  : jiot = JoinIot.WRITE; break;
    default : jiot = JoinIot.NONE;  break;
    }

  }


  // *************************
  public FMJoinPoint() { }


  // *************************
  public FMJoinPoint(JoinPoint jp, ClassWC cwc, Object jrov,
                     JoinPlc jplc, JoinIot jiot, JoinExc jexc, JoinRbl jrbl) {
    setJoinPoint(jp);
    setSourceLocation(jp);
    setJoinPlc(jplc);
    setJoinIot(jiot);
    setJoinExc(jexc);
    setJoinRbl(jrbl);
    setJoinRov(jrov);
    setClassWC(cwc);
  }

  // *************************
  public FMJoinPoint(JoinPoint jp) {
    setJoinPoint(jp);
    setSourceLocation(jp);
  }


  // *************************
  private void setJoinPoint(JoinPoint jp) {
    this.jp = jp;
    this.joinPointStr = jp.toString();  // toShortString() or toLongString()
  }

  // *************************
  private void setSourceLocation(JoinPoint jp) {
    // set up line number
    SourceLocation sl = jp.getSourceLocation();
    this.line = sl.getLine();

    // set up filename
    Class type = sl.getWithinType();
    Package pkg = type.getPackage();
    //String shortName = sl.getFileName();
    this.shortName = sl.getFileName();
    this.filename = String.format("%s/%s",
                                  pkg.getName().replace(".","/"),
                                  shortName);
    // pkg.getName(),
    // type.getName(),
  }

  // ************************* set
  public void setJoinPlc(JoinPlc jplc) { this.jplc = jplc; }
  public void setJoinExc(JoinExc jexc) { this.jexc = jexc; }
  public void setJoinRbl(JoinRbl jrbl) { this.jrbl = jrbl; }
  public void setJoinIot(JoinIot jiot) { this.jiot = jiot; }
  public void setJoinRov(Object  jrov) { this.jrov = jrov; }
  public void setClassWC(ClassWC cwc)  { this.cwc  = cwc;  }

  public void setIox(IOException iox)             { this.iox = iox; }
  public void setFnfx(FileNotFoundException fnfx) { this.fnfx = fnfx; }


  // ************************* get
  public JoinPlc   getJoinPlc()   { return jplc; }
  public JoinExc   getJoinExc()   { return jexc; }
  public JoinRbl   getJoinRbl()   { return jrbl; }
  public JoinIot   getJoinIot()   { return jiot; }
  public Object    getJoinRov()   { return jrov; }
  public ClassWC   getClassWC()   { return cwc; }
  public JoinPoint getJoinPoint() { return jp; }

  public String    getJoinPointStr() { return joinPointStr; } // for coverage
  public String    getFileName()     { return filename;     } // for coverage
  public String	   getShortName()    { return shortName;    }
  public int       getLine()         { return line;         } // for coverage
  public String    getSourceLoc()    { return filename + "(" + line + ")"; } // for cov




  public int getSourceLocHash() { return getSourceLoc().hashCode(); }

  public int getSlJpHash() {  return (getSourceLoc() + joinPointStr).hashCode(); }

  public IOException            getIox() { return iox; }
  public FileNotFoundException getFnfx() { return fnfx; }


  // *************************
  public void setAfter(Object obj) {
    setJoinPlc(JoinPlc.AFTER);
    setJoinRov(obj);
  }

  // *************************
  // for filtering
  public boolean contains(String s) {
    if (joinPointStr.contains(s))
      return true;
    return false;
  }

  // *************************
  public String toString() {
    String buf = "";
    buf += String.format("  %s\n", joinPointStr);
    buf += String.format("   ** PL:%s / EX:%s / BOOL:%s / IO:%s **\n",
                         jplc.toString(), jexc.toString(),
                         jrbl.toString(), jiot.toString());
    buf += String.format("    SourceLoc: %s(%d)\n", filename, line);
    return buf;
  }




}
