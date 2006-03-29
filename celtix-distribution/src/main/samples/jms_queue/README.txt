JMS Transport Demo using Document-Literal Style
==========================================================

This sample demonstrates use of the Document-Literal style 
binding over JMS Transport using queue mechanism.

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

From the samples/jms_queue directory, the ant build script
can be used to build and run the demo.

Using either UNIX or Windows:

  ant build
  ant jmsbroker.start (in the background or another window)
  ant server  (in the background or another window)
  ant client
    

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean



Buildng the demo using wsdl2java and javac
------------------------------------------

From the samples/jms_queue directory, first create the target
directory build/classes and then generate code from the WSDL file.

For UNIX:
  mkdir -p build/classes

  wsdl2java -d build/classes -compile ./wsdl/jms_greeter.wsdl

For Windows:
  mkdir build\classes
    Must use back slashes.

  wsdl2java -d build\classes -compile .\wsdl\jms_greeter.wsdl
    May use either forward or back slashes.

Now compile the provided client and server applications with the commands:

For UNIX:  
  
  export classpath=$CLASSPATH:$CELTIX_HOME/lib/celtix.jar:./build/classes
  javac -d build/classes src/demo/jms_greeter/client/*.java
  javac -d build/classes src/demo/jms_greeter/server/*.java

For Windows:
  set classpath=%classpath%;%CELTIX_HOME%\lib\celtix.jar:.\build\classes
  javac -d build\classes src\demo\jms_greeter\client\*.java
  javac -d build\classes src\demo\jms_greeter\server\*.java



Running the demo using java
---------------------------

From ActiveMQ insallation launch ActiveMQ JMS Broker in seperate window or in
background using the commandline:

For Unix:

cd <activemq.home.dir>/bin
activemq ../conf/activemq.xml

For Windows:
cd <activemq.home.dir>\bin
activemq.bat ..\conf\activemq.xml

The location of <activemq.home.dir> depends on whether you have installed the
binary or source release.  In the binary release, <activemq.home.dir> is in
lib/activemq/3.2.  In the source release, <activemq.home.dir> is in
tools/activemq/3.2.

From the samples/jms_queue directory run the commands, entered on a
single command line:

For UNIX (must use forward slashes):
    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         demo.hwJMS.server.Server &

    java -Djava.util.logging.config.file=$CELTIX_HOME/etc/logging.properties
         demo.hwJMS.client.Client ./wsdl/jms_greeter.wsdl

The server process starts in the background.  After running the client,
use the kill command to terminate the server process.

For Windows (may use either forward or back slashes):
  start 
    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         demo.hwJMS.server.Server

    java -Djava.util.logging.config.file=%CELTIX_HOME%\etc\logging.properties
         demo.hwJMS.client.Client .\wsdl\jms_greeter.wsdl

A new command windows opens for the server process.  After running the
client, terminate the server process by issuing Ctrl-C in its command window.


Now you can stop ActiveMQ JMS Broker by issuing Ctrl-C in its command window.

To remove the code generated from the WSDL file and the .class
files, either delete the build directory and its contents or run:

  ant clean
