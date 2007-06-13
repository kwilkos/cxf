
package ${groupId};

import javax.jws.WebService;

@WebService(endpointInterface = "${groupId}.HelloWorld")
public class HelloWorldImpl implements HelloWorld {

    public String sayHi(String text) {
        return "Hello " + text;
    }
}

