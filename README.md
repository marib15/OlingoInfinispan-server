# OlingoInfinispan-server
Návod na spustenie projektu OlingoInfinispan-server

## Inštalácia Apache Tomcat
Pre aplikáciu OlingoInfinispan-server je využívaný ako server Apache Tomcat. Pri vývoji bola používaná verzia 8.0,
Apache Tomcat je dostupný na adrese: tomcat.apache.org/. Je potrebné su ho stiahnuť a nainštalovať.

## Nastavenie vlastnosti Tomcatu
Aby boli Tomcety medzi sebou schopné sa vidieť (Clustering) je nutné toto nastavit v rámci Tomcatu a to nasledovne:<br />
1. pre OS Windows do súboru tomcat\bin\catalina.bat pridáme na 197. riadok nastavenie:<br />
<br />
     set "JAVA_OPTS=%JAVA_OPTS% -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Djgroups.bind_addr=127.0.0.1"
<br />     
pre Linux do súboru tomcat\bin\catalina.sh prid8me na 249. riado nastavenie:
<br /> 
<br /> 
JAVA_OPTS=" $JAVA_OPTS -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv4Addresses=true -Djgroups.bind_addr=127.0.0.1"
<br /> 
<br />
2. do súboru tomcat\conf\tomcat-users.xml pridáme nasledujúce nastavenie užívateľa:
<br />
\<role rolename="manager-gui"/>
<br />
\<role rolename="manager-script"/>
<br />
\<user username="admin" password="password" roles="manager-gui, manager-script"/>
<br />
<br />
3. V tomto momente vytvoríme kópiu nášho tomcatu. V kópii je následne nutné upraviť súbor tomcat\conf\server.xml <br/>
V súbore musíme prestaviť VŠETKY porty napríklad ich posunutím o +100, teda port:8080 bude prestavený na port:8180.
Naše tomcaty potom budú dostupné na rôznych portoch.

## Spustenie Tomcatu
Pre spustenie tomcatu, je potrebné zavolať cez príkazový riadok súbor na spustenie. Pre Windows je to <br />
tomcat\bin\startup.bat a pre Linux tomcat\bin\startup.sh. Po spustení je možné sa cez webový prehliadač dostať <br />
k manažérovi Tomcatu, pod adresou portu napr. localhost:8080. Do manažéra sa prihlásime pomocou prednastavených údajov.<br />
username = admin , password = password

## Nahratie súboru .war
Po vykonaní operácie Build nad projektom OlingoInfinispan-server dostaneme v podzložke projektu súbor .war, <br />
ktorý nahráme na naše tomcaty. V manažérovi Tomcatu v časti: WAR file to deploy, vložíme vytvorený súbor nášho projektu .war, a stlačíme tlačítko 
Deploy. OlingoInfinispan-server je pripravený na použitie.  
