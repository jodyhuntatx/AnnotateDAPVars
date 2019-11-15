#!/bin/bash -x
GSON=./gson/gson-2.8.5.jar
PAS=./pas/PASJava.jar
DAP=./dap/DAPJava.jar
JAVAREST=./javarest/JavaREST.jar

javac -cp $GSON:$PAS:$DAP:$JAVAREST AnnotateDAPVars.java 
echo "Main-Class: AnnotateDAPVars" > manifest.txt
echo "Class-Path: $GSON $PAS $DAP $JAVAREST" >> manifest.txt
jar cvfm AnnotateDAPVars.jar manifest.txt *.class 

javac -cp $GSON:$DAP:$JAVAREST FindByAnnotation.java 
echo "Main-Class: FindByAnnotation" > manifest.txt
echo "Class-Path: $GSON $DAP $JAVAREST" >> manifest.txt
jar cvfm FindByAnnotation.jar manifest.txt *.class 

rm manifest.txt
