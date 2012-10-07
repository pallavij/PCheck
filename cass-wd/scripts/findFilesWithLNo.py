#!/usr/bin/python
import os
import sys

if len(sys.argv) != 4:
	sys.exit("Must provide the line number, directory name, and whether its the first file or the second")

lNo = sys.argv[1]
dName = sys.argv[2]
fSeqNo = sys.argv[3]

fNames = []

for fn in os.listdir(dName):
	if fn.startswith("u" + fSeqNo):
		f = open(dName + "/" + fn, "r")
		fLines = f.readlines()
		f.close()

		if (not ((lNo+"\n") in fLines)):
			fNames.append(fn)
	
print fNames	
