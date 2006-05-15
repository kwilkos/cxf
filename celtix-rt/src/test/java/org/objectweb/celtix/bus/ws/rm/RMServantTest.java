package org.objectweb.celtix.bus.ws.rm;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import junit.framework.TestCase;


import org.easymock.classextension.IMocksControl;
import org.objectweb.celtix.bus.configuration.wsrm.DestinationPolicyType;
import org.objectweb.celtix.ws.addressing.AddressingProperties;
import org.objectweb.celtix.ws.addressing.AttributedURIType;
import org.objectweb.celtix.ws.addressing.v200408.AttributedURI;
import org.objectweb.celtix.ws.addressing.v200408.EndpointReferenceType;
import org.objectweb.celtix.ws.rm.AcceptType;
import org.objectweb.celtix.ws.rm.CreateSequenceResponseType;
import org.objectweb.celtix.ws.rm.CreateSequenceType;
import org.objectweb.celtix.ws.rm.Expires;
import org.objectweb.celtix.ws.rm.Identifier;
import org.objectweb.celtix.ws.rm.OfferType;
import org.objectweb.celtix.ws.rm.wsdl.SequenceFault;

import static org.easymock.classextension.EasyMock.*;

public class RMServantTest extends TestCase {
    
    private IMocksControl control;
    private RMDestination dest;
    private RMSource src;
    private CreateSequenceType cs;
    private CreateSequenceResponseType csResp;
    private AttributedURIType to;
    private DestinationPolicyType dp;
    private Identifier sid;
    private AddressingProperties maps;
    
    protected void setUp() {
        control = createNiceControl();
    }
    
    protected void tearDown() {
        control.reset();
        control = null;
    }    

    public void testCreateSequenceDefault() throws DatatypeConfigurationException, SequenceFault {        
        
        setupCreateSequence(null, null, true, true);
        
        control.replay();
        
        CreateSequenceResponseType csr = (new RMServant()).createSequence(dest, cs, maps);
        
        control.verify();

        assertSame(sid, csr.getIdentifier());
        assertNotNull(csr.getAccept());  
        assertTrue(!Names.WSA_NONE_ADDRESS
                   .equals(csr.getAccept().getAcksTo().getAddress().getValue()));        
    }
    
    public void testCreateSequenceRejectOffer() throws DatatypeConfigurationException, SequenceFault {        
        
        setupCreateSequence(null, null, true, false);
        
        control.replay();
        
        CreateSequenceResponseType csr = (new RMServant()).createSequence(dest, cs, maps);
        
        control.verify();

        assertSame(sid, csr.getIdentifier());
        assertNotNull(csr.getAccept());  
        assertEquals(Names.WSA_NONE_ADDRESS, csr.getAccept().getAcksTo().getAddress().getValue());        
    }
    
    public void testCreateSequenceNoOfferIncluded() 
        throws DatatypeConfigurationException, SequenceFault {        
        
        setupCreateSequence(null, null, false, false);
        
        control.replay();
        
        CreateSequenceResponseType csr = (new RMServant()).createSequence(dest, cs, maps);
        
        control.verify();

        assertSame(sid, csr.getIdentifier());
        assertNull(csr.getAccept());   
    }
    
    public void testCreateSequenceRequestedDurationNotSupported() 
        throws DatatypeConfigurationException, SequenceFault {        
        
        setupCreateSequence("PT24H", "PT48H", false, false);
        
        control.replay();
        
        CreateSequenceResponseType csr = (new RMServant()).createSequence(dest, cs, maps);
        
        control.verify();

        assertSame(sid, csr.getIdentifier());
        assertNull(csr.getAccept());          
        assertEquals("PT24H", csr.getExpires().getValue().toString());
    }
    
    public void testTerminateSequence() throws SequenceFault {
        dest = control.createMock(RMDestination.class);
        sid = control.createMock(Identifier.class);
        
        (new RMServant()).terminateSequence(dest, sid);
        
    }

    public void testCreateSequenceResponseNotAccepted() {
        setupCreateSequenceResponse(false);
        Identifier unattachedId = control.createMock(Identifier.class);
        control.replay();
        
        RMServant servant = new RMServant();
        servant.setUnattachedIdentifier(unattachedId);
        servant.createSequenceResponse(src, csResp, null);
    
        control.verify();
    }
    
    public void testCreateSequenceResponseAccepted() {
        setupCreateSequenceResponse(true);
        Identifier unattachedId = control.createMock(Identifier.class);
        Identifier offeredId = control.createMock(Identifier.class);
        control.replay();
        
        RMServant servant = new RMServant();
        servant.setUnattachedIdentifier(unattachedId);
        servant.createSequenceResponse(src, csResp, offeredId);
    
        control.verify();
    }
    
    public void testUnattachedIdentifier() throws SequenceFault {
        Identifier unattachedId = control.createMock(Identifier.class);
        control.replay();
        
        RMServant servant = new RMServant();
        assertNull("unexpected unattached ID",
                   servant.clearUnattachedIdentifier());
        servant.setUnattachedIdentifier(unattachedId);
        assertSame("unexpected unattached ID",
                   unattachedId,
                   servant.clearUnattachedIdentifier());
        assertNull("unexpected unattached ID",
                   servant.clearUnattachedIdentifier());
    }


    // expires = "PT24H";
    
    private void setupCreateSequence(String supportedDuration, String requestedDuration, 
                                         boolean includeOffer, boolean acceptOffer)
        throws DatatypeConfigurationException {

        dest = control.createMock(RMDestination.class);
        to = control.createMock(AttributedURIType.class); 
        dp = control.createMock(DestinationPolicyType.class);
        sid = control.createMock(Identifier.class);
        cs = control.createMock(CreateSequenceType.class);
        AttributedURIType messageID = control.createMock(AttributedURIType.class);
        maps = control.createMock(AddressingProperties.class);
        maps.getMessageID();
        expectLastCall().andReturn(messageID);
        
        dest.generateSequenceIdentifier();
        expectLastCall().andReturn(sid);
        
        dest.getDestinationPolicies();
        expectLastCall().andReturn(dp);
        
        Duration d = null;
        if (null != supportedDuration) {
            d = DatatypeFactory.newInstance().newDuration(supportedDuration);
        }
        dp.getSequenceExpiration();
        expectLastCall().andReturn(d);
            
        Expires ex = null;
        if (null != requestedDuration) {
            Duration rd = DatatypeFactory.newInstance().newDuration(requestedDuration);
            ex = RMUtils.getWSRMFactory().createExpires();
            ex.setValue(rd);
        }
        cs.getExpires();
        expectLastCall().andReturn(ex);        
        
        setupOffer(includeOffer, acceptOffer);
    }
    
    private void setupCreateSequenceResponse(boolean accepted) {
        src = control.createMock(RMSource.class);
        dest = control.createMock(RMDestination.class);
        sid = control.createMock(Identifier.class);
        csResp = control.createMock(CreateSequenceResponseType.class);
        csResp.getIdentifier();
        expectLastCall().andReturn(sid);
        src.addSequence(isA(SourceSequence.class));
        expectLastCall();
        src.setCurrent(isA(Identifier.class), isA(SourceSequence.class));
        expectLastCall();
        if (accepted) {
            RMHandler handler = control.createMock(RMHandler.class);
            src.getHandler();
            expectLastCall().andReturn(handler);
            handler.getDestination();
            expectLastCall().andReturn(dest);
            AcceptType accept = control.createMock(AcceptType.class);
            csResp.getAccept();
            expectLastCall().andReturn(accept);
            accept.getAcksTo();
            EndpointReferenceType acksTo = TestUtils.getOldEPR("acks");
            expectLastCall().andReturn(acksTo).times(2);
            dest.addSequence(isA(DestinationSequence.class));
            expectLastCall();
        }
    }

    private void setupOffer(boolean includeOffer, boolean acceptOffer)
        throws DatatypeConfigurationException {

        OfferType o = null;
        if (includeOffer) {
            o = control.createMock(OfferType.class);
        }
        cs.getOffer();
        expectLastCall().andReturn(o);
        
        if (includeOffer) {
            dp.isAcceptOffers();
            expectLastCall().andReturn(acceptOffer);
        }
        
        EndpointReferenceType acksTo =
            control.createMock(EndpointReferenceType.class);
        if (includeOffer && acceptOffer) {
            maps.getTo();
            expectLastCall().andReturn(to);
            RMHandler handler = control.createMock(RMHandler.class);
            dest.getHandler();
            expectLastCall().andReturn(handler);
            RMSource source = control.createMock(RMSource.class);
            handler.getSource();
            expectLastCall().andReturn(source);
            o.getIdentifier();            
            expectLastCall().andReturn(control.createMock(Identifier.class));
            o.getExpires();
            expectLastCall().andReturn(null);
            cs.getAcksTo();
            expectLastCall().andReturn(acksTo);
            AttributedURI address = control.createMock(AttributedURI.class);
            acksTo.getAddress();
            expectLastCall().andReturn(address);
            source.addSequence(isA(SourceSequence.class));
            expectLastCall();
            source.setCurrent(isA(Identifier.class), isA(SourceSequence.class));
            expectLastCall();
        }
     
        cs.getAcksTo();
        expectLastCall().andReturn(acksTo);
    }
}
