Overview 
========

Prerequisite:  This README assumes some familiarity with the Java
Business Integration specification.  See the following URL for more
information: http://java.sun.com/integration/

This demo illustrate how external celtix client can communicate with internal celtix server
which is deployed into celtix service engine through a
generic JBI binding component(as a router). 

The demo consists of a Celtix Service Engine and a ServiceMix Soap
binding component. A celtix service unit (as provider) is deployed into
Celtix Service Engine. A servicemix soap binding service unit(as
transport router) is deployed into ServiceMix Soap binding
component. Celtix service unit and ServiceMix soap binding service
unit are wrapped in celtix demo service assembly.

A standalone celtix client(as consumer) invoke servicemix soap binding
service unit using soap/http, the servicemix soap binding service
route this request to celtix service unit using NMR specified by
JBI. Here servicemix soap binding service unit play the role as a
router, connecting celtix service consumer and provider with different transport.




Deploy Celtix Service Engine into ServiceMix
============================================
Build Instructions
------------------
. Download & Install ServiceMix 
  http://people.apache.org/maven-snapshot-repository/org/apache/servicemix/incubating-servicemix/3.0-SNAPSHOT/
  Note: Must isntall the lastest 3.0 SNAPSHOT version since any previous version still
	has bugs for Celtix ServiceMix integration.

. export SERVICE_MIX_HOME for your shell envirnoment

. Edit build.properties to sepcify celtix.home and jbi.sdk.jar,
  jbi.sdk.jar=$SERVICE_MIX_HOME/lib/servicemix-jbi-3.0-SNAPSHOT.jar

. remove ${SERVICE_MIX_HOME}/lib/optional/axis-saaj-1.3.jar to prevent
  saaj version conflict

. build everything using ant: 'ant build'

Installation & Deployment
-------------------------
Ensure that the $SERVICE_MIX_HOME/bin is on the path.

Start ServiceMix
 >servicemix
And then you can see logs from the shell which you start servicemix, including
ServiceEngine install log, Service Assembly deploy log, celtix service
consumer and provider communication log.

Install and start the Celtix Service Engine:

 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  install-component -Dsm.install.file=./service-engine/build/lib/celtix-service-engine.jar
 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  start-component -Dsm.component.name=CeltixServiceEngine

Install and start the ServiceMix soap binding component

 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml
 install-component  -Dsm.install.file=$SERVICE_MIX_HOME/components/servicemix-http-3.0-SNAPSHOT-installer.zip 
 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  start-component -Dsm.component.name=servicemix-http

Deploy the and start Celtix demo service assembly

 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  deploy-service-assembly -Dsm.deploy.file=./service-assembly/build/lib/celtix-service-assembly.zip 
 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  start-service-assembly -Dsm.service.assembly.name=celtix-demo-service-assembly

Start celtix client
 > ant client



What happened
=============
A standalone celtix client(as consumer) invoke servicemix soap binding
service unit using soap/http, the servicemix soap binding service
route this request to celtix service unit using NMR specified by
JBI. Here servicemix soap binding service unit play the role as a
router, connecting celtix service consumer and provider with different transport.
