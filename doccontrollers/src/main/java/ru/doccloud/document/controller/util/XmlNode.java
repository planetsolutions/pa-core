package ru.doccloud.document.controller.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class XmlNode {

	@JsonProperty("value")
	@JacksonXmlProperty(localName = "value")
    private String value;
    //still ignoring any other attributes

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
