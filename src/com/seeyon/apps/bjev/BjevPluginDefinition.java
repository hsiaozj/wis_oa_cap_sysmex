package com.seeyon.apps.bjev;

import org.apache.log4j.Logger;

import com.seeyon.ctp.common.plugin.PluginDefinition;
import com.seeyon.ctp.common.plugin.PluginInitializer;

public class BjevPluginDefinition implements PluginInitializer {
	public boolean isAllowStartup(PluginDefinition arg0, Logger arg1) {
		return "1".equals(arg0.getPluginProperty("bjev.enabled"));
	}
}
