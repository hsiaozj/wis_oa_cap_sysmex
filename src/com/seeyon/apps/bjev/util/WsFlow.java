package com.seeyon.apps.bjev.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WsFlow {

	private static Log log = LogFactory.getLog(WsFlow.class);
	private static Properties prop = new Properties();
	
	static {
		try {
			InputStreamReader in = new InputStreamReader(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("com/seeyon/apps/bjev/util/wsFlow.properties"), "UTF-8");
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String tryGet(String str) {
		if (str != null) {
			return str.trim();
		}
		return "";
	}

	public static String getSth(String propName) {
		return tryGet(prop.getProperty(propName));
	}
}
