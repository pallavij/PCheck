#!/usr/bin/python
import os
import sys

covDir = "/Users/pallavi/Research/faultInjection/zook-fi/depth-first/"
branches = set([])

for fName in os.listdir(covDir):
	f = open(covDir + fName, 'r')
	flines = f.readlines()
	f.close()
	for fl in flines:
		branches.add(fl.strip())
	
sortedb = list(branches)
sortedb.sort()

f = open(covDir + 'allExpBs', 'w')
for sb in sortedb:
	f.write(sb + '\n')
f.close()
