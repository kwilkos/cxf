package org.objectweb.celtix.fault.http;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import v1.tmf854.fault.GetActiveAlarmsResponseT;
import v1.tmf854.fault.GetActiveAlarmsResponseT.ActiveAlarmList;

public class AsyncAlarmHandler implements AsyncHandler<GetActiveAlarmsResponseT> {

    private GetActiveAlarmsResponseT reply;

    public void handleResponse(Response<GetActiveAlarmsResponseT> response) {
        try {
            System.out.println("handling asynchronous response...");

            reply = response.get();
        } catch (InterruptedException ex) {
            System.out.println("InteruptedException while awaiting response.");
        } catch (java.util.concurrent.ExecutionException ex) {
            System.out.println("Operation received ExecutionException.");
        }
    }

    public ActiveAlarmList getResponse() {
        return reply.getActiveAlarmList();
    }

}
