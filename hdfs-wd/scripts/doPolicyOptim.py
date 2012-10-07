#!/usr/bin/python
import os
import sys
import util

class DoPolicyOptim:

	fInfos = None
	fIdToInfoMap = None
	fIdToCovIdMap = None

	def __init__(self, fInfos):
		self.fInfos = fInfos
		self.fIdToInfoMap = util.getFIdToInfoMap(fInfos)
		self.fIdToCovIdMap = {}


	def doOptim(self, exps, policies):
		finalExps = exps
			
		for policy in policies:
			isCovPolicy = util.isCoveragePolicy(policy)
			if isCovPolicy:
				finalExps = self.doPolicyOptim(finalExps, policy)

		return finalExps		

	def doPolicyOptim(self, exps, policy):
		lFIdToExpsMap = {}

		expsToSOfLFIds = {}
		
		curEqvCl = []
		for exp in exps:
			if not exp.startswith("------"):
				curEqvCl.append(exp.rstrip("\n"))
			else:
				fs = frozenset(curEqvCl)
				curSOfLFIds = set([])
				for fsElem in fs:
					curSOfLFIds.add(((fsElem.rstrip("\n")).split(" "))[-1])

				expsToSOfLFIds[fs] = curSOfLFIds

				curEqvCl = []

		for sExps in expsToSOfLFIds.keys():
			sOfLFIds = expsToSOfLFIds[sExps]
			for lFId in sOfLFIds:
				if lFId in lFIdToExpsMap.keys():
					expsInMap = lFIdToExpsMap[lFId]
					lFIdToExpsMap[lFId] = (expsInMap | sExps)
				else:
					lFIdToExpsMap[lFId] = sExps

		lOfEqvClsOfLFIds = []

		for lFId in lFIdToExpsMap.keys():
			createNewEqvCl = True

			for s in lOfEqvClsOfLFIds:
				if self.areEqv(lFId, s, policy):
					s.add(lFId)
					createNewEqvCl = False
					break

			if createNewEqvCl:
				lOfEqvClsOfLFIds.append(set([lFId]))
			
		optimExps = []	
		for s in lOfEqvClsOfLFIds:
			for lFId in s:
				sOfExps = lFIdToExpsMap[lFId]
				for exp in sOfExps:
					if self.isLastFId(exp, lFId) and self.isExpPresent(exp, exps):
						optimExps.append(exp.rstrip("\n")+"\n")
		 	optimExps.append("-----------------------------------------------------------\n")		
		return optimExps
					

	def areEqv(self, lFId, s, policy):
		info = self.fIdToInfoMap[lFId]

		pId = util.getCovId(lFId, self.fIdToCovIdMap, policy, self.fIdToInfoMap)

		for sElem in s:
			sInfo = self.fIdToInfoMap[sElem]
			sPId = policy.coverageId(sInfo)
			if pId == sPId:
				return True

		return False

	def isLastFId(self, exp, lFId):
		exp = exp.rstrip("\n")
		expParts = exp.split(" ")
		if expParts[-1] == lFId:
			return True
		return False

	def isExpPresent(self, exp, exps):
		return util.isExpPresent(exp, exps)

