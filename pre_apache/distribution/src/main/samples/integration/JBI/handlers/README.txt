Overview 
========

Prerequisite:  This README assumes some familiarity with the Java
Business Integration specification.  See the following URL for more
information: http://java.sun.com/integration/

This demo shows how JAXWS handlers can be used in celtix service
engine.  The demo consists of a Celtix Service Engine and a test service assembly.
The service assembly contains two service units: a service provider (server)
and a service consumer (client).

In each case the, service units are written to the standard JAXWS 2.0
API.  The assembly is deployed to the Celtix Service Engine (CSE).
The CSE connects the service implementation (and client) to the JBI
Normalized Message Router (NMR) using a customized Celtix transport.


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


What happened
=============
The service provider uses a
SOAP protocol handler which simply logs incoming and outgoing messages
to the console.  

The service provider code registers a handler using the @HandlerChain annotation
within the service implementation class. For this demo, LoggingHandler
is SOAPHandler that logs the entire SOAP message content to stdout.

While the annotation in the service implementation class specifies
that the service provider should use the LoggingHandler, the demo shows how
this behaviour is superceded by information obtained from the
celtix-server.xml configuration file, thus allowing control over the
service provider's behaviour without changing the code.  When the
service provider uses the configuration file, LoggingHandler is replaced with
FileLoggingHandler, which logs simple informative messages, not the
entire message content, to the console and adds information to the
demo.log file.

The service consumer includes a logical handler that checks the parameters on
outbound requests and short-circuits the invocation in certain
circumstances. This handler is not specified programatically but
through configuration in the file celtix-client.xml.  