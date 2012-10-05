#!/usr/bin/python
import os
import sys


def findStr(pattern, strs):
	for str in strs:
		if str.find(pattern) != -1:
			return True
	return False	


if len(sys.argv) != 3:
	sys.exit("Must provide the dir name and output script name")

policyDir = sys.argv[1]
dpath = os.path.join(policyDir, "covFiles")

oDirPath = os.path.join(policyDir, "processed-files")
if not os.path.exists(oDirPath):
	os.system("mkdir " + oDirPath)

output = sys.argv[2]

toWr = []
toWr.append("#!/bin/sh\n")

for fname in os.listdir(dpath):
	fpath = os.path.join(dpath, fname)
	if os.path.isdir(fpath):
		for fname1 in os.listdir(fpath):
			fpath1 = os.path.join(fpath, fname1)
			if fname1 == "_files":
				for fname2 in os.listdir(fpath1):
					fpath2 = os.path.join(fpath1, fname2)
					if fname2 == "1.html" or fname2 == "2.html":
					'''
					if fname2 == "1.html" or fname2 == "2.html" or fname2 == "3.html" or fname2 == "4.html" or	\
							fname2 == "5.html" or fname2 == "6.html" or fname2 == "7.html" or 	\
							fname2 == "8.html" or fname2 == "9.html" or fname2 == "10.html" or 	\
							fname2 == "11.html" or fname2 == "a.html" or fname2 == "b.html" or 	\
							fname2 == "c.html" or fname2 == "d.html" or fname2 == "e.html" or	\
							fname2 == "f.html":
					'''		
						f = open(fpath2, "r")
						lines = f.readlines()
						fparts = fname.split("-")
						ofname = ""
						if findStr("FastLeaderElection", lines):
							ofname = "u1" + fparts[1] + ".txt"
						elif findStr("QuorumCnxManager.java", lines):
							ofname = "u2" + fparts[1] + ".txt"
						else:
							continue
						'''
						if findStr("Leader.java", lines):
							ofname = "u1" + fparts[1] + ".txt"
						elif findStr("Follower.java", lines):
							ofname = "u2" + fparts[1] + ".txt"
						elif findStr("Learner.java", lines):
							ofname = "u3" + fparts[1] + ".txt"
						elif findStr("LearnerHandler.java", lines):
							ofname = "u4" + fparts[1] + ".txt"
						else:
							continue
						'''	
						toWr.append("./scripts/findUncoveredLNos.py " + fpath2 + " > " + os.path.join(oDirPath, ofname) + "\n")
						f.close()


f = open(output, "w")
f.writelines(toWr)
f.close()
