#!/usr/bin/python
import sys
import runExp
from policies import onefail


MAX = 1
inclPostFIds = False
useEqvCl = False
rIdOption = -1
clsLast = False
policies = []

#Restrict the number of exps for one failure
limExpsPolicy = onefail.OneFail()
policies.append(limExpsPolicy)

runExp.runExp(MAX, policies, useEqvCl, clsLast, rIdOption, inclPostFIds)
