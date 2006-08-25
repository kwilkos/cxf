Overview 
========

Prerequisite:  This README assumes some familiarity with the Java
Business Integration specification.  See the following URL for more
information: http://java.sun.com/integration/

This demo illustrate how internal celtix client which is deployed into
celtix service engine can communicate with external celtix server through a
generic JBI JMS binding component(as a router). 

The demo consists of a Celtix Service Engine and a ServiceMix JMS
binding component. A celtix service unit (as consumer) is deployed into
Celtix Service Engine. A servicemix JMS binding service unit(as
transport router) is deployed into ServiceMix JMS binding
component. Celtix service unit and ServiceMix JMS binding service
unit are wrapped in celtix demo service assembly.

A celtix client service unit(as consumer) invoke servicemix JMS binding
service unit using NMR, the servicemix JMS binding service
route this request to celtix standalone server using SOAP/JMS. Here servicemix JMS binding service unit play the role as a
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

Start celtix server
 > ant server  -Dthirdparty.classpath=$SERVICE_MIX_HOME/lib/activemq-core-4.0.jar:$SERVICE_MIX_HOME/lib/backport-util-concurrent-2.1.jar:$SERVICE_MIX_HOME/lib/activeio-core-3.0-beta3.jar

Install and start the Celtix Service Engine:

 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  install-component -Dsm.install.file=./service-engine/build/lib/celtix-service-engine.jar
 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  start-component -Dsm.component.name=CeltixServiceEngine

Install and start the ServiceMix jms binding component

 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml install-component  -Dsm.install.file=$SERVICE_MIX_HOME/components/servicemix-jms-3.0-SNAPSHOT-installer.zip 
 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  start-component -Dsm.component.name=servicemix-jms

Deploy the and start Celtix demo service assembly

 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  deploy-service-assembly -Dsm.deploy.file=./service-assembly/build/lib/celtix-service-assembly.zip 
 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  start-service-assembly -Dsm.service.assembly.name=celtix-demo-service-assembly





What happened
=============
A celtix client service unit(as consumer) invoke servicemix JMS binding
service unit using NMR, the servicemix JMS binding service
route this request to celtix standalone server using SOAP/JMS. Here servicemix JMS binding service unit play the role as a
router, connecting celtix service consumer and provider with different transport.
