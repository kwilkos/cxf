package demo.ejb;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;



public class GreeterBean implements SessionBean {

    private SessionContext sessionCtx;
    private String response = null;

    public String sayHi() throws RemoteException {
        System.out.println("sayHi invoked");
        return "Hi from an EJB"; 
    }
    
    public String greetMe(String user) throws RemoteException {
        System.out.println("greetMe invoked user:" + user);
        return "Hi " + user + " from an EJB"; 
    }

    public void ejbActivate() {}
    public void ejbRemove() {}
    public void ejbPassivate() {}
    public void ejbCreate() throws CreateException {}
    public void setSessionContext(SessionContext sessionCtx) { this.sessionCtx = sessionCtx; }

}
