#!/usr/bin/python
import os
import sys

if len(sys.argv) != 3:
	sys.exit("Must provide the file name and the directory name")

fName = sys.argv[1]
dName = sys.argv[2]

f = open(fName, "r")
fLines = f.readlines()
f.close()

c = 1
for l in fLines:
	l = l.strip()
	lParts = l.split()
	wLines = []
	for lp in lParts:
		wLines.append(lp + "\n")
	
	f = open(dName + "/o" + str(c) + ".txt", "w")
	f.writelines(wLines)
	f.close()
	c = c + 1
