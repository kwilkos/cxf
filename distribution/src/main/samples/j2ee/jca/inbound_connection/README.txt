INBOUND CONNECTION CELTIX J2EE DEMO
==================================
==================================

 
This demo will show how to expose an Enterprise Java Bean over
SOAP/HTTP using Celtix. 


Running the Demo
================

There are a number of steps required to successfully run this demo
application:

    . Launch the application server
    . Deploy the Celtix J2EE Connector 
    . Build the demo
    . Deploy the ejb application to the application server
    . Activate the EJB Web Services facade
    . Access the EJB using a Web Services client


Launch the application server
=============================

    The demo requires an application server.  Make sure you have a
    running instance of an application server. 

Deploy the Celtix J2EE Connector
===============================
    
    Before deploy Celtix J2EE Connector, must update
    <config-property-value> with abstract path of CELTIX_HOME in 
    ra.xml file in $CELTIX_HOME/lib/Celtix.rar file.

    The Celtix J2EE Connector must be deployed to the application
    server before running the demo.  A single resource adapter
    deployment will be shared by all of the demos, so this step need
    only be completed once.  


    How to deploy the Celtix J2EE Connector is dependent on your 
    application server. Please consult your vendor documentation
    on connector deployment. Here are basic instructions to deploy
    the connector in JBoss, WebLogic and WebSphere application 
    servers.



JBoss
-----
Copy the connector RAR from its location in the Celtix installation to
the JBoss deployment directory.

  (Unix)    % cd $CELTIX_HOME/lib/
            % cp Celtix.rar \ 
              <jboss-home>/server/default/deploy

  (Windows) > cd %CELTIX_HOME%\lib\
            > copy Celtix.rar 
              <jboss-home>\server\default\deploy

Copy the celtix_j2ee_1_5-ds.xml file to the JBoss deployment directory.

  (Unix)    % cp ./etc/celtixj2ee_1_5-ds.xml <jboss-home>/server/default/deploy

  (Windows) > copy .\etc\celtixj2ee_1_5-ds.xml 
                <jboss-home>\server\default\deploy


Building the Demo
=================

Building the demo requires that there is a JDK available and that the
Celtix environment is correctly set. 

The demo may be built from the directory 
inbound_connection.

Issue the command:

  (Unix)    % ant
  (Windows) > ant



Deploying the demo EJB application
==================================

How to deploy an EJB application archive is dependent on your
application server. Please consult your vender documentation on
application deployment. Here are basic instructions to deploy the
demo application for JBoss, WebLogic and WebSphere application
servers.

JBoss
-----
Copy the EJB archive ./j2ee-archives/greeterejb.jar 
to the JBoss deployment directory.
  
  (Unix)    % cp ./j2ee-archives/greeterejb.jar \ 
              <jboss-home>/server/default/deploy
  (Windows) > copy .\j2ee-archives\greeterejb.jar 
              <jboss-home>\server\default\deploy

Activate the EJB Web Services facade
====================================

Exposing EJBs as Web Services in the Artix J2EE Connector is
controlled by a properties files called ejb_servants.properties.  The
file is located in $CELTIX_HOME/etc, by
default. The ant build script for this demo will automatically update
this file, so please ensure that you have write permissions for this
file:

    ant activate

The location of this file is configurable via the
EJBServantPropertiesURL property.

Please see the documentation for further information on the contents
of the properties files and how it is used. 

NOTE: The Celtix J2EE Connector will check this file every 30 seconds
so it will be necessary to wait this length of time before running the
client. 


Running the Demo
================


Once the resource adapter and the EJB application have been deployed,
the client can be run with the ant build script: 

    ant client 

This will launch an Artix Java Client which contacts the web service
enabled EJB. 















