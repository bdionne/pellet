#!/bin/bash -e

HOST=$1
USER=$2
PASS=$3

curl -v -X GET --basic --user $USER:$PASS $HOST/admin/restart
