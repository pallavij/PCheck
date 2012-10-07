#!/usr/bin/python
import os
import sys

class FInfo:
	fId = None
	eContext = None 
	rpcContext = None
	ft = None
	diskId = None
	nodeId = None
	tIoType = None
	fType = None
	tNodeId = None

	jpStr = None
	fjpSrcLoc = None
	isBefore = None
	expType = None
	isRetFalse = None
	ioType = None
	fstInfos = None
	clippedFst = None

	def __init__(self, fId):
		self.fId = fId

	def getFId(self):
		return self.fId

	def setECtx(self, eCtx):
		self.eContext = eCtx

	def setRpcCtx(self, rpcCtx):
		self.rpcContext = rpcCtx

	def setFt(self, ft):
		self.ft = ft

	def setDiskId(self, diskId):	
		self.diskId = diskId

	def setNodeId(self, nodeId):	
		self.nodeId = nodeId
	
	def setTIoType(self, tIoType):	
		self.tIoType = tIoType
	
	def setFType(self, fType):
		self.fType = fType

	def setTNodeId(self, tNodeId):	
		self.tNodeId = tNodeId

	def getRId(self):
		return self.getRId()
	
	def parseFjp(self, failInfoStr):
		failInfoStr = failInfoStr.lstrip("* ")	
		failInfoStr = failInfoStr.rstrip("* ")	
		failInfoStrParts = failInfoStr.split("/")
		plStr = failInfoStrParts[0]
		self.isBefore = ((plStr.split(":"))[1]).startswith("BEFORE")
		expTypeStr = failInfoStrParts[1]
		self.expType = ((expTypeStr.split(":"))[1]).strip()
		isRetFalseStr = failInfoStrParts[2]
		self.isRetFalse = ((isRetFalseStr.split(":"))[1]).startswith("YES")
		ioTypeStr = failInfoStrParts[3]
		self.ioType = ((ioTypeStr.split(":"))[1]).strip()


	def getRId(self, choice):
		if choice == 1:
			hStr = (str(self.jpStr)+"\n"
					#+str(self.fjpSrcLoc)+"\n"	
					#+"FST:"+str(self.clippedFst)+"\n"	
					#+"NID:"+str(self.nodeId)+"\n"	
					#+"TNID:"+str(self.tNodeId)+"\n"	
					#+"TIOTYPE:"+str(self.tIoType)+"\n"	
					#+"RPC:"+str(self.rpcContext)+"\n"	
					#"+FTYPE:"+str(self.fType)+"\n"
				)	
			h = hash(hStr)	
			return (hStr, h)


class Parser:

	def getRId(self, fName, stDepth, choice):
		f = open(fName, "r")
		fLines = f.readlines()
		f.close()

		fNameParts = fName.split("/")
		lFNameParts = len(fNameParts)
		fName = fNameParts[-1]

		fName = fName.strip("\n")	
		fId = ((fName.split("h"))[1])[0:-4]

		fInf = FInfo(fId)

		relevantLines = []
		startAdding = False
		for line in fLines:
			if line.startswith("##"):
				startAdding = True
			elif line.startswith("]"):
				startAdding = False
			else:
				if startAdding and (line.strip("\n") != ""):
					relevantLines.append(line)

		fInf.setECtx(relevantLines[0].strip("\n")) 		
		fInf.setRpcCtx(relevantLines[1].strip("\n"))
		fInf.setFt(relevantLines[2].strip("\n"))
		fInf.setDiskId(relevantLines[3].strip("\n"))
		fInf.setNodeId(relevantLines[4].strip("\n"))
		fInf.setTIoType(relevantLines[5].strip("\n"))
		fInf.setFType(relevantLines[6].strip("\n"))
		fInf.setTNodeId(relevantLines[7].strip("\n"))
		fInf.jpStr = (relevantLines[8].strip("\n")).lstrip(" ")
		fInf.fjpSrcLoc = (relevantLines[10].strip("\n")).lstrip(" ")
		relevantLines[9] = relevantLines[9].lstrip(" ")		
		relevantLines[9] = relevantLines[9].rstrip("\n")		
		fInf.parseFjp(relevantLines[9])

		l = len(relevantLines)
		fInf.fstInfos = []
		for i in range(11,l):
			fInf.fstInfos.append((relevantLines[i].rstrip("\n")).lstrip(" "))
		fInf.clippedFst = ""
		l = len(fInf.fstInfos)
		for i in range(0, l-stDepth):
			fInf.clippedFst = fInf.clippedFst + fInf.fstInfos[i] + "\n"

		'''
		print "------------------------------------------------------------"
		print "eCtx = " + fInf.eContext
		print "rpcCtx = " + fInf.rpcContext
		print "ft = " + fInf.ft
		print "diskId = " + fInf.diskId
		print "nodeId = " + fInf.nodeId
		print "tIoType = " + fInf.tIoType
		print "fType = " + fInf.fType
		print "tNodeId = " + fInf.tNodeId
		print "jpStr = " + fInf.jpStr
		print "fjpSrcLoc = " + fInf.fjpSrcLoc
		print "fstInfos = " + str(fInf.fstInfos)
		print "isBefore = " + str(fInf.isBefore)
		print "expType = " + str(fInf.expType)
		print "isRetFalse = " + str(fInf.isRetFalse)
		print "ioType = " + str(fInf.ioType)
		print "------------------------------------------------------------"
		'''
		return fInf.getRId(choice)	
