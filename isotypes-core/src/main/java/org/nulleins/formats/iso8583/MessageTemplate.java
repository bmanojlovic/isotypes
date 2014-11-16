package org.nulleins.formats.iso8583;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.nulleins.formats.iso8583.formatters.TypeFormatter;
import org.nulleins.formats.iso8583.types.Bitmap;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.MTI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** A message template is the definition of a specific ISO8583 message, defining its type,
  * content representation and the fields it can contain
  * @author phillipsr */
public class MessageTemplate {
  /** ID of the message template instance (used by Spring */
  private String id;
  /** name of the template (i.e., the message it represents) */
  private String name;
  /** header to be output at the start of a message (may be empty if none) */
  private String header;
  /** ISO8583 message type indicator for the message represented by this template */
  private MTI type;
  /** ISO8583 fields included in the target message */
  private Map<Integer, FieldTemplate> fields = new HashMap<>();
  /** mapping of logical names to field numbers */
  private final Map<String, Integer> nameIndex = new HashMap<>();
  /** bitmap indicating the fields present in the message */
  private final Bitmap bitmap = new Bitmap();
  /** schema to which this template belongs: provides default values, e.g., contentType */
  private MessageFactory schema;

  /** Factory method to create a message template with the supplied properties
    * @param header     to be output at start of message
    * @param mti        message type indicator
    * @param bitmapType is a binary or hex bitmap to be used?
    * @return empty message template instance for the message type specified */
  public static MessageTemplate create(final String header, final MTI mti, final BitmapType bitmapType) {
    return new MessageTemplate(header, mti);
  }

  /** default constructor, used by Spring */
  MessageTemplate() {
  }

  /** instantiate a message template with the supplied properties
    * @param header to be output at start of message
    * @param mti    message type indicator
    * @param header
    * @param mti */
  private MessageTemplate(final String header, final MTI mti) {
    Preconditions.checkNotNull(header);
    Preconditions.checkArgument(!header.isEmpty());
    Preconditions.checkNotNull(mti);
    this.header = header;
    type = mti;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getHeader() {
    return header != null ? header : schema.getHeader();
  }

  public MTI getMessageType() {
    return type;
  }

  public String getType() {
    return type.toString();
  }

  public void setType(final String mti) {
    type = MTI.create(mti);
  }

  public Map<Integer, FieldTemplate> getFields() {
    return fields;
  }

  /**
   * Set the definition of the fields to be used in this message template,
   * and calculate the bitmap describing their presence or otherwise, for
   * all potential 192 fields (primary, secondary and tertiary bitmaps)
   * @param fields Field-f keyed map of field templates
   */
  public void setFields(final Map<Integer, FieldTemplate> fields) {
    this.fields = fields;
    bitmap.clear();
    for (final Integer fieldNb : fields.keySet()) {
      bitmap.setField(fieldNb);
    }
    nameIndex.clear();
    for (final FieldTemplate field : fields.values()) {
      nameIndex.put(field.getName(), field.getNumber());
    }
  }

  /** @return a summary of this template, for logging/debugging usage */
  @Override
  public String toString() {
    return "MTI: " + type.describe()
        + " name: \"" + this.getName() + "\""
        + " header: [" + getHeader() + "]"
        + " #fields: " + this.fields.size();
  }

  /** Add the supplied field <code>template</code> to the set of field in
    * this message template, updating the bitmap to reflect its presence */
  public void addField(final FieldTemplate template) {
    template.setMessageTemplate(this);
    fields.put(template.getNumber(), template);
    bitmap.setField(template.getNumber());
    // add the field to the name index, if set:
    final String fieldName = template.getName();
    if (fieldName != null && !fieldName.isEmpty()) {
      nameIndex.put(template.getName(), template.getNumber());
    }
  }

  public void setFields(final List<FieldTemplate> fields) {
    for ( final FieldTemplate field : fields) {
      addField(field);
    }
  }

  /** @return the bitmap for this message template */
  public Bitmap getBitmap() {
    return bitmap;
  }

  /** Associate this template with the supplied <code>messageFactory</code>
    * to which this message template will belong */
  public void setSchema(final MessageFactory messageFactory) {
    this.schema = messageFactory;
  }

  /** @return a formatter capable of formatting.parsing a field of <code>type</code>
    * @throws MessageException if not formatter registered for the supplied field type */
  TypeFormatter<?> getFormatter(final String type) {
    return schema.getFormatter(type);
  }

  /** @return a list of errors detected, or an empty list, if message is valid
    * Does the supplied message conform to this template?
    * @param message instance to validate against this template */
  List<String> validate(final Message message) {
    final List<String> result = new ArrayList<>();
    if (!message.getMTI().equals(this.type)) {
      result.add("Message MTI (" + message.getMTI() + ") != Template MTI (" + type + ")");
    }
    if (!message.getHeader().equals(getHeader())) {
      result.add("Message header (" + message.getHeader() + ") != Template header (" + getHeader() + ")");
    }
    for (final FieldTemplate field : fields.values()) {
      if (field.isOptional()) {
        continue;
      }
      final Optional<Object> msgField = message.getFields().get(field.getNumber());
      if (msgField == null || !msgField.isPresent()) {
        result.add("Message field missing (" + field + ")");
      } else if (!field.validValue(msgField.get())) {
        result.add("Message field data invalid (" + msgField.get() + ") for field: " + field);
      }
    }
    return result;
  }

  /** @return true if field# <code>fieldNumber</code> is present in this message */
  boolean isFieldPresent(final int fieldNumber) {
    return bitmap.isFieldPresent(fieldNumber);
  }

  /**
   * Answer with the field f mapped to the supplied field name
   * @param fieldName to lookup
   * @return field f
   */
  int getFieldNumberForName(final String fieldName) {
    if (!nameIndex.containsKey(fieldName)) {
      throw new NoSuchFieldError(fieldName);
    }
    return nameIndex.get(fieldName);
  }

  public void addFields(final List<FieldTemplate> fieldTemplates) {
    for(final FieldTemplate template : fieldTemplates) {
      addField(template);
    }
  }
}
