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
	fjp = None
	fst = None
	clippedFst = None

	fstInfos = None
	jpStr = None
	isBefore = None
	expType = None
	isRetFalse = None
	ioType = None
	fjpSrcLoc = None

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

	def setFjp(self, fjp):	
		self.fjp = fjp
		self.parseFjp()
		'''
		print "jpStr = " + self.jpStr
		print "isBefore = " + str(self.isBefore)
		print "expType = " + self.expType
		print "isRetFalse = " + str(self.isRetFalse)
		print "ioType = " + self.ioType
		print "fjpSrcLoc = " + self.fjpSrcLoc
		'''
	
	def setFst(self, fst):	
		self.fst = fst
		self.parseFst()
		#print "fstInfos:"
		#print self.fstInfos
	
	def setClippedFst(self, clippedFst):	
		self.clippedFst = clippedFst

	def getRId(self, option):
		if option == 1:
			return self.getRId1()
		if option == 2:
			return self.getRId2()
		if option == 3:
			return self.getRId3()
		if option == 4:
			return self.getRId4()
		return None
	
	def parseFst(self):
		fst = self.fst
		fstLines = fst.split("\n")

		fstInfos = []

		for fstLine in fstLines:
			if fstLine == "":
				continue
			fstLine = fstLine.strip()
			fstLnParts = fstLine.split("]")
			fstLnInfo = fstLnParts[1]
			fstLnInfoParts = fstLnInfo.split("(")
			cName = fstLnInfoParts[0]
			mNameLNo = fstLnInfoParts[1].split(":")
			mName = mNameLNo[0]
			lNo = (mNameLNo[1].split(")"))[0]
			fstInfos.append(cName.strip()+":"+mName.strip()+":"+lNo.strip())
		
		self.fstInfos = fstInfos	

	def parseFjp(self):
		fjp = self.fjp
		fjpLines = fjp.split("\n")

		callStr = fjpLines[0]
		callStr = callStr.rstrip()
		callStr = callStr.lstrip()
		callStr = callStr[5:-1]
		self.jpStr = callStr

		failInfoStr = fjpLines[1]
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
	
		fjpSrcLocStr = fjpLines[2]
		self.fjpSrcLoc = ((fjpSrcLocStr.split(":"))[1]).strip()


	def getRId1(self):
		h = hash(str(self.jpStr)+"\n"+str(self.fjpSrcLoc)+"\n")
		return str(h)

	def getRId2(self):
		h = hash(str(self.jpStr)+"\n"+str(self.fjpSrcLoc)+"\n"
				+str(self.clippedFst)+"\n")
		return str(h)
	
	def getRId3(self):
		h = hash(str(self.jpStr)+"\n"+str(self.fjpSrcLoc)+"\n"
				+str(self.nodeId)+"\n")
		return str(h)
	
	def getRId4(self):
		h = hash(str(self.jpStr)+"\n"+str(self.fjpSrcLoc)+"\n"
				+str(self.clippedFst)+"\n"
				+str(self.nodeId)+"\n"
				+str(self.tNodeId)+"\n"
				+str(self.tIoType)+"\n"
				+str(self.rpcContext)+"\n"
				+str(self.fType)+"\n"
				)
		return str(h)
	
class Parser:
	
	fInfos = []
	postSetupFIds = set([])
	
	def getFIdDescStrs(self, fName, stackDepth):
		f = open(fName, "r")
		
		line = f.readline()

		curFInfo = None
		curFjpStr = None
		curFstStr = None
		curParseStr = None
		curStack = []

		while (line != ""):
			line = line.rstrip("\n")
			if line.startswith("FID:"):
				if curFInfo != None:
					clippedFstStr = self.getClippedFstStr(curStack, stackDepth)
					curFInfo.setFst(curFstStr)
					curFInfo.setClippedFst(clippedFstStr)
					self.fInfos.append(curFInfo)
				line = line[4:]
				curFInfo = FInfo(line)

			elif line.startswith("ECONTEXT:"):
				line = line[9:]
				curFInfo.setECtx(line)
				if line.startswith("post"):
					self.postSetupFIds.add(curFInfo.getFId())

			elif line.startswith("RPCCONTEXT:"):
				line = line[11:]
				curFInfo.setRpcCtx(line)

			elif line.startswith("FT:"):
				line = line[3:]
				curFInfo.setFt(line)

			elif line.startswith("DISKID:"):
				line = line[7:]
				curFInfo.setDiskId(line)

			elif line.startswith("NODEID:"):
				line = line[7:]
				curFInfo.setNodeId(line)
		
			elif line.startswith("TIOTYPE:"):
				line = line[8:]
				curFInfo.setTIoType(line)
				
			elif line.startswith("FTYPE:"):
				line = line[6:]
				curFInfo.setFType(line)
		
			elif line.startswith("TNODEID:"):
				line = line[8:]
				curFInfo.setTNodeId(line)
				
			elif line.startswith("FJP:"):
				curParseStr = "FJP"
				line = line[4:]
				curFjpStr = line + "\n"
				
			elif line.startswith("FST:"):
				curFInfo.setFjp(curFjpStr)
				curParseStr = "FST"
				line = line[4:]
				curFstStr = line + "\n"
				curStack = []
				curStack.append(line + "\n")

			else:
				if curParseStr == "FJP":
					curFjpStr = curFjpStr + line + "\n"	
					
				if curParseStr == "FST":
					curFstStr = curFstStr + line + "\n"
					curStack.append(line + "\n")
			
			line = f.readline()
		
		if curFInfo != None:
			clippedFstStr = self.getClippedFstStr(curStack, stackDepth) 
			curFInfo.setFst(curFstStr)
			curFInfo.setClippedFst(clippedFstStr)
			self.fInfos.append(curFInfo)
		
		f.close()

	def getClippedFstStr(self, curStack, k):
		clippedFstStr = ""
		l = len(curStack)
		if l < k:
			k = l
		for i in range(k):
			stFrame = curStack.pop()
			clippedFstStr = clippedFstStr + stFrame
		return clippedFstStr	


	def getFInfos(self):	
		return self.fInfos

	def getPostSetupFIds(self):
		return self.postSetupFIds
