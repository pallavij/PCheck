#!/usr/bin/python
import sys
import runExp
from policies import twofail


MAX = 2
inclPostFIds = False
useEqvCl = False
rIdOption = -1
clsLast = False
policies = []

#Restrict the number of exps for two failures
limExpsPolicy = twofail.TwoFail()
policies.append(limExpsPolicy)

runExp.runExp(MAX, policies, useEqvCl, clsLast, rIdOption, inclPostFIds)
