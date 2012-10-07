#!/usr/bin/python
import shlex, subprocess
import os

args = shlex.split("ps ax")
p = subprocess.Popen(args, stdout = subprocess.PIPE)
(stdout, stderr) = p.communicate()
outLines = stdout.split('\n')
for line in outLines:
	if (line.find("/System/Library/Frameworks/JavaVM.framework/Home/bin/java") != -1):
		javaCmdParts = line.split()
		os.system("kill -9 "+javaCmdParts[0])
