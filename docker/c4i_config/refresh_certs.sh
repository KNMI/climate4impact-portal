#!/bin/bash
pushd `pwd`
cd `dirname $0`

echo "Putting cert for accounts.google.com"
echo | openssl s_client -connect accounts.google.com:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > accounts.google.com
keytool -delete -alias accounts.google.com  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias accounts.google.com -file accounts.google.com -keystore esg-truststore.ts -storepass changeit -noprompt

echo "Putting cert for slcs.ceda.ac.uk"
echo | openssl s_client -connect slcs.ceda.ac.uk:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > slcs.ceda.ac.uk
keytool -delete -alias slcs.ceda.ac.uk  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias slcs.ceda.ac.uk -file slcs.ceda.ac.uk -keystore esg-truststore.ts -storepass changeit -noprompt
popd
