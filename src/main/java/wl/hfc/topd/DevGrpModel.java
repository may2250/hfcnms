package wl.hfc.topd;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import wl.hfc.common.*;


//DevGrpModel将承担拓扑的组建，维护，以及组，设备的增删查改的响应
public class DevGrpModel {

	public  CDatabaseEngine ICDatabaseEngine1;
	private LNode rootListNode;//设备树总节点（虚拟）
	public Hashtable listDevHash = new Hashtable();
    public Hashtable listGrpHash = new Hashtable();
    
    public static DevGrpModel me;
    public DevGrpModel(CDatabaseEngine pICDatabaseEngine)
    {

        this.ICDatabaseEngine1 = pICDatabaseEngine;
        me = this;
    }


	public  void initTopodData() {

		Hashtable devHash = ICDatabaseEngine1.DeviceTableGetAllRows();
		Hashtable grpHash = ICDatabaseEngine1.UserGroupTableGetAllRows();
		// List<CDataBasePropery.nojuDeviceTableRow> SlotRowsList =
		// ICDatabaseEngine1.slotTableGetAllRows();
		rootListNode = this.offerTopodModel(devHash, grpHash);			

		
		
		//print rootListNode;
	}

	

	// by group and device collection args
	private  LNode offerTopodModel(Hashtable devLists, Hashtable grpLists) {
		
		listDevHash.clear();
		listGrpHash.clear();
		LNode result = new LNode();
        result.fullpath="设备树";
		result.Level = 0;
		createTree(devLists, grpLists,result);
		return result;

	}

	private void createTree(Hashtable devLists, Hashtable groupLists, LNode rootNode) {

		LinkedList<UserGroupTableRow> rows = new LinkedList<UserGroupTableRow>();

		Enumeration e = groupLists.elements();

		while (e.hasMoreElements()) {

			UserGroupTableRow item = (UserGroupTableRow) e.nextElement();

			if (item.ParentGroupID == -1) {
				rows.add(item);
			}

		}

		// select the all root nodes
		for (UserGroupTableRow dr : rows) {
			
			// add the child group and the device
			devGroup group = new devGroup(dr.UserGroupID, dr.UserGroupName, dr.ParentGroupID);
			group.BindUserGroupTableRow = dr;
			
	
			group.Level=1;
			rootNode.Nodes.add(group);
			group.parent=rootNode;
			group.fullpath=rootNode.fullpath + "/" + group.BindUserGroupTableRow.UserGroupName;
			group.Tag = group;
			System.out.println(	group.fullpath);
            listGrpHash.put(group.BindUserGroupTableRow.UserGroupID, group);

			CreateTreeNode(group, groupLists, devLists);

		}

	}

	
    private void CreateTreeNode(devGroup pgroup,Hashtable groupLists, Hashtable devLists)
    {
        //select all the child row in the grouptable
    	LinkedList<UserGroupTableRow> rows = new LinkedList<UserGroupTableRow>(); 	
    	
    	
		Enumeration e = groupLists.elements();

		while (e.hasMoreElements()) {

			UserGroupTableRow dr = (UserGroupTableRow) e.nextElement();
            if (dr.ParentGroupID == pgroup.BindUserGroupTableRow.UserGroupID)
            {

                //add the group as child
        
                devGroup newgroup = new devGroup(dr.UserGroupID, dr.UserGroupName, dr.ParentGroupID);
         /*       newgroup.x1 = dr.x1;
                newgroup.x2 = dr.x2;
                newgroup.y1 = dr.y1;
                newgroup.y2 = dr.y2;
                newgroup.isTx = dr.isTx;*/
               // newgroup.name = dr.UserGroupName;
                newgroup.BindUserGroupTableRow = dr;
                
                newgroup.Level=pgroup.Level+1;
                pgroup.Nodes.add(newgroup);
                newgroup.parent=pgroup;
                newgroup.fullpath=pgroup.fullpath + "/" + newgroup.BindUserGroupTableRow.UserGroupName;
                newgroup.Tag = newgroup;
        		System.out.println(	newgroup.fullpath);
        	    listGrpHash.put(newgroup.BindUserGroupTableRow.UserGroupID, newgroup);
                CreateTreeNode(newgroup, groupLists, devLists);
            }

		}

    	
 
        //add the device to this group
        //List<CDataBasePropery.nojuDeviceTableRow> rows2 = new List<CDataBasePropery.nojuDeviceTableRow>();
       
		   	
		e = devLists.elements();

		while (e.hasMoreElements()) {
			nojuDeviceTableRow dr1 = (nojuDeviceTableRow) e.nextElement();
            if (dr1.UserGroupID == pgroup.BindUserGroupTableRow.UserGroupID)
            {
       
                
                DevTopd dev = new DevTopd(dr1); 
                dev.BindnojuDeviceTableRow = dr1;
                
                dev.Level=pgroup.Level+1;
                pgroup.Nodes.add(dev);
                dev.parent=dev;
                dev.fullpath = pgroup.fullpath + "/" + dev.BindnojuDeviceTableRow.Name;
                dev.Tag = dev;
        		System.out.println(	dev.fullpath);
         /*       device.x1 = dr1.x1;
                device.x2 = dr1.x2;
                device.y1 = dr1.y1;
                device.y2 = dr1.y2;
                device.isTx = dr1.isTx;*/
               // device.name = dr1.Name;

                // UserGroup newgroup = new UserGroup((int)dr["UserGroupID"], (string)dr["UserGroupName"], (int)dr["ParentGroupID"]);
                listDevHash.put(dev._NetAddress, dev);
                dev.isOline = false;
                dev.OnlineCount = 0;

            }
        }

    }
		
	
    public devGroup handleInsertGrp(Object message)
    {
    	 
    	 Object msgObject=message;         

    	 return new devGroup(1, "", -1);
    }


   
	
}
