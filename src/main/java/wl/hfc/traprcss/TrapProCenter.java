package wl.hfc.traprcss;

import com.adventnet.snmp.snmp2.*;

import java.io.IOException;

import java.util.Hashtable;
import java.util.Map;

import org.snmp4j.PDU;
import org.snmp4j.smi.*;
import org.springframework.context.support.StaticApplicationContext;

import wl.hfc.common.ClsLanguageExmp;
import wl.hfc.common.NlogType;
import wl.hfc.common.NlogType.TrapLogTypes;
import wl.hfc.common.nojuParmsTableRow;
import wl.hfc.common.nojuTrapLogTableRow;
import wl.hfc.online.pmls;

import java.util.Date;

public class TrapProCenter {

	public static class SupportedTrapEnterprises {
		public static String wos2kIdent = "1.3.6.1.4.1.17409.8888.1";
		public static String nscrtvHFCemsTree = "1.3.6.1.4.1.17409.1";
		public static String wos3kIdent = "1.3.6.1.4.1.2000.1.3000";
		public static String es10Ident = "1.3.6.1.4.1.5591.1";
		public static String wos4000Ident = "1.3.6.1.4.1.5591.1.0";
	}

	private Hashtable ParamsHash;

	public TrapProCenter(Boolean enableMibProcess, String pPath) {
		super();
		new MibProcess(pPath);

	}

	public TrapProCenter() {

		// new PlusTrapOids();
		// ����MIBϵͳ�Ĳ�����ϣ��
		ParamsHash = new Hashtable(800, 0.75F);
	}

	private static int tmpVAL = 0;

	public static SnmpOID ParseAlarmInform(byte[] data) {
		SnmpOID oid = null;
		tmpVAL = 0;
		if (data.length < 2)
			return null;
		if (data[0] != 0x06)
			return null;

		int oidindex = 1, oidlen;
		if ((data[1] & 0x80) == 0) {
			oidlen = data[1] + 1;
		} else {
			int arrayindex = 2 + (data[1] & 0x7f);
			oidlen = 0;
			for (int i = 2; i < arrayindex; i++) {
				oidlen = (oidlen << 8) + data[i];
			}
			oidlen += arrayindex - 1;
		}
		byte[] arrayoid = new byte[oidlen];
		System.arraycopy(data, oidindex, arrayoid, 0, oidlen);
		oid = new SnmpOID(new ASN1Parser(arrayoid).decodeOID());
		oidindex += oidlen;
		if (data[oidindex++] != 0x02)
			return null;
		byte[] arrayval = new byte[data.length - oidindex];
		System.arraycopy(data, oidindex, arrayval, 0, arrayval.length);
		tmpVAL = new ASN1Parser(arrayval).decodeInteger();
		return oid;
	}

	public nojuTrapLogTableRow ProcessTrapRequestPduHandler(Map<String, String> alarm, int requestID, PDU pdu) {

		String traptype = alarm.get("traptype");
		String Enterprise = alarm.get("enterprise");
		try {
			if (Integer.valueOf(traptype) != 6)// ���ֶβ�����6����˵����RFC��׼TRAP���塣
			{
				// return ProcessGenericTraps(alarm);
				return new nojuTrapLogTableRow(false);
			} else if (Enterprise.equalsIgnoreCase(SupportedTrapEnterprises.nscrtvHFCemsTree)) {
				nojuTrapLogTableRow tpa = ProcessHFCTraps(alarm, pdu);
				return tpa;
			}

			else {
				return new nojuTrapLogTableRow(false);
			}
		} catch (Exception ex) {

			// log4net.LogManager.GetLogger("logerror").ErrorFormat(ex.ToString()
			// + ex.Message);
			ex.printStackTrace();
			// need to save the log to runing logs
			return new nojuTrapLogTableRow(false);

		}
	}

	public nojuTrapLogTableRow ProcessHFCTraps(Map<String, String> alarm, PDU pdu) throws IOException {
		String status = alarm.get("status");
		String ipdd = alarm.get("ip");
		switch (Integer.valueOf(status)) {
		case 0:// hfcColdstart
				// ParseTrapHfcColdStart(devmac,logicalid);
			return new nojuTrapLogTableRow(false);
		case 1:// hfcAlarmevent
			return ParseTrapHfcAlarmEvent(ipdd, pdu);
		default:
			return new nojuTrapLogTableRow(false);

		}
	}

	public byte[] hexStringToBytes(String hexString) {

		hexString = hexString.toLowerCase();
		final byte[] byteArray = new byte[hexString.length() / 2];
		int k = 0;
		for (int i = 0; i < byteArray.length; i++) {
			// ��Ϊ��16���ƣ����ֻ��ռ��4λ��ת�����ֽ���Ҫ����16���Ƶ��ַ�����λ����
			byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
			byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
			byteArray[i] = (byte) (high << 4 | low);
			k += 2;
		}
		return byteArray;
	}

	public String GetAlarmEnumString(byte num) {
		switch (num) {
		case 1:
			return "NORMAL";
		case 2:
			return "HIHI";
		case 3:
			return "HI";
		case 4:
			return "LO";
		case 5:
			return "LOLO";
		case 6:
			return "Discrete Major";
		case 7:
			return "Discrete Minor";
		default:
			return "Unkown Alarm";
		}

	}

	public nojuParmsTableRow GetParamInfo(String miblabel) {

		return (nojuParmsTableRow) pmls.tab1.get(miblabel);

	}

	public nojuTrapLogTableRow ParseTrapHfcAlarmEvent(String pAddress, PDU pdu) throws IOException {

		// if (pdu.VariableBindings.Count < 3)
		// return new CTrap(false);
		String logicalID;
		String macaddr;
		String trapstring = "";
		String paramName = "";
		VariableBinding vbd = (VariableBinding) pdu.getVariableBindings().elementAt(0);
		macaddr = vbd.toString();
		vbd = (VariableBinding) pdu.getVariableBindings().elementAt(1);
		logicalID = vbd.toString();
		vbd = (VariableBinding) pdu.getVariableBindings().elementAt(2);
		String alarminfoss = (vbd.getVariable().toString());
		alarminfoss = alarminfoss.replace(":", "");
		byte[] alarminfo = hexStringToBytes(alarminfoss);
		if (alarminfo.length < 6)
			return new nojuTrapLogTableRow(false);
		trapstring += ClsLanguageExmp.trapDiscrGet("类型") + GetAlarmEnumString(alarminfo[4]) + " ";
		byte[] alarmvb = new byte[alarminfo.length - 6];
		System.arraycopy(alarminfo, 6, alarmvb, 0, alarmvb.length);
		SnmpOID oid = null;
		tmpVAL = 0;
		String pValue = "";
		// string pValue = string.Empty;
		if ((oid = ParseAlarmInform(alarmvb)) != null) {
			String plabel = MibProcess.getLabel(oid);
			if (!plabel.equalsIgnoreCase("")) {
				nojuParmsTableRow pararmrow = GetParamInfo(plabel);
				int[] nodeoid = MibProcess.getOID(oid);
				int[] oidarray = oid.toIntArray();
				String exstr = "";
				if (oidarray.length > nodeoid.length) {
					for (int i = nodeoid.length; i < oidarray.length; i++) {
						exstr += "." + oidarray[i];
					}
				}

				// //go to trapOid serch the paramName
				// if (pararmrow == null)
				// {
				// if (PlusTrapOids.trapParamOiDLib[oid.ToString()] != null)
				// {
				// pararmrow =
				// GetParamInfo((string)PlusTrapOids.trapParamOiDLib[oid.ToString()]);
				// exstr = string.Empty;
				// }
				//
				// }
				if (pararmrow != null) {
					paramName = pararmrow.ParamDispText + exstr;
					trapstring += ClsLanguageExmp.trapDiscrGet("名称") + paramName + " ";

					if (pararmrow.IsFormatEnable) {

						float tmpf = tmpVAL * pararmrow.FormatCoff;
						// pValue=String.valueOf(tmpf);
						pValue = tmpf + pararmrow.FormatUnit;
						trapstring += ClsLanguageExmp.trapDiscrGet("值") + pValue + " ";

					} else {
						paramName = plabel + exstr;
						pValue = String.valueOf(tmpVAL);
						trapstring += ClsLanguageExmp.trapDiscrGet("值") + pValue + " ";
					}

				} else {
					paramName = oid.toString();
					trapstring += ClsLanguageExmp.trapDiscrGet("名称") + paramName;
					pValue = String.valueOf(tmpVAL);
					trapstring += ClsLanguageExmp.trapDiscrGet("值") + pValue + " ";

				}
				System.out.println(trapstring);
				/*
				 * return new CTrap(NlogType.GetTrapLogType(alarminfo[4]),
				 * pAddress, trapstring, new Date(), paramName);
				 */
				TrapLogTypes type = NlogType.GetTrapLogType(alarminfo[4]);

				return new nojuTrapLogTableRow(NlogType.getAlarmLevel(type), type, pAddress, "neName", trapstring, new Date(), "", "", paramName, pValue);
			}

		}

		return new nojuTrapLogTableRow(false);
	}

}
