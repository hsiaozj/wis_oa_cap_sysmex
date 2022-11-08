package com.seeyon.apps.bjev.common;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Properties;
import com.seeyon.client.CTPRestClient;
import com.seeyon.client.CTPServiceClientManager;

public class ClientResource {

	private static ClientResource clientresource = null;
	
	private ClientResource() {}
	
	private String userName;
	/** REST账号 **/
	private String password;
	/** REST密码 **/
	private String restUrl;
	/** REST接口访问地址 **/
	private String binduser;

	/** binding OA user **/
	
	static {
		clientresource = new ClientResource();
		clientresource.getProperties();
	}

	public synchronized static ClientResource getInstance() {
		return clientresource;
	}

	public Properties getProperties() {
		Properties prop = new Properties();
		// InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("");
		try {
			InputStream ins=Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("com/seeyon/apps/bjev/common/seeyon.properties");
			InputStreamReader in = new InputStreamReader(ins, "UTF-8");
			prop.load(in);
			userName = prop.getProperty("restname").trim();/** REST账号 **/
			password = prop.getProperty("restpwd").trim();/** REST密码 **/
			restUrl = prop.getProperty("ip").trim();/** REST接口访问地址 **/
			binduser = prop.getProperty("restbinduser").trim();/** binding OA user **/
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}
	public static void testExportBusinessFormData() {
		   CTPRestClient client = ClientResource.getInstance().resouresClent();
		   String result = client.get("form/export/Z_RFC_WIS_GET_MATERIAL_LIST_lixi?beginDateTime=2019-05-15 00:00:00&endDateTime=2019-05-15 23:59:59", String.class);
		   writeText(result);
 		   System.out.println(result);
		}
	
	public static void writeText(String content) {
		try {
			//读取文件(字符流)
//			content.getBytes("UTF-8")
//			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(""),"UTF-8"));
			//BufferedReader in = new BufferedReader(new FileReader("d:\\1.txt")));
			//写入相应的文件
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("d:\\test.txt"),"UTF-8"));
			out.write(content);
			
			//BufferedWriter out = new BufferedWriter(new FileWriter("d:\\2.txt"))；
			//读取数据
			//循环取出数据
//			String str = null;
//			while ((str = in.readLine()) != null) {
//			    //写入相关文件
//			    out.write(str);
//			    out.newLine();
//			}
//			
			//清楚缓存
			out.flush();
			//关闭流
//			in.close();
			out.close();
		}catch(Exception ex) {
			ex.printStackTrace();
			
		}
	}
	
	public static void main(String[] args) {
//		String test = "[{\"matnr\":\"880-KKK738-05-A\",\"maktx\":\"SPRING,GROUNDING,UAT,LEFT,SPKR,N84\",\"werks\":\"2042\",\"meins\":\"PC\"},{\"matnr\":\"880-KKK738-05-A\",\"maktx\":\"SPRING,GROUNDING,UAT,LEFT,SPKR,N84\",\"werks\":\"2049\",\"meins\":\"PC\"}]";
//		List<Map<String,Object>> list = JSONUtils.parseJsonToListMap(test);
//		System.out.println(list);
//		Map<String,Object> map = JSONUtils.parseJSON(test);
//		System.out.println(map);
//		Object obj = JSONUtil.parseJSONString(test);
//		System.out.println(obj);
		//testTemplates1();
//		ClientResource.testExportBusinessFormData();
		
//		JSONObject jsonobj = new JSONObject();
//		jsonobj.put("aa", "1");
//		jsonobj.put("bb", "2");
//		System.out.println(jsonobj);
//		
//		String test = "{IT_INPUT=[{SD_DOC=100001, AUDIT_STAT=Z050}], ET_OUTPUT=[{MESSAGE=销售订单号 100001 不存在, SUCCESS=S, SD_DOC=100001}]}";
//		JSONObject jsobj = JSON.parseObject(test.replace("=", ":"));
		//JSONArray jsArr = JSON.parseArray(test);
//		System.out.println(jsobj);
//		testExportBusinessFormData();
	}
	
	public static void testTemplates1() {
		   CTPRestClient client = ClientResource.getInstance().resouresClent();
		   String result = client.get("flow/state/-3435173941326820001", String.class);
		   System.out.println(result);
		   
		  // Long summaryId=5550290916014188958l;
		 //  Long affairId=0l;
		  //String result = client.get("coll/attachments/"+summaryId+"/"+affairId+"/0,2", String.class);
		   
		   //String result = client.get("coll/attachments/"+8589280252534698877L+"/"+0L+"/0", String.class);
		   
		   
		   //String result = client.get("flow/data/-8952602876235791868", String.class);
		   
		   
//		  String templateCode="XM0001";
//		  Map map=new HashMap();
//		  String flowId=client.post("flow/"+templateCode, map,String.class);
		  
		  
		 /* Map res = new HashMap();
		    res.put("affairId", 5654788863194158388l);
		    res.put("memberid", "4415883629670319599");//当前处理人ID
		    res.put("comment", "可以我看到了！");//处理意见
		    res.put("attitude", "0");//态度：1同意，2不同意，0已阅
*/
		   //String result=client.post("affair/finishaffair", res, String.class);
		  
		   
		}
	


	public CTPRestClient resouresClent() {
		CTPServiceClientManager clientManager = CTPServiceClientManager.getInstance(restUrl);
		CTPRestClient client = clientManager.getRestClient();
		boolean flag=client.authenticate(userName, password);
//		if (!binduser.equals("{loginName}")) {
//			client.bindUser(binduser);
//		}
		if(!flag) return null;
		return client;
	}

	public String getUserName() {
		return userName;
	}

	public String getBinduser() {
		return binduser;
	}

	public String getRestUrl() {
		return restUrl;
	}

	public String getPassword() {
		return password;
	}
	
}