package com.seeyon.apps.bjev.util;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {
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
}
