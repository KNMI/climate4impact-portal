# climate4impact-portal
The aim of climate4impact is to enhance the use of research data and to support other climate portals. It has been developed within the European projects IS-ENES, IS-ENES2 and CLIPC. Climate4impact is connected to the Earth System Grid Federation, using certificate based authentication, ESGF search, openid, opendap and thredds catalogs. Climate4impact offers web interfaces for searching, visualizing, analyzing, processing and downloading datasets.  Climate4impact exposes open standards like WMS, WCS and WPS using open source tools.

See https://dev.knmi.nl/projects/impactportal/wiki for details


# Docker

```
# Clone the repo
git clone https://github.com/maartenplieger/climate4impact-portal
cd climate4impact-portal
# Build the docker
docker build -t c4i  .
# Create your own keystore
rm docker/c4i_config/c4i_keystore.jks
keytool -genkey -noprompt -keypass password -alias tomcat \
  -keyalg RSA -storepass password -keystore ./docker/c4i_config/c4i_keystore.jks -deststoretype pkcs12 \
  -dname CN=${HOSTNAME}:444
# Update esg-truststore
wget "https://github.com/ESGF/esgf-dist/raw/master/installer/certs/esg-truststore.ts" -O  ./docker/c4i_config/esg-truststore.ts
cd docker/c4i_config && bash refresh_certs.sh && cd ../../


```

add to /etc/hosts
```
127.0.1.1       <yourhostname>
```

Start
```
docker run  -v `pwd`/docker/c4i_config:/config/ -p 443:443 -e EXTERNAL_HOSTNAME:${HOSTNAME} -e EXTERNAL_ADDRESS_HTTPS="https://${HOSTNAME}/" -it c4i
```

* Visit https://<yourhostname>/impactportal/account/login.jsp
* Add an exception for your self signed certificate
* Click "Show other providers"
* Select "Sign in with BADC/CEDA OpenID"
* Use cc4idev/cc4idev123!
* Your openid is https://ceda.ac.uk/openid/C4I-Dev.C4I-Dev (this is preconfigured for the development env)



Visit https://<yourhostname>/impactportal/account/processing.jsp

