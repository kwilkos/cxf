
This demo shows how the JAXWS handlers can be used.  The server
registers a SOAP protocol handler which simply logs incoming and
outgoing messages to the console.  

On the server side the handlers registered using the @HandlerChain
annotation on the service implementation class.

The client contains a logical handler that checks the parameters on
outbound requests and prevents the further processing of the request
in certain circumstances.

Running the demo
---------------

The ant build script can be used to build and run the demo.  The
server and client targets automatically build the demo.

* ant server 
* ant client
