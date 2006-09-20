RESTful Hello World Demo 
========================

The demo demonstrates the REST based webservices using XML binding and 
JAX-WS Provider/Dispatch. The rest server provides following services: 

A RESTful customer service is provided on URL http://localhost:9000/customerservice/customers, 
users access this URI to query or update customer info.

A HTTP GET request to URL http://localhost:9000/customerservice/customers returns 
a list of customer hyperlinks, this allows client navigates through the 
application states. The xml document returned:

<Customers>
  <Customer href="http://localhost/customerservice/customer?id=1234">
      <id>1234</id>
  </Customer>
  <Customer href="http://localhost/customerservice/customer?id=1235"> 
      <id>1235</id>
  </Customer>
  <Customer href="http://localhost/customerservice/customer?id=1236"> 
      <id>1236</id>
  </Customer>
</Customers>

A HTTP GET request to URL http://localhost:9000/customerservice/customers?id=1234 
returns a customer instance whose id is 1234. The xml document returned:

<Customer>
  <id>1234</id>
  <name>John</name>
  <phoneNumber>123456</phoneNumber>
</Customer>

A HTTP POST request to URL http://localhost:9000/customerservice/customers with data:

<Customer>
  <id>1234</id>
  <name>John</name>
  <phoneNumber>234567</phoneNumber>
</Customer>

updates customer 1234 with the data provided. 

The demo client codes demonstrate how to sent HTTP POST with XML data using 
JAX-WS dispatch and how to sent HTTP GET using URL.openStream().


Please review the README in the samples directory before
continuing.


Prerequisite
------------

If your environment already includes cxf.jar on the
CLASSPATH, and the JDK and ant bin directories on the PATH
it is not necessary to run the environment script described in
the samples directory README.  If your environment is not
properly configured, or if you are planning on using wsdl2java,
javac, and java to build and run the demos, you must set the
environment by running the script.



Building and running the demo using ant
---------------------------------------

From the samples/restful directory, the ant build script
can be used to build and run the demo.

Using either UNIX or Windows:

  ant build
  ant server
  ant client
    

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean



Buildng the demo using wsdl2java and javac
------------------------------------------

From the samples/restful directory, first create the target
directory build/classes and then generate code from the WSDL file.

For UNIX:
  mkdir -p build/classes

  wsdl2java -d build/classes -compile ./wsdl/hello_world.wsdl

For Windows:
  mkdir build\classes
    Must use back slashes.

  wsdl2java -d build\classes -compile .\wsdl\hello_world.wsdl
    May use either forward or back slashes.

Now compile the provided client and server applications with the commands:

For UNIX:  
  
  export CLASSPATH=$CLASSPATH:$CXF_HOME/lib/cxf.jar:./build/classes
  javac -d build/classes src/demo/hw/client/*.java
  javac -d build/classes src/demo/hw/server/*.java

For Windows:
  set classpath=%classpath%;%CXF_HOME%\lib\cxf.jar;.\build\classes
  javac -d build\classes src\demo\hw\client\*.java
  javac -d build\classes src\demo\hw\server\*.java



Running the demo using java
---------------------------

From the samples/hello_world directory run the commands, entered on a
single command line:

For UNIX (must use forward slashes):
    java -Djava.util.logging.config.file=$CXF_HOME/etc/logging.properties
         demo.hw.server.Server &

    java -Djava.util.logging.config.file=$CXF_HOME/etc/logging.properties
         demo.hw.client.Client ./wsdl/hello_world.wsdl

The server process starts in the background.  After running the client,
use the kill command to terminate the server process.

For Windows (may use either forward or back slashes):
  start 
    java -Djava.util.logging.config.file=%CXF_HOME%\etc\logging.properties
         demo.hw.server.Server

    java -Djava.util.logging.config.file=%CXF_HOME%\etc\logging.properties
       demo.hw.client.Client .\wsdl\hello_world.wsdl

A new command windows opens for the server process.  After running the
client, terminate the server process by issuing Ctrl-C in its command window.

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean

