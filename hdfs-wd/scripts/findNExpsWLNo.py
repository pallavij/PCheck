#!/usr/bin/python
import os
import sys

import util
import fInfo
import runExp

stackDepth = 3
inclPostExpFIds = False

p = fInfo.Parser()
p.getFIdDescStrs("/tmp/fi/glob/failPts/fIdStrs", stackDepth)
fInfos = p.getFInfos()
fIdTofInfoMap = util.getFIdToInfoMap(fInfos) 


allFIdsLines = util.readFromFile("coverageInfoAllFsns")
psFIds = util.readFromFile(util.postSetupFIdsFile)

fIdsNotToBeIncl = runExp.getFIdsNotToBeIncluded(fInfos, False, None)

countUpBlk = 0
countAbBlk = 0
countRecBlk = 0
countExps = 0
numUpBlks = 0

for allFIdsLn in allFIdsLines:
	allFIdsLnParts = allFIdsLn.split(":")
	fIdsInj = allFIdsLnParts[0]
	fstFIdInj = (fIdsInj.split(" "))[0]
	fstFIdInj = fstFIdInj.rstrip("\n")
	fstFInf = None 
	if fstFIdInj != "":
		fstFInf = fIdTofInfoMap[fstFIdInj]
	if fstFInf != None and (runExp.isRpcNoise(fstFInf) or (fstFIdInj in util.getFIdSeqsThatFailed())):
		continue

        fIdsInExec = allFIdsLnParts[1]
	fIdsInExec = fIdsInExec.rstrip("\n")
		
	fIdsInExecParts = fIdsInExec.split(" ")

	hasUpBlk = False
	hasAbBlk = False
	hasRecBlk = False
	for fIdsInExecPart in fIdsInExecParts:
		isPSFId = (fIdsInExecPart in psFIds) 
		#if(inclPostExpFIds or ((not inclPostExpFIds) and (not isPSFId))):
		#	if fIdsInExecPart not in fIdsNotToBeIncl:
		fInf = fIdTofInfoMap[fIdsInExecPart]
		rpcContext = fInf.rpcContext
		jpStr = fInf.jpStr
		srcLoc = fInf.fjpSrcLoc
		st = fInf.fst	
	
		if rpcContext.find("Protocol.updateBlock") != -1:
			hasUpBlk = True
		if rpcContext.find("Protocol.abandonBlock") != -1:
			hasAbBlk = True
		if rpcContext.find("Protocol.startBlockRecovery") != -1:
			hasRecBlk = True
		if ((jpStr.find("WritableByteChannel.write") != -1) and (srcLoc.find("net/SocketOutputStream.java") != -1) and (st.find("ipc.Client") != -1) and (rpcContext.find("Protocol.updateBlock") != -1)): 
			numUpBlks = numUpBlks + 1
	if hasUpBlk:
		countUpBlk = countUpBlk + 1
	if hasAbBlk:
		countAbBlk = countAbBlk + 1
	if hasRecBlk:
		countRecBlk = countRecBlk + 1
	if (not hasUpBlk) and (not hasAbBlk):
		print fIdsInj 

	countExps = countExps + 1	

print "No of experiments with updateBlock = " + str(countUpBlk)				
print "No of updateBlock fIds = " + str(numUpBlks)				
print "No of experiments with abandonBlock = " + str(countAbBlk)				
print "No of experiments with recoverBlock = " + str(countRecBlk)				
print "No of experiments considered = " + str(countExps)				
