Basic Setup for Building and Running the Demos
==============================================

As described in the installation notes, extract the Celtix
binary distribution archive into an installation directory
under the root drive.  This creates the sub-directory celtix,
which includes all of the product directories.

To build and run the demos, you must install the J2SE Development
Kit (JDK) 5.0

If you want to use ant to build and run the demos, you must
install the Apache ant 1.6 build utility.

To build and run the demos provided in the Celtix binary
distribution using either ant or wsdl2java, javac and java,
you need to set the environment so that the file celtix.jar
is on the CLASSPATH and to insure that the JDK, ant and
Celtix bin directories are on the PATH.

In each of the demos, source code files for the client and
server mainlines and the Service Endpoint Interface class are
included in the src directory.  The build process will write
generated code into a new directory, build/src, and then place
compiled code into the directory build/classes.

You may find it convenient to use a script to set the required
environment variables.

For UNIX:
  CELTIX_HOME=/<installation_directory>/celtix
  JAVA_HOME=/<jdk_installation_directory>
  ANT_HOME=/<ant_installation_directory>/apache-ant-1.6.5

  export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$CELTIX_HOME/bin:$PATH
  export CLASSPATH=.:$CELTIX_HOME/lib/celtix.jar:./build/classes

For Windows:
  set CELTIX_HOME=C:\<installation_directory>\celtix
  set JAVA_HOME=C:\<jdk_installation_directory>
  set ANT_HOME=C:\<ant_installation_directory>\apache-ant-1.6.5

  set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%CELTIX_HOME%\bin;%PATH%
  set CLASSPATH=.;%CELTIX_HOME%\lib\celtix.jar;.\build\classes

Save a copy of this script in the Celtix samples directory.  Run the
script prior to building and running the demos.