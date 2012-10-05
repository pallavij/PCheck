#!/usr/bin/python
import os
import sys

if len(sys.argv) != 2:
	sys.exit("Must provide the dir containing the files")

dpath = sys.argv[1]

L = 0

for fname in os.listdir(dpath):
	if fname.find(".java") != -1:
		fpath = os.path.join(dpath, fname)
		f = open(fpath, "r")
		lines = f.readlines()
		f.close()
		L = L + len(lines)

print "LOC = " + str(L)	
