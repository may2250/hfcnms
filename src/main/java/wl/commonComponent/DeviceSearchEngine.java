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

import com.adventnet.snmp.snmp2.SnmpAPI;
import com.adventnet.snmp.snmp2.SnmpOID;
import com.adventnet.snmp.snmp2.SnmpPDU;
import com.adventnet.snmp.snmp2.UDPProtocolOptions;
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
    public static EnumLogoVersion logoVersion = EnumLogoVersion.prevail;//当前网管定制版本
    public static Boolean isAdapteLYTBdevs;//海南广电，温州瑞安需要打开
	public DeviceSearchEngine(SearchIpInfo sipIf,StaticMemory staticmemory) throws IOException{
		this.ipinfo = sipIf;
		this.staticmemory = staticmemory;
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
			//判断设备是否已注册
			if(MainKernel.me.listDevHash.containsKey(ipaddr)){
				return;
			}
			
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
			JSONObject rootjson = new JSONObject();
			rootjson.put("cmd", "devsearch-result");
			rootjson.put("ipaddr", result.mNetAddress);
			rootjson.put("devtype", result.mNetType.toString());
			rootjson.put("hfctype", result.HFCType1.toString());
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
        if (destiType == 1)    //btkel ,RTL1550Transmitter，wos3000SCTE,WOS4000
        //   if (true)
        {
            //强制pdu的版本为V2
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

            outpdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.2.0"))); //设备的系统OID。
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.1.0")));//HFC的commonNELogicalID
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.3.0")));//HFC的commonNEModelNumber
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.4.0")));//HFC的commonNESerialNumber
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.18.0")));    //需要实验确定
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.5.0")));    //.2.0
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.19.0")));

        }
        else if (destiType == 2)//otec光平台
        {
            //   outpdu.BroadCastEnable = true;
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.3.0")));
            outpdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.17409.1.3.1.19.0")));

        }
        else if (destiType == 3)//lytb MTRAN2000光平台
        {
            if (isAdapteLYTBdevs)
            {
                outpdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.2.0")));
                outpdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.5.0")));

            }

        }

        AyncSendSnmpPdu(outpdu,cyt);


    }

	
	@Override
    public void run() {		
		try{
			//if广播搜索
	        if (ipinfo.isBroadCast)
	        {
	            SearchAgentByIpAddressAnycBrdcst(ipinfo.community);
	        }
	        else
	        {
	        	int i = 0;
	            //else 根据地址搜索
	            for (; NetDataProcess.CompareIpAddress(ipinfo.ipbegin, ipinfo.ipend) <= 0; )
	            {
	                SearchAgentByIpAddressAnyc(ipinfo.ipbegin, ipinfo.community, ipinfo.destiType);                
					Thread.sleep(20);
					JSONObject rootjson = new JSONObject();
					rootjson.put("cmd", "devsearchprocess");
					rootjson.put("process", i%ipinfo.searchnumbers*100);
					staticmemory.sendRemoteStr(rootjson.toJSONString(), ipinfo.sessionid);
					i++;
					ipinfo.ipbegin = NetDataProcess.IncIpAddress(ipinfo.ipbegin);	                
	            }
	        }
		}catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
    }
}