package wl.hfc.common;
import org.snmp4j.PDU;
import org.snmp4j.CommunityTarget;

import com.adventnet.snmp.snmp2.SnmpPDU;
public interface PduSevr {
    PDU SyncSendSnmpPdu(PDU pdu,CommunityTarget tgt);
    boolean AyncSendSnmpPdu(PDU pdu,CommunityTarget tgt);
   // SnmpTableInfo GetMibTableVariables(SnmpPDU outpdu);
}
