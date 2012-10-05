#!/usr/bin/python
import os
import sys

#covDir = "/Users/pallavi/Research/faultInjection/zook-fi/random/"
covDir = "/Users/pallavi/Research/faultInjection/zook-fi/depth-first/"


allBranches = set([])
for fName in os.listdir(covDir):
	if(fName.startswith('bcovlist')):
		f = open(covDir + fName, 'r')
		flines = f.readlines()
		f.close()
		allBranches = allBranches.union(set(flines))



branches = set([])
n = 1
while True:
	fPath = covDir + 'bcovlist' + str(n) + '.txt'
	if(not os.path.exists(fPath)):
		break
	f = open(fPath, 'r')	
	flines = f.readlines()
	f.close()
	branches = branches.union(set(flines))

	if branches.issubset(allBranches) and allBranches.issubset(branches):
		print 'n = ' + str(n)
		break

	n = n + 1

