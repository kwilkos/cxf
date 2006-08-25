Basic Setup for Building and Running the performance test case 
==============================================

As described in the installation notes, extract the Celtix
binary distribution archive into an installation directory
under the root drive.  This creates the sub-directory celtix,
which includes all of the product directories.

1. setup the build Enviroment

To build and run the performance test case , you must install 
the J2SE Development Kit (JDK) 5.0

If you want to use ant to build and run the performance test case,
you must install the Apache ant 1.6 build utility.

 2. Build the performance test case
There are two types of test case in the performance test case
directory. The one is basic_type testcase ,the othere is complex_type
testcase. The base directory provide a simple testcase base class for
the client to calculate the server reponse time and throughput. There
for you should build the base directory first, and then build the 
 othere directory files. 		
  
   cd base
   ant
   cd ../basic_type
   ant
   cd ../complex_type
   ant

 3. to run the performance test 
You can cd to basic_type/bin or complex_type/bin to run the test
run_client and run_client.bat can take these argument:
    -Operation  to invoke the wsdl defined operation
    -BasedOn Time   setup the invoking count with time
    -Amount   define the invoke times , if based on time it means second
    -PacketSize  define the packet size which client send to server
    -Threads   define the thread number to run the perform test	
 

	
