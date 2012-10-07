#!/usr/bin/python
import os
import sys
import util

#used areEqvHack instead of areEqv

class GenFailPtsToEx:
	eqvCls = None
	policyOptim = None 

	def __init__(self, eqCls, pOptim):
		self.eqvCls = eqCls
		self.policyOptim = pOptim


	def generateFailPtsToExplore (self, posExpsFile, injFIdsFile, fInfos, policies, useEqvCl, 
			clsLast, outFile):
		if not os.path.exists(posExpsFile):
			return

		posExpsF = open(posExpsFile, 'r')	
		posExps = posExpsF.readlines()
		posExpsF.close()

		posExps = self.filterNoise(posExps, fInfos)

		fltExps = posExps
		covPols = []
		for policy in policies:
			fltExps = self.filterUsingPolicy(fltExps, fInfos, policy)
			isCovPolicy = util.isCoveragePolicy(policy)
			if isCovPolicy:
				covPols.append(policy)

		clsExps = fltExps
		if useEqvCl:
			self.eqvCls.getEqvClsForRFIds(clsExps)
			lFIdToPrevFIdsMap = {}
			l = 0

			for clsExp in clsExps:
				clsExp = clsExp.rstrip("\n")
				clsExpFIds = clsExp.split(" ")
				l = len(clsExpFIds)
				if(l <= 1):
					lFIdToPrevFIdsMap[clsExp] = []
				else:	
					lFId = clsExpFIds[-1]
					prevFIds = " ".join(clsExpFIds[0:l-1])
					if(lFId in lFIdToPrevFIdsMap.keys()):
						listOfPrevFIdsSets = lFIdToPrevFIdsMap[lFId]
						foundEqvCls = False
						for s in listOfPrevFIdsSets:
							if(self.isEqv(prevFIds, s)):
								s.add(prevFIds)
								foundEqvCls = True
								break
								
						if (not foundEqvCls):
							listOfPrevFIdsSets.append(set([prevFIds]))

					else:
						lFIdToPrevFIdsMap[lFId] = [set([prevFIds])]


			clsExps = self.getEqvClsOfExps(lFIdToPrevFIdsMap, useEqvCl, clsExps)	

		else:
			if clsLast:
				policy = None
				fIdToCovIdMap = {}
				fIdToInfoMap = util.getFIdToInfoMap(fInfos)
				n = len(covPols)
				if n != 0:
					assert (n == 1)
					policy = covPols[0]

				lFIdToExpsMap = {}
				for clsExp in clsExps:
					clsExp = clsExp.rstrip("\n")
					clsExpFIds = clsExp.split(" ")
					lFId = clsExpFIds[-1]
					
					if n != 0:
						lFId = util.getCovId(lFId, fIdToCovIdMap, policy, fIdToInfoMap) 
					
					
					if lFId in lFIdToExpsMap:
						sExps = lFIdToExpsMap[lFId]
						sExps.add(clsExp)
					else:
						lFIdToExpsMap[lFId] = set([clsExp])
				
				newClsExps = []
				for l, s in lFIdToExpsMap.items():
					for exp in s:
						newClsExps.append(exp.rstrip("\n")+"\n")
					newClsExps.append("-----------------------------------------------------------\n")
				clsExps = newClsExps	


			else:	
				newClsExps = []
				for clsExp in clsExps:
					newClsExps.append(clsExp.rstrip("\n")+"\n")
					newClsExps.append("-----------------------------------------------------------\n")
				clsExps = newClsExps


		self.printEqvClsOfExps(clsExps, outFile)


	def filterNoise(self, posExps, fInfos):
		fIdToFInfosMap = util.getFIdToInfoMap(fInfos)
		newPosExps = []

		failedSeqs = util.readFromFile(util.failedExpsFile)


		for posExp in posExps:
			posExp = posExp.rstrip("\n")

			isRpcNoise = False
			posExpFIds = posExp.split(" ")
			for fId in posExpFIds:
				fIdFInf = fIdToFInfosMap[fId]
				if util.isRpcNoise(fIdFInf):
					isRpcNoise = True
					break
			
			if isRpcNoise:
				#print "yes...rpc noise...posExp is " + posExp
				continue

			l = len(posExpFIds)
			pfx = " ".join(posExpFIds[0:l-1])	
			if pfx not in failedSeqs:
				newPosExps.append(posExp)

		return newPosExps		



	def filterUsingPolicy(self, posExps, fInfos, policy):
		isFilterPolicy = util.isFilterPolicy(policy)
		isCovPolicy = util.isCoveragePolicy(policy)

		fIdToInfoMap = util.getFIdToInfoMap(fInfos)

		filteredPosExps1 = []
		if isFilterPolicy:
			for posExp in posExps:
				posExp = posExp.rstrip("\n")
				posExpFIds = posExp.split(" ")
				if policy.filter(fIdToInfoMap[posExpFIds[-1]]):
					filteredPosExps1.append(posExp)
		else:
			filteredPosExps1 = posExps
				
		filteredPosExps2 = []		
		if isCovPolicy:
			idsAlreadyInjected = []
			fIdToCovIdMap = {}
		
			f = open(util.injFIdsFile, "r")
			injFIds = f.readlines()
			f.close()

			for injFId in injFIds:
				covId = util.getCovId(injFId.strip("\n"), fIdToCovIdMap, policy, fIdToInfoMap)
				idsAlreadyInjected.append(covId)


			for posExp in filteredPosExps1:
				posExp = posExp.rstrip("\n")
				posExpFIds = posExp.split(" ")
				lastFId = posExpFIds[-1]
				
				lastCovId = util.getCovId(lastFId, fIdToCovIdMap, policy, fIdToInfoMap)
				if lastCovId not in idsAlreadyInjected:
					filteredPosExps2.append(posExp)
		else:
			filteredPosExps2 = filteredPosExps1

		return filteredPosExps2			
					

	def isEqv(self, fIds, setOfFIds):
		fIds1 = fIds

		for fIds2 in setOfFIds:
			#all elements in setOfFIds are of the same equivalence class
			#so we need to compare against one of the elements in the set
			#areEqv = self.eqvCls.areEqvHack(fIds1, fIds2)
			areEqv = self.eqvCls.areEqv(fIds1, fIds2)
			return areEqv
	
	def getEqvClsOfExps(self, lFIdToPrevFIdsMap, useEqvCl, posExps):
		
		exps = []
		
		prevFIdsToLFIdsMap = {}
	
		singleFIds = False
		
		for lFId in lFIdToPrevFIdsMap.keys():
			lOfPrevFIdsSets = lFIdToPrevFIdsMap[lFId]
			l = len(lOfPrevFIdsSets)
			if(l == 0):
				singleFIds = True
				exps.append(lFId+"\n")
				exps.append("-----------------------------------------------------------\n")		
				continue

			for s in lOfPrevFIdsSets:
				fs = frozenset(s)
				sFs = self.isSupersetOfFsInMap(fs, prevFIdsToLFIdsMap)

				#if(fs in prevFIdsToLFIdsMap.keys()):
				if sFs != None:
					sOfLFIds = prevFIdsToLFIdsMap[sFs]
					#sOfLFIds = prevFIdsToLFIdsMap[fs]
					sOfLFIds.add(lFId)
				else:
					prevFIdsToLFIdsMap[fs] = set([lFId])

		if(not singleFIds):

			prevFIdsToEqvClsOfLFIdsMap = {}
			
			for fs in prevFIdsToLFIdsMap.keys():
				sOfLFIds = prevFIdsToLFIdsMap[fs]
				lOfEqCls = []
				for lFId in sOfLFIds:
					createNewEqCl = True
					if useEqvCl:
						for eqCl in lOfEqCls:
							eqClFId = (list(eqCl))[0]
							#if(self.eqvCls.areEqvHack(lFId, eqClFId)):
							if(self.eqvCls.areEqv(lFId, eqClFId)):
								createNewEqCl = False
								eqCl.add(lFId)
								break
					if createNewEqCl:
						lOfEqCls.append(set([lFId]))
				prevFIdsToEqvClsOfLFIdsMap[fs] = lOfEqCls

			for fs in prevFIdsToEqvClsOfLFIdsMap.keys():
				eqvClsForFs = prevFIdsToEqvClsOfLFIdsMap[fs]
				for eqvCl in eqvClsForFs:
					for fIdsSeq in fs:
						for lFId in eqvCl:
							expStr = fIdsSeq + " " + lFId
							expStr = expStr.rstrip("\n")
							if util.isExpPresent(expStr, posExps):
								exps.append(expStr+"\n")
					exps.append("-----------------------------------------------------------\n")		

		return exps

	
	def isSupersetOfFsInMap(self, fs, prevFIdsToLFIdsMap):
		sFs = None
		for sKey in prevFIdsToLFIdsMap.keys():
			if sKey >= fs:
				return sKey
		return None	

	def printEqvClsOfExps(self, exps, outFile):
		outF = open(outFile, 'w');
		outF.writelines(exps)
		outF.close()
