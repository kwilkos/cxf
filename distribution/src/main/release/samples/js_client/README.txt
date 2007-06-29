Hello World Client Demo using JavaScript 
=========================================================

The client demo demonstrates the use of the JavaScript to call CXF server

The client side makes call by JAXWS. It use mozilla Rhino library to read 
java script file and run it.

Prerequisite
------------

You should set the CXF_HOME environment to the CXF install path in 
run_client and run_client.bat files.

If your environment already includes cxf-manifest-incubator.jar on the
CLASSPATH, and the JDK and ant bin directories on the PATH.

You also need to download js-1.6R5.jar from
(http://repo1.maven.org/maven2/rhino/js/1.6R5/) and xbean-2.2.0.jar 
from (http://repo1.maven.org/maven2/xmlbeans/xbean/2.2.0/) and place
these two jars under CXF_HOME/lib directory. 


Building and running the demo server using ant
---------------------------------------

From the samples/hello_world directory, the ant build script
can be used to build and run the demo.

Using either UNIX or Windows:

  ant build
  ant server  (in the background or another window)

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean

Running the demo using javascript
---------------------------

From the samples/js_client directory run the commands, entered on a
single command line:

For UNIX (must use forward slashes):
    ./run_client.sh

For Windows (may use either forward or back slashes):
    run_client.bat

When running the client, it can terminate the server process by issuing Ctrl-C in its command window.

It will show the output:
invoke sayHi().   return Bonjour
invoke greetMe(String).   return Hello Jeff

The same time, server give the output:
     [java] Executing operation sayHi

     [java] Executing operation greetMe
     [java] Message received: Jeff
