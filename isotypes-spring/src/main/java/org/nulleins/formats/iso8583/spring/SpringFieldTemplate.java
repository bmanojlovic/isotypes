package org.nulleins.formats.iso8583.spring;

import org.nulleins.formats.iso8583.types.Dimension;

public class SpringFieldTemplate {
  private String messageType;
  private String number;
  private String type;
  private String autogen;
  private String optional;
  private String name;
  private String description;
  private Dimension dimension;

  public String getDefaultValue () {
    return defaultValue;
  }

  public void setDefaultValue (String defaultValue) {
    this.defaultValue = defaultValue;
  }

  private String defaultValue;

  public Dimension getDimension () {
    return dimension;
  }

  public void setDimension (Dimension dimension) {
    this.dimension = dimension;
  }

  public String getMessageType () {
    return messageType;
  }

  public void setMessageType (String messageType) {
    this.messageType = messageType;
  }

  public void setNumber (String number) {
    this.number = number;
  }

  public String getType () {

    return type;
  }

  public void setType (String type) {
    this.type = type;
  }

  public String getAutogen () {
    return autogen;
  }

  public void setAutogen (String autogen) {
    this.autogen = autogen;
  }

  public String getOptional () {
    return optional;
  }

  public void setOptional (String optional) {
    this.optional = optional;
  }

  public String getName () {
    return name;
  }

  public void setName (String name) {
    this.name = name;
  }

  public String getDescription () {
    return description;
  }

  public void setDescription (String description) {
    this.description = description;
  }

  public String getNumber () {
    return number;
  }
}
