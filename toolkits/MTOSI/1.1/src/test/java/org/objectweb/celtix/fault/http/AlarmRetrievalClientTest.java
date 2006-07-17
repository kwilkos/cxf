package org.objectweb.celtix.fault.http;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.objectweb.celtix.testutil.common.AbstractClientServerSetupBase;

import v1.tmf854.fault.ActiveAlarmFilterT;
import v1.tmf854.fault.AlarmT;
import v1.tmf854.fault.CommunicationPatternT;
import v1.tmf854.fault.CommunicationStyleT;
import v1.tmf854.fault.EventInformationT;
import v1.tmf854.fault.GetActiveAlarmsCountResponseT;
import v1.tmf854.fault.GetActiveAlarmsCountT;
import v1.tmf854.fault.GetActiveAlarmsT;
import v1.tmf854.fault.HeaderT;
import v1.tmf854.fault.MsgTypeT;
import v1.tmf854.fault.ProbableCauseT;
import ws.v1.tmf854.fault.http.AlarmRetrieval;
import ws.v1.tmf854.fault.http.AlarmRetrieval_Service;

/**
 * org.objectweb.celtix.fault.http.AlarmRetrievalClientTest
 */
public final class AlarmRetrievalClientTest extends TestCase {

    private static final QName SERVICE_NAME = new QName("tmf854.v1.ws", "AlarmRetrieval");
    private static AlarmRetrieval port;

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AlarmRetrievalClientTest.class);
        return new AbstractClientServerSetupBase(suite) {
            public void startServers() throws Exception {
                boolean ok = launchServer(AlarmRetrievalServer.class);
                if (!ok) {
                    fail("Failed to launch alarm retrieval server.");
                }
            }
        };
    }

    public void setUp() throws Exception {
        super.setUp();

        URL wsdlUrl = getClass().getResource("/wsdl/transport/http/FaultServiceSOAP_HTTP.wsdl");
        assertNotNull("Could not get FaultServiceSOAP_HTTP.wsdl resource.", wsdlUrl);
    
        AlarmRetrieval_Service ss = new AlarmRetrieval_Service(wsdlUrl, SERVICE_NAME);
        assertNotNull("Failed to create AlarmRetrieval_Service", ss);
        port = ss.getAlarmRetrieval();
    }

    public void testGetActiveAlarmsCountAsync() throws Exception {

        HeaderT header = new HeaderT();
        header.setActivityName("getActiveAlarmsCountAsync");
        header.setMsgName("getActiveAlarmsCountAsync");
        header.setMsgType(MsgTypeT.REQUEST);
        header.setSenderURI("http://mtosi.iona.com/fault/sender");
        header.setDestinationURI("http://mtosi.iona.com/fault/destination");
        header.setCommunicationPattern(CommunicationPatternT.SIMPLE_RESPONSE);
        header.setCommunicationStyle(CommunicationStyleT.MSG);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
        header.setTimestamp(formatter.format(new Date()));
        Holder<HeaderT> mtosiHeader = new Holder<HeaderT>(header);

        GetActiveAlarmsCountT mtosiBody = new GetActiveAlarmsCountT();
        mtosiBody.setFilter(new ActiveAlarmFilterT());

        // use polling method to obtain response
        
        System.out.println("Invoking getActiveAlarmsCountAsync using polling.");
        Response<GetActiveAlarmsCountResponseT> response =
            port.getActiveAlarmsCountAsync(mtosiBody, mtosiHeader);

        while (!response.isDone()) {
            System.out.println("waiting for operation response...");
            Thread.sleep(100);
        }

        try {
            System.out.println("Active Alarms Count: " + response.get().getActiveAlarmCount());
        } catch (InterruptedException ex) {
            System.out.println("InteruptedException while awaiting response.");
        } catch (java.util.concurrent.ExecutionException ex) {
            System.out.println("Operation received ExecutionException.");
        }
    }

    public void testGetActiveAlarmsAsync() throws Exception {

        HeaderT header = new HeaderT();
        header.setActivityName("getActiveAlarmsAsync");
        header.setMsgName("getActiveAlarmsAsync");
        header.setMsgType(MsgTypeT.REQUEST);
        header.setSenderURI("http://mtosi.iona.com/fault/sender");
        header.setDestinationURI("http://mtosi.iona.com/fault/destination");
        header.setCommunicationPattern(CommunicationPatternT.SIMPLE_RESPONSE);
        header.setCommunicationStyle(CommunicationStyleT.MSG);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
        header.setTimestamp(formatter.format(new Date()));
        Holder<HeaderT> mtosiHeader = new Holder<HeaderT>(header);

        GetActiveAlarmsT mtosiBody = new GetActiveAlarmsT();
        mtosiBody.setFilter(new ActiveAlarmFilterT());

        // use callback method to obtain response
        
        System.out.println("Invoking getActiveAlarmsAsync using callback.");
        AsyncAlarmHandler handler = new AsyncAlarmHandler();
        Future<?> response = port.getActiveAlarmsAsync(mtosiBody, mtosiHeader, handler);

        while (!response.isDone()) {
            Thread.sleep(100);
        }
        System.out.println("getActiveAlarmsAsync operation completed.");
        List<AlarmT> alarms = handler.getResponse().getActiveAlarm();

        int alarmCount = alarms.size();
        if (alarmCount == 0) {
            System.out.println("No alarm.");
        } else {
            System.out.println("Displaying details for " + alarmCount
                + " alarm" + (alarmCount == 1 ? ":" : "s:"));
            System.out.println();
        }

        int i = 0;
        for (AlarmT alarm : alarms) {
            System.out.println("Alarm #" + i++ + ":");
            EventInformationT eventInfo = alarm.getEventInfo();
            System.out.println("- Notification ID: " + eventInfo.getNotificationId());
            System.out.println("- Object type: " + eventInfo.getObjectType().value());
            System.out.println("- OS time: " + eventInfo.getOsTime());

            if (eventInfo.getNeTime() != null) {
                System.out.println("- NE time: " + eventInfo.getNeTime());
            }

            String layerRate = alarm.getLayerRate();
            if (layerRate != null) {
                System.out.print("- Layer/Rate: " + layerRate);
            }

            ProbableCauseT probableCause = alarm.getProbableCause();
            String type = probableCause.getType();
            if (type != null) {
                System.out.print("- Probable cause type: " + type);
            }

            String perceivedSeverity = alarm.getPerceivedSeverity();
            if (perceivedSeverity != null) {
                System.out.print("- Perceived severity: " + perceivedSeverity);
            }

            System.out.println("- Service affecting: " + alarm.getServiceAffecting().value());
            System.out.println("- Root Cause Alarm Indication: "
                + ((alarm.isRcaiIndicator()) ? "YES" : "NO"));
            System.out.println();
        }
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(AlarmRetrievalClientTest.class);
    }
}
