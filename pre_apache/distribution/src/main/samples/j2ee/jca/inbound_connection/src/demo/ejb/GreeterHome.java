package demo.ejb;


import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
//import org.objectweb.hello_world_soap_http.Greeter;


public interface GreeterHome extends EJBHome {
    GreeterRemote create() throws CreateException, RemoteException;
}

