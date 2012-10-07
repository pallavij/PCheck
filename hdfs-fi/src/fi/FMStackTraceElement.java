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


// public class FMStackTraceElement implements Writable {
public class FMStackTraceElement {
  
  private String fileName;
  private String className;
  private String methodName;
  private int lineNumber;
  private int hashCode;
  

  //********************************************
  // writable interface
  // this is serialization (see DatanodeID.java for example)
  // need to decide how to serialize the RPC
  //********************************************
  public void write(DataOutput out) throws IOException {

    // xml rpc
    out.writeUTF(fileName);
    out.writeUTF(className); 
    out.writeUTF(methodName);
    out.writeInt(lineNumber);
    out.writeInt(hashCode);

    // hadoop rpc
    // UTF8.writeString(out, fileName);
    // UTF8.writeString(out, className);
    // UTF8.writeString(out, methodName);
    // out.writeInt(lineNumber);
    // out.writeInt(hashCode);

  }
  
  public void readFields(DataInput in) throws IOException {
    
    // xml rpc
    fileName = in.readUTF();
    className = in.readUTF();
    methodName = in.readUTF(); 
    lineNumber = in.readInt();
    hashCode = in.readInt();

    // hadoop rpc
    // fileName = UTF8.readString(in);
    // className = UTF8.readString(in);
    // methodName = UTF8.readString(in);
    // lineNumber = in.readInt();
    // hashCode = in.readInt();
    
  }

  
  
  //********************************************
  // rest
  //********************************************

  public FMStackTraceElement() {
    
  }
  
  public FMStackTraceElement(StackTraceElement s) { 
    fileName = s.getFileName();
    className = s.getClassName();
    methodName = s.getMethodName();
    lineNumber = s.getLineNumber();
    hashCode = s.hashCode();
  }
  
  public String getFileName() {
    return fileName;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public int getLineNumber() {
    return lineNumber;
  }
  
  public int getHashCode() {
    return hashCode;
  }

  
}