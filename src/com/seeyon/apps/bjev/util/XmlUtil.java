package com.seeyon.apps.bjev.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;

public class XmlUtil {

	public static <T> T getBean(String xml,Class<T> c) {
		XStream xstream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
		xstream.processAnnotations(c);
		T obj=c.cast(xstream.fromXML(xml));
		return obj;
	}
	

	
	public static String getXml(Object obj) {
		XStream xstream = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
		xstream.processAnnotations(obj.getClass());
		String xml = xstream.toXML(obj);
		return xml;
	}
	
	
	public static String turnMeanings(String s) {
		String rst=s;
		if(s!=null) {
			rst=rst.replace("&", "&amp;");
			rst=rst.replace("<", "&lt;");
			rst=rst.replace(">", "&gt;");
			rst=rst.replace("'", "&apos;");
			rst=rst.replace("\"", "&quot;");
		}
		return rst;
	}
	
	public static String subStringBetween(String str, String strStart, String strEnd) {         
		/* 找出指定的2个字符在 该字符串里面的 位置 */        
		int strStartIndex = str.indexOf(strStart);        
		int strEndIndex = str.indexOf(strEnd);        
		/* index 为负数 即表示该字符串中 没有该字符 */        
		if (strStartIndex < 0) {            
			return "字符串 :---->" + str + "<---- 中不存在 " + strStart + ", 无法截取目标字符串";        
		}        
		if (strEndIndex < 0) {            
			return "字符串 :---->" + str + "<---- 中不存在 " + strEnd + ", 无法截取目标字符串";        
		}        
		/* 开始截取 */        
		String result = str.substring(strStartIndex, strEndIndex).substring(strStart.length());        
		return result;    
	}
	
	public static List<String> match(String s) {
        List<String> results = new ArrayList<String>();
        Pattern p = Pattern.compile("<item>(.*?)</item>");
        Matcher m = p.matcher(s);
        while (m.find()) {
            results.add("<item>"+m.group(1)+"</item>");
        }
        return results;
    }
	
	/**
	 * 获取字符串的字节长度,中文按2字节算
	 * @param chstring
	 * @return
	 */
	public static int length(String chstring) {
	    int length = 0;
	    if(StringUtils.isBlank(chstring))
	        return 0;
	    for(int i = 0; i < chstring.length(); i++) {
	        char c = chstring.charAt(i);
	        if(isChinese(c))
	            length += 2;
	        else
	            length += 1;
	    }
	    return length;
	}
	
	/**
	 * 判断字符是不是中文字符
	 * @param c
	 * @return
	 */
	public static boolean isChinese(char c) {
	    int ascii = (int)c;
	    if(ascii >= 0 && ascii <= 255)
	        return false;
	    return true;
	}
	
	/**
	 * 字符串截取，支持中文
	 * @param chstring
	 * @param offset
	 * @param length
	 * @return
	 */
	public static String subChString(String chstring, int offset, int length) {
	    if(StringUtils.isBlank(chstring))
	        return chstring;
	    int num = 0;
	    int index = -1;
	    StringBuffer sb = new StringBuffer();
	    for(int i = 0; i < chstring.length(); i++) {
	        char c = chstring.charAt(i);
	        int move = 0;
	        if(isChinese(c))
	            move = 2;
	        else
	            move = 1;
	        index += move;
	        if(index >= offset) {
	            sb.append(c);
	            num += move;
	        }
	        if(num >= length)
	            break;
	    }
	    return sb.toString();
	}
	
    public static String readFile(String path){
    	String str = "";
    	 FileInputStream fis = null;
    	 FileChannel fc = null;
    	try {
    		File file = new File(path);
 	        fis = new FileInputStream(file);
	        fc = fis.getChannel();
	        ByteBuffer bb = ByteBuffer.allocate(new Long(file.length()).intValue());
	        //fc向buffer中读入数据
	        fc.read(bb);
	        bb.flip();
	        str = new String(bb.array(),"UTF-8");
    	}catch(Exception ex) {
    		ex.printStackTrace();
    	}finally {
    		if(fc != null) {
    			try {
					fc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    		if(fis != null) {
    			try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
        return str;

    }
}
