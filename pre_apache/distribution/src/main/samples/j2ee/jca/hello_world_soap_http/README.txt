HELLO WORLD (SOAP OVER HTTP) CELTIX J2EE DEMO
============================================
============================================

This demo will show how to connect with a Celtix Web service using a
Servlet deployed in an application server.


Running the Demo
================

There are a number of steps required to successfully run this demo
application:

    . Launch the application server
    . Deploy the Celtix J2EE Connector 
    . Build the demo
    . Deploy the web application to the application server
    . Launch the Celtix Server
    . Accessing the web application 


Launch the application server
=============================

    The demo requires an application server.  Make sure you have a
    running instance of an application server. 

    

Deploy the Celtix J2EE Connector
===============================

    The Celtix J2EE Connector must be deployed to the application
    server before running the demo.  A single resource adapter
    deployment will be shared by all of the demos, so this step need
    only be completed once.  


    How to deploy the Celtix J2EE Connector is dependent on your 
    application server. Please consult your vendor documentation
    on connector deployment. Here are basic instructions to deploy
    the connector in JBoss application servers.


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
CELTIX environment is correctly set. 

The demo may be built from the directory 
hello_world_soap_http.

Issue the command:

  (Unix)    % ant
  (Windows) > ant



Launch the CELTIX Service
========================

Run the CELTIX service provided by the basic/hello_world_soap_http
demo.

To launch the service:

1.  Move into the sample/hello_world/ 
directory.
2.  Start it_container and deploy a C++ Web service into it.
    Issue the command: 
  (Unix)    % ant server
  (Windows) > ant server


See hello_world/README.txt file for full details.


Deploying the demo WAR archive
==============================

How to deploy a WAR archive is dependent on your 
application server. Please consult your vendor documentation
on application deployment. Here are basic instructions to deploy
the demo application for JBoss, WebLogic and WebSphere application 
servers.

JBoss
-----
Copy the WAR archive ./build/lib/helloworld.war 
to the JBoss deployment directory.
  
  (Unix)    % cp ./build/lib/helloworld.war \ 
              <jboss-home>/server/default/deploy
  (Windows) > copy .\build\lib\helloworld.war 
              <jboss-home>\server\default\deploy



Accessing the web application 
=============================

Using a web browser access the URI below corresponding to your
application server. (These URI assume that the application
server is running in the same machine as the web browser)

JBoss
-----
http://localhost:8080/helloworld/*.do


The web application provides a simple Web front-end to the Hello World
Application. 



