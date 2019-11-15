#!/bin/bash -
PASACCOUNT_SRC="PASAccountList.java PASAccount.java PASPlatformProperties.java PASSecretManagement.java PASRemoteMachinesAccess.java"
PASACCOUNTDETAILS_SRC="PASAccountDetailList.java PASAccountDetail.java KeyValue.java" 
PASGROUP_SRC="PASAccountGroup.java PASAccountGroupMember.java"
javac -cp ../gson/gson-2.8.5.jar:../javarest/JavaREST.jar  PASJava.java $PASACCOUNT_SRC $PASACCOUNTDETAILS_SRC $PASGROUP_SRC
jar cvf PASJava.jar *.class 
