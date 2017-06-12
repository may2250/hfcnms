package wl.commonComponent;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.json.simple.JSONObject;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import wl.hfc.common.CDevForCMD;
import wl.hfc.common.DProcess;
import wl.hfc.common.DevTopd;
import wl.hfc.common.NetTypes;
import wl.hfc.common.OidToHFCType;
import wl.hfc.common.PduSevr;
import wl.hfc.common.nojuDeviceTableRow.HFCTypes;

public class PDUServerSearch{
	private Snmp session;
	
	
	public PDUServerSearch() throws IOException
    {
		initSnmpAPI();        
    }
	
	
	private void initSnmpAPI() throws IOException {		
		// 创建SNMP协议通信引擎对象。
		DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
		session = new Snmp(transport);
		session.listen();

	}
	
	public PDU SyncSendSnmpPdu(PDU outpdu, CommunityTarget cTgt) {

		PDU response = null;//

		try {
			// response PDU

			ResponseEvent responseEvent = this.session.send(outpdu, cTgt);

			response = responseEvent.getResponse();

			if (response != null) {

				if (response.getErrorIndex() == response.noError && response.getErrorStatus() == response.noError) {

					return response;

				}
			}

		} catch (Exception ex) {
			// Console.Write(ex.Source);
			return response;

		}
		return response;
		// System.Console.Out.WriteLine("进行了一次同步发送");

	}

	public boolean AyncSendSnmpPdu(final PDU outpdu, final CommunityTarget target) {
		final CountDownLatch latch = new CountDownLatch(1);  
		ResponseListener listener = new ResponseListener() {
			public void onResponse(ResponseEvent event) {
				//System.out.println("---------->开始异步解析<------------");
				if (event != null && event.getResponse() != null) {
					try {
						readResponse(event);
					} catch (Exception e) {
						e.printStackTrace();
					}
					session.cancel(outpdu, this);
					latch.countDown();  
				}
			}
		};

		// 发送报文
		try {
			session.send(outpdu, target, null, listener);
			return true;
		} catch (Exception e) {
			return false;
		}

	}
	
	@SuppressWarnings("unchecked")
	public void readResponse(ResponseEvent respEvnt) {
		// 解析Response
		// System.out.println("------------>解析Response<----------");

		if (respEvnt != null && respEvnt.getResponse() != null) {
			Vector<VariableBinding> recVBs = respEvnt.getResponse().getVariableBindings();
			
			VariableBinding recVB = recVBs.elementAt(0);			
			if (!(recVB.getVariable().toString().equalsIgnoreCase("Null"))) {

				TestOnlineMessageCallback(respEvnt);

			}
		}

	}
	
	private void TestOnlineMessageCallback(ResponseEvent respEvnt) {
		String ipaddr=respEvnt.getPeerAddress().toString();
		ipaddr = ipaddr.substring(0,ipaddr.indexOf("/"));
		Vector<VariableBinding> recVBs= (Vector<VariableBinding>)respEvnt.getResponse().getVariableBindings();
		System.out.println("------------>解析Response<----------" + recVBs.toString());
		HFCTypes devtype = OidToHFCType.getType(recVBs);
		if (devtype == HFCTypes.Unknown) // 处理未知设备
		{
			return;
		}
		try
        {
			System.out.println("------------>发现设备.....<----------" + ipaddr);
			
			CDevForCMD result = new CDevForCMD();
			result.mNetAddress = ipaddr;
			result.HFCType1 = devtype;
			result.mNetType = DProcess.getNetTypes(devtype);
			if (devtype == HFCTypes.ES26 || devtype == HFCTypes.Cisco_64657T || devtype == HFCTypes.CiscoEDFA || devtype == HFCTypes.OTECWos
					|| devtype == HFCTypes.wos3000SCTE || devtype == HFCTypes.wos4000 || devtype == HFCTypes.LYTB_MTRAN2000 || devtype == HFCTypes.wos5000
					|| devtype == HFCTypes.TransDM_SCTE) {

				result.ID = "";
				result.MD = "";
				result.SN = "";
				result.DEVICEID = "";
			} else {
				result.ID = recVBs.elementAt(1).getVariable().toString();					
				result.MD = recVBs.elementAt(2).getVariable().toString();					
				result.SN = recVBs.elementAt(3).getVariable().toString();					
				result.DEVICEID = recVBs.elementAt(6).getVariable().toString();
				
			}
        }
        catch (Exception e)
        {
            return;
        }
	}
}
