#!/bin/sh

# Depends on https://github.com/wg/wrk
# brew install wrk
wrk -t10 -c100 -d5s --latency -s testconfig $1 http://localhost:8090


