package wl.hfc.common;




public class nojuParmsTableRow {
	public String ParamMibLabel;
	public String ParamOrignalOID;
	public String ParamMibOID;
	public String ParamDispText;
	public boolean IsFormatEnable;
	public float FormatCoff;
	public String FormatText;
	public String FormatUnit;

	public nojuParmsTableRow(String miblabel, String miboid, String disptxt,
			boolean fmten, float fmtcoff, String fmttxt, String fmtunit) {
		ParamMibLabel = miblabel;
		ParamDispText = disptxt;

		
		ParamMibOID = miboid;
		
		
		IsFormatEnable = fmten;
		FormatCoff = fmtcoff;
		FormatText = fmttxt;
		FormatUnit = fmtunit;
	}
	
	
    public nojuParmsTableRow(String miboid, paramInfor paramInfor1)
    {
        ParamMibOID = miboid;
        
        IsFormatEnable = paramInfor1.fmten;
        FormatCoff = paramInfor1.fmtcoff;
        FormatText = paramInfor1.fmttxt;
        FormatUnit = paramInfor1.fmtunit;

    }
    

    

}
