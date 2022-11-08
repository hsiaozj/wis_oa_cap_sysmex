package com.seeyon.apps.bjev.services.pojo;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("Interface")
public class PbInterface {
	
	@XStreamImplicit(itemFieldName = "Entry")
	private PbItem [] items;

	public PbItem[] getItems() {
		return items;
	}

	public void setItems(PbItem[] items) {
		this.items = items;
	}
}
