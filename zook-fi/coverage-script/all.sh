#!/bin/sh


# ----------------------
function runAll {
    section "Run All"

    call getCoverage

}

# ----------------------
function call {
    $1 | tee /tmp/out.txt
}


# ----------------------
function include {
    source util.sh
    source coverage.sh
}



# ----------------------
# include
# ----------------------
include


# ----------------------
# init
# ----------------------
init


# ----------------------
# run special
# ----------------------
if [ $# != 0 ]; then
    section "Custom: $*"
    $*
    exit
fi


# ----------------------
# run al
# ----------------------
runAll


# ----------------------
# finish
# ----------------------
finish
