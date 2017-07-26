#!/bin/bash -e

HOST=$1
PASS=$2

USER="pellet"

curl -v -X GET --basic --user $USER:$PASS $HOST/admin/shutdown
