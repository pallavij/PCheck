#!/usr/bin/python
import os
import sys

covDir = "/home/st/pallavi/Research/PCheck/zook-fi/coverage/"
fName = covDir + "bcov.txt"

branches = set([])

f = open(fName, 'r')
flines = f.readlines()
os.remove(fName)
f.close()

for fl in flines:
	branches.add(fl)

sortedb = list(branches)
sortedb.sort()

fExpNoName = covDir + "expnum.txt"
num = 1
if os.path.exists(fExpNoName):
	fENo = open(fExpNoName, 'r')
	expN = fENo.readline()
	expN = expN.strip()
	num = int(expN)
	fENo.close()


oFName = covDir + "bcovlist" + str(num) + ".txt"
f = open(oFName, 'w')
f.writelines(sortedb)
f.close()


f = open(fExpNoName, 'w')
f.write(str(num+1) + '\n')
f.close()
