#!/bin/sh



# ------------------------- 
function FATAL {
    echo ""
    echo "# FATAL ERROR: $1"
    echo ""
    sleeping 3600
}



# ----------------------
function init {

    ZOOK_HOME=${HOME}/zook-fi
    ZHM=$ZOOK_HOME

    COV_SCRIPT_DIR=$ZHM/mysrc/coverage-script/

    HISTORY_FILE=dat/history.txt
    STATES_FILE=dat/states.txt    
    UNCOVERED_FILE=dat/uncovered.txt

    QUORUM_DIR=$ZHM/quorum

    LOGS_DIR=/tmp/fi/logs

    cd $COV_SCRIPT_DIR
    thisDir=`pwd`
    echo ""
    echo "   Working dir: $thisDir"
    echo ""
    
}




# ----------------------
function section {

    echo ""
    echo "# #################### $1"
    echo ""
}


# ----------------------
function clean {
    section "Clean"
    cd $COV_SCRIPT_DIR
    rm -vf dat/*
    rm -vf src/*
    rm -fv .tmp-*
}

# ----------------------
function finish {
    section "Finish"
    rm -fv .tmp-*
    echo ""
    echo ""
}
