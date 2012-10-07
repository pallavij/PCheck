#!/usr/bin/python
import os
import sys
import math


if len(sys.argv) != 2:
	sys.exit("Must provide the path of the histData directory")

dName = sys.argv[1]
cLines = []

if os.path.exists(dName):
	for hname in os.listdir(dName):
		hpath = os.path.join(dName, hname)
		if os.path.isdir(hpath):
			L = 0
			for cname in os.listdir(hpath):
				cpath = os.path.join(hpath, cname)
				f = open(cpath, "r")
				lines = f.readlines()
				f.close()
				L = L + len(lines)
			cLines.append(hname + " " + str(L) + "\n")

print cLines
