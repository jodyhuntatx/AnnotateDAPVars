#!/bin/bash

source ./annotation.config
java -cp ./pas:./dap -jar AnnotateDAPVars.jar "$@"
