Hello World Demo using HTTPS communications
=============================================

This demo takes the hello world demo a step further 
by doing the communication using HTTPS.

Please review the README in the samples directory before
continuing.



Prerequisite
------------

If your environment already includes cxf-manifest-incubator.jar on the
CLASSPATH, and the JDK and ant bin directories on the PATH
it is not necessary to run the environment script described in
the samples directory README.  If your environment is not
properly configured, or if you are planning on using wsdl2java,
javac, and java to build and run the demos, you must set the
environment by running the script.



Building and running the demo using ant
---------------------------------------

From the samples/hello_world_https directory, the ant build script
can be used to build demo.

Using either UNIX or Windows:

  ant build
    

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean


The demo illustrates how authentication can be achieved through
configuration using 2 different scenarios. The non-defaulted security
policy values are be specified via configuration files.

Scenario 1: A HTTPS listener is started up. The listener requires
client authentication so the client must provide suitable
credentials. The listener configuration is taken from the server.xml
file located in this directory.  The client's security data is taken
from from the secure_client.xml file in this directory,
using the bean name:
"{http://apache.org/hello_world_soap_http}SoapPort.http-conduit".
The client does NOT provide the appropriate credentials and so the
invocation on the server fails.

To run:

  ant server
  ant insecure.client

Scenario 2: The same HTTPS listener is used. The client's security
data is taken from the client.xml file in this directory,
using the bean name:
"{http://apache.org/hello_world_soap_http}SoapPort.http-conduit".
The client is configured to provide the certificate
src/demo/hw_https/resources/celtix.p12 to the server and so the server
authenticates the client's certificate using its trust store
src/demo/hw_https/resources/abigcompany_ca.pem. Likewise the client
authenticates the servers certificate against its CA
src/demo/hw_https/resources/abigcompany_ca.pem. 
Note also the usage of the CiphersuitesFilters configuration in
the client.xml and server.xml, where each party imposes different
ciphersuites contraints, so that the ciphersuite eventually
negotiated during the TLS handshake is acceptable to both sides.

But please note that it is not adviseable to store sensitive data such
as passwords stored in a clear text configuration file, unless the
file is sufficiently protected by OS level permissions. The approach
taken here is for demonstration reasons only.

To run:

  ant server
  ant secure.client
