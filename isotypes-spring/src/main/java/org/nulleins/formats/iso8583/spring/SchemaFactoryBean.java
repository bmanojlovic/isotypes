package org.nulleins.formats.iso8583.spring;

import org.nulleins.formats.iso8583.AutoGenerator;
import org.nulleins.formats.iso8583.AutoGeneratorFactory;
import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.MessageTemplate;
import org.springframework.beans.factory.FactoryBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Bean factory to create a MessageFactory instance from the parsed XML definition
  * @author phillipsr */
public class SchemaFactoryBean implements FactoryBean<MessageFactory> {
  private String id;
  private final Map<String, MessageTemplate> messages = new HashMap<>();
  /* map of field lists by message MTI */
  private final Map<String, List<FieldTemplate>> fields = new HashMap<>();
  private String description;
  private AutoGenerator autogen;
  private final List<FormatterSpec> formatters = new ArrayList<>();
  private MessageFactory schema;

  /** Set the message object that will be decorated and returned by this bean factory
    * @param schema the top-level process object defined */
  public void setSchema(final MessageFactory schema) {
    this.schema = schema;
  }

  public void setMessages(final List<MessageTemplate> messageList) {
    for (final MessageTemplate message : messageList) {
      messages.put(message.getType(), message);
    }
  }

  public void setFormatters(final List<FormatterSpec> formatterList) {
    for (final FormatterSpec formatter : formatterList) {
      formatters.add(formatter);
    }
  }

  public void setFields(final List<FieldTemplate> fieldList) {
    for (final FieldTemplate field : fieldList) {
      List<FieldTemplate> messageFields = fields.get(field.getMessageType());
      if (messageFields == null) {
        messageFields = new ArrayList<>();
        fields.put(field.getMessageType(), messageFields);
      }
      messageFields.add(field);
    }
  }

  public void setDescription(final String description) {
    this.description = description;
  }
  public void setAutogen(final AutoGenerator autogen) {
    this.autogen = autogen;
  }

  /** @return the message factory object, with its state fields assigned from the
    *   values previously set, providing the required type conversions */
  @Override
  public MessageFactory getObject() throws Exception {
    MessageFactory.Builder builder = MessageFactory.Builder()
        .id(schema.getId())
        .bitmapType(schema.getBitmapType())
        .charset(schema.getCharset())
        .contentType(schema.getContentType())
        .header(schema.getHeader())
        .description(description)
        .autogen(new AutoGeneratorFactory(autogen));

    for (final FormatterSpec item : formatters) {
      builder = builder.addFormatter(item.getType(), item.getFormatter());
    }
    for (final Map.Entry<String, MessageTemplate> item : messages.entrySet()) {
      final MessageTemplate message = item.getValue();
      setMessageFields(item.getKey(), message);
      builder = builder.addTemplate(message);
    }
    final MessageFactory schema = builder.build();
    schema.initialize();
    return schema;
  }

  private void setMessageFields(final String type, final MessageTemplate message) {
    final List<FieldTemplate> fields = this.fields.get(type);
    for (final FieldTemplate field : fields) {
      if (message.getFields().containsKey(field.getNumber())) {
        throw new IllegalStateException("duplicate field f: "
            + field.getNumber() + " defined for Message type "
            + message.getMessageTypeIndicator());
      }
      field.setMessageTemplate(message);
      message.addField(field);
    }
  }

  /** @return the target object type that this factory will create */
  @Override
  public Class<?> getObjectType() {
    return MessageFactory.class;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSingleton() {
    return true;
  }
}