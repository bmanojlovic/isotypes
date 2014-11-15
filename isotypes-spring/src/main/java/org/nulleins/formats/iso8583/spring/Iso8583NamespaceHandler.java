package org.nulleins.formats.iso8583.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class Iso8583NamespaceHandler extends NamespaceHandlerSupport {
  @Override
  public void init() {
    registerBeanDefinitionParser("schema",
        new SchemaDefinitionParser());
  }

}
