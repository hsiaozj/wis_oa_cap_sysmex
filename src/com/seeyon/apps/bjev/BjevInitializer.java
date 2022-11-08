package com.seeyon.apps.bjev;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.AbstractSystemInitializer;
import com.seeyon.ctp.common.log.CtpLogFactory;

public class BjevInitializer extends AbstractSystemInitializer {
	private static final Log log = CtpLogFactory.getLog(BjevInitializer.class);

	public void destroy() {
		log.info("北汽新能源集成");
	}

	public void initialize() {

	}
}
