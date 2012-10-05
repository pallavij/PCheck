


package org.fi;

import org.fi.*;
import org.fi.FMServer.FailType;
import org.fi.FMJoinPoint.*;

import java.io.*;
import java.util.*;


public class FMFilter {

  public FMFilter() {}

  public static int naiveC = 0;
  public static int optC = 0;
  public static int naiveD = 0;
  public static int optD = 0;
  public static int naiveN = 0;
  public static int optN = 0;
  public static int naiveCorrupt = 0;
  public static int optCorrupt = 0;


  // *************************************************
  // This is the part where "extensible. Given information
  // in fac, and ft.  You can specify which failure that you
  // want to exercise
  // *************************************************
  public static boolean passServerFilter(FMAllContext fac, FailType ft, FIState fis) {

    boolean passFilter = false;
    FMJoinPoint fjp = fac.fjp;

    String jpStr = fjp.getJoinPointStr();
    if (!jpStr.contains(" java") && !jpStr.contains("(java")) return false;
    
    
    //pallavi, enable/disable this to get the unoptimized number

    //if (ft == FailType.CRASH)  passFilter = true;
    //if (ft == FailType.BADDISK) passFilter = true;
    //if (ft == FailType.NETFAILURE) passFilter = true;
    //if (ft == FailType.CORRUPTION) passFilter = true;
   
    //passFilter = filterCrash(fac,ft,fis);
    //passFilter = filterNetFail(fac,ft,fis);
 
    boolean netfail = (ft == FailType.NETFAILURE);
    boolean readIO = (fjp.getJoinIot() == JoinIot.READ);
    return (netfail && readIO);
    //return netfail;


  }


  private static boolean filterCrash(FMAllContext fac,
                                             FailType ft, FIState fis) {

    FMJoinPoint fjp = fac.fjp;
    FMContext ctx = fac.ctx;
    String nodeId = ctx.getNodeId();
    boolean crashFailure = (ft == FailType.CRASH);
    boolean writeIO = (fjp.getJoinIot() == JoinIot.WRITE);
    boolean isNetIO = Util.isNetIO(ctx.getTargetIO());
    boolean failBefore = (fjp.getJoinPlc() == JoinPlc.BEFORE);

    if (crashFailure
                    //&& failBefore && writeIO
                    //&& (!isNetIO || (isNetIO && !isNetworkSendToCrashedNode(fac, ft, fis)))
                    ) {

           return true;
    }
    return false;

  }

   /******
   private static boolean isNetworkSendToCrashedNode(FMAllContext fac,
                                                    FailType ft, FIState fis){
    FMJoinPoint fjp = fac.fjp;
    FMContext ctx = fac.ctx;
    boolean crashFailure = (ft == FailType.CRASH);
    boolean isNetIO = Util.isNetIO(ctx.getTargetIO());
    boolean writeIO = (fjp.getJoinIot() == JoinIot.WRITE);

    if (crashFailure && isNetIO && writeIO){
      String targetNode = "Unknown";
      if (fis != null) {
        targetNode = fis.getTargetNode();
      }
      Set<String> crashedNodes = FMLogic.crashedNodes;
      if (crashedNodes.contains(targetNode)){
        Util.MESSAGE("yes...network message to crashed node " + targetNode);
        return true;
      }
    }
    return false;
  }
  *****/

  // ********************************
  private static boolean filterNetFail(FMAllContext fac, FailType ft, FIState fis) {

    FMJoinPoint fjp = fac.fjp;
    FMContext ctx = fac.ctx;

    boolean isNetFail = (ft == FailType.NETFAILURE);
    boolean readIO = (fjp.getJoinIot() == JoinIot.READ);
    boolean writeIO = (fjp.getJoinIot() == JoinIot.WRITE);

    String node = fis.getNodeId();
    String tnode = fis.getTargetNode();

    boolean tNodeAlreadyReadFrom = false;

    Set<String> tNodesReadingFrom = (Set<String>)FMLogic.netReads.get(node);
    if((tNodesReadingFrom != null) && tNodesReadingFrom.contains(tnode)){
      tNodeAlreadyReadFrom = true;
    }


    //if((isNetFail && writeIO) || (isNetFail && readIO && !tNodeAlreadyReadFrom)){
    if((isNetFail && writeIO) || (isNetFail && readIO)){
      return true;
    }
    return false;

  }

    public static void filterCrash(FMAllContext fac){
        FIState fis = new FIState(fac, FailType.CRASH);
        //String nodeId = fis.getNodeId();
        //boolean inDataNode = nodeId.contains("DataNode");

        //if(!inDataNode) return;

        //unoptimized case
        Util.MESSAGE("In filterCrash...#crash(naive) = " + (++naiveC));

        //optimized case
        boolean failBefore = (fac.fjp.getJoinPlc() == JoinPlc.BEFORE);
        boolean writeIO = (fac.fjp.getJoinIot() == JoinIot.WRITE);

        if(failBefore && writeIO)
                Util.MESSAGE("In filterCrash...#crash(opt) = " + (++optC));
  }

      // ********************************
  public static void filterCorruption(FMAllContext fac) {

    FIState fis = new FIState(fac, FailType.CORRUPTION);
    //String nodeId = fis.getNodeId();
    //boolean inDataNode = nodeId.contains("DataNode");

    //if (!inDataNode)
    //        return;

    //unoptimized case
    Util.MESSAGE("In filterCorruption...#corrupt(naive) = " + (++naiveCorrupt));


    String fType = fis.getFileType();
    boolean isFileAlreadyCorrupted = FMLogic.corruptedFiles.contains(fType);

    if(!isFileAlreadyCorrupted){
        //optimized case
        FMLogic.recordCorruptedFile(fac,FailType.CORRUPTION,fis);
        Util.MESSAGE("In filterCorruption...#corrupt(opt) = " + (++optCorrupt));
    }


  }

  public static void filterNetFail(FMAllContext fac){
        FIState fis = new FIState(fac, FailType.NETFAILURE);
        String nodeId = fis.getNodeId();
        String tnode = fis.getTargetNode();
        boolean inDataNode = nodeId.contains("DataNode");
        boolean isNetIO = Util.isNetIO(fac.ctx.getTargetIO());

        //if(!isNetIO) return;
        //if(!inDataNode) return;

        //unoptimized case
        Util.MESSAGE("In filterNetFail...#netfail(naive) = " + (++naiveN));

        //optimized case
        boolean writeIO = (fac.fjp.getJoinIot() == JoinIot.WRITE);
        boolean tNodeAlreadyReadFrom = false;
        Set<String> tNodesReadingFrom = (Set<String>)FMLogic.netReads.get(nodeId);
        if((tNodesReadingFrom != null) && tNodesReadingFrom.contains(tnode)){
                tNodeAlreadyReadFrom = true;
        }

        if(writeIO || (!writeIO && !tNodeAlreadyReadFrom)){
                Util.MESSAGE("In filterNetFail...#netfail(opt) = " + (++optN));
                FMLogic.recordReadFromBadNode(fac, FailType.NETFAILURE, fis);
        }
  }


    public static void filterDiskFail(FMAllContext fac, FailType ft){
        FIState fis = new FIState(fac, ft);
        //String nodeId = fis.getNodeId();
        //boolean inDataNode = nodeId.contains("NameNode");
        //boolean isDiskIO = Util.isDiskIO(fac.ctx.getTargetIO());

        //if(!isDiskIO) return;
        //if(!inDataNode) return;

        //unoptimized case
        Util.MESSAGE("In filterDiskFail...#diskfail(naive) = " + (++naiveD));

        //optimized case
        boolean writeIO = (fac.fjp.getJoinIot() == JoinIot.WRITE);
        String fType = fis.getFileType();
        boolean isFileAlreadyOnFailedDisk = FMLogic.filesOnFailedDisks.contains(fType);


        if(writeIO || (!writeIO && !isFileAlreadyOnFailedDisk)){
                Util.MESSAGE("In filterDiskFail...#diskfail(opt) = " + (++optD));
                FMLogic.recordFileOnFailedDisk(fac,ft,fis);
        }
  }

   


}


