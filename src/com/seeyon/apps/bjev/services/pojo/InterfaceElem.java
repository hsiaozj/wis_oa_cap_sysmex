package com.seeyon.apps.bjev.services.pojo;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("Interface")
public class InterfaceElem {
	
	@XStreamImplicit(itemFieldName = "Entry")
	private DataItem [] items;

	public DataItem[] getItems() {
		return items;
	}

	public void setItems(DataItem[] items) {
		this.items = items;
	}
	
}
