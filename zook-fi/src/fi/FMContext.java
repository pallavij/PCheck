
package org.fi;


// ************************************ java
import java.io.*;
import java.lang.*;
import java.lang.management.ManagementFactory;


public class FMContext {

  // to test memory, if there is any leaakge, we'll hit an exception
  // private char [] big = new char [1024*1024];

  private int cutpointRandomId = 0;
  private String targetIO = "";
  private String nodeId = "Node-Unknown";
  private int pid = 0;

  //PALLAVI:Sending of vote specific fields
  private long leader = 0;
  private long zxid = 0;
  private long epoch = 0;
  private String vctxStack = "";

  //********************************************
  // writable interface
  // this is serialization (see DatanodeID.java for example)
  // need to decide how to serialize the RPC
  //********************************************
  public void write(DataOutput out) throws IOException {

    // xml rpc
    out.writeInt(cutpointRandomId);
    out.writeUTF(targetIO);
    out.writeUTF(nodeId);
    out.writeInt(pid);

    //PALLAVI: vote sending context
    out.writeLong(leader);	
    out.writeLong(zxid);	
    out.writeLong(epoch);	
    out.writeUTF(vctxStack);

  }

  public void readFields(DataInput in) throws IOException {

    // xml rpc
    cutpointRandomId = in.readInt();
    targetIO = in.readUTF();
    nodeId = in.readUTF();
    pid = in.readInt();

    //PALLAVI: vote sending context
    leader = in.readLong();
    zxid = in.readLong();
    epoch = in.readLong();
    vctxStack = in.readUTF();
  }

  public FMContext() {
    setAttributes();
  }

  public FMContext(String s) {
    targetIO = new String(s);
    setAttributes();
  }

  //PALLAVI: for setting VoteContext
  public FMContext(String s, VoteContext vctx){
    targetIO = new String(s);
    setAttributes();

    if(vctx != null){
	leader = vctx.getLeader();
	zxid = vctx.getZxid();
	epoch = vctx.getEpoch();
	vctxStack = vctx.getStack(); 
    }
  }

  private void setAttributes() {
    setPidAndNodeId();
  }

  public static String computeNodeId(){
    String pidStr = Util.getPid();
    int sid = Util.getZkIdFromPid(pidStr);
    if(sid != 0){
	return "Node-"+sid;	
    }
    return "Node-Unknown";    
  }

  public static int nodeIntId(){
    String pidStr = Util.getPid();
    int sid = Util.getZkIdFromPid(pidStr);
    return sid;
  }

  public void setPidAndNodeId() {
    nodeId = computeNodeId();
  }

  public void setTargetIO(String f) {
    targetIO = new String (f);
  }
  
  public String getTargetIO() {
    return targetIO;
  }
  
  public int getPid() {
    return pid;
  }

  public String getNodeId() {
    return nodeId;
  }

  public long getLeader() {
    return leader;
  }
  
  public long getZxid() {
    return zxid;
  }
  
  public long getEpoch() {
    return epoch;
  }
  
  public String getVctxStack() {
    return vctxStack;
  }
  
  public void setCutpointRandomId() {
    cutpointRandomId = Util.r8();
  }

  public int getCutpointRandomId() {
    return cutpointRandomId;
  }


  public String toString() {
    String buf = String.format("  [targetIO][%s] at *%s* \n", targetIO, nodeId);
    
    StringBuilder sb = new StringBuilder(buf);
    if(vctxStack != ""){
	sb.append(String.format("  [leader = %d][zxid = %d][epoch = %d]\n", leader, zxid, epoch));
	sb.append(String.format("  [vote sending context = \n %s", vctxStack));
	sb.append("  ]\n");
	buf = sb.toString();
    }
    
    return buf;
  }


  private String getTargetIOPrint(String f) {
    String forPrint = "  [targetIO][";
    int width = 58;
    int i;
    for (i = 0; i+width < f.length() ; i += width) {
      forPrint += f.substring(i, i+width);
      forPrint += "\n     ";
    }
    forPrint += f.substring(i, f.length());
    forPrint += "]";
    return forPrint;
  }

}
