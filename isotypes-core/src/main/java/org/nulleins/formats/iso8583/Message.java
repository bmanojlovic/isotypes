package org.nulleins.formats.iso8583;

import com.google.common.base.Preconditions;
import org.apache.commons.collections.ListUtils;
import org.nulleins.formats.iso8583.types.MTI;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** An ISO8583 message instance, being a f of header values and a set of field values
  * <p/>
  * Every message has a reference to the <code>template</code> that describes the message
  * and its content
  * @author phillipsr */
public class Message implements Comparable<Message> {
  private final MTI messageTypeIndicator;
  private final Map<Integer, Object> fields = new HashMap<>();
  private final String header;
  private MessageTemplate template;

  /** Instantiate a new message, of the type specified
    * @param messageTypeIndicator
    * @param header
   * @throws IllegalArgumentException if the supplied MTI is null */
  public Message(final MTI messageTypeIndicator, final String header) {
    Preconditions.checkNotNull(messageTypeIndicator, "MTI cannot be null");
    this.messageTypeIndicator = messageTypeIndicator;
    this.header = header;
  }

  /** Answer with this message's MTI */
  public MTI getMTI() {
    return messageTypeIndicator;
  }

  public String getHeader() {
    return header != null ? header : template.getHeader();
  }

  public Map<Integer, Object> getFields() {
    return fields;
  }

  public void setFields(final Map<Integer, Object> fields) {
    this.fields.clear();
    this.fields.putAll(fields);
  }

  /** Set the value of the field specified
    * @param fieldNumber of the field to receive the value
    * @param value       object to set
    * @throws NoSuchFieldError         if the field is not defined for this message,
    * @throws IllegalArgumentException if the value data type supplied is not
    *                                  compatible with the defined field type  */
  public void setFieldValue(final int fieldNumber, final Object value) {
    if (!template.isFieldPresent(fieldNumber)) {
      throw new NoSuchFieldError(fieldNumber + "");
    }
    final FieldTemplate field = template.getFields().get(fieldNumber);
    if (!field.validValue(value)) {
      throw new IllegalArgumentException("Supplied value (" + value + ") not valid for field:" + field);
    }
    fields.put(fieldNumber, value);
  }

  /** Set the value of the named field
    * @param fieldName the field to receive the value
    * @param value     object to set
    * @throws NoSuchFieldError         if the field is not defined for this message
    * @throws IllegalArgumentException if the value data type supplied is not
    *                                  compatible with the defined field type */
  public void setFieldValue(final String fieldName, final Object value) {
    setFieldValue(template.getFieldNumberForName(fieldName), value);
  }

  /** @return the value of the field specified
    * @param fieldNumber of field whose value is requested
    * @throws NoSuchFieldError if the field is not defined for this message */
  public Object getFieldValue(final int fieldNumber) {
    if (!template.isFieldPresent(fieldNumber)) {
      throw new NoSuchFieldError(fieldNumber + "");
    }
    return fields.get(fieldNumber);
  }

  /** @return the value of the field specified
    * @param fieldName of field whose value is requested
    * @throws NoSuchFieldError if the field is not defined for this message */
  public Object getFieldValue(final String fieldName) {
    return getFieldValue(
        template.getFieldNumberForName(fieldName));
  }

  /** Remove the field specified from this message's field set
    * @param fieldNumber
    * @throws NoSuchFieldError if the field is not defined for this message */
  public void removeField(final int fieldNumber) {
    if (!template.isFieldPresent(fieldNumber)) {
      throw new NoSuchFieldError(fieldNumber + "");
    }
    fields.remove(fieldNumber);
  }

  /** @return an empty list if this message is valid according to its template,
    * otherwise return a list of error messages */
  public List<String> validate() {
    return template.validate(this);
  }

  /** Set the message template that defines this message instance
    * @param messageTemplate */
  public void setTemplate(final MessageTemplate messageTemplate) {
    this.template = messageTemplate;
  }

  /** @return a summary of this field, for logging purposes */
  @Override
  public String toString() {
    return "Message mti=" + messageTypeIndicator + " header=" + this.getHeader() + " #field=" + fields.size();
  }

  public Message asType(final MTI messageTypeIndicator, final MessageTemplate template, final Map<? extends Integer, ?> fields) {
    return Builder()
        .messageTypeIndicator(messageTypeIndicator)
        .header(header)
        .template(template)
        .fields(new HashMap<>(fields))
        .build();
  }

  /** @return an iterator to iterate over the multi-line desc of this message,
    * including message type information, field type information and field values */
  public Iterable<String> describe() {
    return new Describer(template, fields);
  }

  /** Add all the supplied field values to this message
    * @param fieldValues */
  public void addFields(final Map<Integer, Object> fieldValues) {
    fields.putAll(fieldValues);
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

  /** @return a new builder, for constructing messages */
  public static Builder Builder() {
    return new Builder();
  }

  @Override
  public int compareTo(final Message other) {
    return this.toString().compareTo(other.toString());
  }

  public static class Builder {
    private MTI messageTypeIndicator;
    private String header;
    private Map<Integer, Object> fields = Collections.emptyMap();
    private MessageTemplate template;

    public Builder messageTypeIndicator(final MTI code) {
      this.messageTypeIndicator = code;
      return this;
    }

    public Builder header(final String header) {
      this.header = header;
      return this;
    }

    public Builder template(final MessageTemplate template) {
      this.template = template;
      return this;
    }

    public Builder fields(final Map<Integer, Object> fields) {
      this.fields = fields;
      return this;
    }

    public Message build() {
      final Message result = new Message(messageTypeIndicator, header);
      result.setFields(fields);
      result.setTemplate(template);
      return result;
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) { return true;}
    if (other == null || getClass() != other.getClass()) { return false;}
    final Message message = (Message) other;

    if (!messageTypeIndicator.equals(message.messageTypeIndicator)) { return false; }

    if(fields.size() != message.fields.size()) { return false; }
    for ( final Map.Entry<Integer,Object> item : fields.entrySet()) {
      final Object that = message.fields.get(item.getKey());
      if ( !item.getValue().toString().equals(that.toString())) {
        return false;
      }
    }

    return header.equals(message.header);
  }

  @Override
  public int hashCode() {
    int result = messageTypeIndicator.hashCode();
    result = 31 * result + fields.hashCode();
    result = 31 * result + header.hashCode();
    return result;
  }
}
