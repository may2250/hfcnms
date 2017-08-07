package wl.hfc.online;



import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.util.Hashtable;










import org.w3c.dom.*;
import org.xml.sax.SAXException;

import wl.hfc.common.PduSevr;


import wl.hfc.common.nojuParmsTableRow;
import wl.hfc.topd.MainKernel;

import org.apache.log4j.Logger;
import org.snmp4j.PDU;


public class pmls {

	// private static String thisparamrowsFile = Application.StartupPath +
	// @"\phs.wspb";

	public static Hashtable<String, nojuParmsTableRow> tab1;

	public static Hashtable<String, nojuParmsTableRow> taben;
	private static Document doc;
	private static Document docen;
	private static Logger log = Logger.getLogger(pmls.class);

	private static SnmpUtil util;
	public static PDU recePDU;
	public static pmls me;
	
	public PduSevr sver;
	public pmls() {
		tab1 = new Hashtable<String, nojuParmsTableRow>();
		taben = new Hashtable<String, nojuParmsTableRow>();
		loadDXml();
		me = this;
	}

	public void loadDXml() {
		long lasting = System.currentTimeMillis();
		String filePath = pmls.class.getResource("/").toString();
		filePath = filePath.substring(filePath.indexOf("file:") + 5);
		//log.info("----------------path--->>>" + filePath+ "phs.xml");

	    	
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			File f = new File(filePath + "phs.xml");
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(f);
			
			f=new File(filePath + "phsen.xml");
			builder = factory.newDocumentBuilder();
			docen = builder.parse(f);

		}catch (ParserConfigurationException e) { 
	         e.printStackTrace();  
	         log.info(e.getMessage());
	    } catch (SAXException e) { 
	         e.printStackTrace(); 
	         log.info(e.getMessage());
	    } catch (IOException e) { 
	         e.printStackTrace(); 
	         log.info(e.getMessage());
	    } 
		Element rootElement = doc.getDocumentElement();
		NodeList rootNode = rootElement.getChildNodes();		
		
		boolean IsFormatEnable;
		for (int i = 0; i < rootNode.getLength(); i++) {
			Node node = rootNode.item(i);
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
				//System.out.println("----------" + TagName1);

			}

		}
		
		
		 rootElement = docen.getDocumentElement();
		 rootNode = rootElement.getChildNodes();		
		
		for (int i = 0; i < rootNode.getLength(); i++) {
			Node node = rootNode.item(i);
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
				taben.put(TagName1, resRow);


			}

		}

		System.out.println("----------");	

	}



	public static void doElementGet() {

		NodeList ns = doc.getElementsByTagName("addChannelNumber");
		Element elt = (Element) ns.item(0);
		String sss = elt.getAttribute("ParamOrignalOID");

	}




}
