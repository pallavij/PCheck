#!/usr/bin/python

import sys
import os

if len(sys.argv) < 4:
	print('Correct usage: ./findDiffOfTwoFiles.py <file1> <file2> <fileToWrite>')
	sys.exit(0)

f = open(sys.argv[1], "r")
lines1 = f.readlines()
f.close()

f = open(sys.argv[2], "r")
lines2 = f.readlines()
f.close()

diff = set(lines1) - set(lines2)
f = open(sys.argv[3], "w")
f.writelines(list(diff))
f.close()
