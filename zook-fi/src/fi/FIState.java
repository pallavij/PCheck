
package org.fi;

import org.fi.FMServer.*;

import java.io.*;
import java.lang.*;


import java.util.Map;
import java.util.TreeMap;


public class FIState {

  public FMJoinPoint fjp;
  public FMContext ctx;
  public FMStackTrace fst;
  public FailType ft;
  
  String targetIoType   = "Unknown";
  String targetNode = "Unknown";
  String diskId   = "Unknown";
  String fileType = "Unknown";

  int completeHashId;
  String completeHashIdStr;

  int staticHashId;
  String staticHashIdStr;

  
  //********************************************
  // public FIState() { }

  //********************************************
  public FIState(FMAllContext fac, FailType ft) {
    this.fjp = fac.fjp;
    this.ctx = fac.ctx;
    this.fst = fac.fst;
    this.ft  = ft;

    setIoProperties(ctx);

    // then let's build the completeHashId
    generateCompleteHashId();
    generateStaticHashId();
  }


  //********************************************
  // here is how we convert the context into a file type.
  // For metadata file, we can just say the metadata file is
  // the file type. but for data file, there is too many,
  // so let's do this.
  // Must also include: disk number
  //********************************************
  public void setIoProperties(FMContext ctx) {
    
    String targetIO = ctx.getTargetIO();
    
    // net io
    if (Util.isNetIO(targetIO)) {
      targetIoType = "NetIO";
      targetNode = Util.getServerIdFromNetTargetIO(targetIO);
    }
    
    
    // disk io
    else if (Util.isDiskIO(targetIO)) {
      targetIoType = "DiskIO";
      //diskId = Util.getDiskIdFromTargetIO(targetIO);
    }

  }
  

  //********************************************
  // this is our policy of hash id of the state,
  // i.e the abstract representation of the state
  void generateCompleteHashId() {

    // IMPORTANT NOTE:
    // MAKE SURE the experiment is repeatable ... i.e.
    // if completeHashIdStr contains something undeterminism
    // (e.g. block generation stamp), then it's hard to repeat
    // the experiment!!

    // what is a state,
    // a state is a combination of:
    //   a. the node id (the least # of failures)
    //   b. file type (or file transfer to)
    //   c. the join point (important because in one line, many things can happen)
    //   d. join point 
    //   e. file type (rather than filename, because not good for datablocks)
    
      // all options
      completeHashIdStr = 
	"\n" + 
	ft.toString()     + "\n" + 
	"IoType: " + targetIoType            + "\n" +
	"DiskId: " + diskId            + "\n" +
	"FType: " + fileType            + "\n" +
	"NodeId: " + ctx.getNodeId()   + "\n" +
	"TgNode: " + targetNode        + "\n" +
        "Leader: " + ctx.getLeader()   + "\n" +	
        "Zxid: " + ctx.getZxid()   + "\n" +	
        "Epoch: " + ctx.getEpoch()   + "\n" +	
        "Vote Sending Context's Stack: " + ctx.getVctxStack()   + "\n" +	
	//"CountP: " + fjp.counterAsPort     + "\n" +
	fjp.toString()    + "\n" +
	fst.toString()    + "\n" +
	"\n";
      

    completeHashId = completeHashIdStr.hashCode();

  }

  //********************************************
  public int getCompleteHashId() {
    return completeHashId;
  }

  //********************************************
  public int getHashId() {
    return getCompleteHashId();
  }

  //********************************************
  public String getCompleteHashIdStr() {
    return completeHashIdStr;
  }
  
  //********************************************
  public String getHashIdStr() {
    return getCompleteHashIdStr();
  }

  //********************************************
  // this is just a metric for coverage, which is mostly
  // static (sourceloc, etc.) and dynamic (stack, ), excluding the fail type
  //********************************************
  private void generateStaticHashId() {
    
    staticHashIdStr = 
	"\n" + 
	ft.toString()     + "\n" + 
	"IoType: " + targetIoType            + "\n" +
	//"DiskId: " + diskId            + "\n" +
	//"FType: " + fileType            + "\n" +
	//"NodeId: " + ctx.getNodeId()   + "\n" +
	//"TgNode: " + targetNode        + "\n" +
        //"Leader: " + ctx.getLeader()   + "\n" +	
        //"Zxid: " + ctx.getZxid()   + "\n" +	
        //"Epoch: " + ctx.getEpoch()   + "\n" +	
        "Vote Sending Context's Stack: " + ctx.getVctxStack()   + "\n" +	
	//"CountP: " + fjp.counterAsPort     + "\n" +
	fjp.toString()    + "\n" +
	fst.toString()    + "\n" +
	"\n";
    staticHashId = staticHashIdStr.hashCode();
  }
  
  // ***************  
  public int getStaticHashId() {
    return staticHashId;
  }
  
  // ***************
  public String getStaticHashIdStr() {  
    return staticHashIdStr;
  }

  public String getDiskId() {
    return diskId;
  }
  
  public String getNodeId(){
        return ctx.getNodeId();
  }

  public String getTargetNode(){
          return targetNode;
  }

  public String getFileType(){
          return fileType;
  }

}
