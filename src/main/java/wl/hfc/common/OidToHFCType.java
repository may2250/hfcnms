package wl.hfc.common;

import java.util.Vector;

import org.snmp4j.smi.VariableBinding;
import wl.hfc.common.nojuDeviceTableRow.HFCTypes;

public class OidToHFCType {

	// HFCTYPE遵循一个原则，先确定万隆自己的设备，随后应用层根据实际版本发布需求是否兼容他家的设备来进一步调用第三方厂家的识别逻辑
	// ，如lid ==
	// ".1.3.6.1.4.1.17409.1.11"是标准的EDFA设备，直接被识别为prevail的EDFA，调用第三方厂家的识别逻辑后，会将识别结果更改覆盖；
	// public static EnumLogoVersion logoVersion = EnumLogoVersion.prevail;

	/// <summary>
	/// 按照给定的HFC设备类型，返回该类型的名称。
	/// </summary>
	/// <param name="type">HFC设备类型。</param>
	/// <returns>设备类型名称。</returns>
	public static String GetHFCTypeString(HFCTypes type) {
		switch (type) {
		case EDFA:
		case HfcFlyEdfa:// 可调式
		case ZBLEDFA:// ZBL厂家设备
			return ClsLanguageExmp.viewGet("光放大器");
		case HfcEdfaWE_HD_SWITCH:
			return ClsLanguageExmp.viewGet("光放大器带切换");
		case Trans1310DM:
		case Trans1550DM:
		case TransOld:
		case TransEM:
			return ClsLanguageExmp.viewGet("光发射机");
		case HfcOptrv:
		case HfcOptrvFofChongqin:
		case HfcReceSXGDS8602J:
		case HfcMinWorkstationJKQ1G1:
		case JLED1202:
		case HfcMinWorkstation:
			return ClsLanguageExmp.viewGet("光接收机");
		case HfcReceJDS:
		case HfcWR1001JS:
			return ClsLanguageExmp.viewGet("光收带切换");
		case OSW:
			return ClsLanguageExmp.viewGet("光切换开关");
		case PreAMP:// 室内型
			return ClsLanguageExmp.viewGet("前置放大器");
		case RFSW:
			return ClsLanguageExmp.viewGet("射频切换开关");
		case HfcWorkstation:
		case HfcPR0WorkStation:
			return ClsLanguageExmp.viewGet("光工作站");
		case HfcWR1004SJL:
		case HfcWR1004SJLofHJS1G2:
		case HfcWR1004SJLMLD1G4GD:
			return ClsLanguageExmp.viewGet("光工作站带切换");

		case HfcWorkStationWR8604HJ:
			return ClsLanguageExmp.isEn ? "WorkStaton With Switch" : "光AGC工作站";
		case SignalGenarator:
			return ClsLanguageExmp.isEn ? "Signal source" : "信号源";
		case RTL1550Transmitter:
			return "RTL1550Transmitter";
		case RTN_WR2004RJ:
			return ClsLanguageExmp.viewGet("反向光接收机");
		case ES26:
			return "BKTEL-ES10";
		case CiscoEDFA:
			return "CISCO-64667C";
		case Cisco_64657T:
			return "Cisco_64657T";

		case wos2000:
			return "WOS2000";
		case wos4000:
			return "Wos4000";

		case Wos3000:
			return "Wos3000";
		case wos3000SCTE:
			return "Wos3000";
		case wos5000:
			return "Wos5000";
		case WR1082_ONU:
			return ClsLanguageExmp.isEn ? "Optical Receiver && ONU" : "光机ONU一体机";

		case LYTB_TBF1000:
			return "TBF1000";
		case LYTBEDFA:
			return "EDFA";
		case LYTBOSswitch:
			return ClsLanguageExmp.viewGet("光切换开关");
		case LYTB_MTRAN2000:
			return "MTRAN 2000";

		case WR2G600R:
			return ClsLanguageExmp.isEn ? "Satellite Optical Receiver" : "卫星光接收机";
		case WR2G600R6_Port:
			return "F-LB61-RX";
		case WT2G600R6_Port:
			return "F-LB61-TX";

		case OTECWos:
			return "AOB01-12";
		case OTECAOT_1310M_10:
			return "AOT-1310M-10";
		case OTECEDFA_AOA1550:
			return "AOA-1550-22";
		case OTECEDFA_EOA1522:
			return "EOA-1522";
		case OTECAOR1000CGMII:
			return "AOR1000CGMII";

		default:
			return ClsLanguageExmp.viewGet("未知设备");
		}

	}

	/// <summary>
	///
	/// 根据oid,lid,devicetype,deviceid,sysname获得对应的HFCTypes
	/// </summary>
	/// <param name="system0id"></param>
	/// <param name="commonDeviceOid"></param>
	/// <param name="stringMD"></param>
	/// <param name="sysname"></param>
	/// <param name="stringID"></param>
	/// <returns></returns>
	public static HFCTypes GetHFCTypeByOid(String system0id, String commonDeviceOid, String stringMD, String stringID) {
		// 6,7 TRANS
		// 11 EDFA
		// 8,9,10 RECE
		if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.5591.1")) {
			return HFCTypes.wos4000;
		}
		if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.5591.1.11")) {
			// return HFCTypes.wos4000;
		} else if (commonDeviceOid.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.11"))// EDFA
		{
			if (stringID.equalsIgnoreCase("WE-HD-SWITCH")) {
				return HFCTypes.HfcEdfaWE_HD_SWITCH;
			} else if (stringID.equalsIgnoreCase("OcomclientsEDFA")) {
				return HFCTypes.OcomclientEDFA;
			} else {
				return HFCTypes.EDFA;
			}

		} else if (commonDeviceOid.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.6"))// DM TRANS
		{
			if (stringID.equalsIgnoreCase("WT-1550-DM"))
				return HFCTypes.Trans1550DM;
			else
				return HFCTypes.Trans1310DM;
		} else if (commonDeviceOid.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.7"))// EM&&OLD TRANS
		{
			if (stringID.equalsIgnoreCase("TRANS-1550"))
				return HFCTypes.TransEM;
			else
				return HFCTypes.TransOld;
		} else if (commonDeviceOid.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.8")) {
			return HFCTypes.RTN_WR2004RJ;
		} else if (commonDeviceOid.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.9")) {
			if (stringMD.equalsIgnoreCase("SXGDS8602J"))
				return HFCTypes.HfcReceSXGDS8602J;
			else if (stringID.equalsIgnoreCase("WR2G600R")) {
				return HFCTypes.WR2G600R;
			} else if (stringID.equalsIgnoreCase("RJ-1G-2-III-CQ")) {
				return HFCTypes.HfcOptrvFofChongqin;
			} else {
				return HFCTypes.HfcOptrv;
			}
		} else if (commonDeviceOid.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.10"))// 接收机
		{
			if (stringID.equalsIgnoreCase("ScanID                         ")
					|| stringID.equalsIgnoreCase("WL00OR220000")) {
				if (stringMD.equalsIgnoreCase("WR8602JL") || stringMD.equalsIgnoreCase("WR8604JL")
						|| stringMD.equalsIgnoreCase("WR8602RJ") || stringMD.equalsIgnoreCase("WR8602RJL")
						|| stringMD.equalsIgnoreCase("WR8604RJL") || stringMD.equalsIgnoreCase("WR8604DJ")
						|| stringMD.equalsIgnoreCase("WR8602JL-CM") || stringMD.equalsIgnoreCase("WR8600")
						|| stringMD.equalsIgnoreCase("WR1004DJ") || stringMD.equalsIgnoreCase("WR1002RJ")
						|| stringMD.equalsIgnoreCase("SCN-1000-2") || stringMD.equalsIgnoreCase("SCN-870-2")
						|| stringMD.equalsIgnoreCase("WR8602ML") || stringMD.equalsIgnoreCase("WR8602JLE")
						|| stringMD.equalsIgnoreCase("WR8602ME") || stringMD.equalsIgnoreCase("FMAU1121")
						|| stringMD.equalsIgnoreCase("WR8602MF-B") || stringMD.equalsIgnoreCase("WR8602M-B")
						|| stringMD.equalsIgnoreCase("WR8604DJ-1G") || stringMD.equalsIgnoreCase("OPS2600")
						|| stringMD.equalsIgnoreCase("WR8602MFH-B"))
					return HFCTypes.HfcMinWorkstation;

				else if (stringMD.equalsIgnoreCase("WR8602JDS"))
					return HFCTypes.HfcReceJDS;
				else if (stringMD.equalsIgnoreCase("WR8604HA") || stringMD.equalsIgnoreCase("WR8604G-S")
						|| stringMD.equalsIgnoreCase("WR8602G-S") || stringMD.equalsIgnoreCase("WR8604HC")
						|| stringID.equalsIgnoreCase("HC-860"))
					return HFCTypes.HfcPR0WorkStation;
				else if (stringMD.equalsIgnoreCase("WR8604HJ") || stringMD.equalsIgnoreCase("SCN-1000-4")
						|| stringMD.equalsIgnoreCase("SCN-870-4") || stringMD.equalsIgnoreCase("WR8604HJ-1G")
						|| stringMD.equalsIgnoreCase("OPS2500") || stringMD.equalsIgnoreCase("WR8604HJ-1G-2P")
						|| stringMD.equalsIgnoreCase("OPS2500-D2R") || stringMD.equalsIgnoreCase("WR8604"))
					return HFCTypes.HfcWorkStationWR8604HJ;
				else
					return HFCTypes.HfcMinWorkstation;
			}

			else if (stringID.equalsIgnoreCase("JLE-86-2") || stringID.equalsIgnoreCase("CEAM-1G-2")
					|| stringID.equalsIgnoreCase("JL-86-2") || stringID.equalsIgnoreCase("DJ-1G-4")
					|| stringID.equalsIgnoreCase("DJ-1G-4-R") || stringID.equalsIgnoreCase("DJ-86-4")
					|| stringID.equalsIgnoreCase("JL-86-4") || stringID.equalsIgnoreCase("RJL-86-4")
					|| stringID.equalsIgnoreCase("RJL-86-2") || stringID.equalsIgnoreCase("RJ-86-2")
					|| stringID.equalsIgnoreCase("RJ-1G-2") || stringID.equalsIgnoreCase("J-1G-2S")
					|| stringID.equalsIgnoreCase("ML-86-2") || stringID.equalsIgnoreCase("JE-1G-2")
					|| stringID.equalsIgnoreCase("JLE-1G-2") || stringID.equalsIgnoreCase("DM-86-2")
					|| stringID.equalsIgnoreCase("ME-86-2") || stringID.equalsIgnoreCase("JL-CM-2")
					|| stringID.equalsIgnoreCase("J-1G-2") || stringID.equalsIgnoreCase("JL-1G-2S")
					|| stringID.equalsIgnoreCase("JL-1G-2") || stringID.equalsIgnoreCase("JL-1G-4")
					|| stringID.equalsIgnoreCase("J-B-1G-2") || stringID.equalsIgnoreCase("M-B-86-2")
					|| stringID.equalsIgnoreCase("CEAM-1G-2") || stringID.equalsIgnoreCase("RJ-1G-2-II")
					|| stringID.equalsIgnoreCase("RJ-1G-2-III") || stringID.equalsIgnoreCase("ID-FMAU")
					|| stringID.equalsIgnoreCase("ID-FMAU-2"))
				return HFCTypes.HfcMinWorkstation;
			else if (stringID.equalsIgnoreCase("JDS-86-2"))
				return HFCTypes.HfcReceJDS;
			else if (stringID.equalsIgnoreCase("HJ-1G") || stringID.equalsIgnoreCase("HJ-860"))
				return HFCTypes.HfcWorkStationWR8604HJ;
			else if (stringID.equalsIgnoreCase("JS-1G-2") || stringID.equalsIgnoreCase("JS-1G-2S")
					|| stringID.equalsIgnoreCase("JSE-1G-2") || stringID.equalsIgnoreCase("JDS-1G-2-II"))
				return HFCTypes.HfcWR1001JS;
			else if (stringID.equalsIgnoreCase("SJL-1G-4"))
				return HFCTypes.HfcWR1004SJL;
			else if (stringID.equalsIgnoreCase("HJS-1G-4"))
				return HFCTypes.HfcWR1004SJLofHJS1G2;
			else if (stringID.equalsIgnoreCase("JK-1G-1"))
				return HFCTypes.HfcMinWorkstationJKQ1G1;
			else if (stringID.equalsIgnoreCase("MLD-1G-4-GD"))
				return HFCTypes.HfcWR1004SJLMLD1G4GD;
			else if (stringID.equalsIgnoreCase("ID-1082M-ONU"))
				return HFCTypes.WR1082_ONU;
			else if (stringID.equalsIgnoreCase("JLED-1202"))
				return HFCTypes.JLED1202;

			else
				return HFCTypes.HfcWorkstation;
		} else if (commonDeviceOid.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.12")) {
			return HFCTypes.PreAMP;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.2000.1.3000")) {
			return HFCTypes.Wos3000;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.8888.1")) {
			return HFCTypes.wos2000;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.9")) {
			return HFCTypes.HfcOptrv;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.10")) {
			return HFCTypes.HfcWorkstation;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.11")) {
			return HFCTypes.EDFA;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.6")) {
			if (stringID.equalsIgnoreCase("WT-1550-DM"))
				return HFCTypes.Trans1550DM;
			else
				return HFCTypes.Trans1310DM;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.7")) {
			if (stringID.equalsIgnoreCase("TRANS-1550"))
				return HFCTypes.TransEM;
			else
				return HFCTypes.TransOld;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.8686")) {
			return HFCTypes.OSW;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.66")) {
			return HFCTypes.RFSW;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.67")) {
			return HFCTypes.SignalGenarator;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.15"))// CHM's WR2G600R6
		{
			return HFCTypes.WT2G600R6_Port;
		} else if (system0id.equalsIgnoreCase(".1.3.6.1.4.1.17409.1.16"))// CHM's WR2G600R6
		{
			return HFCTypes.WR2G600R6_Port;
		}

		return HFCTypes.Unknown;
	}

	public static HFCTypes getType(Vector<VariableBinding> recVBs) {

		VariableBinding vbd = recVBs.elementAt(0);
		String oid = "." + vbd.getVariable().toString();
		vbd = recVBs.elementAt(2);
		String MDss = vbd.getVariable().toString();

		HFCTypes devtype = HFCTypes.Unknown;
		if (recVBs.size() == 4)
			devtype = OidToHFCType.GetHFCTypeByOid(oid, null, null, null);// 需要实验确定
		else if (recVBs.size() == 3) {
			String textsss = oid;
			if (oid.equalsIgnoreCase(".1.3.6.1.4.1.7501.1.2.100")) {
				devtype = HFCTypes.ES26;
			} else if (oid.equalsIgnoreCase(".1.3.6.1.4.1.8072.3.2.10")) {
				devtype = HFCTypes.Cisco_64657T;
			} else if (oid.equalsIgnoreCase(".1.3.6.1.4.1.5591.1.11.1.3")) {
				devtype = HFCTypes.CiscoEDFA;
			} else if (oid.equalsIgnoreCase(".1.3.6.1.4.1.5591.1.11")) {
				if (MDss.equalsIgnoreCase("WOS3000")) {
					devtype = HFCTypes.wos3000SCTE;
				}
				/*
				 * else if (oid2222.c("5000")) { devtype = HFCTypes.wos5000; }
				 */
				else {
					devtype = HFCTypes.wos4000;
				}

			} else if (oid.equalsIgnoreCase(".1.3.6.1.4.1.40146.1.11"))// ascent定制wos4000
			{
				/*
				 * if (CommonHardCoding.logoVersion == EnumLogoVersion.ascent) { devtype =
				 * HFCTypes.wos4000; }
				 */
			} else if (oid.equalsIgnoreCase(".1.3.6.1.4.1.5591.1.11.1.1")) {
				devtype = HFCTypes.TransDM_SCTE;
			}
		} else if (recVBs.size() >= 7) {
			vbd = recVBs.elementAt(4);
			String lid = "." + vbd.getVariable().toString();

			vbd = recVBs.elementAt(6);
			String devid = vbd.getVariable().toString();
			devtype = OidToHFCType.GetHFCTypeByOid(oid, lid, MDss, devid);// 需要实验确定

		}

		// 根据网管特殊版本识别其他厂商的设备
		HFCTypes outherCompanyType = HFCTypes.Unknown;

		// 如果获取到了第三方的，则覆盖当前的devicetype
		if (outherCompanyType != HFCTypes.Unknown) {
			devtype = outherCompanyType;
		}

		return devtype;

	}

}