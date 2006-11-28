Basic Setup for Building and Running the Demos
==============================================

As described in the installation notes, extract the Apache CXF
binary distribution archive into an installation directory
under the root drive.  This creates the sub-directory cxf,
which includes all of the product directories.

To build and run the demos, you must install the J2SE Development
Kit (JDK) 5.0

If you want to use ant to build and run the demos, you must
install the Apache ant 1.6 build utility.

To build and run the demos provided in the Apache CXF binary
distribution using either ant or wsdl2java, javac and java,
you need to set the environment so that the file cxf-incubator.jar
is on the CLASSPATH and to insure that the JDK, ant and
CXF_HOME/bin directories are on the PATH.

To build and run the demos provided in the Apache CXF source distribution
using ant you will need to edit the common_build.xml file.
Uncomment the line:
<import file="../../../target/srcbuild_paths.xml" optional="true"/>

In each of the demos, source code files for the client and
server mainlines and the Service Endpoint Interface class are
included in the src directory.  The build process will write
generated code into a new directory, build/src, and then place
compiled code into the directory build/classes.

You may find it convenient to use a script to set the required
environment variables.

For UNIX:
  CXF_HOME=/<installation_directory>/cxf
  JAVA_HOME=/<jdk_installation_directory>
  ANT_HOME=/<ant_installation_directory>/apache-ant-1.6.5

  export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$CXF_HOME/bin:$PATH
  export CLASSPATH=.:$CXF_HOME/lib/cxf-incubator.jar:./build/classes

For Windows:
  set CXF_HOME=C:\<installation_directory>\cxf
  set JAVA_HOME=C:\<jdk_installation_directory>
  set ANT_HOME=C:\<ant_installation_directory>\apache-ant-1.6.5

  set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%CXF_HOME%\bin;%PATH%
  set CLASSPATH=.;%CXF_HOME%\lib\cxf-incubator.jar;.\build\classes

Save a copy of this script in CXF_HOME/samples.  Run the
script prior to building and running the demos.


Basic Setup for Building and Running the Demos in a Servlet Container
=====================================================================

Since Apache CXF requires JDK/JRE 5.0, you must use a servlet container
that is compatible with this JDK/JRE.  A suitable servlet container is
Tomcat 5.5 or above.

Be certain to start the servlet container under an environment in which
the JAVA_HOME environment variable points to the JDK/JRE 5.0 installation
and the JAVA_HOME bin directory is included in the system PATH.

Make sure you have copied all jars (except cxf-integration-* jars) from 
CXF_HOME/lib to <TomcatInstallationDirectory>/shared/lib.
