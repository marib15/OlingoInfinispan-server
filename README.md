# OlingoInfinispan-server
Installation instructions for OlingoInfinispan-server

## Installation Apache Tomcat
OlingoInfinispan-server use Apache Tomcat server. During developmnet we used version 8.0,
Apache Tomcat is available on adress: tomcat.apache.org/. You need download and install.

## Setting Tomcat property
Tomcet need see, what other tomcats do. Therefor we use clustering, which is set up in Tomcat folder <br />
1. for OS Windows we add into file tomcat\bin\catalina.bat, on line 197.:<br />
<br />
     set "JAVA_OPTS=%JAVA_OPTS% -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Djgroups.bind_addr=127.0.0.1"
<br />     
for OS Linux we add into file tomcat\bin\catalina.sh, on line 249.:
<br /> 
<br /> 
JAVA_OPTS=" $JAVA_OPTS -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Djgroups.bind_addr=127.0.0.1"
<br /> 
<br />
2. into file tomcat\conf\tomcat-users.xml we add settings for user:
<br />
\<role rolename="manager-gui"/>
<br />
\<role rolename="manager-script"/>
<br />
\<user username="admin" password="password" roles="manager-gui, manager-script"/>
<br />
<br />
3. In this moment, we create copy tomcat. We edit file tomcat\conf\server.xml in created copy tomcat <br/>
We must change all ports, for example we add 100, so port:8080 will be port:8180. Every tomcat will be available <br/>
on other port, but ports will see changes other ports

## Start Tomcat
Tomcat is started on command line by file. For Windows is file tomcat\bin\startup.bat and for Linux is file:<br/> 
tomcat\bin\startup.sh . After starting of tomcat, we can open manager for tomcat in web browser on adress according <br/>
to default port, for example localhost:8080 . For loging in tomcat manager we use default name and password. <br/>
username = admin , password = password

## Deploying .war file
When we build projct OlingoInfinispan-server, we have file .war in subfolder. We deploy this file on our tomcats. <br/>
In tomcat manager, in part: WAR file to deploy, we add created .war file. OlingoInfinispan-server is ready for using.  


# Using OlingoInfinispan server

## Windows
For using on Window, we can recommend tool 'I am only!'. You can download on link: http://www.swensensoftware.com/im-only-resting

## Linux
For using on Linux, we can use tool CURL.
Example request for Linux:
### POST
curl -X POST -H ”content-type: application/json; charset=UTF-8” -d ’{”ID”:”dva”,”json”:”{\”ID\”:\”dva\”,\”name\”:\”Martin\”,
\”age\”:25}"}’ \<serviceroot\>/JSONs -v <br/>
We must escape letter ” in JSON dokument, because reader consider it like initial and end for String

### PUT
curl -X PUT -H ”content-type: application/json; charset=UTF-8” -d ’{\”ID\”:\”dva\”,\”name\”:\”Martin\”, \”age\”:25}’
\<serviceroot\>/JSONs\(\’key\’\) -v

### DELETE
curl -X DELETE -H ”Accept: application/json; charset=UTF-8”
\<serviceroot\>/JSONs\(\’key\’\)

### GET WITH KEY
curl -X GET -H ”Accept: application/json; charset=UTF-8”
\<serviceroot\>/JSONs\(\’1\’\)

### GET WITH FILTER
curl -X GET -H ”Accept: application/json; charset=UTF-8”
\<serviceroot\>/JSONs?\$filter=name%20eq%20Martin
