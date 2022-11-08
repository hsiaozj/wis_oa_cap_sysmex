package com.seeyon.apps.bjev.common;

import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.util.Enums;

public class CtrlFieldType {
	private String key;
	private String fieldName;
	private Enums.FieldType fieldType;
	private FormFieldComEnum[] supportFieldType;
	
	public CtrlFieldType(String key, String fieldName, Enums.FieldType fieldType, FormFieldComEnum[] supportFieldType) {
		this.key = key;
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.supportFieldType = supportFieldType;
	}

	public String getKey() {
		return this.key;
	}

	public String getText() {
		return this.fieldName;
	}

	public FormFieldComEnum[] getSupportFieldType() {
		return this.supportFieldType;
	}

	public Enums.FieldType getFieldType() {
		return this.fieldType;
	}
}
