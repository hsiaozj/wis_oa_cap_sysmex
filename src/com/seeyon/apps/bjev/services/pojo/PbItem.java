package com.seeyon.apps.bjev.services.pojo;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class PbItem {

	@XStreamAlias("SENDER")
	private String sender;

	@XStreamAlias("RECEIVER")
	private String receiver;

	@XStreamAlias("DTSEND")
	private String dtsend;

	@XStreamAlias("INTFID")
	private String intfid;

	@XStreamAlias("MSGID")
	private String msgid;

	@XStreamAlias("ZOBLNR")
	private String zoblnr;

	@XStreamAlias("BUKRS")
	private String bukrs;

	@XStreamAlias("ZSTATUS")
	private String zstatus;

	@XStreamAlias("ZMESSAGE")
	private String zmessage;

	@XStreamAlias("ZTIME")
	private String ztime;

	@XStreamAlias("ZRSV01")
	private String zrsv01;

	@XStreamAlias("ZRSV02")
	private String zrsv02;

	@XStreamAlias("ZRSV03")
	private String zrsv03;

	@XStreamAlias("ZRSV04")
	private String zrsv04;

	@XStreamAlias("ZRSV05")
	private String zrsv05;

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

	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getZoblnr() {
		return zoblnr;
	}

	public void setZoblnr(String zoblnr) {
		this.zoblnr = zoblnr;
	}

	public String getBukrs() {
		return bukrs;
	}

	public void setBukrs(String bukrs) {
		this.bukrs = bukrs;
	}

	public String getZstatus() {
		return zstatus;
	}

	public void setZstatus(String zstatus) {
		this.zstatus = zstatus;
	}

	public String getZmessage() {
		return zmessage;
	}

	public void setZmessage(String zmessage) {
		this.zmessage = zmessage;
	}

	public String getZtime() {
		return ztime;
	}

	public void setZtime(String ztime) {
		this.ztime = ztime;
	}

	public String getZrsv01() {
		return zrsv01;
	}

	public void setZrsv01(String zrsv01) {
		this.zrsv01 = zrsv01;
	}

	public String getZrsv02() {
		return zrsv02;
	}

	public void setZrsv02(String zrsv02) {
		this.zrsv02 = zrsv02;
	}

	public String getZrsv03() {
		return zrsv03;
	}

	public void setZrsv03(String zrsv03) {
		this.zrsv03 = zrsv03;
	}

	public String getZrsv04() {
		return zrsv04;
	}

	public void setZrsv04(String zrsv04) {
		this.zrsv04 = zrsv04;
	}

	public String getZrsv05() {
		return zrsv05;
	}

	public void setZrsv05(String zrsv05) {
		this.zrsv05 = zrsv05;
	}
}
