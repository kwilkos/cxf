Spring HTTP demo
=============================================
This example will lead you through creating your first service with
Spring. You'll learn how to:

    * Writing a simple JAX-WS "code-first" service
    * Set up the HTTP servlet transport
    * Use CXF's Spring beans

For more information see the documentation for this example in the
user's guide.


Prerequisite
------------

If your environment already includes cxf-manifest-incubator.jar on the
CLASSPATH, and the JDK and ant bin directories on the PATH
it is not necessary to set the environment as described in
the samples directory README.  If your environment is not
properly configured, or if you are planning on using wsdl2java,
javac, and java to build and run the demos, you must set the
environment.



Building and running the demo using ant
---------------------------------------

From the samples/hello_world directory, the ant build script
can be used to build and run the demo.

Using either UNIX or Windows:

  ant build
  ant server
  ant client
    

To remove the code generated from the WSDL file and the .class
files, run:

  ant clean
