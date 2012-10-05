#!/bin/sh

# ----------------------
function getCoverage {

    section "Get Coverage"
    copySourceFiles
    grepStatesFromSource
    grepStatesFromLogs
    analyzeUncovered
    printCovered
    printUncovered
    getCountInfo
    
}

# ----------------------------
function getCountInfo {

    section "Get Info"

    statesCnt=`awk 'END { print NR }' $STATES_FILE`
    historyCnt=`awk 'END { print NR }' $HISTORY_FILE`
    uncoveredCnt=`awk 'END { print NR }' $UNCOVERED_FILE`

    echo "    States    : $statesCnt lines"
    echo "    History   : $historyCnt lines"  
    echo "    Uncovered : $uncoveredCnt lines"

}

# ----------------------------
function printCovered {
    section "Covered states"
    cat -n $HISTORY_FILE > .tmp-1
    sed -e 's/     /    0/' .tmp-1 > .tmp-2
    cat .tmp-2
}

# ----------------------------
function printUncovered {
    section "Uncovered states"
    cat -n $UNCOVERED_FILE > .tmp-1
    sed -e 's/     /    0/' .tmp-1 > .tmp-2
    cat .tmp-2
}

# ----------------------------
function analyzeUncovered {

    section "Analyze uncovered"
    
    # analyze uncovered
    comm -23 $STATES_FILE $HISTORY_FILE > $UNCOVERED_FILE

}




# ----------------------
# put in states_file
# ----------------------
function grepStatesFromSource {

    section "Grep states from source"
    
    grep -h ">> FLE.l " src/*.java > .tmp-1
    grep -h ">> QP.rn " src/*.java > .tmp-2
    
    cat .tmp-1 .tmp-2 > .tmp-states

    filterStates .tmp-states
    cp .tmp-states $STATES_FILE


}



# ----------------------
# add to HISTORY_FILE
# ----------------------
function grepStatesFromLogs {

    section "Grep states from Logs"

    touch $HISTORY_FILE
    
    grep -h ">> FLE.l " $LOGS_DIR/*.out > .tmp-1
    grep -h ">> QP.rn "  $LOGS_DIR/*.out > .tmp-2

    cat .tmp-1 .tmp-2 > .tmp-history

    filterStates .tmp-history
    
    cat .tmp-history >> $HISTORY_FILE
    
    sort -u $HISTORY_FILE > .tmp-9
    cp .tmp-9 $HISTORY_FILE

}


# ----------------------
# - only print 2nd and 3rd column, and
# - important states have "-" (e.g. NT-")
# ----------------------
function filterStates {

    if [ $# != 1 ]; then
	FATAL "filter states no arg"
    fi

    ff=$1
    
    awk '{print $2, $3}' $ff > .tmp-1
    
    grep "-" .tmp-1 > .tmp-2

    sort -u .tmp-2 > .tmp-3

    cp .tmp-3 $ff

}







# ----------------------
function copySourceFiles {
    cd $COV_SCRIPT_DIR
    cp $ZHM/quorum/FastLeaderElection.java src/
    cp $ZHM/quorum/QuorumPeer.java src/
}
