package com.seeyon.apps.bjev.common;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class A8xDataRow implements Map<String, Object>, Serializable{
private static final long serialVersionUID = -8187619071948421600L;
	
	private Map<String, Object> dataMap;

	public A8xDataRow(int size) {
		this.dataMap = new HashMap<String, Object>(size);
	}

	public A8xDataRow() {
		this.dataMap = new HashMap<String, Object>(30);
	}

	public Object remove(String key) {
		return this.dataMap.remove(key);
	}

	public void put(String key, int value) {
		putDataItem(key, Integer.valueOf(value));
	}

	public void put(String key, long value) {
		putDataItem(key, Long.valueOf(value));
	}

	public void put(String key, short value) {
		putDataItem(key, Short.valueOf(value));
	}

	public void put(String key, byte value) {
		putDataItem(key, Byte.valueOf(value));
	}

	public void put(String key, float value) {
		putDataItem(key, Float.valueOf(value));
	}

	public void put(String key, double value) {
		putDataItem(key, Double.valueOf(value));
	}

	public void put(String key, boolean value) {
		putDataItem(key, Boolean.valueOf(value));
	}

	public void put(String key, char value) {
		putDataItem(key, Character.valueOf(value));
	}

	public void put(String key, String value) {
		putDataItem(key, value);
	}

	public void put(String key, java.sql.Date value) {
		putDataItem(key, value);
	}

	public void put(String key, Timestamp value) {
		putDataItem(key, value);
	}

	public int size() {
		return this.dataMap.size();
	}

	public void clear() {
		this.dataMap.clear();
	}

	public boolean containsKey(Object key) {
		return this.dataMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return this.dataMap.containsValue(value);
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		return this.dataMap.entrySet();
	}

	public boolean isEmpty() {
		return this.dataMap.isEmpty();
	}

	public Set<String> keySet() {
		return this.dataMap.keySet();
	}

	public Object put(String key, Object value) {
		putDataItem(key, value);
		return this.dataMap;
	}

	public void putAll(Map<? extends String, ? extends Object> t) {
		this.dataMap.putAll(t);
	}

	public Object remove(Object key) {
		return this.dataMap.remove(key);
	}

	public Collection<Object> values() {
		return this.dataMap.values();
	}

	public String toString() {
		return this.dataMap == null ? "" : this.dataMap.toString();
	}

	public String getStringValue(String key) {
		if (this.dataMap == null) {
			return "";
		}

		java.sql.Date date = getSqlDateValue(key);
		if (date != null) {
			return date.toString();
		}

		Object temp = getDataItem(key);
		if (temp == null) {
			return "";
		}

		return temp == null ? "" : temp.toString().trim();
	}

	public long getLongValue(String key) {
		if (this.dataMap == null) {
			return 0;
		}
		Object temp = getDataItem(key);
		if (temp != null) {
			try {
				return Long.parseLong(temp.toString().trim());
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}

	public int getIntValue(String key) {
		if (this.dataMap == null) {
			return 0;
		}
		Object temp = getDataItem(key);
		if (temp != null) {
			try {
				return Integer.parseInt(temp.toString().trim());
			} catch (Exception e) {
				return 0;
			}
		}
		return 0;
	}

	public double getDoubleValue(String key) {
		if (this.dataMap == null) {
			return 0d;
		}
		Object temp = getDataItem(key);
		if (temp != null) {
			try {
				return Double.parseDouble(temp.toString().trim());
			} catch (Exception e) {
				return 0d;
			}
		}
		return 0d;
	}

	public java.sql.Date getSqlDateValue(String key) {
		if (this.dataMap == null) {
			return null;
		}
		Object temp = getDataItem(key);
		if (temp != null) {
			if ((temp instanceof java.sql.Date)) {
				return (java.sql.Date) temp;
			}
			if ((temp instanceof java.util.Date)) {
				return new java.sql.Date(((java.util.Date) temp).getTime());
			}
			try {
				return java.sql.Date.valueOf(temp.toString().trim());
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	public String getSqlDateString(String key) {
		java.sql.Date tmp = getSqlDateValue(key);
		if (tmp != null) {
			return tmp.toString();
		} else {
			return null;
		}

	}

	public String getSqlTimestampString(String key) {
		Timestamp tmp = getSqlTimestampValue(key);
		if (tmp != null) {
			return tmp.toString();
		} else {
			return null;
		}
	}

	public Timestamp getSqlTimestampValue(String key) {
		if (this.dataMap == null) {
			return null;
		}
		Object temp = getDataItem(key);
		if (temp != null) {
			if ((temp instanceof Timestamp)) {
				return (Timestamp) temp;
			}
			if ((temp instanceof java.util.Date)) {
				return new Timestamp(((java.util.Date) temp).getTime());
			}
			try {
				return Timestamp.valueOf(temp.toString().trim() + ".0");
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	protected void putDataItem(String key, Object value) {
		this.dataMap.put(key, value);
	}

	public Object getDataItem(String key) {
		return this.dataMap.get(key);
	}

	@Override
	public Object get(Object key) {
		// TODO Auto-generated method stub
		return this.dataMap.get(key);
	}
}
