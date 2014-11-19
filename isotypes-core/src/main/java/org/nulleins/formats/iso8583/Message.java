package org.nulleins.formats.iso8583;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.collections.ListUtils;
import org.nulleins.formats.iso8583.types.MTI;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/** An ISO8583 message instance, being a f of header values and a set of field values
 * <p/>
 * Every message has a reference to the <code>template</code> that describes the message
 * and its content
 * @author phillipsr */
public final class Message implements Comparable<Message> {
  private final Map<Integer, Optional<Object>> fields;
  private final String header;
  private final MessageTemplate template;

  /** Instantiate a new message, of the type specified
   * @param template
   * @param header
   * @throws IllegalArgumentException if the supplied MTI is null */
  private Message(final MessageTemplate template, final String header, final Map<Integer, Optional<Object>> fieldValues) {
    Preconditions.checkNotNull(template, "Template cannot be null");
    Preconditions.checkNotNull(fieldValues, "Fields cannot be null");
    this.template = template;
    this.header = header != null ? header : template.getHeader();
    this.fields = ImmutableMap.copyOf(fieldValues);
  }

  /** Answer with this message's MTI */
  public MTI getMTI() {
    return template.getMessageType();
  }

  public String getHeader() {
    return header != null ? header : template.getHeader();
  }

  public Map<Integer, Optional<Object>> getFields() {
    return fields;
  }

  /** @return the value of the field specified
   * @param fieldNumber of field whose value is requested
   * @throws NoSuchFieldError if the field is not defined for this message */
  public Optional<Object> getFieldValue(final int fieldNumber) {
    if (!template.isFieldPresent(fieldNumber)) {
      throw new NoSuchFieldError(fieldNumber + "");
    }
    return fields.get(fieldNumber);
  }

  /** @return the value of the field specified
   * @param fieldName of field whose value is requested
   * @throws NoSuchFieldError if the field is not defined for this message */
  public Optional<Object> getFieldValue(final String fieldName) {
    return getFieldValue(
        template.getFieldNumberForName(fieldName));
  }

  /** @return an empty list if this message is valid according to its template,
   * otherwise return a list of error messages */
  public List<String> validate() {
    return template.validate(this);
  }

  /** @return a summary of this field, for logging purposes */
  @Override
  public String toString() {
    return "Message mti=" + template.getMessageType() + " header=" + this.getHeader() + " #field=" + fields.size();
  }

  /** @return an iterator to iterate over the multi-line desc of this message,
   * including message type information, field type information and field values */
  public Iterable<String> describe() {
    return new Describer(template, fields);
  }

  /** @return true if message is valid, according to it's template (all the required fields are present) */
  public boolean isValid() {
    return ListUtils.EMPTY_LIST.equals(this.validate());
  }

  /** @return true if field <code>f</code> present in the message
   * @param number */
  public boolean isFieldPresent(final int number) {
    return template.isFieldPresent(number);
  }

  @Override
  public int compareTo(final Message other) {
    return this.toString().compareTo(other.toString());
  }

  public Message withValues(final Map<Integer, Optional<Object>> fieldValues) {
    return new Message(template,header,fieldValues);
  }

  /** @return a new builder, for constructing messages */
  public static Builder Builder() {
    return new Builder();
  }

  public static class Builder {
    private MessageTemplate template;
    private String header;
    private Map<Integer, Optional<Object>> fields = Collections.emptyMap();

    public Builder header(final String header) {
      this.header = header;
      return this;
    }

    public Builder template(final MessageTemplate template) {
      this.template = template;
      return this;
    }

    public Builder fields(final Map<Integer, Object> fields) {
      return optionalFields(Maps.transformValues(fields, toOptional()));
    }

    public Builder optionalFields(final Map<Integer, Optional<Object>> fields) {
      this.fields = fields;
      return this;
    }

    public Message build() {
      return new Message(template, header, fields);
    }
  }

  public static Function<Object,Optional<Object>> toOptional() {
    return new Function<Object,Optional<Object>>() {
      @Override
      public Optional<Object> apply(final Object input) {
        return Optional.fromNullable(input);
      }
    };
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    final Message message = (Message) other;

    if (!template.getMessageType().equals(message.getMTI())) {
      return false;
    }

    if (fields.size() != message.fields.size()) {
      return false;
    }
    for (final Map.Entry<Integer, Optional<Object>> item : fields.entrySet()) {
      final Object that = message.fields.get(item.getKey()).or("");
      if (!that.toString().equals(item.getValue().or("").toString())) {
        return false;
      }
    }

    return header.equals(message.header);
  }

  @Override
  public int hashCode() {
    int result = template.getBitmap().hashCode();
    result = 31 * result + fields.hashCode();
    result = 31 * result + header.hashCode();
    return result;
  }
}
