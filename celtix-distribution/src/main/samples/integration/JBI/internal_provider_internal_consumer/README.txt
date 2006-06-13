Overview 
========

Prerequisite:  This README assumes some familiarity with the Java
Business Integration specification.  See the following URL for more
information: http://java.sun.com/integration/

This demo shows how Celtix can be used to implement service
implementations for a Java Business Integration (JBI) container,. The
demo consists of a Celtix Service Engine and a test service assembly.
The service assembly contains two service units: a service provider (server)
and a service consumer (client).

In each case the, service units are written to the standard JAXWS 2.0
API.  The assembly is deployed to the Celtix Service Engine (CSE).
The CSE connects the service implementation (and client) to the JBI
Normalized Message Router (NMR) using a customized Celtix transport.

The JBI/NMR transport in this demo support InOut and InOnly message exchange pattern.

ServiceMix and OpenESB are two JBI container implementation we are
using here. Here we can see Celtix Service Engine can be deployed into
different JBI implementation without any change. 


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

Install the Celtix Service Engine:

 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  install-component -Dsm.install.file=./service-engine/build/lib/celtix-service-engine.jar
 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  start-component -Dsm.component.name=CeltixServiceEngine

Deploy the Celtix demo service assembly

 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  deploy-service-assembly -Dsm.deploy.file=./service-assembly/build/lib/celtix-service-assembly.zip 
 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml  start-service-assembly -Dsm.service.assembly.name=celtix-demo-service-assembly


More lifecycle management task
 > ant -f $SERVICE_MIX_HOME/ant/servicemix-ant-task.xml -projecthelp

Deploy Celtix Service Engine into OpenESB
=========================================
Build Instructions
------------------
. Download & Install Glassfish Application Server 
  https://glassfish.dev.java.net/downloads/04May06.html
  Note: must install 9.0 (Build 48 04-May-06) since previous version use
    early JAXB implementation which will conflict with Celtix

. export GLASSFISH_HOME for your shell environment

. Download & Install OpenESB JBI Framework (v 1.1)

. export OPEN_ESB_HOME for your shell environment

. export JBI_HOME=$GLASSFISH_HOME/domains/domain1/jbi

. Edit build.properties to sepcify celtix.home and jbi.sdk.jar,
  jbi.sdk.jar=$OPEN_ESB_HOME/appserver/jbi.jar

. build everything using ant: 'ant build'


Installation & Deployment
-------------------------

Ensure that the $GLASSFISH_HOME/bin and $JBI_HOME/bin is on the path.  If you deployed OpenESB
into the default domain (domain1) on Glassfish, this JBI_HOME can be
found in $GLASSFISH_HOME/domains/domain1/jbi.

Start OpenESB

 > asadmin start-domain domain1

You should see log like
Starting Domain domain1, please wait.
Log redirected to /local/glassfish/domains/domain1/logs/server.log

In server.log you can see ServiceEngine install log, Service Assembly deploy log, celtix service
consumer and provider communication log.


Install the Celtix Service Engine:

 > jbiadmin install-component service-engine/build/lib/celtix-service-engine.jar
 > jbiadmin start-component CeltixServiceEngine

Deploy the Celtix demo service assembly

 > jbiadmin deploy-service-assembly service-assembly/build/lib/celtix-service-assembly.zip
 > jbiadmin start-service-assembly celtix-demo-service-assembly

More lifecycle management task

 > jbiadmin help

Stop OpenESB
 > asadmin stop-domain domain1

What happened
=============
The SE will start both Serivce Units in the assembly.  The consumer is
coded to wait for the providers endpoint to activate.  Once the
provider endpoint has activated, the consumer sends messages to the
provider.  These messages are taken by the Celtix JBI transport,
wrapped in a NormalizedMessage and sent via the NMR to the service
provider.  The responses are sent back in a similar fashion. greetMe
use InOut MEP, sayHi use InOnly MEP.
