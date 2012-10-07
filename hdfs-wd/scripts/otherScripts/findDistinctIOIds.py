#!/usr/bin/python

import sys
import os

if len(sys.argv) < 2:
	print('Correct usage: python findDistinctIOIds.py <input-file>')
	sys.exit(0)

dirName = sys.argv[1]
count = 0

for fName in os.listdir(dirName):
	if fName.find('runAppend') != -1:
		f = open(os.path.join(dirName, fName), "r")
		lines = f.readlines()
		f.close()

		for line in lines:
			if line.find('IO:WRITE') != -1:
				count = count + 1
				break

print "No of WRITE IO IDs " + str(count)

