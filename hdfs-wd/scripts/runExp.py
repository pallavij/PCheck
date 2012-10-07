#!/usr/bin/python
import os
import sys

import util
import genFailPtsToEx
import genEqvCls
import doPolicyOptim
import fInfo

#used printEqvClsMapHack instead of printEqvClsMap


def getAllMap(inclPostExpFIds, fIdsNotToBeIncl):
	os.system("cat " + util.allFPtsFile + " >> " + util.globAllFPtsFile)
	
	fIdsSeqToAllFIdsMap = {}
	allFIdsLines = util.readFromFile(util.globAllFPtsFile)

	psFIds = util.readFromFile(util.postSetupFIdsFile)

	for allFIdsLn in allFIdsLines:
		allFIdsLnParts = allFIdsLn.split(":")
		fIdsSeq = allFIdsLnParts[0]
		fIdsInExec = allFIdsLnParts[1]
		fIdsInExec = fIdsInExec.rstrip("\n")
		
		fIdsInExecParts = fIdsInExec.split(" ")
		#setOfFIdsInExec = set([])
		setOfFIdsInExec = []
		for fIdsInExecPart in fIdsInExecParts:
			isPSFId = (fIdsInExecPart in psFIds) 
			if(inclPostExpFIds or ((not inclPostExpFIds) and (not isPSFId))):
				if fIdsInExecPart not in fIdsNotToBeIncl:
					#setOfFIdsInExec.add(fIdsInExecPart)
					setOfFIdsInExec.append(fIdsInExecPart)

		fIdsSeqToAllFIdsMap[fIdsSeq] = setOfFIdsInExec

	return fIdsSeqToAllFIdsMap	


def getRHashesMap(p, fInfos, rIdOption):
	postSetupFIds = p.getPostSetupFIds()
	util.writeToFile(list(postSetupFIds), util.postSetupFIdsFile, True)

	rHashesMap = {}

	rHashStrs = set([])

	for info in fInfos:
		fId = info.getFId()
		rId = info.getRId(rIdOption)
		#rHashesMap[fId] = rId
		rHashStrs.add(str(fId)+":"+str(rId)+"\n")

	#write mappings to the file in the glob directory
	util.writeToFile(rHashStrs, util.globRHashesFile, True)

	#read all mappings from the file in the glob directory
	f = open(util.globRHashesFile, "r")
	mappings = f.readlines()
	for mapping in mappings:
		mapping = mapping.rstrip("\n")
		mappingParts = mapping.split(":")
		rHashesMap[mappingParts[0]] = mappingParts[1]
	f.close()

	return rHashesMap


#used in the case when fsn = 0
def createFPtsFiles():
	fIdsLine = ""
	posExps = []

	for fPtName in os.listdir(util.fPtsDirPath):
		fIdsLine += fPtName[1:-4]
		fIdsLine += " "
		posExps.append(fPtName[1:-4] + "\n")

	fIdsLine = fIdsLine.rstrip(" ")
	fIdsLine += "\n"
	fIdsLine = ":" + fIdsLine

	#util.writeToFile([fIdsLine], util.allFPtsFile, True)
	util.writeToFile(posExps, util.subqFPtsFile, True)


def writeInjectedFIds(dName, outFName):
	injFIds = set([])
	for subDName in os.listdir(dName):
		if subDName.find("wiped") == -1:
			subDPath = os.path.join(dName, subDName)
			if os.path.isdir(subDPath):
				for fName in os.listdir(subDPath):
					if fName.startswith("fsn"):
						fId = fName[6:-4]
						injFIds.add(fId+"\n")
	
	injFIdsInFile = []
	if os.path.exists(outFName):
		f = open(outFName, "r")
		injFIdsInFile = f.readlines()
		f.close()

	sOfInjFIdsInFile = set(injFIdsInFile)
	sInjFIdsToWr = injFIds - sOfInjFIdsInFile

	f = open(outFName, "a")
	f.writelines(list(sInjFIdsToWr))
	f.close()


def getFIdsNotToBeIncluded(fInfos, write, fName):
	fIdsToWr = []
	for fInfo in fInfos:
		if util.isRpcNoise(fInfo):
			fIdsToWr.append(fInfo.fId + "\n")
		if util.isNoiseForRFIds(fInfo):
			fIdsToWr.append(fInfo.fId + "\n")
		ioType = (fInfo.ioType).rstrip("\n")
		if ioType.find("WRITE") == -1:
			fIdsToWr.append(fInfo.fId + "\n")
		if fInfo.isBefore != True:
			fIdsToWr.append(fInfo.fId + "\n")
	fIdsToWr = list(set(fIdsToWr))		

	if write:
		f = open(fName, "w")
		f.writelines(fIdsToWr)
		f.close()

	fIdsNotToBeIncl = []
	for fId in fIdsToWr:
		fIdsNotToBeIncl.append(fId.strip("\n"))
	return fIdsNotToBeIncl	


def run(maxFsn):
	enableFailure = True
	if (maxFsn == 0):
		maxFsn = 1
		enableFailure = False
	
	homeDir = os.getenv('HOME')
	
	if homeDir.find("pallavi") != -1:
		util.setSSHKeys(homeDir)
	
	util.run(maxFsn, enableFailure)

	writeInjectedFIds(util.allExpsDir, util.injFIdsFile)
	#os.system("cat " + util.fIdDescStrsFile + " >> " + util.globFIdDescStrsFile)

	util.writeToGlobFIdDescStrsFile()
	util.writeFIdSeqsThatFailed()


def processLocalFiles(fsn, policies, useEqvCl, clsLast, rIdOption, inclPostFIds):
	#writeInjectedFIds(util.allExpsDir, util.injFIdsFile)
	util.createEqvClsInfoDir()

	if fsn == 0:
		createFPtsFiles()
	
	p = fInfo.Parser()
	stackDepth = util.stackDepth
	p.getFIdDescStrs(util.globFIdDescStrsFile, stackDepth)

	fInfos = p.getFInfos()

	fIdsNotToBeIncl = getFIdsNotToBeIncluded(fInfos, True, util.fIdsNotToBeInclFile)

	rHMap = getRHashesMap(p, fInfos, rIdOption)
	allMap = getAllMap(inclPostFIds, fIdsNotToBeIncl)
	gEqCls = genEqvCls.EqvCls(allMap, rHMap, fInfos)

	#short-cut to get the equivalence classes
	#gEqCls.shortCutForGettingEqvCls()

	doPOptim = doPolicyOptim.DoPolicyOptim(fInfos)

	gFPts = genFailPtsToEx.GenFailPtsToEx(gEqCls, doPOptim)
	gFPts.generateFailPtsToExplore(util.subqFPtsFile, util.injFIdsFile, fInfos, policies, useEqvCl, clsLast, util.globFPtsToConsiderFile + str(fsn+1))

	if(fsn != 0):
		#gEqCls.printEqvClsMapHack(util.globFPtsPath + "eqvCls" + str(fsn))		
		gEqCls.printEqvClsMap(util.globFPtsPath + "eqvCls" + str(fsn))		


def runExp(MAX, policies, useEqvCl, clsLast, rIdOption, inclPostFIds):
	homeDir = os.getenv('HOME')

	if (not os.path.exists(util.globFPtsPath)):
		os.makedirs(util.globFPtsPath)

	if (not os.path.exists(util.localFPtsPath)):
		os.makedirs(util.localFPtsPath)

	util.rmLocalFailPtsFiles()
	util.rmGlobalFailPtsFiles()

	for fsn in range(MAX + 1):
		run(fsn)

		processLocalFiles(fsn, policies, useEqvCl, clsLast, rIdOption, inclPostFIds)

		if(fsn != MAX):
			util.rmLocalFailPtsFiles()

	os.system('./scripts/killjava.py')


