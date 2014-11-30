package org.nulleins.formats.iso8583;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.nulleins.formats.iso8583.formatters.TypeFormatter;
import org.nulleins.formats.iso8583.formatters.TypeFormatters;
import org.nulleins.formats.iso8583.types.Bitmap;
import org.nulleins.formats.iso8583.types.MTI;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** A message template is the definition of a specific ISO8583 message, defining its type,
  * content representation and the field list it can contain
  * @author phillipsr */
public class MessageTemplate {
  /** name of the template (i.e., the message it represents) */
  private final String name;
  /** header to be output at the start of a message (may be empty if none) */
  private final String header;
  /** ISO8583 message type indicator for the message represented by this template */
  private final MTI type;
  /** ISO8583 fields included in the target message */
  private final Map<Integer, FieldTemplate> fields;
  /** mapping of logical names to field numbers */
  private final Map<String, Integer> nameIndex;
  /** bitmap indicating the fields present in the message */
  private final Bitmap bitmap;

  private Optional<TypeFormatters> formatters = Optional.absent();

  /** Factory method to create a message template with the supplied properties
    * @param header     to be output at start of message
    * @param mti        message type indicator
    * @param name
    * @param fields message field-list defining the template
    * @return empty message template instance for the message type specified */
  public static MessageTemplate create(final String header, final MTI mti, final String name, final Map<Integer, FieldTemplate> fields) {
    return new MessageTemplate(header, mti, name, fields);
  }

  /** instantiate a message template with the supplied properties
    * @param header to be output at start of message
    * @param mti    message type indicator
    * @param header
    * @param mti */
  private MessageTemplate(final String header, final MTI mti, final String name, final Map<Integer, FieldTemplate> fields) {
    Preconditions.checkNotNull(header);
    Preconditions.checkNotNull(mti);
    Preconditions.checkNotNull(fields);
    Preconditions.checkArgument(!fields.isEmpty());
    this.header = header;
    this.name = name;
    type = mti;
    this.fields = ImmutableMap.copyOf(fields);
    bitmap = createBitmap(fields);
    final Map<String, Integer> nameIndex = new HashMap<>();
    for (final FieldTemplate field : fields.values()) {
      if ( field.getName() != null) {
        nameIndex.put (field.getName (), field.getNumber ());
      }
    }
    this.nameIndex = ImmutableMap.copyOf(nameIndex);
  }

  public String getName() {
    return name;
  }
  public String getHeader() { return header; }
  public MTI getMessageType() {
    return type;
  }
  public String getType() {
    return type.toString();
  }
  public Bitmap getBitmap() { return bitmap; }
  public Map<Integer, FieldTemplate> getFields() {
    return fields;
  }

  /** @return a bitmap set from the definition of the fields to be used in this message template,
   * @param fields Field-f keyed map of field templates */
  public Bitmap createBitmap(final Map<Integer, FieldTemplate> fields) {
    Bitmap result = Bitmap.empty();
    for (final Integer fieldNb : fields.keySet()) {
      result = result.withField(fieldNb);
    }
    return result;
  }

  /** @return a summary of this template, for logging/debugging usage */
  @Override
  public String toString() {
    return "MTI: " + type.describe()
        + " name: \"" + this.getName() + "\""
        + " header: [" + getHeader() + "]"
        + " #fieldlist: " + this.fields.size();
  }

  /** @return a formatter capable of formatting/parsing a field of <code>type</code>
    * @throws MessageException if not formatter registered for the supplied field type */
  TypeFormatter<?> getFormatter(final String type) {
    Preconditions.checkState(formatters.isPresent() && formatters.get().hasFormatter(type),
        "Template must have a formatter for field type: " + type);
    return formatters.get().getFormatter(type);
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
      } else if (!validValue(msgField.get(), field)) {
        result.add("Message field data invalid (" + msgField.get() + ") for field: " + field);
      }
    }
    return result;
  }

  /** @return true if <code>value</code> is a valid value for the field described herein */
  private boolean validValue(final Object value, final FieldTemplate field) {
    Preconditions.checkState(formatters.isPresent() && formatters.get().hasFormatter(field.getType()),
        "Template must have a formatter for field type: " + field.getType());
    return formatters.get().getFormatter(field.getType()).isValid(value, field.getType(), field.getDimension());
  }

  Object parse(final byte[] data, final FieldTemplate field) {
    Preconditions.checkNotNull(data);
    Preconditions.checkNotNull(field);
    Preconditions.checkState(formatters.isPresent() && formatters.get().hasFormatter(field.getType()),
        "Template must have a formatter for field type: " + field.getType());
    final String type = field.getType();
    try {
      return formatters.get().getFormatter(type).parse(type, field.getDimension(), data.length, data);
    } catch (final ParseException e) {
      final String value = new String(data);
      throw new MessageException("Failed to parse field: " + this + ", with value ["+value+"]", e);
    }
  }

  /** @return true if field# <code>fieldNumber</code> is present in this message */
  boolean isFieldPresent(final int fieldNumber) {
    return bitmap.isFieldPresent(fieldNumber);
  }

  /** @return the field number mapped to the supplied field name
   * @param fieldName to lookup */
  int getFieldNumberForName(final String fieldName) {
    if (!nameIndex.containsKey(fieldName)) {
      throw new NoSuchFieldError(fieldName);
    }
    return nameIndex.get(fieldName);
  }

  public MessageTemplate with(final TypeFormatters formatters) {
    Preconditions.checkNotNull(formatters);
    final MessageTemplate result = MessageTemplate.create(header, type, name, fields);
    result.formatters = Optional.of(formatters);
    return result;
  }

  public static Builder Builder() {
    return new Builder();
  }

  public static class Builder {
    private MTI type;
    private String name;
    private String header;
    private Map<Integer,FieldTemplate> fields;

    public MessageTemplate build() {
      return MessageTemplate.create(header,type,name,fields);
    }

    public Builder type(final MTI type) {
      this.type = type;
      return this;
    }

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder header(final String header) {
      this.header = header;
      return this;
    }

    public Builder fieldlist(final List<FieldTemplate> fields) {
      this.fields = Maps.uniqueIndex(fields, keyFunction());
      return this;
    }

    public Builder fieldmap(final Map<Integer,FieldTemplate> fields) {
      this.fields = fields;
      return this;
    }
  }

  private static Function<FieldTemplate, Integer> keyFunction() {
    return new Function<FieldTemplate,Integer>() {
      @Override
      public Integer apply(final FieldTemplate input) {
        return input.getNumber();
      }
    };
  }

}
