package org.apache.hello_world_soap_http;

import org.apache.hello_world_rpclit.GreeterRPCLit;
import org.apache.hello_world_rpclit.types.MyComplexStruct;


public class NotAnnotatedGreeterImplRPCLit implements GreeterRPCLit {

    public String greetMe(String me) {
        return "Hello " + me;
    }

    public String sayHi() {
        return "Bonjour";
    }

    public MyComplexStruct sendReceiveData(MyComplexStruct in) {
        return in;
    }
}
