package com.seeyon.apps.bjev.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import com.seeyon.cap4.form.bean.FormBean;
import com.seeyon.cap4.form.bean.FormDataBean;
import com.seeyon.cap4.form.bean.FormDataMasterBean;
import com.seeyon.cap4.form.bean.FormDataSubBean;
import com.seeyon.cap4.form.bean.FormFieldBean;
import com.seeyon.cap4.form.bean.FormTableBean;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;

public class CAP4FormKit {

	private static final Log LOGGER = CtpLogFactory.getLog(CAP4FormKit.class);
	private static String pattern = "[0-9]+";

	public static FormBean getFormBean(CAP4FormManager cap4FormManager, String code) {
		FormBean formBean = null;
		try {
			formBean = cap4FormManager.getFormByFormCode(code);
		} catch (BusinessException e) {
			LOGGER.error("获取表单发生异常,编号：" + code, e);
		}
		return formBean;
	}

	/**
	 * 根据表单的显示名称获取字段的值
	 * 
	 * @param bean
	 * @param disPlay
	 * @return
	 */
	public static Object getFieldValue(FormDataBean bean, String disPlay) {
		if (bean == null) {
			return null;
		}
		FormTableBean table = bean.getFormTable();
		if (table == null) {
			return null;
		}
		FormFieldBean field = table.getFieldBeanByDisplay(disPlay);
		if (field == null) {
			return null;
		}
		return bean.getFieldValue(field.getName());
	}

	/**
	 * 获取表单字段的显示值
	 * 
	 * @param bean
	 * @param disPlay
	 * @return
	 */
	public static String getFieldDisplayValue(FormDataBean bean, String disPlay) {
		if (bean == null) {
			return null;
		}
		FormTableBean table = bean.getFormTable();
		if (table == null) {
			return null;
		}
		FormFieldBean field = table.getFieldBeanByDisplay(disPlay);
		if (field == null) {
			return null;
		}
		return getFieldDisplayValue(bean, field);
	}

	public static String getFieldDisplayValue(FormDataBean bean, FormFieldBean field) {
		String strVal = "";
		if (field == null) {
			return null;
		}
		try {
			Object[] o = field.getDisplayValue(bean.getFieldValue(field.getName()));
			strVal = (o[1] == null ? "" : String.valueOf(o[1]));
			return strVal;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Object obj = bean.getFieldValue(field.getName());
		strVal = (obj == null ? "" : String.valueOf(obj));
		return strVal;
	}

	public static int getIntValue(FormDataBean bean, String disPlay) {
		Object value = getFieldValue(bean, disPlay);
		int val = (value == null ? 0 : Integer.parseInt(value + ""));
		return val;
	}

	public static String getFieldStrValue(FormDataBean bean, String disPlay) {
		Object value = getFieldValue(bean, disPlay);
		String strVal = (value == null ? "" : String.valueOf(value));
		return strVal;
	}

	public static Object getFieldValueByName(FormDataBean bean, String fieldName) {
		return bean.getFieldValue(fieldName);
	}

	/**
	 * 根据表单的显示名称获取字段是 field000？
	 * 
	 * @param bean
	 * @param disPlay
	 * @return
	 */
	public static String getFieldTaleId(FormBean bean, String disPlay) {
		if (bean == null) {
			return null;
		}
		FormTableBean table = bean.getMasterTableBean();
		return getFieldTaleId(table, disPlay);
	}

	/**
	 * 直接根据table来获取
	 * 
	 * @param table
	 * @param disPlay
	 * @return
	 */
	public static String getFieldTaleId(FormTableBean table, String disPlay) {
		if (table == null) {
			return null;
		}
		FormFieldBean field = table.getFieldBeanByDisplay(disPlay);
		if (field == null) {
			return null;
		}
		return field.getName();
	}

	public static String getFieldTaleId(FormDataBean bean, String disPlay) {
		if (bean == null) {
			return null;
		}
		FormTableBean table = bean.getFormTable();
		if (table == null) {
			return null;
		}
		FormFieldBean field = table.getFieldBeanByDisplay(disPlay);
		if (field == null) {
			return null;
		}
		return field.getName();
	}

	public static FormFieldBean getFieldBean(FormDataBean bean, String disPlay) {
		if (bean == null) {
			return null;
		}
		FormTableBean table = bean.getFormTable();
		if (table == null) {
			return null;
		}
		return table.getFieldBeanByDisplay(disPlay);
	}

	public static FormFieldBean getFieldBean(FormTableBean bean, String disPlay) {
		if (bean == null) {
			return null;
		}
		return bean.getFieldBeanByDisplay(disPlay);
	}

	public static void setCellValueByFieldName(FormDataBean bean, String FieldName, Object value) {
		bean.addFieldValue(FieldName, value);
	}

	/**
	 * Description:
	 * 
	 * <pre></pre>
	 * 
	 * @param bean
	 *            这里的bean必须是getMasterBean() 方法获取到的bean
	 * @param disPlay
	 * @param value
	 */
	public static void setCellValue(FormDataBean bean, String disPlay, Object value) {
		FormFieldBean cell = CAP4FormKit.getFieldBean(bean, disPlay);
		if (cell != null) {
			bean.addFieldValue(cell.getName(), value);
		}
	}

	public static void setCellValue(FormDataSubBean bean, String disPlay, Object value) {
		FormFieldBean cell = CAP4FormKit.getFieldBean(bean, disPlay);
		if (cell != null) {
			bean.addFieldValue(cell.getName(), value);
		}
	}

	/**
	 * Description:
	 * 
	 * <pre>
	 * 只适用于只有一个子表的表单
	 * </pre>
	 * 
	 * @param colManager
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static List<FormDataSubBean> getSubBeans(FormDataMasterBean master) throws Exception {
		Map<String, List<FormDataSubBean>> subs = master.getSubTables();
		if (null != subs && subs.size() > 0) {
			for (String key : subs.keySet()) {
				return subs.get(key);
			}
		}
		return null;
	}

	/**
	 * Description:
	 * 
	 * <pre>
	 * 获取从表字段
	 * </pre>
	 * 
	 * @param sub
	 * @param disPlay
	 * @return
	 */
	public static Object getSubFieldValue(FormDataSubBean sub, String disPlay) {
		return getFieldValue(sub, disPlay);
	}

	/**
	 * <pre>
	 * 获取从表数据,key:表名,value:从表数据记录Map(key:字段名，value:值)
	 * </pre>
	 * 
	 * @param subBeansMap
	 * @return
	 * @throws Exception
	 */
	public static Map<String, List<Map>> getSubTableData(Map<String, List<FormDataSubBean>> subBeansMap, String pattern)
			throws Exception {
		Map<String, List<Map>> resultData = new HashMap();
		if (null != subBeansMap && subBeansMap.size() > 0) {
			Iterator<Entry<String, List<FormDataSubBean>>> iters = subBeansMap.entrySet().iterator();
			while (iters.hasNext()) {
				Entry<String, List<FormDataSubBean>> entry = iters.next();
				String subTableName = entry.getKey();
				List<FormDataSubBean> subBeanList = entry.getValue();
				resultData.put(subTableName, getSubBeansData(subBeanList, pattern));
			}
		}
		return resultData;
	}

	/**
	 * <pre>
	 * 根据从表名获取从表bean
	 * </pre>
	 * 
	 * @param master
	 * @param subTableName
	 * @return
	 */
	public static List<FormDataSubBean> getSubBeansBySubTable(FormDataMasterBean master, String subTableName) {
		return master.getSubData(subTableName);
	}

	/**
	 * <pre>
	 * 获取从表单数据
	 * </pre>
	 * 
	 * @param subBeanList
	 * @return
	 */
	public static List<Map> getSubBeansData(List<FormDataSubBean> subBeanList, String pattern) {
		List<Map> resultList = new ArrayList();
		if (subBeanList != null && subBeanList.size() > 0) {
			for (FormDataSubBean formDataSubBean : subBeanList) {
				Map<String, Object> subFormData = formDataSubBean.getRowData();
				if (subFormData != null && subFormData.size() > 0) {
					Iterator<Entry<String, Object>> iters = subFormData.entrySet().iterator();
					while (iters.hasNext()) {
						Entry<String, Object> entry = iters.next();
						String key = entry.getKey();
						Object val = entry.getValue();
						if (val != null && (val instanceof Date)) {
							Date d = (Date) val;
							if(StringUtils.isBlank(pattern)) {
								pattern = "yyyyMMdd";
							}
							subFormData.put(key, DateFormatUtils.format(d, pattern));
						} else if (val != null && (val instanceof BigDecimal)) {
							BigDecimal bd = (BigDecimal) val;
							subFormData.put(key, bd.toString());
						} else if (val != null && !(val instanceof java.lang.String)) {
							subFormData.put(key, val + "");
						}
					}
					resultList.add(subFormData);
				}
			}
		}
		return resultList;
	}

	/**
	 * <pre>
	 * 获取主表单数据
	 * </pre>
	 * 
	 * @param formDataMasterBean
	 * @return
	 */
	public static List<Map> getFormDataMasterBeanData(FormDataMasterBean formDataMasterBean, String pattern) {
		List<Map> resultList = new ArrayList();
		Map<String, Object> formData = formDataMasterBean.getRowData();
		if (formData != null && formData.size() > 0) {
			Iterator<Entry<String, Object>> iters = formData.entrySet().iterator();
			while (iters.hasNext()) {
				Entry<String, Object> entry = iters.next();
				String key = entry.getKey();
				Object val = entry.getValue();
				if (val != null && (val instanceof Date)) {
					Date d = (Date) val;
					if(StringUtils.isBlank(pattern)) {
						pattern = "yyyyMMdd";
					}
					formData.put(key, DateFormatUtils.format(d, pattern));
				} else if (val != null && (val instanceof BigDecimal)) {
					BigDecimal bd = (BigDecimal) val;
					formData.put(key, bd.toString());
				} else if (val != null && !(val instanceof java.lang.String)) {
					formData.put(key, val + "");
				}
			}
			resultList.add(formData);
		}
		return resultList;
	}

	public static void procMainDataZeros(Map<String, Object> mainData, List<String[]> colParamList, int length)
			throws Exception {
		if (mainData != null && mainData.size() > 0) {
			Iterator<Entry<String, Object>> iterator = mainData.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Object> entry = iterator.next();
				String key = entry.getKey();
				for (String[] valueArr : colParamList) {
					String colName = valueArr[0]; // key
					String mark = valueArr[1]; // value
					if (key.equals(colName)) {
						// 取出对应值
						String val = (String) entry.getValue();
						if (mark.equals("1")) {
							val = val.replaceAll("\\s*", "");
							if (Pattern.matches(pattern, val)) {
								int strLen = val.length();
								while (strLen < length) {
									val = "0" + val;
									strLen = val.length();
								}
							}
						} else if (mark.equals("2")) {
							val = val.replaceAll("\\s*", "");
							val = Integer.valueOf(val) * 10 + "";
							int strLen = val.length();
							while (strLen < length) {
								val = "0" + val;
								strLen = val.length();
							}
						} else {
							val = val.replaceAll("\\s*", "");
							int strLen = val.length();
							while (strLen < length) {
								val = "0" + val;
								strLen = val.length();
							}
						}
						mainData.put(colName, val);
					}
				}
			}

		}

	}

	public static void procPreZeros(Map<String, List<Map>> subBeansDataMap, String subTable,
			List<String[]> colParamList, int length) throws Exception {
		if (null != subBeansDataMap && subBeansDataMap.size() > 0) {
			Iterator<Entry<String, List<Map>>> iters = subBeansDataMap.entrySet().iterator();
			while (iters.hasNext()) {
				Entry<String, List<Map>> entry = iters.next();
				String subTableName = entry.getKey();
				List<Map> subDataList = entry.getValue();
				if (subTableName.equals(subTable)) {
					if (subDataList != null) {
						for (String[] param : colParamList) {
							String colName = param[0];
							String mark = param[1];
							for (Map map : subDataList) {
								String val = (String) map.get(colName);
								if (StringUtils.isNotBlank(val)) {
									if (mark.equals("1")) {
										val = val.replaceAll("\\s*", "");
										if (Pattern.matches(pattern, val)) {
											int strLen = val.length();
											while (strLen < length) {
												val = "0" + val;
												strLen = val.length();
											}
										}
									} else if (mark.equals("2")) {
										val = val.replaceAll("\\s*", "");
										val = Integer.valueOf(val) * 10 + "";
										int strLen = val.length();
										while (strLen < length) {
											val = "0" + val;
											strLen = val.length();
										}
									} else {
										val = val.replaceAll("\\s*", "");
										int strLen = val.length();
										while (strLen < length) {
											val = "0" + val;
											strLen = val.length();
										}
									}
									map.put(colName, val);
								}
							}
						}
						subBeansDataMap.put(subTable, subDataList);
					}
				}
			}
		}
	}

	public static String getTableNameByDisPlayName(FormBean formBean, String disPlayName, boolean isMainTable) {
		String tableName = "";
		if (isMainTable) {
			FormTableBean formTableBean = formBean.getMasterTableBean();
			if (formTableBean != null) {
				tableName = formTableBean.getTableName();
			}
		} else {
			List<FormTableBean> subTableList = formBean.getSubTableBean();
			if (subTableList != null) {
				for (FormTableBean formTableBean : subTableList) {
					FormFieldBean fieldBean = formTableBean.getFieldBeanByDisplay(disPlayName);
					if (fieldBean != null) {
						tableName = formTableBean.getTableName();
						break;
					}
				}
			}
		}
		return tableName;
	}

	/**
	 * 根据一组中文显示名称获取字段显示名称
	 */
	public static Map getMasterTableColumnsByFormCodeAndName(String[] returnFields, FormBean cap4formBean)
			throws Exception {
		Map<String, String> paramMap = new HashMap<String, String>();

		if (cap4formBean != null) {
			FormTableBean formTableBean = cap4formBean.getMasterTableBean();
			if (formTableBean != null) {
				for (String fieldName : returnFields) {
					Map<String, com.seeyon.cap4.form.bean.FormFieldBean> formTableBeanMap = formTableBean
							.getFieldMap4Display();
					if (formTableBeanMap != null) {
						FormFieldBean fieldBean = formTableBeanMap.get(fieldName);
						if (fieldBean != null) {
							paramMap.put(fieldName, fieldBean.getName());
						}
					}
				}
			}
		}
		return paramMap;
	}
}
