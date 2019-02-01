#!/bin/bash
pushd `pwd`
cd `dirname $0`

echo "Putting cert for accounts.google.com"
echo | openssl s_client -connect accounts.google.com:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > accounts.google.com
keytool -delete -alias accounts.google.com  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias accounts.google.com -file accounts.google.com -keystore esg-truststore.ts -storepass changeit -noprompt


echo "Putting cert for ceda.ac.uk"
echo | openssl s_client -connect ceda.ac.uk:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > ceda.ac.uk
keytool -delete -alias ceda.ac.uk  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias ceda.ac.uk -file ceda.ac.uk -keystore esg-truststore.ts -storepass changeit -noprompt


echo "Putting cert for slcs.ceda.ac.uk"
echo | openssl s_client -connect slcs.ceda.ac.uk:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > slcs.ceda.ac.uk
keytool -delete -alias slcs.ceda.ac.uk  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias slcs.ceda.ac.uk -file slcs.ceda.ac.uk -keystore esg-truststore.ts -storepass changeit -noprompt

echo "Putting cert for slcs.ceda.ac.uk:7512"
echo | openssl s_client -connect slcs.ceda.ac.uk:7512 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > slcs.ceda.ac.uk7512
keytool -delete -alias slcs.ceda.ac.uk7512  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias slcs.ceda.ac.uk7512 -file slcs.ceda.ac.uk7512 -keystore esg-truststore.ts -storepass changeit -noprompt

echo "Putting cert for slcs1.ceda.ac.uk443"
echo | openssl s_client -connect slcs1.ceda.ac.uk:7512 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > slcs1.ceda.ac.uk443
keytool -delete -alias slcs1.ceda.ac.uk443  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias slcs1.ceda.ac.uk443 -file slcs1.ceda.ac.uk443 -keystore esg-truststore.ts -storepass changeit -noprompt

echo "Putting cert for slcs1.ceda.ac.uk7512"
echo | openssl s_client -connect slcs1.ceda.ac.uk:7512 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > slcs1.ceda.ac.uk7512
keytool -delete -alias slcs1.ceda.ac.uk7512  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias slcs1.ceda.ac.uk7512 -file slcs1.ceda.ac.uk7512 -keystore esg-truststore.ts -storepass changeit -noprompt


### Make sure that this service trusts itself by adding its certificate to the trust store ###

# 1) Export certificate from a keystore to a file called climate4impact-tomcat-ca.pem
keytool -export -alias tomcat -rfc -file climate4impact-tomcat-ca.pem -keystore c4i_keystore.jks -storepass password

# 2) Put this certificate from climate4impact-tomcat-ca.pem into the truststore
keytool -delete -alias climate4impact-tomcat-ca  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias climate4impact-tomcat-ca -file climate4impact-tomcat-ca.pem -keystore esg-truststore.ts -storepass changeit -noprompt


#eytool -import -v -trustcacerts -alias slcs.ceda.ac.uk -file slcs.ceda.ac.uk -keystore /usr/lib/jvm/java/jre/lib/security/cacerts -storepass changeit -noprompt


popd
