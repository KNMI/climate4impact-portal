FROM centos:7

RUN yum update -y && yum install -y \
    epel-release

RUN yum clean all && yum groupinstall -y "Development tools"

RUN yum update -y && yum install -y \
    hdf5-devel \
    netcdf \
    netcdf-devel \
    proj \
    proj-devel \
    sqlite \
    sqlite-devel \
    udunits2 \
    udunits2-devel \
    make \
    # conda dependency
    bzip2 \
    # adaguc dependencies
    libxml2-devel \
    cairo-devel \
    gd-devel \
    postgresql-devel \
    # java dependencies
    ant \
    tomcat \



RUN mkdir /scr
WORKDIR /src

# conda
RUN curl -L -O https://repo.continuum.io/miniconda/Miniconda2-latest-Linux-x86_64.sh
RUN bash ./Miniconda2-latest-Linux-x86_64.sh -p /miniconda -b
ENV PATH=/miniconda/bin:${PATH}
RUN conda update -y conda
RUN conda config --add channels conda-forge

# isntall python dependencies
RUN yes | pip install --upgrade pip
RUN conda update -y conda && conda install -y \
    lxml \
    numpy \
    scipy \
    netcdf4 \
    python-dateutil \
    isodate \
    psycopg2 \
    requests \
    prov \
    pydotplus

RUN pip install python-magic

# install icclim (will be conda in the future)
RUN curl -L -O https://github.com/cerfacs-globc/icclim/archive/4.2.5.tar.gz
RUN tar xvf 4.2.5.tar.gz
WORKDIR /src/icclim-4.2.5
RUN gcc -fPIC -g -c -Wall ./icclim/libC.c -o ./icclim/libC.o
RUN gcc -shared -o ./icclim/libC.so ./icclim/libC.o
RUN python setup.py install

# install clipc combine toolkit
RUN pip install https://dev.knmi.nl/projects/clipccombine/repository/raw/dist/clipc_combine_process-1.6.tar.gz

# install provenance toolkit
WORKDIR /src
RUN curl -L -O https://github.com/KNMI/wps_prov/archive/master.tar.gz
RUN tar xvf master.tar.gz
WORKDIR /src/wps_prov-master
RUN python setup.py install

# install pywps
WORKDIR /src
RUN curl -L -O https://github.com/geopython/pywps/archive/pywps-3.2.5.tar.gz
RUN tar xvf pywps-3.2.5.tar.gz
# the rest is setting up the env variables but those paths will depend on monted data dir

# install adaguc
WORKDIR /src
RUN curl -L  https://github.com/KNMI/adaguc-server/archive/master.tar.gz > adaguc-server.tar.gz
RUN tar xvf adaguc-server.tar.gz
WORKDIR /src/adaguc-server-master
ENV ADAGUCCOMPONENTS="-DENABLE_CURL -DADAGUC_USE_POSTGRESQL"
RUN bash compile.sh

# install impact portal
WORKDIR /src
RUN curl -L https://github.com/KNMI/climate4impact-portal/archive/master.tar.gz > climate4impact.tar.gz
RUN tar xvf climate4impact.tar.gz
WORKDIR /src/climate4impact-portal-master
ENV TOMCAT_LIBS=/usr/share/java/tomcat
RUN ant
# configure server
RUN mv /usr/share/tomcat/conf/server.xml /usr/share/tomcat/conf/server_oryg.xml
RUN ln -s /config/server.xml /usr/share/tomcat/conf/server.xml
RUN cp /src/climate4impact-portal-master/impactportal.war /usr/share/tomcat/webapps/
ENV IMPACTPORTAL_CONFIG=/config/config.xml
