package org.nulleins.formats.iso8583.spring;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import org.nulleins.formats.iso8583.*;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.MTI;
import org.springframework.beans.factory.FactoryBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Bean factory to create a MessageFactory instance from the parsed XML definition
  * @author phillipsr */
public class SchemaFactoryBean implements FactoryBean<MessageFactory> {

  private final Map<String, SpringMessageTemplate> messages = new HashMap<>();
  /* map of field lists by message MTI */
  private final Map<String, List<SpringFieldTemplate>> fields = new HashMap<>();
  private String description;
  private AutoGenerator autogen;
  private final List<FormatterSpec> formatters = new ArrayList<>();
  private SpringMessageSchema schema;

  /** Set the message object that will be decorated and returned by this bean factory
    * @param schema the top-level process object defined */
  public void setSchema(final SpringMessageSchema schema) {
    this.schema = schema;
  }

  public void setMessages(final List<SpringMessageTemplate> messageList) {
    for (final SpringMessageTemplate message : messageList) {
      messages.put(message.getType(), message);
    }
  }

  public void setFormatters(final List<FormatterSpec> formatterList) {
    for (final FormatterSpec formatter : formatterList) {
      formatters.add(formatter);
    }
  }

  public void setFields(final List<SpringFieldTemplate> fieldList) {
    for (final SpringFieldTemplate field : fieldList) {
      List<SpringFieldTemplate> messageFields = fields.get(field.getMessageType());
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
        .charset(new CharEncoder (schema.getCharset()))
        .contentType (schema.getContentType ())
        .header (schema.getHeader ())
        .description (description)
        .autogen (new AutoGeneratorFactory (autogen));

    for (final FormatterSpec item : formatters) {
      builder = builder.addFormatter(item.getType(), item.getFormatter());
    }
    for (final Map.Entry<String, SpringMessageTemplate> item : messages.entrySet()) {
      final SpringMessageTemplate messageTemplate = item.getValue();
      setMessageFields(item.getKey(), messageTemplate);
      final List<FieldTemplate> fieldList = FluentIterable.from (messageTemplate.getFields ())
              .transform (fieldTemplateTransformer).toList ();
      final MessageTemplate message = MessageTemplate.Builder ()
          .type (MTI.create (messageTemplate.getType ()))
          .name (messageTemplate.getName ())
          .fieldlist (fieldList)
          .header (schema.getHeader ())
          .build ();
      builder = builder.addTemplate(message);
    }
    return  builder.build();
  }

  /** function to transform SpringFieldTemplates to FieldTemplates */
  private static final Function<SpringFieldTemplate, FieldTemplate> fieldTemplateTransformer =
     new Function<SpringFieldTemplate, FieldTemplate> () {
      @Override
      public FieldTemplate apply (final SpringFieldTemplate input) {
        return FieldTemplate.localBuilder ().get ()
            .f (Integer.valueOf (input.getNumber ()))
            .type (input.getType ())
            .name (input.getName ())
            .dimension (input.getDimension ())
            .desc (input.getDescription ())
            .autogenSpec (input.getAutogen ())
            .defaultValue (input.getDefaultValue ())
            .build ();
      }
    };

  private void setMessageFields(final String type, final SpringMessageTemplate message) {
    message.setFields (this.fields.get(type));
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