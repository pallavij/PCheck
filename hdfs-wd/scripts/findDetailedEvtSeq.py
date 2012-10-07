#!/usr/bin/python
import os
import sys

if len(sys.argv) != 4:
	sys.exit("Must provide the file name containing the sequence, the directory containing files that have information regarding sequence ids, and the output file name.")

fName = sys.argv[1]
dName = sys.argv[2]
oFileName = sys.argv[3]

f = open(fName, "r")
fLines = f.readlines()
f.close()

outLines = []

for line in fLines:
	line = line.strip()
	hFileName = dName + "/h" + line + ".txt"
	if (not os.path.exists(hFileName)):
		print "File Not Found!!!"
	f = open(hFileName, "r")
	oLines = f.readlines()
	f.close()
	oLines.append("\n")
	outLines.extend(oLines)

f = open(oFileName, "w")
f.writelines(outLines)
f.close()
