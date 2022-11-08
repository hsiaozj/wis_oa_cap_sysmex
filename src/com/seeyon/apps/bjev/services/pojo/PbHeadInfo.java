package com.seeyon.apps.bjev.services.pojo;

public class PbHeadInfo {
	
	private String sender;
	
	private String receiver;
	
	private String dtsend;
	
	private String intfid;

	private String msgid;

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getDtsend() {
		return dtsend;
	}

	public void setDtsend(String dtsend) {
		this.dtsend = dtsend;
	}

	public String getIntfid() {
		return intfid;
	}

	public void setIntfid(String intfid) {
		this.intfid = intfid;
	}
	
}
