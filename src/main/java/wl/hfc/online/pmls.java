package wl.hfc.online;



import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.Hashtable;





import org.w3c.dom.*;

import wl.hfc.common.PduSevr;


import wl.hfc.common.nojuParmsTableRow;

import org.snmp4j.PDU;


public class pmls {

	// private static String thisparamrowsFile = Application.StartupPath +
	// @"\phs.wspb";
	public static pmls paramxml1;
	public static Hashtable<String, nojuParmsTableRow> tab1;
	private static Document doc;

	public PduSevr sver;
	private static SnmpUtil util;
	public static PDU recePDU;

	public pmls() {
		tab1 = new Hashtable<String, nojuParmsTableRow>();
		loadDXml();
	}

	public static void loadDXml() {
		long lasting = System.currentTimeMillis();
		File f = new File("D:\\phs.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(f);

		} catch (Exception ex) {

			// write the log to the log4nert

			// set it to the loginformation
		}

		NodeList rootNode = doc.getChildNodes();
		NodeList rootNodeChilds = rootNode.item(0).getChildNodes();

		boolean IsFormatEnable;
		for (int i = 0; i < rootNodeChilds.getLength(); i++) {
			Node node = rootNodeChilds.item(i);
			if (node instanceof Element) {
				Element elt = (Element) node;
				String TagName1 = elt.getTagName();

				Float fmtcoff = new Float(elt.getAttribute("FormatCoff"));
				String IsFormatEnableS = elt.getAttribute("IsFormatEnable");

				if (IsFormatEnableS.equals("True")) {
					IsFormatEnable = true;
				} else {
					IsFormatEnable = false;
				}

				nojuParmsTableRow resRow = new nojuParmsTableRow(TagName1,
						elt.getAttribute("ParamOrignalOID"),
						elt.getAttribute("ParamDispText"), IsFormatEnable,
						fmtcoff, elt.getAttribute("FormatText"),
						elt.getAttribute("FormatUnit"));
				tab1.put(TagName1, resRow);
				// System.out.println(TagName1);

			}

		}

	}



	public static void doElementGet() {

		NodeList ns = doc.getElementsByTagName("addChannelNumber");
		Element elt = (Element) ns.item(0);
		String sss = elt.getAttribute("ParamOrignalOID");

	}




}
