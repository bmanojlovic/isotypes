package org.nulleins.formats.iso8583;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import org.apache.commons.beanutils.PropertyUtils;
import org.nulleins.formats.iso8583.formatters.TypeFormatter;
import org.nulleins.formats.iso8583.formatters.TypeFormatters;
import org.nulleins.formats.iso8583.io.BCDMessageWriter;
import org.nulleins.formats.iso8583.io.CharMessageWriter;
import org.nulleins.formats.iso8583.io.MessageWriter;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;


/** ISO8583 Message factory, configured with a set of message templates (a schema), capable of
 * creating and parsing ISO8583 templates
 * <p/>
 * Usually configured via an XML specification (<code>&lt;iso:schema&gt;</code>),
 * it may also be created by setting the following fields and calling <code>initialize()</code>:
 * <dl>
 * <dt>header</dt><dd>Text header to prepend to templates, e.g., ISO015000077</dd>
 * <dt>templates</dt><dd>A map of MTI:MessageTemplates, defining the templates
 * understood by this factory instance</dd>
 * <dt>contentType</dt><dd>Enumeration specifying the content type of templates
 * created or parsed (one of BCD, ASCII, EBCDIC)</dd>
 * <dt>bitmapType</dt><dd>Type of bitmap to be used, one of BINARY, HEX</dd>
 * </dl>
 * @author phillipsr */
public class MessageFactory {
  private final Map<MTI, MessageTemplate> templates = new HashMap<>();
  private BitmapType bitmapType = BitmapType.HEX;
  private ContentType contentType = ContentType.TEXT;
  private CharEncoder charset = CharEncoder.ASCII;
  private String header = "";
  private String description;
  private String id;
  private boolean strict = Boolean.TRUE;
  private TypeFormatters formatters;
  private MessageParser parser;

  private Optional<AutoGeneratorFactory> autoGenerator = Optional.absent();

  MessageFactory() {
  }

  public void initialize() {
    if (templates == null || templates.isEmpty()) {
      throw new IllegalStateException("Factory has no message definitions: cannot be initialized");
    }
    if (formatters == null) {
      formatters = new TypeFormatters(charset);
    }
    if (parser == null) {
      parser = MessageParser.create(header, templates, contentType, charset, bitmapType);
    }
  }

  public void initialize(final Map<String, TypeFormatter<?>> formatterList, final List<MessageTemplate> templateList) {
    formatters = new TypeFormatters(charset);
    for (final Map.Entry<String, TypeFormatter<?>> item : formatterList.entrySet()) {
      this.addFormatter(item.getKey(), item.getValue());
    }
    if (!templateList.isEmpty()) {
      this.addTemplates(templateList);
    }
    initialize();
  }

  public void setStrict(final boolean strict) {
    this.strict = strict;
  }

  /** @return the default bitmap type used in this factory */
  public BitmapType getBitmapType() {
    return bitmapType;
  }

  /** Set the bitmap type, one of BINARY or HEX
   * @param bitmapType
   * @throws IllegalArgumentException if bitmapType is null */
  public void setBitmapType(final BitmapType bitmapType) {
    Preconditions.checkNotNull(bitmapType, "bitmapType may not be null");
    this.bitmapType = bitmapType;
  }

  /** Answer with the default message context type used in this factory */
  public ContentType getContentType() {
    return contentType;
  }

  /** Set the message (numeric) content type, one of ASCII, EBCDIC or BCD
   * @param contentType
   * @throws IllegalArgumentException if the content type is null */
  public void setContentType(final ContentType contentType) {
    Preconditions.checkNotNull(contentType, "contentType cannot not be null, must be one of: " + Arrays.toString(ContentType.values()));
    this.contentType = contentType;
  }

  public CharEncoder getCharset() {
    return charset;
  }

  public void setCharset(final CharEncoder charset) {
    Preconditions.checkNotNull(charset, "charset cannot be null");
    this.charset = charset;
  }

  /** @return the header field value used (can be null) */
  public String getHeader() {
    return header;
  }

  /** Set the value of the header field, prepended to templates generated by,
   * and expected at the start of templates parsed by this factory
   * @param header field value (can be null or empty) */
  public void setHeader(final String header) {
    this.header = header;
  }

  /** @return the text desc of this factory: not used in message creatio */
  public String getDescription() {
    return description;
  }

  /** Set the desc of this factory; this is for documentary purposes, and is
   * usually set from with the iso:schema XML element
   * @param description */
  public void setDescription(final String description) {
    this.description = description;
  }

  /** @return the Spring bean ID */
  public String getId() {
    return id;
  }

  /** Set the Spring bean ID for this factory; typically only used
   * for referencing the factory bean for Spring usage
   * @param id */
  public void setId(final String id) {
    this.id = id;
  }

  /** Set Auto-generator to use for the automatic generation of field values;
   * this is an optional dependency, and will only be used of fields are
   * defined in the schema with an 'autogen' property.
   * </p>
   * The usual way to set this field is via Spring IoC */
  public void setAutoGeneratorFactory(final AutoGeneratorFactory autoGenerator) {
    this.autoGenerator = Optional.of(autoGenerator);
  }

  /** @return the ISO8583 templates defined in this factory's schema */
  public Collection<MessageTemplate> getTemplates() {
    return templates.values();
  }

  /** Add a <code>message</code> to this factory's schema */
  public void addTemplate(final MessageTemplate message) {
    message.setSchema(this);
    this.templates.put(message.getMessageType(), message);
  }

  /** @return a string representation of this message factory */
  @Override
  public String toString() {
    return "MessageFactory id=" + getId()
        + " desc='" + getDescription() + "'"
        + " header=" + getHeader()
        + " contentType=" + getContentType()
        + " charset=" + getCharset()
        + " bitmapType=" + getBitmapType()
        + (templates != null ? (" templates# " + templates.size()) : "");
  }

  /** @return a new ISO8583 message instance of the type requested, setting the field values
   * from the supplied parameter map, keyed by field f, matching
   * <code>&lt;iso:message&gt;</code> configuration for this message type
   * @param type   type of message to create
   * @param params field value to include in message, indexed by field f
   * @throws IllegalArgumentException - if the supplied MTI is null */
  public Message createByNumbers(final MTI type, final Map<Integer, Object> params) {
    return Message.Builder()
      .template(templates.get(type))
      .header(header)
      .fields(params).build();
  }

  /** @return the map of fields that were written a message to the supplied <code>output</code> stream
   * @param message
   * @param output
   * @throws java.io.IOException
   * @see #writeFromNumberMap(org.nulleins.formats.iso8583.types.MTI, java.util.Map, java.io.OutputStream) */
  public Map<Integer, Optional<Object>> writeToStream(final Message message, final OutputStream output) throws IOException {
    return writeFromNumberMap(message.getMTI(), message.getFields(), output);
  }

  /** Create a message for the type and parameters specified and write it to the <code>output</code> stream
   * @param type   of the message to be written
   * @param params map of field # to field value (maybe updated if autogen or default required)
   * @param output stream to write formatted ISO8583 message onto
   * @throws java.io.IOException      if writing to the output stream fails for any reason
   * @throws IllegalArgumentException if the type supplied is not defined in this factory's schema,
   *                                  the output stream is null or null/empty message parameters have been supplied */
  public Map<Integer, Optional<Object>> writeFromNumberMap(final MTI type, final Map<Integer, Optional<Object>> params, final OutputStream output)
      throws IOException {
    Preconditions.checkArgument(templates.containsKey(type), "Message not defined for MTI=" + type);
    Preconditions.checkNotNull(output, "Output stream cannot be null");
    Preconditions.checkArgument(params != null && !params.isEmpty(), "Message parameters are required");

    final MessageTemplate template = templates.get(type);
    final MessageWriter writer = getOutputWriter(contentType, charset);
    final DataOutputStream dos = getDataOutputStream(output);

    writer.appendHeader(header, dos);
    writer.appendMTI(type, dos);
    writer.appendBitmap(template.getBitmap(), bitmapType, dos);

    // Iterate over the fields in order of field f, appending the field's data to the output stream
    final Map<Integer, Optional<Object>> result = new HashMap<>();
    for (final Integer key : new TreeSet<>(template.getFields().keySet())) {
      final FieldTemplate fieldTemplate = template.getFields().get(key);
      final Optional<Object> value = params.get(key);
      result.put(key, writeField(value, writer, dos, fieldTemplate));
    }
    dos.flush();

    return ImmutableMap.copyOf(result);
  }

  private Optional<Object> getAutoGenValue(final String autogen, final FieldTemplate field) {
      Preconditions.checkArgument(autoGenerator.isPresent(),
            "Message requires AutoGen field, but the (optional) AutoGenerator has not been set in the MessageFactory");
      return autoGenerator.get().generate(autogen, field);
  }

  private Optional<Object> writeField(final Optional<Object> param, final MessageWriter writer, final DataOutputStream dos, final FieldTemplate field)
      throws IOException {
    Optional<Object> data = param;
    if (!data.isPresent() && !field.isOptional()) {
      // first, try to autogen, and then fall back to default (if any)
      final String autogen = field.getAutogen();
      if (autogen != null && !autogen.isEmpty()) {
        data = getAutoGenValue(autogen, field);
      }
      if (!data.isPresent()) {
        data = Optional.<Object>fromNullable(field.getDefaultValue());
      }
      Preconditions.checkState(data.isPresent(),"No value for field: " + field);
    }
    if (data.isPresent()) {
      writer.appendField(field, data.get(), dos);
    }
    return data;
  }

  /** @return the supplied output stream wrapped in a DataOutputStream, if required
   * @param output */
  private DataOutputStream getDataOutputStream(final OutputStream output) {
    if (output instanceof DataOutputStream) {
      return (DataOutputStream) output;
    }
    return new DataOutputStream(output);
  }

  /** @return the appropriate message writer for the supplied content type
   * @param contentType
   * @param charset
   *
   * @throws MessageException if no output writer is defined for the context type supplied */
  private MessageWriter getOutputWriter(final ContentType contentType, final CharEncoder charset) {
    if (contentType == ContentType.TEXT) {
      return new CharMessageWriter(charset);
    } else { //  BCD
      return new BCDMessageWriter(charset);
    }
  }

  /** @return a new message instance of the specified <code>type</code>, setting the field
   * values from properties of the <code>bean</code>, as named in the Message template
   * 'name' field
   * @param type (MTI) of ISO message to create
   * @param bean holding value to populate message fields
   * @throws IllegalArgumentException if the type supplied is not defined in this factory's schema */
  public Message createFromBean(final MTI type, final Object bean, final Map<Integer, Object> extraFields) {
    Preconditions.checkArgument(templates.containsKey(type), "Message not defined for MTI=" + type);
    final Map<Integer, Object> fieldValues = Maps.transformEntries(templates.get(type).getFields(), mapBeanValues(bean));
    return createByNumbers(type, new HashMap<Integer, Object>() {{
      putAll(fieldValues);
      putAll(extraFields);
    }});
  }

  /** @return the template registered against <code>type</code> */
  public MessageTemplate getTemplate(final MTI type) {
    Preconditions.checkArgument(canBuild(type), "Template for " + type + " not defined");
    return templates.get(type);
  }

  /** set <code>formatter</code> to handle formatting and parsing fields of <code>type</code> */
  public void addFormatter(final String type, final TypeFormatter<?> formatter) {
    formatters.setFormatter(type, formatter);
  }

  /** @return the formatter for message <code>type</code> */
  TypeFormatter<?> getFormatter(final String type) {
    Preconditions.checkArgument(formatters.hasFormatter(type),
        "No formatter registered for field type=[" + type + "] in " + formatters);
    return formatters.getFormatter(type);
  }

  /** @return a message parsed from the supplied <code>bytes</code> array (message data)
   * @throws java.text.ParseException
   * @throws java.io.IOException */
  public Message parse(final byte[] bytes) throws ParseException, IOException {
    return this.parse(new ByteArrayInputStream(bytes));
  }

  /** @return A message representation, parsed from the supplied input stream
   * @param input stream from which an ISO8583 message can be read
   *
   * @throws java.text.ParseException if the input message is not well-formed or does not
   *                                  conform to the message specification configured
   * @throws IllegalArgumentException if the input stream supplied is null
   * @throws java.io.IOException      when an error occurs reading from the input stream */
  public Message parse(final InputStream input) throws ParseException, IOException {
    Preconditions.checkNotNull(input, "Input stream cannot be null");
    final DataInputStream dis;
    if (!(input instanceof DataInputStream)) {
      dis = new DataInputStream(input);
    } else {
      dis = (DataInputStream) input;
    }
    return parser.parse(dis);
  }

  /** @return an ISO8583 message of the type requested, setting the field values
   * from the supplied parameter map, matching the names in the
   * <code>&lt;iso:message&gt;</code> configuration for this message type
   * @param type   MTI of the message to be created
   * @param params map of message fields, keyed by names
   * @throws IllegalArgumentException if the type is not defined in this factory's schema */
  public Message createByNames(final MTI type, final Map<String, Object> params) {
    Preconditions.checkArgument(templates.containsKey(type), "Message not defined for MTI=" + type);
    // convert the name map supplied to a field f keyed map

    final Map<String, Object> fields = Maps.filterEntries(params, Predicates.notNull());
    return createByNumbers(type, Maps.transformEntries(templates.get(type).getFields(), mapValuesByName(fields)));
  }

  /** @return an empty ISO8583 message of the type requested, from the configured
   * <code>&lt;iso:message&gt;</code> template
   * @param mti type of message
   * @param fields */
  public Message create(final MTI mti, final Map<Integer, Object> fields) {
    final MessageTemplate template = templates.get(mti);
    return Message.Builder()
        .template(template)
        .fields(fields).build();
  }

  /** @return transform <code>original</code> message to the <code>messageType</code> specified
   * (usually a response), setting its fields from the other fields ("move-corresponding" semantics),
   * and adding new fields that may be required in the new message
   * @param messageType    type of target message
   * @param original message to duplicate
   * @param extraFields required for new message */
  public Message transform(final MTI messageType, final Message original, final HashMap<String, Object> extraFields) {
    final MessageTemplate template = templates.get(messageType);
    final Map<Integer, Object> fieldValues = new HashMap<Integer, Object>() {{
      putAll(Maps.transformEntries(template.getFields(), mapValuesByName(extraFields)));
      putAll(Maps.transformValues(Maps.filterEntries(original.getFields(),
          new Predicate<Entry<Integer, Optional<Object>>>() {
            @Override
            public boolean apply(final Entry<Integer, Optional<Object>> input) {
              return template.isFieldPresent(input.getKey());
            }
          }), fromOptional()));
    }};
    return Message.Builder()
        .template(template)
        .header(original.getHeader())
        .fields(fieldValues)
        .build();
  }

  public static Function<Optional<Object>, Object> fromOptional() {
    return new Function<Optional<Object>, Object>() {
      @Override
      public Object apply(final Optional<Object> input) {
        return input.orNull();
      }
    };
  }

  private static EntryTransformer<Integer, FieldTemplate, Object> mapValuesByName(final Map<String, Object> params) {
    return new EntryTransformer<Integer, FieldTemplate, Object>() {
      @Override
      public Object transformEntry(final Integer number, final FieldTemplate field) {
        return params.get(field.getName());
      }
    };
  }

  private static EntryTransformer<Integer, FieldTemplate, Object> mapBeanValues(final Object bean) {
    return new EntryTransformer<Integer, FieldTemplate, Object>() {
      @Override
      public Object transformEntry(final Integer number, final FieldTemplate field) {
        try {
          return PropertyUtils.getProperty(bean, field.getName());
        } catch (final Exception e) {
          return null;
        }
      }
    };
  }

  /** @return byte array of message data, either text or binary depending upon the
   * content type specified in the iso:schema in the configuration
   * @param message ISO8583 message to convert to a byte array
   * @throws MessageException if an error occurred creating the byte representation of the message */
  public byte[] getMessageData(final Message message) {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      this.writeToStream(message, new DataOutputStream(baos));
      return baos.toByteArray();
    } catch (final IOException e) {
      throw new MessageException("Failed to translate message to byte stream", e);
    }
  }

  public void addTemplates(final List<MessageTemplate> messages) {
    for (final MessageTemplate message : messages) {
      addTemplate(message);
    }
  }

  public static Builder Builder() {
    return new Builder();
  }

  public boolean canBuild(final MTI messageType) {
    return templates.containsKey(messageType);
  }

  public static class Builder {
    private String id;
    private ContentType contentType;
    private BitmapType bitmapType;
    private String description;
    private String header;
    private CharEncoder charset;
    private AutoGeneratorFactory autogen;
    private final Map<String, TypeFormatter<?>> formatters = new HashMap<>();
    private final List<MessageTemplate> templates = new ArrayList<>();

    public Builder id(final String id) {
      this.id = id;
      return this;
    }

    public Builder contentType(final ContentType contentType) {
      this.contentType = contentType;
      return this;
    }

    public Builder bitmapType(final BitmapType bitmapType) {
      this.bitmapType = bitmapType;
      return this;
    }

    public Builder description(final String description) {
      this.description = description;
      return this;
    }

    public Builder header(final String header) {
      this.header = header;
      return this;
    }

    public Builder charset(final CharEncoder charset) {
      this.charset = charset;
      return this;
    }

    public Builder autogen(final AutoGeneratorFactory autogen) {
      this.autogen = autogen;
      return this;
    }

    public Builder addFormatter(final String type, final TypeFormatter<?> formatter) {
      formatters.put(type, formatter);
      return this;
    }

    public Builder addTemplate(final MessageTemplate message) {
      templates.add(message);
      return this;
    }

    public Builder templates(final List<MessageTemplate> messages) {
      templates.addAll(messages);
      return this;
    }

    public MessageFactory build() {
      Preconditions.checkNotNull(id);
      Preconditions.checkNotNull(contentType);
      Preconditions.checkNotNull(bitmapType);
      final MessageFactory result = new MessageFactory();
      result.setId(id);
      result.setContentType(contentType);
      result.setBitmapType(bitmapType);
      if (description != null) {
        result.setDescription(description);
      }
      if (header != null) {
        result.setHeader(header);
      }
      if (charset != null) {
        result.setCharset(charset);
      }
      if (autogen != null) {
        result.setAutoGeneratorFactory(autogen);
      }
      result.initialize(formatters, templates);
      return result;
    }
  }


}
