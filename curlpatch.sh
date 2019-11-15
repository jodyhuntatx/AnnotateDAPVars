#!/bin/bash
AUTH_HEADER=$1
BODY_CONTENT=$2
REQUEST_URL=$3
echo "curl -sk -H \"Accept: application/json\" -H \"Content-Type: application/json\" -H \"Authorization: $AUTH_HEADER\" -XPATCH -d \"$BODY_CONTENT\" $REQUEST_URL" >> debug.out
curl -sk -H \"Accept: application/json\" -H \"Content-Type: application/json\" -H \"Authorization: $AUTH_HEADER\" -XPATCH -d \"$BODY_CONTENT\" $REQUEST_URL
