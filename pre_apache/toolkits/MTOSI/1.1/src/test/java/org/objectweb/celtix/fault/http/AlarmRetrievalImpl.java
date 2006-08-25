package org.objectweb.celtix.fault.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Holder;
import javax.xml.ws.Response;

import v1.tmf854.fault.AlarmT;
import v1.tmf854.fault.EventInformationT;
import v1.tmf854.fault.GetActiveAlarmsCountResponseT;
import v1.tmf854.fault.GetActiveAlarmsCountT;
import v1.tmf854.fault.GetActiveAlarmsResponseT;
import v1.tmf854.fault.GetActiveAlarmsT;
import v1.tmf854.fault.HeaderT;
import v1.tmf854.fault.MsgTypeT;
import v1.tmf854.fault.NamingAttributesT;
import v1.tmf854.fault.ObjectTypeT;
import v1.tmf854.fault.ProbableCauseT;
import v1.tmf854.fault.ServiceAffectingT;
import ws.v1.tmf854.fault.http.AlarmRetrieval;
import ws.v1.tmf854.fault.http.ProcessingFailureException;

@WebService(serviceName = "AlarmRetrieval", portName = "AlarmRetrieval",
            endpointInterface = "ws.v1.tmf854.fault.http.AlarmRetrieval",
            targetNamespace = "tmf854.v1.ws")
public class AlarmRetrievalImpl implements AlarmRetrieval {

    private static final Logger LOG = 
        Logger.getLogger(AlarmRetrievalImpl.class.getPackage().getName());

    private List<AlarmT> alarms = new Vector<AlarmT>();

    public void addAlarm(int alarmID) {

        AlarmT alarm = new AlarmT();

        EventInformationT eventInfo = new EventInformationT();
        ProbableCauseT probableCause = new ProbableCauseT();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");

        switch (alarmID) {
        case 1:
            eventInfo.setNotificationId("0001239");
            eventInfo.setObjectType(ObjectTypeT.OT_EQUIPMENT);
            eventInfo.setObjectName(new NamingAttributesT());
            eventInfo.setOsTime(formatter.format(new Date()));
            eventInfo.setNeTime(formatter.format(new Date()));
            eventInfo.setEdgePointRelated(Boolean.FALSE);
            probableCause.setType("PROP_one_probable_cause_type");
            alarm.setEventInfo(eventInfo);
            alarm.setIsClearable(false);
            alarm.setLayerRate("PROP_layer_rate_one");
            alarm.setProbableCause(probableCause);
            alarm.setPerceivedSeverity("PROP_one_perceived_severity");
            alarm.setServiceAffecting(ServiceAffectingT.SA_UNKNOWN);
            alarm.setRcaiIndicator(false);
            break;

        case 2:
            eventInfo.setNotificationId("9876543");
            eventInfo.setObjectType(ObjectTypeT.OT_OS);
            eventInfo.setObjectName(new NamingAttributesT());
            eventInfo.setOsTime(formatter.format(new Date()));
            eventInfo.setEdgePointRelated(Boolean.FALSE);
            probableCause.setType("PROP_two_probable_cause_type");
            alarm.setEventInfo(eventInfo);
            alarm.setIsClearable(true);
            alarm.setLayerRate("PROP_layer_rate_two");
            alarm.setProbableCause(probableCause);
            alarm.setPerceivedSeverity("PROP_two_perceived_severity");
            alarm.setServiceAffecting(ServiceAffectingT.SA_SERVICE_AFFECTING);
            alarm.setRcaiIndicator(false);
            break;

        default:
            return;
        }

        alarms.add(alarm);
    }

    public Future<?> getActiveAlarmsCountAsync(
        GetActiveAlarmsCountT mtosiBody,
        Holder<HeaderT> mtosiHeader,
        AsyncHandler<GetActiveAlarmsCountResponseT> asyncHandler) { 
        LOG.info("Executing operation Future<?> getActiveAlarmsCountAsync");
        System.out.println("Executing operation Future<?> getActiveAlarmsCountAsync");
        return null;
    }

    public Response<GetActiveAlarmsCountResponseT> getActiveAlarmsCountAsync(
        GetActiveAlarmsCountT mtosiBody,
        Holder<HeaderT> mtosiHeader) { 
        LOG.info("Executing operation Response<?> getActiveAlarmsCountAsync");
        System.out.println("Executing operation Response<?> getActiveAlarmsCountAsync");
        return null;
    }

    public GetActiveAlarmsCountResponseT getActiveAlarmsCount(
        GetActiveAlarmsCountT mtosiBody,
        Holder<HeaderT> mtosiHeader) throws ProcessingFailureException {
        LOG.info("Executing operation getActiveAlarmsCount");
        System.out.println("getActiveAlarmsCount() called.");

        mtosiHeader.value.setMsgName("getActiveAlarmsCountResponse");
        mtosiHeader.value.setMsgType(MsgTypeT.RESPONSE);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
        mtosiHeader.value.setTimestamp(formatter.format(new Date()));

        GetActiveAlarmsCountResponseT response = new GetActiveAlarmsCountResponseT();
        response.setActiveAlarmCount(alarms.size()); 

        return response;
    }

    public Future<?> getActiveAlarmsAsync(
        GetActiveAlarmsT mtosiBody,
        Holder<HeaderT> mtosiHeader,
        AsyncHandler<GetActiveAlarmsResponseT> asyncHandler) { 
        LOG.info("Executing operation Future<?> getActiveAlarmsAsync");
        System.out.println("Executing operation Future<?> getActiveAlarmsAsync");
        return null;
    }

    public Response<GetActiveAlarmsResponseT> getActiveAlarmsAsync(
        GetActiveAlarmsT mtosiBody,
        Holder<HeaderT> mtosiHeader) { 
        LOG.info("Executing operation Response<?> getActiveAlarmsAsync");
        System.out.println("Executing operation Response<?> getActiveAlarmsAsync");
        return null;
    }

    public GetActiveAlarmsResponseT getActiveAlarms(
        GetActiveAlarmsT mtosiBody,
        Holder<HeaderT> mtosiHeader) throws ProcessingFailureException {
        LOG.info("Executing operation getActiveAlarms");
        System.out.println("getActiveAlarms() called.");

        mtosiHeader.value.setMsgName("getActiveAlarmsResponse");
        mtosiHeader.value.setMsgType(MsgTypeT.RESPONSE);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
        mtosiHeader.value.setTimestamp(formatter.format(new Date()));

        GetActiveAlarmsResponseT.ActiveAlarmList alarmList = new GetActiveAlarmsResponseT.ActiveAlarmList();
        alarmList.getActiveAlarm().addAll(alarms);
        GetActiveAlarmsResponseT response = new GetActiveAlarmsResponseT();
        response.setActiveAlarmList(alarmList);

        return response;
    }

}
