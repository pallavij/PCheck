#!/usr/bin/python
import os
import sys
from sets import Set

if len(sys.argv) != 3:
	sys.exit("Must provide the dir name and the number id of the files whose coverage to compute")

dName = sys.argv[1]
id = sys.argv[2]

uncoveredLines = None

for fName in os.listdir(dName):
	if fName.startswith("u" + id):
		f = open(os.path.join(dName, fName), "r")
		lines = f.readlines()
		f.close()
		if uncoveredLines == None:
			uncoveredLines = Set(lines)
		else:
			uncoveredLines = uncoveredLines.intersection(Set(lines))


f = open("uncoveredLines" + id + ".txt", "w")
l = list(uncoveredLines)
l.sort()
f.writelines(l)		
f.close()
