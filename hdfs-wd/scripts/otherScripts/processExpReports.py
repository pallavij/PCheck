#!/usr/bin/python
import os
import string

path = "/tmp/fi/expResult"

def contains(str, substr):
	  return str.find(substr) > -1

for f in os.listdir(path):
	if (not contains(f, 'wiped')):
		exppath = os.path.join(path, f)
		for hashf in os.listdir(exppath):
			if contains(hashf, 'fsn1'):
				hashf.replace('fsn1-', '')
				hashf.replace('.txt', '')
				print(f + ' ' + hashf)
