package wl.hfc.common;
import org.snmp4j.PDU;
import org.snmp4j.CommunityTarget;
public interface PduSevr {
    PDU SyncSendSnmpPdu(PDU pdu,CommunityTarget tgt);
    boolean AyncSendSnmpPdu(PDU pdu,CommunityTarget tgt);
   // SnmpTableInfo GetMibTableVariables(SnmpPDU outpdu);
}
