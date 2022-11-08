package com.seeyon.apps.bjev.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormFieldComEnum;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.util.Enums;
import com.seeyon.cap4.form.util.FormUtil;
import com.seeyon.ctp.util.Strings;


public class A8xFormDataWrapper {
	// 表单定义
		private FormBean formBean;

		// 表单数据
		private FormDataMasterBean masterBean;

		private A8xDataRow masterDataRow;

		private Map<Integer, List<A8xDataRow>> childDataRowMap;

		private long formRecordId;

		public A8xFormDataWrapper(FormBean formBean, FormDataMasterBean masterBean) {
			this.formBean = formBean;
			this.masterBean = masterBean;
			this.formRecordId = masterBean.getId();
		}

		public FormBean getFormBean() {
			return formBean;
		}

		public FormDataMasterBean getFormDataMasterBean() {
			return masterBean;
		}

		public long getFormRecordId() {
			return formRecordId;
		}

		/**
		 * 获取主表-名称
		 */
		public String getMasterTableName() {
			String temp = null;
			if (formBean != null) {
				temp = formBean.getMasterTableBean().getTableName();
			}
			return temp;
		}

		/**
		 * 控件名-字段名对照
		 */
		public Map<String, String> getFieldDisplayMap() {
			Map<String, String> tmp = null;
			if (formBean != null) {
				tmp = formBean.getAllFieldDisplayMap();
			} else {
				tmp = new HashMap<String, String>(1);
			}
			return tmp;
		}

		/**
		 * 字段名-控件名-对照
		 */
		public Map<String, String> getFieldNameMap() {
			Map<String, String> tmp = null;
			if (formBean != null) {
				tmp = formBean.getAllFieldNameMap();
			} else {
				tmp = new HashMap<String, String>(1);
			}
			return tmp;
		}

		/**
		 * 数据库字段名与值对照Map
		 */
		public Map<String, Object> getMasterDataMap() {
			Map<String, Object> tmp = null;
			if (masterBean != null) {
				tmp = masterBean.getRowData();
			}
			return tmp;
		}

		public A8xDataRow getChildDataRow(String display, String subRecordId) {
			List<A8xDataRow> tmp = getChildDataRowList(display);
			if (tmp == null) {
				return null;
			}
			for (A8xDataRow tmpRow : tmp) {
				if (tmpRow.getStringValue("id").equals(subRecordId)) {
					return tmpRow;
				}
			}
			return null;
		}

		public List<String> getChildDisplayNames() {
			List<FormTableBean> tmpFormTableBeanList = formBean.getSubTableBean();
			int size = tmpFormTableBeanList == null ? -1 : tmpFormTableBeanList.size();
			if (size <= -1) {
				return null;
			}
			List<String> list = new ArrayList<String>(size);
			for (int index = 0; index < size; index++) {
				list.add(tmpFormTableBeanList.get(index).getDisplay());

			}
			return list;
		}

		public List<A8xDataRow> getChildDataRowList(String display) {
			List<FormTableBean> tmpFormTableBeanList = formBean.getSubTableBean();
			int size = tmpFormTableBeanList == null ? -1 : tmpFormTableBeanList.size();
			if (size <= -1) {
				return null;
			}
			int curIndex = -1;
			for (int index = 0; index < size; index++) {
				if (display.equals(tmpFormTableBeanList.get(index).getDisplay())) {
					curIndex = index;
					break;
				}
			}
			if (curIndex == -1) {
				return null;
			}

			List<FormFieldBean> childFormFieldBeans = tmpFormTableBeanList.get(curIndex).getFields();
			if (childDataRowMap == null) {
				childDataRowMap = new HashMap<Integer, List<A8xDataRow>>(8);
			} else {
				List<A8xDataRow> temp = childDataRowMap.get(curIndex);
				if (temp != null) {
					return temp;
				}
			}

			String childTablename = tmpFormTableBeanList.get(curIndex).getTableName();
			List<A8xDataRow> tmpList = null;
			List<FormDataSubBean> subList = masterBean.getSubData(childTablename);
			if (subList != null) {
				size = subList.size();
				tmpList = new ArrayList<A8xDataRow>(size);
				FormDataSubBean tmpFormDataSubBean = null;
				for (int i = 0; i < size; i++) {
					tmpFormDataSubBean = subList.get(i);
					tmpList.add(convertRowData2Map(tmpFormDataSubBean.getRowData(), childFormFieldBeans));
				}
			}
			childDataRowMap.put(curIndex, tmpList);
			return tmpList;
		}

		/**
		 * 获取重复表数据List 数据库字段名与值对照Map
		 */
		public List<Map<String, Object>> getChildDataList(int index) {
			List<FormTableBean> tmpFormTableBeanList = formBean.getSubTableBean();
			int size = tmpFormTableBeanList == null ? -1 : tmpFormTableBeanList.size();
			if (index >= size) {
				return null;
			}
			String childTablename = tmpFormTableBeanList.get(index).getTableName();
			if (childTablename == null) {
				return null;
			}
			List<Map<String, Object>> tmpList = null;
			List<FormDataSubBean> subList = masterBean.getSubData(childTablename);
			if (subList != null) {
				size = subList.size();
				tmpList = new ArrayList<Map<String, Object>>(size);
				FormDataSubBean tmpFormDataSubBean = null;
				for (int i = 0; i < size; i++) {
					tmpFormDataSubBean = subList.get(i);
					tmpList.add(tmpFormDataSubBean.getRowData());
				}
			}
			return tmpList;
		}

		/**
		 * 中文控件名与值对照Map
		 */
		public A8xDataRow getMasterDataRow() {
			if (masterDataRow == null) {
				masterDataRow = convertRowData2Map(getMasterDataMap(), formBean.getMasterTableBean().getFields());
			}
			return masterDataRow;
		}

		/**
		 * 获取重复表数据List 中文控件名与值对照Map
		 * 
		 * @param index 子表index
		 * @return
		 */
		public List<A8xDataRow> getChildDataRowList(int index) {
			if (childDataRowMap == null) {
				childDataRowMap = new HashMap<Integer, List<A8xDataRow>>(8);
			} else {
				List<A8xDataRow> temp = childDataRowMap.get(index);
				if (temp != null) {
					return temp;
				}
			}

			List<FormTableBean> tmpFormTableBeanList = formBean.getSubTableBean();
			int size = tmpFormTableBeanList == null ? -1 : tmpFormTableBeanList.size();
			if (index >= size) {
				return null;
			}

			List<FormFieldBean> childFormFieldBeans = formBean.getSubTableBean().get(index).getFields();

			String childTablename = tmpFormTableBeanList.get(index).getTableName();
			List<A8xDataRow> tmpList = null;
			List<FormDataSubBean> subList = masterBean.getSubData(childTablename);
			if (subList != null) {
				size = subList.size();
				tmpList = new ArrayList<A8xDataRow>(size);
				FormDataSubBean tmpFormDataSubBean = null;
				for (int i = 0; i < size; i++) {
					tmpFormDataSubBean = subList.get(i);
					tmpList.add(convertRowData2Map(tmpFormDataSubBean.getRowData(), childFormFieldBeans));
				}
			}
			childDataRowMap.put(index, tmpList);
			return tmpList;
		}

		private A8xDataRow convertRowData2Map(Map<String, Object> theRowData, List<FormFieldBean> formFieldBeanList) {
			if (theRowData == null) {
				return null;
			}
			A8xDataRow tmp = new A8xDataRow(theRowData.size() + 10);
			Object value = null;
			String displayName = null;
			String dbFieldName = null;
			for (FormFieldBean tmpFormFieldBean : formFieldBeanList) {
				dbFieldName = tmpFormFieldBean.getName();
				displayName = tmpFormFieldBean.getDisplay();
				value = theRowData.get(dbFieldName);
				if (value != null) {
					Map<String, String> tempRes = getDisplayValueMap(value, tmpFormFieldBean);
					tmp.put(displayName, tempRes.get("value"));
					// tmp.put(displayName + "CODE", tempRes.get("showValue2"));
					// tmp.put(displayName + "VALUE", tempRes.get("value"));
				} else {
					tmp.put(displayName, "");
					// tmp.put(displayName + "CODE", "");
					// tmp.put(displayName + "VALUE", "");
				}
			}
			return tmp;
		}

		public void destory() {
			masterBean = null;
			if (masterDataRow != null) {
				masterDataRow.clear();
				masterDataRow = null;
			}
			if (childDataRowMap != null) {
				childDataRowMap.clear();
				childDataRowMap = null;
			}
		}

		public List<FormFieldBean> getChildFormFieldBeans(String display) {
			List<FormTableBean> tmpFormTableBeanList = formBean.getSubTableBean();
			int size = tmpFormTableBeanList == null ? -1 : tmpFormTableBeanList.size();
			if (size <= -1) {
				return null;
			}
			int curIndex = -1;
			for (int index = 0; index < size; index++) {
				if (display.equals(tmpFormTableBeanList.get(index).getDisplay())) {
					curIndex = index;
					break;
				}
			}
			if (curIndex == -1) {
				return null;
			}
			return tmpFormTableBeanList.get(curIndex).getFields();
		}

		public List<FormFieldBean> getChildFormFieldBeans(int index) {
			List<FormTableBean> tmpFormTableBeanList = formBean.getSubTableBean();
			int size = tmpFormTableBeanList == null ? -1 : tmpFormTableBeanList.size();
			if (size <= -1 || index >= size) {
				return null;
			}
			return tmpFormTableBeanList.get(index).getFields();
		}

		public Map<String, String> getDisplayValueMap(Object value, FormFieldBean formFieldBean) {
			Map<String, String> dataInfo = new HashMap<String, String>();
			try {
				Object[] values = formFieldBean.getDisplayValue(value, false);
				if ((values[0] != null) && (FormFieldComEnum.FLOWDEALOPITION.getKey().equalsIgnoreCase(formFieldBean.getInputType())) && (formFieldBean.getFormatType().contains(Enums.FlowDealOptionsType.signet.getKey()))) {
					Map<String, Object> resMap = FormUtil.getFlowDealSignetHtml(String.valueOf(values[0]));
					String dealStr = (String) resMap.get("html");
					values[1] = dealStr;
					values[2] = dealStr;
				}
				if (((FormFieldComEnum.RADIO.getKey().equalsIgnoreCase(formFieldBean.getInputType())) || (FormFieldComEnum.RADIO.getKey().equalsIgnoreCase(formFieldBean.getInputType()))) && ((values[0] == null) || (Strings.isBlank(String.valueOf(values[0])))) && ("0".equals(String.valueOf(values[2])))) {
					values[2] = "";
				}
				dataInfo.put("value", values[0] == null ? "" : String.valueOf(values[0]));
				dataInfo.put("showValue", values[1] == null ? "" : String.valueOf(values[1]));
				dataInfo.put("showValue2", values[2] == null ? "" : String.valueOf(values[2]));
			} catch (Exception e) {
				dataInfo.put("value", value == null ? "" : value.toString());
				dataInfo.put("showValue", value == null ? "" : value.toString());
				dataInfo.put("showValue2", value == null ? "" : value.toString());
			}
			return dataInfo;
		}
}
