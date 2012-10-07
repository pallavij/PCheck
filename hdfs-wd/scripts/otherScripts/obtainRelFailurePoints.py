#!/usr/bin/python
import os
import sys
import re

fPtsDirPath = "/tmp/fi/coverageComplete"

#set ssh keys so that one can ssh to localhost w/o requiring a password
def setSSHKeys (homeDir):
	os.system('rm ' + homeDir + '/.ssh/id_dsa')
	os.system('rm ' + homeDir + '/.ssh/authorized_keys')
	os.system("ssh-keygen -t dsa -P '' -f " + homeDir + "/.ssh/id_dsa")
	os.system("cat " + homeDir + "/.ssh/id_dsa.pub >> " + homeDir + "/.ssh/authorized_keys")

#write down the names of all files (each file containing info about a failure point) in a directory
def writeFailPtsToFile (fileName):
	f = open(fileName, 'w')
	for failPt in os.listdir(fPtsDirPath):
		f.write(failPt+'\n')
	f.close()

#find which failure points are available only as a result of the injection of the last injected failure
def findHashesOfRelFailPts (fileName1, fileName2):
	f1 = open(fileName1, 'r')
	f2 = open(fileName2, 'r')
	fPts1 = f1.readlines()
	fPts2 = f2.readlines()
	fPtsDiff = list(set(fPts2) - set(fPts1))
	f1.close()
	f2.close()
	return fPtsDiff

#print the hash of each failure point to a file
def printHashesToFile (fPtsHashes):
	finalLines = []
	append = False
	for fPtHash in fPtsHashes:
		fPtHash = fPtHash.rstrip('\n')
		fPtPath = os.path.join(fPtsDirPath, fPtHash)
		f = open(fPtPath, 'r')
		fLines = f.readlines()
		for fLine in fLines:
			fLine = fLine.rstrip('\n')
			if(fLine == '# Complete Hash Id Str: '):
				append = True
			if(fLine == '# Cross-info: '):
				append = False
			if(append and (fLine != '')):	
				finalLines.append(fLine+'\n')
		f.close()
	f = open('failPts.txt', 'w')
	f.writelines(finalLines)
	f.close()

#if the line no within a hash being processed is <= 4 
def lNoIsLe4(hFileLine):
	hFileLine = hFileLine.rstrip("\n")
	return hFileLine

#if the line no within a hash being processed is == 5 
def lNoIs5 (hFileLine):
	hFileLine = hFileLine.rstrip("\n")
	fileTypeAndState = hFileLine.split('-')
	if(fileTypeAndState[0] == "Unknown"):
		return ("Unknown", "Unknown")
	return (fileTypeAndState[0], fileTypeAndState[1])	

#if the line no within a hash being processed is == 7 
def lNoIs7 (hFileLine):
	hFileLine = hFileLine.rstrip("\n")
	failureAttrs = hFileLine.split()
	isBefore = False
	isWriteIO = False
	for attr in failureAttrs:
		if (attr.startswith("PL")):
			PLAndIsBefore = attr.split(':')
			#print PLAndIsBefore
			if(PLAndIsBefore[1] == "BEFORE"):
				isBefore = True
			else:
				isBefore = False
		if (attr.startswith("IO")):
			IOAndIsWrite = attr.split(':')
			#print IOAndIsWrite
			if(IOAndIsWrite[1] == "WRITE"):
				isWriteIO = True
			else:
				isWriteIO = False
	return (isBefore, isWriteIO)		

#if the line no within a hash being processed is > 7 (line within the stack) 
def lNoGt7 (hFileLine):
	hFileLine = hFileLine.rstrip("\n")
	p = re.compile('[\w\$<>]+')
	sElemInfo = p.findall(hFileLine)
	#print sElemInfo
	length = len(sElemInfo)
	lNo = int(sElemInfo[length-1])
	mName = sElemInfo[length-2]
	cName = sElemInfo[length-3]
	return (cName, mName, lNo)

#things that should go into the filters file before each filter is printed out
def printPreambleForFiltersFile (filtersFile, className):
	preambleLines = [
			"package org.fi;\n",
			"import org.fi.FMServer.FailType;\n",
			"import java.util.List;\n",
			"import java.util.LinkedList;\n",
			"\n",
			"public class " + className + "{\n",
			"\tpublic FMAllContext fac;\n",
			"\tpublic FailType ft;\n",
			"\tpublic FIState fis;\n",
			"\n",
			"\tpublic " + className + "(FMAllContext fac, FailType ft, FIState fis){\n",
			"\t\tthis.fac = fac;\n",
			"\t\tthis.ft = ft;\n",
			"\t\tthis.fis = fis;\n",
			"\t}\n"
			]
	filtersFile.writelines(preambleLines)

def toJavaBool (val):
	if (val == True) : 
		return "true"
	return "false"

#print each filter line
def printFilterMethod (failType, diskId, nodeId, IOType, isDataFile, isCurrent, isBefore, 
			isWrite, stack, filtersFile, filterNo):
	linesToBeWritten = [
			"\tpublic boolean computeFilter" + str(filterNo) + "(){\n",
			"\t\tList<StackElemInfo> stack = new LinkedList<StackElemInfo>();\n"
			]
	for (cName, mName, lNo) in stack:
		line = "\t\tstack.add(new StackElemInfo(\"" + cName + "\", \"" + mName + "\", "+ str(lNo) + "));\n"
		linesToBeWritten.append(line)
	line = ("\t\tboolean filter = FMFilter.filterForFailure(fac, ft, fis, \"" + failType + 
	"\", \"" + diskId + "\", \"" + nodeId + "\", \"" + IOType + "\", " + toJavaBool(isDataFile) + ", " + 
	toJavaBool(isCurrent) + ", " + toJavaBool(isBefore) + ", " + toJavaBool(isWrite) + ", stack" + ");\n\n")
	linesToBeWritten.append(line)
	linesToBeWritten.append("\t\treturn filter;\n")
	linesToBeWritten.append("\t}\n")
	filtersFile.writelines(linesToBeWritten)

#things that go into the filters file after the filters are printed out
def printPostambleForFiltersFile (filtersFile, numFilters):
	filtersStr = ""
	for i in range(numFilters):
		filtersStr = filtersStr + "computeFilter" + str(i+1) + "()"
		if(i != (numFilters - 1)):
			filtersStr = filtersStr + " ||"
 	if numFilters == 0:
		filtersStr = "true"
	
	postambleLines = [
			 "\tpublic boolean computeFilter(){\n",
			 "\t\treturn (" + filtersStr + ");\n",
			 "\t}\n",
			 "\tpublic int getNumFilters(){\n",
			 "\t\treturn " + str(numFilters) + ";\n",
			 "\t}\n",
			 "}\n"
			 ]
	filtersFile.writelines(postambleLines)

#parse the file with hashes of failure points and generate the Java file containing filters
def parseFileWithHashes (hashesFile, fileToWrite):
	hFile = open(hashesFile, 'r')
	hFileLines = hFile.readlines()
	
	filtersFile = open(fileToWrite, 'w')
	#chop off .java from the end of the name of fileToWrite
 	className = fileToWrite[:-5]
	printPreambleForFiltersFile(filtersFile, className)

	lNoWithinHash = 0
	partOfStack = False

 	failType = None
	diskId = None
	nodeId = None
	IOType = None
	isDataFile = False
	isCurrent = False
	isBefore = False
	isWrite = False
	stack = []

	filterNo = 0

	for hFileLine in hFileLines:
		if (hFileLine.startswith('# Complete')):
			if (not (failType == None)) : 
				#print ("********************************************")
				#print ("FailType = " + failType) 
				#print ("DiskId = " + diskId)
				#print ("NodeId = " + nodeId)
				#print ("IOType = " + IOType)
				#print ("isDataFile = " + str(isDataFile))
				#print ("isCurrent = " + str(isCurrent)) 
				#print ("isBefore = " + str(isBefore)) 
				#print ("isWrite = " + str(isWrite)) 
				#print ("stack = " + str(stack))
				filterNo = filterNo + 1
				printFilterMethod(failType, diskId, nodeId, IOType, isDataFile, isCurrent, isBefore, 
						isWrite, stack, filtersFile, filterNo)
			lNoWithinHash = 0
			partOfStack = False
			stack = []
		elif (hFileLine.startswith('#') or hFileLine.startswith('  call')):
			continue
		elif (hFileLine.startswith('    SourceLoc')):
			partOfStack = True
			continue
		else:
			lNoWithinHash = lNoWithinHash + 1
			
			if (lNoWithinHash == 1) : 
				failType = lNoIsLe4(hFileLine)
			if (lNoWithinHash == 2) : 
				diskId = lNoIsLe4(hFileLine)
			if (lNoWithinHash == 3) : 
				nodeId = lNoIsLe4(hFileLine)
			if (lNoWithinHash == 4) : 
				IOType = lNoIsLe4(hFileLine)
			if (lNoWithinHash == 5) : 
				(fType, fState) = lNoIs5(hFileLine)
				if fType == "DataFile":
					isDataFile = True
				else:
					isDataFile = False
				if fState == "Current":
					isCurrent = True
				else:
					isCurrent = False
			if (lNoWithinHash == 7) : 
				(isBefore, isWrite) = lNoIs7(hFileLine)
			if (lNoWithinHash > 7) : 
				(cName, mName, lNo) = lNoGt7(hFileLine)
				stack.append((cName, mName, lNo))
	
	if (not (failType == None)) : 
		filterNo = filterNo + 1
		printFilterMethod(failType, diskId, nodeId, IOType, isDataFile, isCurrent, isBefore, 
				isWrite, stack, filtersFile, filterNo)
	
	printPostambleForFiltersFile(filtersFile, filterNo)
	
	hFile.close()
	filtersFile.close()			

def rmFailPtsFiles():
	os.system('rm -f failPts1.txt')
	os.system('rm -f failPts2.txt')
	os.system('rm -f failPts.txt')


if len(sys.argv) < 2:
	print('Correct usage: python obtainRelFailurePoints.py <No. of failures in an execution>')
	sys.exit(0)

maxFsn = int(sys.argv[1])	

homeDir = os.getenv('HOME')

for fsn in range(maxFsn - 1):
	maxFsn1 = fsn
	enableFailureExp1 = True
	if (fsn == 0):
		maxFsn1 = 1
		enableFailureExp1 = False
 	rmFailPtsFiles()
	setSSHKeys(homeDir)

	os.system('ant -DMAX_FSN=' + str(maxFsn1) + ' ' + '-DenableFailure=' + str(enableFailureExp1)  + ' -DenableCoverage=true')
	writeFailPtsToFile('failPts1.txt')

	os.system('ant -DMAX_FSN=' + str(fsn + 1) + ' ' + '-DenableFailure=true -DenableCoverage=true')
	writeFailPtsToFile('failPts2.txt')

	fPtsHashes = findHashesOfRelFailPts('failPts1.txt', 'failPts2.txt')
	printHashesToFile(fPtsHashes)
	parseFileWithHashes("failPts.txt", "FMFiltersForFailure" + str(fsn+2) + ".java")
	
#os.system('ant -DMAX_FSN=' + str(maxFsn) + ' ' + '-DenableFailure=true -DenableCoverage=true')
