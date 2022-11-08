package com.seeyon.apps.bjev.services.pojo;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class DataItem {
	
	//标识:C 基金中心,W wbs,V 供应商
	@XStreamAlias("C_FLAG")
	private String cFlag;
	
	//公司代码
	@XStreamAlias("BUKRS")
	private String bukrs;
	
	//主数据代码：主数据唯一标识
	@XStreamAlias("MDATA")
	private String mdata;
	
	//主数据描述1
	@XStreamAlias("MTEXT0")
	private String mtext0;
	
	//主数据描述2
	@XStreamAlias("MTEXT1")
	private String mtext1;
	
	//主数据描述3
	@XStreamAlias("MTEXT2")
	private String mtext2;
	
	//主数据描述4
	@XStreamAlias("MTEXT3")
	private String mtext3;
	
	//主数据描述5
	@XStreamAlias("MTEXT4")
	private String mtext4;

	public String getcFlag() {
		return cFlag;
	}

	public void setcFlag(String cFlag) {
		this.cFlag = cFlag;
	}

	public String getBukrs() {
		return bukrs;
	}

	public void setBukrs(String bukrs) {
		this.bukrs = bukrs;
	}

	public String getMdata() {
		return mdata;
	}

	public void setMdata(String mdata) {
		this.mdata = mdata;
	}

	public String getMtext0() {
		return mtext0;
	}

	public void setMtext0(String mtext0) {
		this.mtext0 = mtext0;
	}

	public String getMtext1() {
		return mtext1;
	}

	public void setMtext1(String mtext1) {
		this.mtext1 = mtext1;
	}

	public String getMtext2() {
		return mtext2;
	}

	public void setMtext2(String mtext2) {
		this.mtext2 = mtext2;
	}

	public String getMtext3() {
		return mtext3;
	}

	public void setMtext3(String mtext3) {
		this.mtext3 = mtext3;
	}

	public String getMtext4() {
		return mtext4;
	}

	public void setMtext4(String mtext4) {
		this.mtext4 = mtext4;
	}

}
