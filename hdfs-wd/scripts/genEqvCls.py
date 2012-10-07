#!/usr/bin/python
import os
import sys
import util
import runExp

#Hack ---> Hack until the stable state fId collection is fixed

class EqvCls:

	fIdsSeqToRFIdsExcMap = {}
	allFIdsMap = {}
	rHashesMap = {}

	fInfos = None

	#used in Hack
	rFIdsSeqToSetOfFIdsSeqsMap = {}
	rFIdsSeqToSetOfRFIdsExcMap = {}

	def __init__(self, allMap, rHMap, fInfos):
		self.fIdsSeqToRFIdsExcMap = {}
		self.allFIdsMap = allMap
		self.rHashesMap = rHMap
		self.fInfos = fInfos
		self.rFIdsSeqToSetOfFIdsSeqsMap = {}
		self.rFIdsSeqToSetOfRFIdsExcMap = {}

	def shortCutForGettingEqvCls(self):
		fIdToFInfosMap = util.getFIdToInfoMap(self.fInfos)
		failedFIdSeqs = util.getFIdSeqsThatFailed()
		expsConsidered = []
		for fIdsSeq in self.allFIdsMap.keys():
			if fIdsSeq != "":
				fstFId = (fIdsSeq.split(" "))[0]
				fstFInf = fIdToFInfosMap[fstFId]
				if (not util.isRpcNoise(fstFInf)) and (fstFId not in failedFIdSeqs) :
					self.getResSetOfFIdsInExec(fIdsSeq)
					expsConsidered.append(fstFId)
		self.writeInjFIds(expsConsidered, self.fInfos)			
		#self.writeEqvCls(eqvClsMap, self.fInfos)
			

	#S1 = set of all fIds observed during execution in which injFIds are injected
	#S2 = set of all fIds observed during execution in which (injFIds-the last fId) are injected
	#S3 = S1\S2 and map each fId in S3 to the reduced fId in which the node Id is not considered
	def getResSetOfFIdsInExec(self, injFIds):
		if(injFIds not in self.allFIdsMap.keys()):
			return None
		
		if(injFIds in self.fIdsSeqToRFIdsExcMap.keys()):
			return (self.fIdsSeqToRFIdsExcMap)[injFIds]

		fIdsForInjFIds = (self.allFIdsMap)[injFIds]

		lastDelim = injFIds.rfind(" ")
		if(lastDelim != -1): 
			injFIdsWOLastFId = injFIds[0:lastDelim]
		else:	
			injFIdsWOLastFId = ""
	
		#fIdsForInjFIdsWOLastFId = set([])
		#if(injFIdsWOLastFId in self.allFIdsMap.keys()):
		fIdsForInjFIdsWOLastFId = (self.allFIdsMap)[injFIdsWOLastFId]

		'''
		fIdsDiff = self.listDiff(fIdsForInjFIds, fIdsForInjFIdsWOLastFId)
		rFIdsDiffSet = self.getRFIdsSet(set(fIdsDiff))
		(self.fIdsSeqToRFIdsExcMap)[injFIds] = rFIdsDiffSet
		return rFIdsDiffSet
		'''

		rFIdsForInjFIds = self.getRFIdsSet(fIdsForInjFIds)
		rFIdsForInjFIdsWOLastFId = self.getRFIdsSet(fIdsForInjFIdsWOLastFId)
		
		rFIdsExcForInjFIds = rFIdsForInjFIds.difference(rFIdsForInjFIdsWOLastFId)
		#rFIdsExcForInjFIds = self.listDiff(rFIdsForInjFIds, rFIdsForInjFIdsWOLastFId)

		(self.fIdsSeqToRFIdsExcMap)[injFIds] = rFIdsExcForInjFIds
		return rFIdsExcForInjFIds

	
	def getRFIdsSet(self, fIdsSet):
		rFIdsSet = set([])
		#rFIdsSet = []
		for fId in fIdsSet:
			rFId = (self.rHashesMap)[fId]
			rFIdsSet.add(rFId)
			#rFIdsSet.append(rFId)
		return rFIdsSet	

	
	def listDiff(self, l1, l2):
		diff = set([])
		for e in l1:
			if e not in l2:
				diff.add(e)
			else:
				if l1.count(e) > l2.count(e):
					diff.add(e)
		return diff			

	
	def areEqv(self, injFIds1, injFIds2):
		rFIdsExcForInjFIds1 = self.getResSetOfFIdsInExec(injFIds1)
		rFIdsExcForInjFIds2 = self.getResSetOfFIdsInExec(injFIds2)
		
		if((rFIdsExcForInjFIds1 == None) or (rFIdsExcForInjFIds2 == None)):
			return False
		
		if(rFIdsExcForInjFIds1 == rFIdsExcForInjFIds2):
			return True
		else:
			return False


	def printEqvClsMap(self, outFile):
		eqvClsMap = {}
		for fIdsSeq in self.fIdsSeqToRFIdsExcMap.keys():
			execRIds = self.fIdsSeqToRFIdsExcMap[fIdsSeq]
			execRIds = set(sorted(list(execRIds)))
			execRIdsStr = " ".join(execRIds)
			if execRIdsStr in eqvClsMap.keys():
				eqvCl = eqvClsMap[execRIdsStr]
				eqvCl.add(fIdsSeq)
			else:
				eqvClsMap[execRIdsStr] = set([fIdsSeq])
		
		
		outF = open(outFile, "w")
		count = 1
		lOfExecRIdsStrs = []
		for execRIdsStr in eqvClsMap.keys():
			eqvCl = ",".join(eqvClsMap[execRIdsStr])
			eqvCl += ":"
			eqvCl += execRIdsStr
			eqvCl += "\n"
			outF.write(eqvCl)
			#for debugging
			self.writeEqFIds(eqvClsMap[execRIdsStr], count)
			self.writeSetOfRIds(execRIdsStr, count)
			self.writeSetOfRIdsDetails(execRIdsStr, count)
			lOfExecRIdsStrs.append(execRIdsStr)
			count = count + 1
		outF.close()

		#for debugging
		self.writeSetRelations(lOfExecRIdsStrs)
		#self.writeEqvCls(eqvClsMap, self.fInfos)

	def writeInjFIds(self, expsConsidered, fInfos):
		f = open(util.globAllFPtsFile, "r")
		fIdCovs = f.readlines()
		f.close()

		f = open(util.globFPtsPath+"eqvClsInfo/injFIds", "w")

		for fIdCov in fIdCovs:
			fIdCov = fIdCov.rstrip("\n")
			if not fIdCov.startswith(":"):
				fIds = (fIdCov.split(":"))[0]
				fstFId = (fIds.split(" "))[0]
				if fstFId in expsConsidered:
					f.write("FID:"+fstFId+"\n")
					util.printFIdInfo(f, fstFId, fInfos)
		f.close()			

	
	def writeEqFIds(self, sOfFIdsSeqs, count):
		fIdsSeqsLst = sorted(list(sOfFIdsSeqs))
		f = open(util.globFPtsPath+"eqvClsInfo/c"+str(count)+"-eqfids", "w")
		for fIdsSeqs in fIdsSeqsLst:
			fstFId = ((fIdsSeqs.strip("\n")).split(" "))[0]
			f.write("FID="+fstFId+":\n")
			util.printFIdInfo(f, fstFId, self.fInfos)
		f.close()

	def writeSetOfRIds(self, execRIdsStr, count):	
		f = open(util.globFPtsPath+"eqvClsInfo/c"+str(count)+"-setrids", "w")
		rIds = (execRIdsStr.strip("\n")).split(" ")
		for rId in sorted(rIds):
			f.write(rId+"\n")
		f.close()

	
	def writeSetOfRIdsDetails(self, execRIdsStr, count):
		file = open(util.globFPtsPath+"eqvClsInfo/c"+str(count)+"-detailedsetrids", "w")
		rIds = (execRIdsStr.strip("\n")).split(" ")
		for rId in sorted(rIds):
			file.write(rId+":\n")
			for f,r in self.rHashesMap.items():
				if r == rId:
					util.printFIdInfo(file, f, self.fInfos)
					break
		file.close()

	def writeSetRelations(self, lOfExecRIdsStrs):
		file = open(util.globFPtsPath+"eqvClsInfo/setrelations", "w")
		
		lOfSetOfExecRIds = []
		for execRIdsStr in lOfExecRIdsStrs:
			execRIdsStr = execRIdsStr.strip("\n")
			if execRIdsStr != "":
				lOfSetOfExecRIds.append(sorted(set(execRIdsStr.split(" "))))
			else:
				lOfSetOfExecRIds.append(set([]))
		for i in range(len(lOfSetOfExecRIds)):
			for j in range(len(lOfSetOfExecRIds)):
				if i != j:
					s1 = set(lOfSetOfExecRIds[i])
					s2 = set(lOfSetOfExecRIds[j])
					file.write("c%02d c%02d [ %2d ] [ %2d ] [ %3d ]\n" %(i+1,j+1,len(s1-s2),len(s2-s1),len(s1-s2)-len(s2-s1)))
		file.close()

		file = open(util.globFPtsPath+"eqvClsInfo/detailedsetrelations", "w")
		for i in range(len(lOfSetOfExecRIds)):
			for j in range(len(lOfSetOfExecRIds)):
				if i != j:
					s1 = set(lOfSetOfExecRIds[i])
					s2 = set(lOfSetOfExecRIds[j])
					file.write("c"+str(i+1)+" c"+str(j+1)+":\n")
					file.write("c"+str(i+1)+" - c"+str(j+1)+":\n")
					for e in sorted(s1-s2):
						file.write(e+"\n")
					file.write("c"+str(j+1)+" - c"+str(i+1)+":\n")
					for e in sorted(s2-s1):
						file.write(e+"\n")
					file.write("\n\n\n")	
		file.close()

	def writeEqvCls(self, expsConsidered, eqvClsMap, fInfos):
		fName = "/Users/pallavi/Research/faultInjection/hdfs-wd/write-eqv-final-haryadi/write-eqv-all/injFIds"
		f = open(fName, "r")
		lines = f.readlines()
		f.close()

		linesToWr = []
		fIdToInfoMap = util.getFIdToInfoMap(fInfos)
		count = 1
		for line in lines:
			if line.startswith("FID:"):
				fId = (line.strip("\n")).lstrip("FID:")
				eq = self.getEqvCl(fId, eqvClsMap)
				if eq != -1:
					fInf = fIdToInfoMap[fId]
					nId = fInf.nodeId
					#linesToWr.append(str(count)+"\t"+fId+"\t"+nId+"\t"+nId+":"+"%02d"+"\t"+str(eq)+"\n" %(int(count)))
					linesToWr.append(str(count)+"\t"+fId+"\t"+nId+"\t"+nId+":"+str(count)+"\t"+str(eq)+"\n")
					count = count + 1

		f = open(util.globFPtsPath+"eqvClsInfo/eqvCls", "w")
		f.writelines(linesToWr)
		f.close()

	def getEqvCl(self, fId, eqvClsMap):
		eq = -1
		count = 1
		for k in eqvClsMap.keys():
			s = eqvClsMap[k]
			if fId in s:
				return count
			count = count + 1
		return eq	

	#Hack; Assumption: each posExp has at least two fIds
	def getEqvClsForRFIds(self, posExps):
		for posExp in posExps:
			posExp = posExp.rstrip("\n")
			posExpFIds = posExp.split(" ")
			l = len(posExpFIds)
			prevFIds = " ".join(posExpFIds[0:l-1])
			prevRFIds = self.getRFIds(prevFIds)

			self.addToMap(self.rFIdsSeqToSetOfFIdsSeqsMap, prevRFIds, prevFIds)
			
			execFIds = self.getResSetOfFIdsInExec(prevFIds)
			
			if(execFIds != None):
				self.addToMap(self.rFIdsSeqToSetOfRFIdsExcMap, prevRFIds, frozenset(execFIds))

	#Used in Hack
	def getRFIds(self, fIdsSeq):
		if fIdsSeq == "":
			return ""

		fIdsSeq = fIdsSeq.rstrip(" ")
		fIds = fIdsSeq.split(" ")
		rFIds = ""
		for fId in fIds:
			rFIds += (self.rHashesMap)[fId]
			rFIds += " "
		rFIds = rFIds.rstrip(" ")
		return rFIds

	#Used in Hack:
	def addToMap(self, m, k, se):
		if k in m.keys():
			s = m[k]
			s.add(se)
		else:
			m[k] = set([se])

	#Hack
	def areEqvHack(self, injFIds1, injFIds2):
		injRFIds1 = self.getRFIds(injFIds1)
		injRFIds2 = self.getRFIds(injFIds2)
	
		if(injRFIds1 == injRFIds2):
			return True

		s1 = None
		s2 = None
		if injRFIds1 in self.rFIdsSeqToSetOfRFIdsExcMap.keys():
			s1 = self.rFIdsSeqToSetOfRFIdsExcMap[injRFIds1]
		if injRFIds2 in self.rFIdsSeqToSetOfRFIdsExcMap.keys():
			s2 = self.rFIdsSeqToSetOfRFIdsExcMap[injRFIds2]

		if((s1 == None) or (s2 == None) or (len(s1) == 0) or (len(s2) == 0)):
			return False

		si = s1.intersection(s2)

		if(len(si) != 0):
			return True
		else: 
			return False
	
	#Hack
	def printEqvClsMapHack(self, outFile):
		eqvCls = []
		for rFIdsSeq in self.rFIdsSeqToSetOfRFIdsExcMap.keys():
			s = self.rFIdsSeqToSetOfRFIdsExcMap[rFIdsSeq]
			createNewEqvCl = True
			
			for eqvCl in eqvCls:
				matchEqvCl = True
				for seq in eqvCl:
					sInEqvCl = self.rFIdsSeqToSetOfRFIdsExcMap[seq]
					if((len(s) == 0) or (len(sInEqvCl) == 0) or 
							(len(s.intersection(sInEqvCl)) == 0)):
						matchEqvCl = False
						break
				if matchEqvCl:
					createNewEqvCl = False
					eqvCl.add(rFIdsSeq)
					break
						
			if(createNewEqvCl):
				eqvCls.append(set([rFIdsSeq]))

		outF = open(outFile, "w")
		for s in eqvCls:
			if(len(s) != 0):
				sOfFIdsSeqs = set([])
				for rFIdsSeq in s:
					fIdsSeqs = self.rFIdsSeqToSetOfFIdsSeqsMap[rFIdsSeq]
					sOfFIdsSeqs = sOfFIdsSeqs.union(fIdsSeqs)

				eqvCl = ",".join(sOfFIdsSeqs)
				eqvCl += "\n"
				outF.write(eqvCl)
		outF.close()
