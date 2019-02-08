#!/bin/bash
pushd `pwd`
cd `dirname $0`




wget "https://github.com/ESGF/esgf-dist/raw/master/installer/certs/esg-truststore.ts" -O  esg-truststore.ts

echo "Putting cert for accounts.google.com"
echo | openssl s_client -connect accounts.google.com:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > accounts.google.com.pem
keytool -delete -alias accounts.google.com  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias accounts.google.com -file accounts.google.com.pem -keystore esg-truststore.ts -storepass changeit -noprompt


# 
# echo "Putting cert for ceda.ac.uk"
# echo | openssl s_client -connect ceda.ac.uk:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > ceda.ac.uk.pem
# keytool -delete -alias ceda.ac.uk  -keystore esg-truststore.ts -storepass changeit > /dev/null
# keytool -import -v -trustcacerts -alias ceda.ac.uk -file ceda.ac.uk.pem -keystore esg-truststore.ts -storepass changeit -noprompt
# 
# 
# echo "Putting cert for slcs.ceda.ac.uk"
# echo | openssl s_client -connect slcs.ceda.ac.uk:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > slcs.ceda.ac.uk.pem
# keytool -delete -alias slcs.ceda.ac.uk  -keystore esg-truststore.ts -storepass changeit > /dev/null
# keytool -import -v -trustcacerts -alias slcs.ceda.ac.uk -file slcs.ceda.ac.uk.pem -keystore esg-truststore.ts -storepass changeit -noprompt
# 
# # echo "Putting cert for slcs.ceda.ac.uk:7512"
# # echo | openssl s_client -connect slcs.ceda.ac.uk:7512 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > slcs.ceda.ac.uk7512
# # keytool -delete -alias slcs.ceda.ac.uk7512  -keystore esg-truststore.ts -storepass changeit > /dev/null
# # keytool -import -v -trustcacerts -alias slcs.ceda.ac.uk7512 -file slcs.ceda.ac.uk7512 -keystore esg-truststore.ts -storepass changeit -noprompt
# # 
# echo "Putting cert for slcs1.ceda.ac.uk443"
# echo | openssl s_client -connect slcs1.ceda.ac.uk:443 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > slcs1.ceda.ac.uk443.pem
# keytool -delete -alias slcs1.ceda.ac.uk443  -keystore esg-truststore.ts -storepass changeit > /dev/null
# keytool -import -v -trustcacerts -alias slcs1.ceda.ac.uk443 -file slcs1.ceda.ac.uk443.pem -keystore esg-truststore.ts -storepass changeit -noprompt
# # 
# echo "Putting cert for slcs1.ceda.ac.uk7512"
# echo | openssl s_client -connect slcs1.ceda.ac.uk:7512 2>&1 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'  > slcs1.ceda.ac.uk7512.pem
# keytool -delete -alias slcs1.ceda.ac.uk7512  -keystore esg-truststore.ts -storepass changeit > /dev/null
# keytool -import -v -trustcacerts -alias slcs1.ceda.ac.uk7512 -file slcs1.ceda.ac.uk7512.pem -keystore esg-truststore.ts -storepass changeit -noprompt

### Instal ESGF certs ###
#rm -rf esg_trusted_certificates*
curl -L https://raw.githubusercontent.com/ESGF/esgf-dist/master/installer/certs/esg_trusted_certificates.tar > esg_trusted_certificates.tar
tar -xvf esg_trusted_certificates.tar > /dev/null
mkdir -p certificates
for file in esg_trusted_certificates/*.0;do 
  a=${file##*/}
  base=${file%.*}
  signing_policy_file="${base}".signing_policy
  # Filter subject and issuer
  if [ -f $signing_policy_file ]; then
    cp $file $signing_policy_file ./certificates
  fi
done


### Make sure that this service trusts itself by adding its certificate to the trust store ###

# 1) Export certificate from a keystore to a file called climate4impact-tomcat-ca.pem
keytool -export -alias tomcat -rfc -file climate4impact-tomcat-ca.pem -keystore c4i_keystore.jks -storepass password

# 2) Put this certificate from climate4impact-tomcat-ca.pem into the truststore
keytool -delete -alias climate4impact-tomcat-ca  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias climate4impact-tomcat-ca -file climate4impact-tomcat-ca.pem -keystore esg-truststore.ts -storepass changeit -noprompt

#3) Put this certificate from climate4impact-tomcat-ca.pem into the globus truststore
cp climate4impact-tomcat-ca.pem ./certificates/
c_rehash ./certificates/

### Add the impactportal CA for issuing certificates in the truststore as well ###

echo "Putting impactportal CA into truststore"
keytool -delete -alias climate4impact-certissuer-ca  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias climate4impact-certissuer-ca -file impactportal_CA.pem -keystore esg-truststore.ts -storepass changeit -noprompt

cp impactportal_CA.pem ./certificates/
c_rehash ./certificates/

### Add the clipc CA ###

echo "Putting CLIPC CA into truststore"
keytool -delete -alias knmi_clipc_ca  -keystore esg-truststore.ts -storepass changeit > /dev/null
keytool -import -v -trustcacerts -alias knmi_clipc_ca -file knmi_clipc_ca.pem -keystore esg-truststore.ts -storepass changeit -noprompt

cp knmi_clipc_ca.pem ./certificates/
c_rehash ./certificates/


popd
