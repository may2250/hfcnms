package wl.hfc.online;



import org.json.simple.JSONObject;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;









import wl.hfc.common.PduSevr;


import wl.hfc.common.SnmpTableInfo;
import wl.hfc.common.VariableSnmpVar;

import java.util.ArrayList;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;

public class SnmpEngine {

	public static CommunityTarget createMajorPDU(String ipAddress, String pROCommunity, int pSnmpVersion) {
		try {

			CommunityTarget target = new CommunityTarget();

			target.setCommunity(new OctetString(pROCommunity));

			target.setVersion(pSnmpVersion);//

			target.setAddress(new UdpAddress(ipAddress + "/" + "161"));// DFVdf

			target.setRetries(1); //

			target.setTimeout(3000); //

			return target;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static PDU createThredParamPDU(VariableSnmpVar tmpTagInfo) {
		PDU ThredParamPdu = new PDU();
		ThredParamPdu.setType(PDU.GET);
		for (int i = 0; i < tmpTagInfo.subVariableSnmpVarS.length; i++) {
			ThredParamPdu.add(new VariableBinding(tmpTagInfo.subVariableSnmpVarS[i].FullSnmpOid));
		}

		return ThredParamPdu;
	}

	public static PDU createTableThredParamPDU(VariableSnmpVar tmpTagInfo, int rowNumber) {
		PDU ThredParamPdu = new PDU();
		ThredParamPdu.setType(PDU.GET);
		for (int i = 0; i < tmpTagInfo.subVariableSnmpVarS.length; i++) {
			String oidrs = tmpTagInfo.subVariableSnmpVarS[i].VarInfo.ParamMibOID.toString() + "." + rowNumber;
			ThredParamPdu.add(new VariableBinding(new OID(oidrs)));
		}

		return ThredParamPdu;
	}

	public static VariableSnmpVar[] ParseBasicVars(VariableSnmpVar[] specificVar, PDU inpdu) throws Exception {

		if (inpdu.getVariableBindings().size() != specificVar.length)
			return null;

		for (int i = 0; i < specificVar.length; i++) {
			specificVar[i].CurrentVarBind = (VariableBinding) inpdu.getVariableBindings().elementAt(i);
			if (specificVar[i].CurrentVarBind == null) {
				throw new Exception("paramGetException,Failed!");
			}
		}

		return specificVar;
	}

	public static SnmpTableInfo GetMibTableVariables(PDU outpdu, CommunityTarget cgt, PduSevr server) {
		SnmpTableInfo retval = new SnmpTableInfo();
		int[] entryoid1 = null;
		if (outpdu.getVariableBindings().size() <= 0)
			return retval;

		retval.ColNum = outpdu.getVariableBindings().size();
		VariableBinding vb0 = (VariableBinding) outpdu.getVariableBindings().elementAt(0);
		byte[] entryoid = vb0.getOid().toByteArray();

		PDU inpdu;

		while (true) {
			// ���÷��͵�PDU�����͵����硣

			// ����PDU
			inpdu = server.SyncSendSnmpPdu(outpdu, cgt);
			if (inpdu == null) {
				// if return a null pdu ,tell the above level
				retval.IsNetErr = true;
				break;
			}
			// �����յ�PDU�ı������б����Ƿ���ȷ��
			if (inpdu.getVariableBindings().size() != retval.ColNum)
				break;
			// ���GetNext������õı���OID�Ƿ���ȷ��
			VariableBinding vb1 = (VariableBinding) inpdu.getVariableBindings().elementAt(0);
			byte[] invaroid = vb1.getOid().toByteArray();
			if (invaroid.length <= entryoid.length)
				break;
			for (int i = entryoid.length - 1; i >= 0; i--) {
				if (invaroid[i] != entryoid[i])
					return retval;
			}

			for (int i = 0; i < inpdu.getVariableBindings().size(); i++) {
				retval.TableCells.add((VariableBinding) inpdu.getVariableBindings().elementAt(i));
			}
			retval.RowNum++;
			// ������һ��(��һ�в��� ��GetNext�����ı������б�
			outpdu.clear();
			for (int i = 0; i < retval.ColNum; i++) {
				VariableBinding vbItem = (VariableBinding) inpdu.getVariableBindings().elementAt(i);

				outpdu.add(new VariableBinding(vbItem.getOid()));
			}
		}
		return retval;

	}

	public static ArrayList<VariableBinding> cutMajorVaribaleWithThold(WosParamForSetInfo paramSetList, VariableSnmpVar tmpTagInfo) {
		ArrayList<VariableBinding> result = new ArrayList<VariableBinding>();

		for (int k = 0; k < 5; k++) {
			if (tmpTagInfo.subVariableSnmpVarS[k].CurrentVarBind != null) {
				result.add(new VariableBinding(tmpTagInfo.subVariableSnmpVarS[k].FullSnmpOid, new Integer32(paramSetList.pmSetList[k])));
			}
		}

		result.add(new VariableBinding(tmpTagInfo.subVariableSnmpVarS[5].FullSnmpOid, new OctetString(new byte[] { paramSetList.sByte })));
		return result;
	}

	public static ArrayList<VariableBinding> cutTableVaribaleWithThold(WosParamForSetInfo paramSetList, VariableSnmpVar tmpTagInfo, int rowNumber) {
		ArrayList<VariableBinding> result = new ArrayList<VariableBinding>();
		String oidrs;
		for (int k = 0; k < 5; k++) {

			oidrs = tmpTagInfo.subVariableSnmpVarS[k].VarInfo.ParamMibOID.toString() + "." + rowNumber;
			result.add(new VariableBinding(new OID(oidrs), new Integer32(paramSetList.pmSetList[k])));

		}

		oidrs = tmpTagInfo.subVariableSnmpVarS[5].VarInfo.ParamMibOID.toString() + "." + rowNumber;
		result.add(new VariableBinding(new OID(oidrs), new OctetString(new byte[] { paramSetList.sByte })));
		return result;

	}

	
	
	//基本参数
	public static JSONObject snmpVarToJason(VariableSnmpVar[] pMjVariables, JSONObject pJson) {

		for (int i = 0; i < pMjVariables.length; i++) {
			// System.out.print(arr[i] + "  ");
			String vale = pMjVariables[i].ToDispString();
			pJson.put(pMjVariables[i].VarInfo.ParamMibLabel, vale);

		}

		return pJson;

	}

	
	//表参数值
	public  static JSONObject tabVarToJason( VariableSnmpVar[] cInputVariables,VariableSnmpVar[] cOutputVariables,SnmpTableInfo tabVariables, SnmpTableInfo pOutVariables, JSONObject pJson) {
		int enumi, enumj;
		int i = 0;

		for (enumi = 0; enumi < tabVariables.RowNum; enumi++) {
			for (enumj = 0; enumj < tabVariables.ColNum; enumj++) {

				String vale = cInputVariables[enumj].ToDispString(tabVariables.TableCells.get(i));
				pJson.put(cInputVariables[enumj].VarInfo.ParamMibLabel + "_row" + enumi, vale);
				i++;
			}
		}

		i = 0;

		for (enumi = 0; enumi < pOutVariables.RowNum; enumi++) {
			for (enumj = 0; enumj < pOutVariables.ColNum; enumj++) {

				String vale = cOutputVariables[enumj].ToDispString(pOutVariables.TableCells.get(i));
				pJson.put(cOutputVariables[enumj].VarInfo.ParamMibLabel + "_row" + enumi, vale);
				i++;
			}
		}

		return pJson;

	}
	
	//基本参数门限
	public static JSONObject ThreadPramVarToJason(VariableSnmpVar pMjVariable, JSONObject pJson, boolean isWithName) {

		// System.out.print(arr[i] + "  ");
		String vale;
		if (pMjVariable.withNoThreashold) {
			for (int j = 0; j < pMjVariable.subVariableSnmpVarS.length; j++) {

				if (j == 5) {
					vale = pMjVariable.subVariableSnmpVarS[j].ToDispString();
					byte brst = (byte) Integer.parseInt(vale, 16);
					if ((brst & 0x08) != 0)
						pJson.put(pMjVariable.VarInfo.ParamMibLabel + "HIHI", "1");
					else
						pJson.put(pMjVariable.VarInfo.ParamMibLabel + "HIHI", "0");
					if ((brst & 0x04) != 0)
						pJson.put(pMjVariable.VarInfo.ParamMibLabel + "HI", "1");
					else
						pJson.put(pMjVariable.VarInfo.ParamMibLabel + "HI", "0");
					if ((brst & 0x02) != 0)
						pJson.put(pMjVariable.VarInfo.ParamMibLabel + "LO", "1");
					else
						pJson.put(pMjVariable.VarInfo.ParamMibLabel + "LO", "0");
					if ((brst & 0x01) != 0)
						pJson.put(pMjVariable.VarInfo.ParamMibLabel + "LOLO", "1");
					else
						pJson.put(pMjVariable.VarInfo.ParamMibLabel + "LOLO", "0");

				} else

				{
					vale = pMjVariable.subVariableSnmpVarS[j].ToDispString();
					pJson.put(pMjVariable.VarInfo.ParamMibLabel + j, vale);

				}

			}

		}

		return pJson;

	}

		
	//表参数门限
	public static JSONObject ThreadPramVarToJason(VariableSnmpVar tableVariable, JSONObject pJson, int row, boolean isWithName) {

		// System.out.print(arr[i] + "  ");
		String vale;
		if (tableVariable.withNoThreashold) {
			VariableSnmpVar[] subVariableSnmpVarS = tableVariable.subTableVariableSnmpVarSS.get(row);
			for (int j = 0; j < subVariableSnmpVarS.length; j++) {

				if (j == 5) {
					vale = subVariableSnmpVarS[j].ToDispString();
					byte brst = (byte) Integer.parseInt(vale, 16);
					if ((brst & 0x08) != 0)
						pJson.put(tableVariable.VarInfo.ParamMibLabel + row + "HIHI", "1");
					else
						pJson.put(tableVariable.VarInfo.ParamMibLabel + row + "HIHI", "0");
					if ((brst & 0x04) != 0)
						pJson.put(tableVariable.VarInfo.ParamMibLabel + row + "HI", "1");
					else
						pJson.put(tableVariable.VarInfo.ParamMibLabel + row + "HI", "0");
					if ((brst & 0x02) != 0)
						pJson.put(tableVariable.VarInfo.ParamMibLabel + row + "LO", "1");
					else
						pJson.put(tableVariable.VarInfo.ParamMibLabel + row + "LO", "0");
					if ((brst & 0x01) != 0)
						pJson.put(tableVariable.VarInfo.ParamMibLabel + row + "LOLO", "1");
					else
						pJson.put(tableVariable.VarInfo.ParamMibLabel + row + "LOLO", "0");

				} else

				{
					vale = subVariableSnmpVarS[j].ToDispString();
					pJson.put(tableVariable.VarInfo.ParamMibLabel + row + j, vale);

				}

			}

		}

		return pJson;

	}
	
	public static JSONObject ThreadPramVarToJason(VariableSnmpVar pMjVariable, JSONObject pJson) {

		// System.out.print(arr[i] + "  ");
		String vale;
		if (pMjVariable.withNoThreashold) {
			for (int j = 0; j < pMjVariable.subVariableSnmpVarS.length; j++) {

				if (j == 5) {
					vale = pMjVariable.subVariableSnmpVarS[j].ToDispString();
					byte brst = (byte) Integer.parseInt(vale, 16);
					if ((brst & 0x08) != 0)
						pJson.put("HIHI", "1");
					else
						pJson.put("HIHI", "0");
					if ((brst & 0x04) != 0)
						pJson.put("HI", "1");
					else
						pJson.put("HI", "0");
					if ((brst & 0x02) != 0)
						pJson.put("LO", "1");
					else
						pJson.put("LO", "0");
					if ((brst & 0x01) != 0)
						pJson.put("LOLO", "1");
					else
						pJson.put("LOLO", "0");

				} else

				{
					vale = pMjVariable.subVariableSnmpVarS[j].ToDispString();
					pJson.put("value" + j, vale);

				}

			}

		}

		return pJson;

	}
	
	public static JSONObject ThreadPramVarToJason(VariableSnmpVar tableVariable, JSONObject pJson, int row) {

		// System.out.print(arr[i] + "  ");
		String vale;
		if (tableVariable.withNoThreashold) {
			VariableSnmpVar[] subVariableSnmpVarS = tableVariable.subTableVariableSnmpVarSS.get(row);
			for (int j = 0; j < subVariableSnmpVarS.length; j++) {

				if (j == 5) {
					vale = subVariableSnmpVarS[j].ToDispString();
					byte brst = (byte) Integer.parseInt(vale, 16);
					if ((brst & 0x08) != 0)
						pJson.put("HIHI", "1");
					else
						pJson.put("HIHI", "0");
					if ((brst & 0x04) != 0)
						pJson.put("HI", "1");
					else
						pJson.put("HI", "0");
					if ((brst & 0x02) != 0)
						pJson.put("LO", "1");
					else
						pJson.put("LO", "0");
					if ((brst & 0x01) != 0)
						pJson.put("LOLO", "1");
					else
						pJson.put("LOLO", "0");

				} else

				{
					vale = subVariableSnmpVarS[j].ToDispString();
					pJson.put("value" + j, vale);

				}

			}

		}

		return pJson;

	}
	


	

	
	
	public static ArrayList<VariableBinding> cutMajorVaribaleSingle(WosParamForSetInfo paramSetList, VariableSnmpVar tmpTagInfo) {
		ArrayList<VariableBinding> result = new ArrayList<VariableBinding>();
		if (tmpTagInfo.CurrentVarBind != null)
			result.add(new VariableBinding(tmpTagInfo.FullSnmpOid, new Integer32(paramSetList.pmSetList[0])));
		return result;
	}

	public static ArrayList<VariableBinding> cutTableVaribaleSingle(WosParamForSetInfo paramSetList, VariableSnmpVar tmpTagInfo, int rowNumber) {
		String oidrs;
		ArrayList<VariableBinding> result = new ArrayList<VariableBinding>();
		if (tmpTagInfo.CurrentVarBind != null) {
			oidrs = tmpTagInfo.VarInfo.ParamMibOID.toString() + "." + rowNumber;
			result.add(new VariableBinding(new OID(oidrs), new Integer32(paramSetList.pmSetList[0])));
		}

		return result;
	}

}
