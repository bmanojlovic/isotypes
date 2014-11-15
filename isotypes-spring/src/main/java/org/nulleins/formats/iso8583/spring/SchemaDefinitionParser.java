package org.nulleins.formats.iso8583.spring;

import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.MessageTemplate;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.MTI;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/** Spring bean definition parser to parse ISO message schema
  * @author phillipsr */
public class SchemaDefinitionParser extends AbstractBeanDefinitionParser {
  @Override
  protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
    final BeanDefinitionBuilder factory
        = BeanDefinitionBuilder.rootBeanDefinition(SchemaFactoryBean.class);
    final AbstractBeanDefinition messageSet = parseMessageSet(element);
    factory.addPropertyValue("schema", messageSet);

    final Element desc = DomUtils.getChildElementByTagName(element, "description");
    if (desc != null) {
      factory.addPropertyValue("description", desc.getTextContent().trim());
    }
    final Element autogen = DomUtils.getChildElementByTagName(element, "autogen");
    if (autogen != null) {
      factory.addPropertyReference("autogen", autogen.getTextContent().trim());
    }

    final Element formatters = DomUtils.getChildElementByTagName(element, "formatters");
    if (formatters != null) {
      final List<Element> formatterList = DomUtils.getChildElementsByTagName(formatters, "formatter");
      parseFormatters(formatterList, factory);
    }

    final List<Element> messages = DomUtils.getChildElementsByTagName(element, "message");
    if (messages != null && messages.size() > 0) {
      parseMessages(messages, factory);
    }
    return factory.getBeanDefinition();
  }

  private static AbstractBeanDefinition parseMessageSet(final Element element) {
    final BeanDefinitionBuilder messageSet
        = BeanDefinitionBuilder.rootBeanDefinition(MessageFactory.class);
    messageSet.addPropertyValue("id", element.getAttribute("id"));
    messageSet.addPropertyValue("header", element.getAttribute("header"));
    messageSet.addPropertyValue("strict", element.getAttribute("strict"));
    final BitmapType bitmapType = BitmapType.valueOf(element.getAttribute("bitmapType").trim().toUpperCase());
    messageSet.addPropertyValue("bitmapType", bitmapType.toString());
    final ContentType contentType = ContentType.valueOf(element.getAttribute("contentType").trim().toUpperCase());
    messageSet.addPropertyValue("contentType", contentType.toString());
    final String charset = element.getAttribute("charset").trim().toUpperCase();
    messageSet.addPropertyValue("charset", charset);

    return messageSet.getBeanDefinition();
  }

  private static void parseMessages(final List<Element> messages, final BeanDefinitionBuilder factory) {
    final ManagedList<AbstractBeanDefinition> messageList = new ManagedList<>(messages.size());
    final ManagedList<AbstractBeanDefinition> allFields = new ManagedList<>();
    for (final Element messageElement : messages) {
      final MTI type = MTI.create(messageElement.getAttribute("type").trim());
      final AbstractBeanDefinition message = parseMessage(type, messageElement);
      messageList.add(message);
      final List<Element> fields = DomUtils.getChildElementsByTagName(messageElement, "field");
      if (fields != null) {
        for (final Element element : fields) {
          allFields.add(parseField(type, element));
        }
      }
    }
    factory.addPropertyValue("fields", allFields);
    factory.addPropertyValue("messages", messageList);
  }

  private static AbstractBeanDefinition parseMessage(final MTI type, final Element element) {
    final BeanDefinitionBuilder result
            = BeanDefinitionBuilder.rootBeanDefinition(MessageTemplate.class);
    result.addPropertyValue("type", type.toString());
    result.addPropertyValue("name", element.getAttribute("name"));
    return result.getBeanDefinition();
  }

  private static AbstractBeanDefinition parseField(final MTI type, final Element element) {
    final BeanDefinitionBuilder result
           = BeanDefinitionBuilder.rootBeanDefinition(FieldTemplate.class);

    result.addPropertyValue("messageType", type.toString());
    result.addPropertyValue("number", element.getAttribute("f"));
    result.addPropertyValue("type", element.getAttribute("type"));
    result.addPropertyValue("autogen", element.getAttribute("autogen"));
    result.addPropertyValue("optional", element.getAttribute("optional"));
    result.addPropertyValue("name", element.getAttribute("name"));
    result.addPropertyValue("description", element.getAttribute("desc"));

    final String defaultValue = element.getTextContent();
    if (defaultValue != null && !defaultValue.isEmpty()) {
      result.addPropertyValue("defaultValue", defaultValue);
    }
    final String dim = element.getAttribute("dim");
    if (dim != null && !dim.isEmpty()) {
      result.addPropertyValue("dimension", Dimension.parse(dim));
    }
    return result.getBeanDefinition();
  }

  private static void parseFormatters(final List<Element> formatters, final BeanDefinitionBuilder factory) {
    final ManagedList<AbstractBeanDefinition> formattersList = new ManagedList<>(formatters.size());
    for (final Element element : formatters) {
      formattersList.add(parseFormatter(element));
    }
    factory.addPropertyValue("formatters", formattersList);
  }

  private static AbstractBeanDefinition parseFormatter(final Element element) {
    final BeanDefinitionBuilder result
        = BeanDefinitionBuilder.rootBeanDefinition(FormatterSpec.class);
    result.addPropertyValue("type", element.getAttribute("type"));

    final String refAttr = element.getAttribute("ref");
    final String classAttr = element.getAttribute("class");
    if (!classAttr.isEmpty() && !refAttr.isEmpty()) {
      throw new IllegalStateException("Cannot specify both class and ref for a formatter: choose one! ");
    }
    result.addPropertyValue("spec", refAttr.isEmpty() ? classAttr : refAttr);

    setFormatter(element, result);
    return result.getBeanDefinition();
  }

  private static void setFormatter(final Element nodeElement, final BeanDefinitionBuilder formatter) {
    final String formatterName = nodeElement.getAttribute("class");
    // check is 'action=class-name' is specified
    if (formatterName != null && !formatterName.isEmpty()) { // formatter class specified, check that it is a valid class name
      try {
        final Class<?> formatterClass = SchemaDefinitionParser.class.getClassLoader().loadClass(formatterName);
        formatter.addPropertyValue("spec", formatterClass);
      } catch (final Exception e) {
        throw new IllegalStateException("could load action class for action: " + formatterName, e);
      }
    } else {
      final String refName = nodeElement.getAttribute("ref");
      if (refName != null && !refName.isEmpty()) { // set the formatter as a property reference
        formatter.addPropertyReference("spec", refName);
      }
    }
  }

}