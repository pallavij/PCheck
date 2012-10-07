#!/usr/bin/python
import os
import sys
import shlex, subprocess
import inspect

relHdfsPath = "../hdfs-trunk/"
localFPtsPath = "/tmp/fi/failPts/"
globFPtsPath = "/tmp/fi/glob/failPts/"
fPtsDirPath = "/tmp/fi/coverageComplete"
allFPtsFile = localFPtsPath + "coverageInfoAllFsns"
subqFPtsFile = localFPtsPath + "coverageInfoSubqFsns"
rHashesFile = localFPtsPath + "rHashIds"
globAllFPtsFile = globFPtsPath + "coverageInfoAllFsns"
globRHashesFile = globFPtsPath + "rHashIds"
globFPtsToConsiderFile = globFPtsPath + "fPtsToConsider"

subqRFPtsFile = localFPtsPath + "coverageInfoRHashesSubqFsns"
rHashesInjFile = localFPtsPath + "rHashIdsInj"

fIdDescStrsFile = localFPtsPath + "fIdStrs"
globFIdDescStrsFile = globFPtsPath + "fIdStrs"

postSetupFIdsFile = globFPtsPath + "postSetupFIds"

allExpsDir = "/tmp/fi/expResult/"
injFIdsFile = globFPtsPath + "injFIds"

fIdsNotToBeInclFile = globFPtsPath + "fIdsNotToBeIncl"
failedExpsFile = localFPtsPath + "failedFIdSeqs"

stackDepth = 3

#set ssh keys so that one can ssh to localhost w/o requiring a password
def setSSHKeys (homeDir):
	#os.system('chmod 700 ' + homeDir + '/.ssh')
	#os.system('touch ' + homeDir + '/.ssh/authorized_keys')
	#os.system('chmod 600 ' + homeDir + '/.ssh/authorized_keys')
	os.system('rm ' + homeDir + '/.ssh/id_dsa')
	os.system('rm ' + homeDir + '/.ssh/authorized_keys')
	os.system("ssh-keygen -t dsa -P '' -f " + homeDir + "/.ssh/id_dsa")
	os.system("cat " + homeDir + "/.ssh/id_dsa.pub >> " + homeDir + "/.ssh/authorized_keys")

#write down the names of all files (each file containing info about a failure point) in a directory
def writeFailPtsToFile (outFile):
	outF = open(outFile, 'w')
	for fPtName in os.listdir(fPtsDirPath):
		outF.write(fPtName[1:-4]+'\n')
	outF.close()

def rmLocalFailPtsFiles():
	os.system('rm -rf ' + localFPtsPath + '*')

def rmGlobalFailPtsFiles():
	os.system('rm -rf ' + globFPtsPath + '*')

def run(maxFsn, enableFailureExp):
	print "maxFsn = " + str(maxFsn)
	os.system('make kill')
	
	#args = shlex.split("ant -DMAX_FSN=' + str(maxFsn) + ' ' + '-DenableFailure=' + str(enableFailureExp)  + ' -DenableCoverage=true")
	args = ['ant', '-DMAX_FSN='+str(maxFsn), '-DenableFailure='+str(enableFailureExp), '-DenableCoverage=true']
	p = subprocess.Popen(args)
	p.wait()
	
	os.system('make kill')

def writeToFile(content, fName, append):	
	prevContent = []
	if append and os.path.exists(fName): 
		f = open(fName, "r")
		prevContent = f.readlines()
		f.close()

	if append:
		f = open(fName, "a")	
		for line in content:
			line = line.rstrip("\n")
			if ((line+"\n") not in prevContent):
				f.write(line+"\n")
		f.close()	
	else:
		f = open(fName, "w")
		for line in content:
			line = line.rstrip("\n")
			f.write(line+"\n")
		f.close()

def readFromFile(fName):
	if os.path.exists(fName):
		f = open(fName, "r")
		lines = f.readlines()
		f.close()
	
		fContent = []
		for line in lines:
			fContent.append(line.rstrip("\n"))
		return fContent	

	else:
		return []

def isExpPresent(exp, exps):
	return ((exp in exps) or ((exp.strip("\n")+"\n") in exps))

def isFilterPolicy(policy):
	for name, data in inspect.getmembers(policy, inspect.ismethod):
		if name.find("filter") != -1:
			return True
	return False	


def isCoveragePolicy(policy):	
	for name, data in inspect.getmembers(policy, inspect.ismethod):
		if name.find("coverageId") != -1:
			return True
	return False	


def getCovId(fId, fIdToCovIdMap, policy, fIdToInfoMap):				
	covId = None
	if fId in fIdToCovIdMap.keys():
		covId = fIdToCovIdMap[fId]
	else:
		covId = policy.coverageId(fIdToInfoMap[fId])
		fIdToCovIdMap[fId] = covId
	return covId		


def getFIdToInfoMap(fInfos):
	fIdToInfoMap = {}

	for fInfo in fInfos:
		fId = fInfo.getFId()
		fIdToInfoMap[fId] = fInfo

	return fIdToInfoMap

def printFIdInfo(file, fId, fInfos):	
	fIdToFInfosMap = getFIdToInfoMap(fInfos)
	fInf = fIdToFInfosMap[fId]
	file.write("ECONTEXT="+fInf.eContext+"\n")
	file.write("RPCCONTEXT="+fInf.rpcContext+"\n")
	file.write("FT="+fInf.ft+"\n")
	file.write("DISKID="+fInf.diskId+"\n")
	file.write("NODEID="+fInf.nodeId+"\n")
	file.write("TIOTYPE="+fInf.tIoType+"\n")
	file.write("FTYPE="+fInf.fType+"\n")
	file.write("TNODEID="+fInf.tNodeId+"\n")
	file.write("FJP="+fInf.fjp+"\n")
	file.write("FST="+fInf.fst+"\n")

def createEqvClsInfoDir():
	os.system('mkdir ' + globFPtsPath + "eqvClsInfo")

def isRpcNoise(fInfo):
	jpStr = fInfo.jpStr
	srcLoc = fInfo.fjpSrcLoc
	fst = fInfo.fst
	rpcContext = fInfo.rpcContext

	#if ((jpStr.find("DataOutputStream.flush") != -1) and (srcLoc.find("ipc/Client.java") != -1) 
	#	and (fst.find("writeRpcHeader") != -1)):
	#	return True
	if rpcContext.find("blockReceived") != -1:
		return True
	if fst.find("ProtocolProxy") != -1:
		return True
	if ((jpStr.find("DataOutputStream.flush") != -1) and (srcLoc.find("ipc/Client.java") != -1)):
		return True
	if rpcContext.find("ClientProtocol.renewLease") != -1:
		return True
	return False

def isNoiseForRFIds(fInfo):
	jpStr = fInfo.jpStr
	srcLoc = fInfo.fjpSrcLoc
	nodeId = fInfo.nodeId
	tNodeId = fInfo.tNodeId
	fst = fInfo.fst

	if ((jpStr.find("WritableByteChannel.write") != -1) and 
			(srcLoc.find("ipc/Server.java") != -1) and 
			((tNodeId.find("Unknown") != -1) or (nodeId.find("Unknown") != -1))):
		return True
	if fst.find("ipc.") == -1: 
		fInfo.setRpcCtx("Unknown")
	if srcLoc.find("ipc/Server.java") != -1:
		fInfo.setRpcCtx("Unknown")
	if ((jpStr.find("WritableByteChannel.write") != -1) and 
			(srcLoc.find("hadoop/net/SocketOutputStream.java") != -1) and 
			(fst.find("ipc.") != -1)):
				fInfo.setFst("\n")
				fInfo.setClippedFst("\n")

	return False


def writeToGlobFIdDescStrsFile():
        localLines = []
        globLines = []
        linesToWr = []

        if os.path.exists(fIdDescStrsFile):
                f = open(fIdDescStrsFile, "r")
                localLines = f.readlines()
                f.close()

        if os.path.exists(globFIdDescStrsFile):
                f = open(globFIdDescStrsFile, "r")
                globLines = f.readlines()
                f.close()

        write = False
        for line in localLines:
                if line.startswith("FID:"):
                        if write:
                                write = False
                        if line not in globLines:
                                write = True
                                linesToWr.append(line)
                else:
                        if write:
                                linesToWr.append(line)
        if write:
                write = False

        f = open(globFIdDescStrsFile, "a")
        f.writelines(linesToWr)
        f.close()


def writeFIdSeqsThatFailed():
	failedFsnSeqs = []
	for expPath in os.listdir(allExpsDir):
		if expPath.find('wiped') == -1:
			isFailedExp = False
			fsns = []
			for expSubPath in os.listdir(allExpsDir + '/' + expPath):
				if expSubPath.find('FAIL') != -1:
					isFailedExp = True
				if expSubPath.startswith('fsn'):
					fsns.append(expSubPath)
			if isFailedExp:
				fsns = sorted(fsns)
				fsnsStr = ""
				for fsn in fsns:
					fsnH = (fsn.split("h"))[1]
					fsnsStr = fsnsStr + fsnH[0:-4]
					fsnsStr = fsnsStr + " "
				fsnsStr = fsnsStr[:-1]
				failedFsnSeqs.append(fsnsStr)

	f = open(failedExpsFile, "w")
	for seq in failedFsnSeqs:
		f.write(seq + "\n")
	f.close()

