Groovy script demo
=============================================
This example will lead you through creating groovy web service 
implement with Spring. You'll learn how to:

    * Writing a simple groovy script web service

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

Get some thirdparty jars:

You can run the following command to get the thirdparty jars

  ant get.dep

Spring script jars:
* spring-aop-2.0.4.jar
* spring-support-2.0.4.jar
download from 
http://repo1.maven.org/maven2/org/springframework/
copy into $CXF_HOME/lib/

Groovy jars:
* groovy-1.0.jar
* antlr-2.7.6.jar
* asm-2.2.3.jar
download from:
http://repo1.maven.org/maven2/groovy/groovy/1.0/groovy-1.0.jar
http://repo1.maven.org/maven2/antlr/antlr/2.7.6/antlr-2.7.6.jar
http://repo1.maven.org/maven2/asm/asm/2.2.3/asm-2.2.3.jar
copy into $CXF_HOME/samples/groovy_spring_support/lib

Building and running the demo using Ant
---------------------------------------
From the base directory of this sample (i.e., where this README file is
located), the Ant build.xml file can be used to build and run the demo. 
The server and client targets automatically build the demo.

Using either UNIX or Windows:

  ant server  (from one command line window)
  ant client  (from a second command line window)
    

To remove the code generated from the WSDL file and the .class
files, run "ant clean".
