# climate4impact-portal
The aim of climate4impact is to enhance the use of research data and to support other climate portals. It has been developed within the European projects IS-ENES, IS-ENES2 and CLIPC. Climate4impact is connected to the Earth System Grid Federation, using certificate based authentication, ESGF search, openid, opendap and thredds catalogs. Climate4impact offers web interfaces for searching, visualizing, analyzing, processing and downloading datasets.  Climate4impact exposes open standards like WMS, WCS and WPS using open source tools.

See https://dev.knmi.nl/projects/impactportal/wiki for details


# Docker

```
git clone https://github.com/maartenplieger/climate4impact-portal
cd climate4impact-portal
docker build -t c4i  .
rm docker/c4i_config/c4i_keystore.jks
keytool -genkey -noprompt -keypass password -alias tomcat \
  -keyalg RSA -storepass password -keystore ./docker/c4i_config/c4i_keystore.jks -deststoretype pkcs12 \
  -dname CN=${HOSTNAME}
wget "https://github.com/ESGF/esgf-dist/raw/master/installer/certs/esg-truststore.ts" -O  ./docker/c4i_config/esg-truststore.ts
```

add to /etc/hosts
```
127.0.1.1       <yourhostanme>
```

Start
```
docker run  -v `pwd`/docker/c4i_config:/config/ -p 443:443 -e EXTERNAL_HOSTNAME:${HOSTNAME} -e EXTERNAL_ADDRESS_HTTPS="https://${HOSTNAME}/" -it c4i
```

Visit https://<yourhostanme>/impactportal/account/login.jsp
Visit https://<yourhostname>/impactportal/account/processing.jsp

