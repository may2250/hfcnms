package wl.hfc.online;


import org.snmp4j.event.ResponseEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.transport.DefaultUdpTransportMapping;



import wl.hfc.common.IPduSevr;

public class PDUServerForOneDev implements IPduSevr {
	//public static PDUServerForOneDev me;
	private Snmp _session;
	// private TrapProCenter tpPrcss;


	public PDUServerForOneDev(int snmpVersion) {
		initSnmpAPI(snmpVersion);
		//me = this;
	}

	public void initSnmpAPI(int snmpVersion) {

		// 锟斤拷锟斤拷SNMP协锟斤拷通锟斤拷锟斤拷锟斤拷锟斤拷锟�
		try
		{
			_session = new Snmp(new DefaultUdpTransportMapping());

			_session.listen(); 

		}
		catch(Exception e)
		{
			
		}
		

	}

	public PDU SyncSendSnmpPdu(PDU outpdu,CommunityTarget cTgt) {
		
		
		PDU response = null;// 	

		try {			
			// response PDU

			ResponseEvent responseEvent = this._session.send(outpdu, cTgt); 													
		
		
			response = responseEvent.getResponse();
		
			if (response != null) {
		
				if (response.getErrorIndex() == response.noError
						&& response.getErrorStatus() == response.noError) {
		
					return response;
		
					
		
				}
			}
	
		} catch (Exception ex) {
			// Console.Write(ex.Source);
			return response;

		}
		return response;
		// System.Console.Out.WriteLine("锟斤拷锟斤拷锟斤拷一锟斤拷同锟斤拷锟斤拷锟斤拷");

	}

	public boolean AyncSendSnmpPdu(PDU outpdu,CommunityTarget cTgt) {		
//		try {
//			this._session.send(outpdu);
//
//		} catch (Exception ex) {
//			// Console.Write(ex.Source);
//			return false;
//		}	
		
		return true;
	
	}	

}