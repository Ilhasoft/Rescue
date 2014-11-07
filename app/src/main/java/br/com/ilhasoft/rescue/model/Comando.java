package br.com.ilhasoft.rescue.model;

public class Comando {
	private String key;
	private String keyAttr;
	private String tag;
	private String value;

	public Comando(String tag, String key, String value) {
		this.tag = tag;
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKeyAttr() {
		return keyAttr;
	}

	public void setKeyAttr(String keyAttr) {
		this.keyAttr = keyAttr;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Comando [key=" + key + ", keyAttr=" + keyAttr + ", tag=" + tag
				+ ", value=" + value + "]";
	}

}