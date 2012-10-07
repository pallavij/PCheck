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


// A NodeProcess is built from the tmp-pid files
public class NodeProcess {

  private String name;   // the node's name (e.g. namenode, pdatanode-1, fi)
  private String pid;    // the pid
  private String tmpPidFile; // the tmp-pid file name
  private boolean isDn;  // is this a datanode?
  private String dnId;   // the datanode Id if applicable
  private boolean isNn;
  private boolean isClient;
  private boolean isFI;

  public NodeProcess(String tmpPidFile, String pid) {

    this.pid = pid;
    this.tmpPidFile = tmpPidFile;
    
    // now let's create the name
    // remove the hadoop prefix and .pid 
    String tmp = tmpPidFile.replaceAll(Driver.HADOOP_USERNAME + "-", "");
    tmp = tmp.replaceAll(".pid", "");
    name = tmp;
    
    // now let's see if it's a datanode,
    // if so, cut the filename so we get the dnId

    isNn = false;
    isDn = false;
    isClient = false;
    isFI = false;
    dnId = "-X";

    if (name.contains("datanode")) {
	  isDn = true;
	  dnId = name.replaceAll("pdatanode", "");
    }
    else if (name.contains("namenode") && !name.contains("secondary")) {
          isNn = true;
    }
    else if (name.contains("client")) {
	  isClient = true;
    }
    else if (name.contains("fi")){
	  isFI = true;
    }
			       
  }
  
  public String toString() {
    return String.format("%-5d  %-12s  %2s  \n", pid, name, dnId);
  }

  public String getName() {
    return name;
  }

  public String getPid() {
    return pid;
  }

  public String getTmpPidFile() {
    return tmpPidFile;
  }

  public String getDnId() {
    return dnId;
  }
  
  public boolean isDataNode() {
    return isDn;
  }

  public boolean isNameNode() { return isNn; }
  public boolean isClient() { return isClient; }

  public boolean isFI() {return isFI;}

}
