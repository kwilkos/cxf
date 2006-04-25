Hello World Demo using HTTPS communications
=============================================

This demo takes the hello world demo a step further 
by doing the communication using HTTPS.

Please review the README in the samples directory before
continuing.



Prerequisite
------------

If your environment already includes celtix.jar on the
CLASSPATH, and the JDK and ant bin directories on the PATH
it is not necessary to run the environment script described in
the samples directory README.  If your environment is not
properly configured, or if you are planning on using wsdl2java,
javac, and java to build and run the demos, you must set the
environment by running the script.



Building and running the demo using ant
---------------------------------------

From the samples/hello_world_https_https directory, the ant build script
can be used to build demo.

Using either UNIX or Windows:

  ant build
    

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean


The demo illustrates how authentication can be achieved through configuration
using 3 different scenarios. For each scenario the correct security related data 
must be provided. The security data values are defaulted but non default values
can be specified by using configurations files, or by developing a Java class to
provide the data, or through the combination of the Java class and the configuration 
file.

Scenario 1: A HTTPS listener is started up. The listener requires client 
authentication so the client must provide suiteable credentials. The listener 
configuration is read in from the celtix-server.xml file located in this directory. 
The client's security data is read in from the celtix-client.xml, using the bean id
celtix.{http://objectweb.org/hello_world_soap_http}SOAPService/InsecurePort.http-client.
The client does NOT provide the appropriate credentials and so the invocation on 
the server fails. 

To run
ant server
ant client -Duser=insecure_user

Scenario 2: The same HTTPS listener is used. The client's security data is
read in from the celtix-client.xml file in this directory, using the bean id
celtix.{http://objectweb.org/hello_world_soap_http}SOAPService/SecurePort.http-client.
The cleint is configured to provide the src\demo\hw_https\resources\celtix.p12 
certificate to the server and so the server authenticates the client's certificate 
using it's trust store 
src\demo\hw_https\resources\abigcompany_ca.pem.
Likewise the client authenticates the servers certificate against its CA
src\demo\hw_https\resources\abigcompany_ca.pem
But please note it's not adviseable to have sensitive data such as passwords stored in 
a clear text configuration file. It is done here for demonstration reasons only. Scenario
3 below shows how to overcome this problem.

To run
ant server
ant client -Duser=secure_user


Scenario 3: This is similar to scenario 2 but uses a security configurer Java class to
retrieve the celtix.p12 certificate password securely. 
(The security configurer is supported for Celtix 1.0 but the API is subject to change in 
future release.) For this release to create a security configurer simply write a 
Java class that implements the method 

public void configure(SSLClientPolicy sslPolicyParam)

for a client or 

public void configure(SSLServerPolicy sslPolicyParam)

for a server.

Then on the client side set the java system property to point to the
name of the class you have developed. The name of the system property
is derived by concatenating the string 

"celtix.security.configurer." 
with the bean id.

For example a typical client side system property would id look like

"celtix.security.configurer.celtix.{http://objectweb.org/hello_world_soap_http}SOAPService/StrictSecurePort.http-client"

And also a typical server side system property would look like

"celtix.security.configurer.celtix.http-listener.9001"

The code for setting the appropriate property is available 
in the Client.java and Server.java classes.
(For further information on Bean ids please also refer to 
the celtix configuration guide).

The property value be the fully qualified Java class that implements the 
public void configure(SSLClientPolicy sslPolicyParam)
or 
public void configure(SSLServerPolicy sslPolicyParam)
methods. Celtix will use reflection to load the class
and will pass an instance of the client side or server side
policy. The code can then populate any or all of the data in 
the policy. 
To see the structrure of the SSLClientPolicy or SSLServerPolicy
please refer to its structure as defined in the security.xsd file.


In this demo the demo.hw_https.common.DemoSecurityConfigurer class retrieves
the password for the celtix.p12 certificate via a dialog box so that the 
data does not need to be stored in configuration. When either the the server
or the client are run as follows, a dialog box appears prompting for the 
password.

To run 
ant server -Dsecurity_mode=strict_server
ant client -Duser=strict_secure_user

You will be prompted via a dialog for the password, please enter "celtixpass".

The invocation from the client to the server should succeed.