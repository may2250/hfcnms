package wl.commonComponent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;


import com.xinlong.util.SearchIpInfo;
import com.xinlong.util.StaticMemory;

import wl.hfc.common.CDevForCMD;
import wl.hfc.common.ClsLanguageExmp.EnumLogoVersion;
import wl.hfc.common.DProcess;
import wl.hfc.common.NetDataProcess;
import wl.hfc.common.OidToHFCType;
import wl.hfc.common.PduSevr;
import wl.hfc.common.nojuDeviceTableRow.HFCTypes;
import wl.hfc.topd.MainKernel;

public class DeviceSearchEngine extends Thread{
	private SearchIpInfo ipinfo;
	private Snmp session;
	public static PDUServerSearch pdusearcher;
	private static StaticMemory staticmemory;
    public static EnumLogoVersion logoVersion = EnumLogoVersion.prevail;//褰撳墠缃戠瀹氬埗鐗堟湰
    private int processint = 1;
    
	public DeviceSearchEngine(SearchIpInfo sipIf,StaticMemory staticmemory) throws IOException{
		this.ipinfo = sipIf;
		this.staticmemory = staticmemory;
		initSnmpAPI();    
	}
	
	private void initSnmpAPI() throws IOException {		
		// 鍒涘缓SNMP鍗忚閫氫俊寮曟搸瀵硅薄銆�
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
		// System.Console.Out.WriteLine("杩涜浜嗕竴娆″悓姝ュ彂閫�");

	}

	public boolean AyncSendSnmpPdu(final PDU outpdu, final CommunityTarget target) {
		final CountDownLatch latch = new CountDownLatch(1);  
		ResponseListener listener = new ResponseListener() {
			public void onResponse(ResponseEvent event) {
				//System.out.println("---------->寮�濮嬪紓姝ヨВ鏋�<------------");
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

		// 鍙戦�佹姤鏂�
		try {
			session.send(outpdu, target, null, listener);
			return true;
		} catch (Exception e) {
			return false;
		}

	}
	
	@SuppressWarnings("unchecked")
	public void readResponse(ResponseEvent respEvnt) {
		// 瑙ｆ瀽Response
		// System.out.println("------------>瑙ｆ瀽Response<----------");

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
		//System.out.println("------------>瑙ｆ瀽Response<----------" + recVBs.toString());
		HFCTypes hfctyp1 = OidToHFCType.getType(recVBs);
		if (hfctyp1 == HFCTypes.Unknown) // 澶勭悊鏈煡璁惧
		{
			return;
		}
		try
        {
			System.out.println("------------>鍙戠幇璁惧.....<----------" + ipaddr);
			//鍒ゆ柇璁惧鏄惁宸叉敞鍐�
			if(MainKernel.me.listDevHash.containsKey(ipaddr)){
				return;
			}
			
			CDevForCMD result = new CDevForCMD();
			result.mNetAddress = ipaddr;
			result.HFCType1 = hfctyp1;
			result.mNetType = DProcess.getNetTypes(hfctyp1);
			if (hfctyp1 == HFCTypes.ES26 || hfctyp1 == HFCTypes.Cisco_64657T || hfctyp1 == HFCTypes.CiscoEDFA || hfctyp1 == HFCTypes.OTECWos
					|| hfctyp1 == HFCTypes.wos3000SCTE || hfctyp1 == HFCTypes.wos4000 || hfctyp1 == HFCTypes.LYTB_MTRAN2000 || hfctyp1 == HFCTypes.wos5000
					|| hfctyp1 == HFCTypes.TransDM_SCTE) {

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
			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "devsearch-result");
			rootjson.put("ipaddr", result.mNetAddress);
			rootjson.put("devtype", "  ");
			rootjson.put("hfctype",OidToHFCType.GetHFCTypeString(result.HFCType1 ));
			rootjson.put("rcommunity", ipinfo.community);
			staticmemory.sendRemoteStr(rootjson.toJSONString(), ipinfo.sessionid);
        }
        catch (Exception e)
        {
            return;
        }
	}
	
	private void SearchAgentByIpAddressAnycBrdcst(String commu) throws UnknownHostException, InterruptedException
    {
        for (int i = 0; i < 3; i++)
        {
            Thread.sleep(20);
            SearchAgentByIpAddressAnyc(InetAddress.getByAddress(("255.255.255.255").getBytes()), commu, 1);
        }


    }
	
	private void SearchAgentByIpAddressAnyc(InetAddress ipaddr, String commu, int destiType)
    {
        PDU outpdu;
        outpdu = new PDU();
        CommunityTarget cyt = new CommunityTarget();
        cyt.setCommunity(new OctetString(commu));
        cyt.setAddress(new UdpAddress(ipaddr.toString()+"/161"));        
        outpdu.setType(PDU.GET);
        if (destiType == 1)    //WOS4000
        {
        	cyt.setVersion(SnmpConstants.version2c);
            //   outpdu.BroadCastEnable = true;
            //    outpdu.ClientID = this.AsyncClientID;
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.2.0")));
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.5.0")));
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.5591.1.3.1.3.0")));

        }
        else if (destiType == 0)
        {
            //    outpdu.BroadCastEnable = true;
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.2.0"))); //璁惧鐨勭郴缁烵ID銆�
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.1.0")));//HFC鐨刢ommonNELogicalID
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.3.0")));//HFC鐨刢ommonNEModelNumber
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.4.0")));//HFC鐨刢ommonNESerialNumber
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.18.0")));    //闇�瑕佸疄楠岀‘瀹�
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.5.0")));    //.2.0
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.19.0")));
        }

        AyncSendSnmpPdu(outpdu,cyt);


    }

	
	@Override
    public void run() {		
		try{
			//if骞挎挱鎼滅储
	        if (ipinfo.isBroadCast)
	        {
	            SearchAgentByIpAddressAnycBrdcst(ipinfo.community);
	        }
	        else
	        {
	        	long startip = NetDataProcess.getIP(ipinfo.ipbegin);
	        	long endip = NetDataProcess.getIP(ipinfo.ipend);
	            //else 鏍规嵁鍦板潃鎼滅储
	        	//System.out.println("--startip="+ startip+"----endip="+endip);
	            while (startip <= endip)
	            {
	                SearchAgentByIpAddressAnyc(NetDataProcess.toIP(startip), ipinfo.community, ipinfo.destiType);  
					Thread.sleep(20);
					JSONObject rootjson = new JSONObject();
					rootjson.put("cmd", "devsearchprocess");
					float process = Math.abs((float) processint / ipinfo.searchnumbers * 100);
					//System.out.println("--process===="+process);
					rootjson.put("process", process);
					staticmemory.sendRemoteStr(rootjson.toJSONString(), ipinfo.sessionid);
					processint++;
					//ipinfo.ipbegin = NetDataProcess.IncIpAddress(ipinfo.ipbegin);	  
					startip++;
	            }
	        }
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
    }
}
