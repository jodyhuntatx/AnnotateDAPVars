#!/bin/bash 

source ./annotation.config

if [[ $# != 3 ]]; then
  echo "Usage: $0 <prop-value1> <prop-value2> <prop-value3>"
  exit -1
fi

SEARCHSTR="$1$2$3"

# delete & re-init the java key store
echo "Initializing Java key store..."
rm -f $JAVA_KEY_STORE_FILE
keytool -importcert -trustcacerts -file $CONJUR_CERT_FILE -keystore conjur.jks &> /dev/null <<EOF
$JAVA_KEY_STORE_PASSWORD
$JAVA_KEY_STORE_PASSWORD
yes
EOF

# run the app
java -cp ./dap/DAPJava.jar:./javarest/JavaREST.jar -jar FindByAnnotation.jar "$SEARCHSTR"

rm $JAVA_KEY_STORE_FILE
