MTOSI FAULT DEMO (SOAP OVER HTTP)
=================================
=================================

This code example demonstrates a simple cxf
based client/server Web service implementing
the MTOSI alarm retrieval service.


Prerequisite
------------

If your environment already includes cxf-incubator.jar on the
CLASSPATH, and the JDK and ant bin directories on the PATH
it is not necessary to run the environment script described in
the samples directory README.  If your environment is not
properly configured, or if you are planning on using wsdl2java,
javac, and java to build and run the demos, you must set the
environment by running the script.


Building and running the demo using ant
---------------------------------------

From the samples/mtosi_1.1/alarm_retrieval directory, the ant
build script can be used to build and run the demo.

Using either UNIX or Windows:

  ant build
  ant server
  ant client


To remove the code generated from the WSDL file and the .class
files, run:

  ant clean


What Happens When the Demo Runs
===============================

When the server process starts, it opens an 
HTTP connection on the TCP/IP port specified 
by the location attribute within the port 
specification.

The client application has access to the 
same WSDL file, from which it obtains the 
connection information.  The client makes 
asynchronous SOAP over HTTP requests against
the Web service hosted by this server process.

In the "fault" demo, the client invokes the
getActiveAlarmsCount and the getActiveAlarms
operations on the AlarmRetrieval interface.

The client extracts data from the Alarm information
resulting from the call to getActiveAlarms, and
displays it in a formatted way:

  Alarm #0:
  - Notification ID: 0001239
  - Object type: OT_EQUIPMENT
  - OS time: 20060101123000.0Z
  - NE time: 20060101122900.0Z
  - Layer/Rate: LR_DS0_64K
  - Probable cause type: AIS
  - Perceived severity: PS_MAJOR
  - Service affecting: SA_UNKNOWN
  - Root Cause Alarm Indication: NO
  (...)

See the following source for details on
how the types conveyed by the messages
are created and populated:

   - src/demo/fault/client/Client.java (client side)
   - src/demo/fault/server/AlarmRetrievalImpl.java (server side)


