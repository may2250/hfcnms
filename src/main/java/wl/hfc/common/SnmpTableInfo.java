package wl.hfc.common;
import org.snmp4j.smi.VariableBinding;
import java.util.ArrayList;


public class SnmpTableInfo  {
    public   ArrayList<VariableBinding> TableCells;
    public   ArrayList StatusTableCells;
    public int ColNum;
    public int RowNum;
    public boolean IsNetErr;
    public SnmpTableInfo()
    {    	
        TableCells = new  ArrayList<VariableBinding>();
        ColNum = 0;
        RowNum = 0;
        IsNetErr = false;
    }

}