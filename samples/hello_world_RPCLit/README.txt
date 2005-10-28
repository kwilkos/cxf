
Yes, it's the ever present Hello World demo.  No project is 
complete without one. However, this demo demonstrates use
of RPC-Literal Style.



Running the demo
---------------

The ant build script can be used to build and run the demo.

* ant build
* ant server 
* ant client -Dop=sayHi 
* ant client -Dop=greetMe -Dparam1="some string"
* ant client -Dop=sendReceiveData -Dparam1="some string" -Dparam2="some other string" -Dparam3="<integer value>"
