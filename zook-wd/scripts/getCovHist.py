#!/usr/bin/python
import os
import sys
import math

def findNumSubs(dirPath, subPfx):
	count = int(0)
	for fname in os.listdir(dirPath):
		if fname.startswith(subPfx):
			count = count + 1
	return count		



if len(sys.argv) != 4:
	sys.exit("Must provide the dir name that has coverage html files (covFiles), the number of files to jump an interval by, and the number of files whose coverage we want to compute")

covHtmlPDir = sys.argv[1]
W = int(sys.argv[2])
NC = int(sys.argv[3])

covHtmlDir = os.path.join(covHtmlPDir, "covFiles")
if(os.path.exists(covHtmlDir)):
	
	N = findNumSubs(covHtmlDir, "exp-")
	#N = 100
	nSteps = math.ceil(float(N)/W)

	print "N = " + str(N) + " W = " + str(W) + " nSteps = " + str(nSteps) 
 
	histDataPath = os.path.join(covHtmlPDir, "histData")
	os.system("mkdir " + histDataPath)
	
	for i in range(0, int(nSteps)):
		histDirElemPath = os.path.join(histDataPath, "h"+str(i+1))
		os.system("mkdir " + histDirElemPath)
		tmpPath = os.path.join(histDirElemPath, "covFiles")
		os.system("mkdir " + tmpPath)

		upper = W*(i+1)
		if upper > N:
			upper = N

		for j in range(0, upper):
			expPath = os.path.join(covHtmlDir, "exp-" + str(j+1))
			os.system("cp -r " + expPath + " " + tmpPath)

		os.system("./scripts/findAllUncoveredLNos.py " + histDirElemPath + " cmd.sh")

		processedDir = os.path.join(histDirElemPath, "processed-files")
		os.system("chmod +x cmd.sh")
		os.system("./cmd.sh")
		os.system("rm cmd.sh")

		for i in range(1, NC+1):
			os.system("./scripts/allUncoveredLNos.py " + processedDir + " " + str(i))
	
			os.system("cp uncoveredLines" + str(i) + ".txt " + histDirElemPath)
			os.system("rm uncoveredLines" + str(i) + ".txt")
			
		os.system("rm -rf " + tmpPath)
		os.system("rm -rf " + processedDir)

	os.system("./scripts/totLineNos.py " + histDataPath)
	
else:
	sys.exit("No covFiles directory present...Exiting!")
	


