package ru.doccloud.document.controller.util;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class queryParam {
	@JsonProperty("name")
	@JacksonXmlProperty(localName = "name")
	public String mfield;
	@JsonProperty("operation")
	@JacksonXmlProperty(localName = "operator")
	public String moperand;
	@JsonProperty("value")
	@JacksonXmlProperty(localName = "value")
	public String mvalue;
	@JsonProperty("from")
	@JacksonXmlProperty(localName = "from")
	public XmlNode mfrom;
	@JsonProperty("to")
	@JacksonXmlProperty(localName = "to")
	public XmlNode mto;
	
	
	public queryParam(){
		super();
	}
	public queryParam(String field, String operand, String value) {
		super();
		this.mfield = field;
		this.moperand = operand;
		this.mvalue = value;
	}
	public String getField() {
		return mfield;
	}
	public void setField(String field) {
		this.mfield = field;
	}
	public String getOperand() {
		return moperand;
	}
	public void setOperand(String operand) {
		this.moperand = operand;
	}
	public String getValue() {
		return mvalue;
	}
	public void setValue(String value) {
		this.mvalue = value;
	}
	public XmlNode getFrom() {
		return mfrom;
	}
	public void setFrom(XmlNode mfrom) {
		this.mfrom = mfrom;
	}
	public XmlNode getTo() {
		return mto;
	}
	public void setTo(XmlNode mto) {
		this.mto = mto;
	}
	
	
}
