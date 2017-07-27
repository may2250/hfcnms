package wl.hfc.common;


public class nojuUserAuthorizeTableRow 
{

    //public int level{ get; set; }

    private int _UserID;
    /// <summary>
    /// 获取用户名
    /// </summary>
    public int UserID;


    public String UserName;




    public boolean Encrypted;


    private byte[] _PassWord;


    public String PassWord;




    public String PhoneNmbr="";
  



    public String smtpAddress="";


    public byte AuthTotal=3;//1:super admin;2:admin;3:guest

    //public List<int> GRPIDList = new List<int>();

    public nojuUserAuthorizeTableRow()
    {
        this.AuthTotal = 2;//默认普通观察者

    }

    //public UserAuthorizeTableRow(int userid)
    //{
    //    this._UserID = userid;
    //}

    public nojuUserAuthorizeTableRow(int userid, String username, byte authtotal,String password)
    {
    	
        this._UserID = userid;
        this.UserName = username;
        this.PassWord = password;
        this.AuthTotal = authtotal;


    }




}
