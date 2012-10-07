#!/usr/bin/python
import os
import sys

eqClsDir = "/Users/pallavi/Research/faultInjection/hdfs-wd/write-eqv-final-haryadi/write-eqv-all-but-node"

fIdsWEqCls = []

for fName in os.listdir(eqClsDir):
	if fName.find('eqfids') != -1:
		f = open(os.path.join(eqClsDir, fName), "r")	
		lines = f.readlines()
		f.close()

		eqCl = (fName.split("-"))[0]
		eqCl = eqCl.lstrip("c")
		eqCl = int(eqCl)

		for line in lines:
			line = line.strip("\n")
			if line.startswith("FID="):
				line = line.lstrip("FID=")
				line = line.rstrip(":")
				fIdsWEqCls.append(line+"\tc%02d\n" %(eqCl))

fIdsWEqCls = sorted(fIdsWEqCls)

f = open("eqvCls", "w")
f.writelines(fIdsWEqCls)
f.close()

