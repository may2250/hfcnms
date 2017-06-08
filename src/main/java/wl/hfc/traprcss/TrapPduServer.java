package wl.hfc.traprcss;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.json.simple.JSONValue;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import redis.clients.jedis.Jedis;

import wl.hfc.alarmlog.CurrentAlarmModel;

import wl.hfc.common.PduSevr;
import wl.hfc.common.nojuTrapLogTableRow;

import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.smi.*;

public class TrapPduServer {

	public static TrapProCenter trpcss;
	public static String TRAP_ADDRESS = "udp:0.0.0.0/";
	//private static final String TRAP_SERVER_PORT_KEY = "global:trapserver:port";

	private static Snmp snmp = null;
	private Address listenAddress;

	public static CurrentAlarmModel realTrapResponder;

	public TrapPduServer() {

		try {
			// get trap port from db

			String trapport = "162";
			TRAP_ADDRESS = TRAP_ADDRESS + trapport;
			System.out.println("+++++++++TRAP_ADDRESS=" + TRAP_ADDRESS);

			listenAddress = GenericAddress.parse(System.getProperty("snmp4j.listenAddress", TRAP_ADDRESS));
			TransportMapping transport;

			transport = new DefaultUdpTransportMapping((UdpAddress) listenAddress);

			snmp = new Snmp(transport);

			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv1());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv2c());
			snmp.getMessageDispatcher().addMessageProcessingModel(new MPv3());
			snmp.listen();

		} catch (Exception e) {
			e.printStackTrace();
		}

		CommandResponder pduHandler = new CommandResponder() {
			public synchronized void processPdu(CommandResponderEvent e) {

				// doWork
				System.out.println("Ҳ��ҮҮҮҮeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" + e);
				doReceive(e);
			}

		};

		snmp.addCommandResponder(pduHandler);

		// while(true)
		// {
		// try
		// {
		// Thread.sleep(1000);
		// System.out.println("123123123");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }

	}

	@SuppressWarnings("unchecked")
	public void doReceive(CommandResponderEvent event) {
		// /process response
		if (event != null && event.getPDU() != null) {
			PDU inPdu = event.getPDU();

			Vector<VariableBinding> recVBs = (Vector<VariableBinding>) inPdu.getVariableBindings();

			if (event.getSecurityModel() == 2) {
				// trapv2

			} else if (event.getSecurityModel() == 1) {
				int status = ((PDUv1) inPdu).getSpecificTrap();
				int traptype = ((PDUv1) inPdu).getGenericTrap();
				OID enterprise = ((PDUv1) inPdu).getEnterprise();

				// logger.info("--traptype---->>>"+((PDUv1)event.getPDU()).getGenericTrap());
				Map<String, String> hfcalarmhash = new LinkedHashMap();
				hfcalarmhash.put("status", String.valueOf(status));
				hfcalarmhash.put("traptype", String.valueOf(traptype));
				hfcalarmhash.put("enterprise", enterprise.toString());
				hfcalarmhash.put("ip", event.getPeerAddress().toString());
				try {
					nojuTrapLogTableRow traprst = trpcss.ProcessTrapRequestPduHandler(hfcalarmhash, 0, inPdu);
					realTrapResponder.insertTrapLog(traprst);

					// trpcss.ProcessHFCTraps(hfcalarmhash, inPdu);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// for (int i = 0; i < recVBs.size(); i++) {
				// VariableBinding recVB = recVBs.elementAt(i);
				// String content = recVB.getVariable().toString();
				// }

				return;
			}

		}

	}

}
