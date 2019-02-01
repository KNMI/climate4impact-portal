# climate4impact-portal
The aim of climate4impact is to enhance the use of research data and to support other climate portals. It has been developed within the European projects IS-ENES, IS-ENES2 and CLIPC. Climate4impact is connected to the Earth System Grid Federation, using certificate based authentication, ESGF search, openid, opendap and thredds catalogs. Climate4impact offers web interfaces for searching, visualizing, analyzing, processing and downloading datasets.  Climate4impact exposes open standards like WMS, WCS and WPS using open source tools.

See https://dev.knmi.nl/projects/impactportal/wiki for details


# Docker

The docker can be used to setup the climate4impact portal on your own workstation. It uses a default user to obtain a credential from a myproxyserver. Using the provided configuration inside the ./docker/c4i_config folder you can only log in as the default user.

## Known shortcomings:
- Due to configuration limitations the portal can only be run on port 444 (2019-02-01). You need to change ./docker/c4i_config/server.xml and the docker run command if you want a different port.
- The adaguc-server in this docker uses a sqlite database. Due to concurrent access to the sqlite database, sometimes a visualization of a datasets fails at the first time. Just press reload to see the visualization. This is a known bug with sqlite, with postgresql it works fine.
- You can only login as the user which is configured in ./docker/c4i_config/config.xml

# Clone the climate4impact-portal repository to your workstation
```
git clone https://github.com/maartenplieger/climate4impact-portal
cd climate4impact-portal
```
# Build the docker
```
docker build -t c4i  .
```
# Create your own keystore
```
rm docker/c4i_config/c4i_keystore.jks
keytool -genkey -noprompt -keypass password -alias tomcat \
  -keyalg RSA -storepass password -keystore ./docker/c4i_config/c4i_keystore.jks -deststoretype pkcs12 \
  -dname CN=${HOSTNAME}:444
 ```
# Update esg-truststore
```
cd docker/c4i_config && bash refresh_certs.sh && cd ../../
```

On your host, edit /etc/hosts and add a line:
```
127.0.1.1       <yourhostname>
```
You can find your hostname simply with
```
 hostname
 ```

# Make a folder for the impactspace 

This folder is mounted using the docker run command.
```
mkdir ~/impactspace
```

# Start the docker container:
```
docker run -v ~/impactspace:/impactspace -v /etc/hosts:/etc/hosts -v `pwd`/docker/c4i_config:/config/ -p 444:444 -e EXTERNAL_HOSTNAME:${HOSTNAME} -e EXTERNAL_ADDRESS_HTTPS="https://${HOSTNAME}:444/" -it c4i
```
# Log in
* Visit https://<yourhostname>/impactportal/account/login.jsp
* Add an exception for your self signed certificate
* Click "Show other providers" and select "Sign in with BADC/CEDA OpenID"
* Use account cc4idev/cc4idev123!
* Your openid is https://ceda.ac.uk/openid/C4I-Dev.C4I-Dev (this is preconfigured for the development env ./docker/c4i_config/config.xml)

# Start working
* Visit https://<yourhostname>/impactportal/account/processing.jsp

