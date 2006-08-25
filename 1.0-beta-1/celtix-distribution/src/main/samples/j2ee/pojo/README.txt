J2EE POJO Demo
==============

J2EE provides support for deploying Web Service endpoints as part of a
J2EE application.  These endpoints can be either POJO classes deployed
as part of a Web Application or Stateless Session Bean as part of an
EJB Application.  J2EE 1.4 supports JAX-RPC 1.x, not JAX-WS.  For more
information see here:
http://java.sun.com/developer/technicalArticles/J2EE/j2ee_ws/

Celtix can be used to extend the functionality of an application
server like Geronimo 1.x to allow the deployment of JAX-WS based web
services.

This demo provides a POJO web services implemented using JAX-WS.  The
implementation of this service is identical to that in the hello_world
demo, as is the client.  The only difference is how the service is
packaged and deployed.  The war has the following structure:

WEB-INF/wsdl/hello_world.wsdl
WEB-INF/webservices.xml
WEB-INF/web.xml
WEB-INF/geronimo-web.xml
WEB-INF/classes/

The webservices.xml file contains information about which ports and
services are to be deployed.  Normally, the web.xml describes the Java
class that implements a servlet .  In this case, the servlet-class
specifies the WS service implementation.  The geronimo-web.xml
contains Geronimo specific deployment information such as the context
root of the deployed application.

Prerequisites
=============

A special build of Geronimo that allows the Celtix runtime to be
deployed is required. See the Celtix Wiki at: 
    https://wiki.objectweb.org/celtix/Wiki.jsp?page=CeltixGeronimo
for more information.

The Geronimo installation directory will be referred to as GHOME.  Add
$GHOME/bin (Unix) or %GHOME%\bin (Windows) to your PATH environment
variable.

Deployment
==========

o Extract the Geronimo package.  This location will be referred to as
GHOME.	

o Create the directory $GHOME/repository/celtix/jars

o Copy the jars from $CELTIX_HOME/lib to $GHOME/repository/celtix/jars

o Start the Geronimo application server: 
  java -jar  $GHOME/bin/server.jar (in another window)
  or
  $GHOME/bin/startup.sh
  The latter command will start the server in the background.

  Note: Geronimo is not currently certified on JDK 1.5 and starting
  	and the server will issue a warning on startup.


o Deploy the Celtix GBean to Geronimo
  
  $ deploy.sh ./geronimo-plan.xml

  This will report success with the following message

       Deployed geronimo/celtix-deployer/1.2/car


o Build the war using ant: 
  
  % ant build 

o Deploy the WAR to the application server

   o For Geronimo: 

   deploy ./build/lib/wspojo.war 

o Run the client using ant

    ant client 



  


     

