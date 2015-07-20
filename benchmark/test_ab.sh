#!/bin/sh

# Depends on ApacheBench https://httpd.apache.org/docs/2.2/programs/ab.html

#
# On Debian
# ----------
# sudo apt-get install apache2-utils
#
# Included on Mac OS (nothing to do)
#
ab -n 100000 -c 10 -k "http://localhost:8090/search?q=Kosmos"
