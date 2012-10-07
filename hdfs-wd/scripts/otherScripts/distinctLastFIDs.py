#!/usr/bin/python

import sys

if len(sys.argv) < 3:
	print('Correct usage: python distinctLastFIDs.py <Name of file that has paths with failure injection points> <file in which to write the distinct FIDs>')
	sys.exit(0)

f = open(sys.argv[1], "r")
lines = f.readlines()

lastFIds = []

for line in lines:
	fIds = line.split()
	lastFId = fIds[-1]
	if (not ((lastFId + '\n') in lastFIds)):
		lastFIds.append(lastFId + '\n')
#print len(lastFIds)
f.close()


f = open(sys.argv[2], 'w')
f.writelines(lastFIds)
f.close()
