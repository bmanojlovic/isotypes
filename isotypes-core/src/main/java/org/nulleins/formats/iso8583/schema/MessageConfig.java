package org.nulleins.formats.iso8583.schema;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.MessageTemplate;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Build a MessageFactory from a supplied Json/Yaml/HOCON configuration */
public final class MessageConfig {
  private final MessageFactory factory;

  private MessageConfig(final String configFile) {
    factory = buildFactory(ConfigFactory.load(configFile));
  }

  private MessageConfig(final InputStream configStream) {
    factory = buildFactory(ConfigFactory.parseReader(new InputStreamReader(configStream)));
  }

  private MessageConfig(final URL configUrl) {
    factory = buildFactory(ConfigFactory.parseURL(configUrl));
  }

  public static MessageFactory configure(final String configFile) {
    return new MessageConfig(configFile).factory;
  }

  public static MessageFactory configure(final InputStream configUrl) {
    return new MessageConfig(configUrl).factory;
  }

  public static MessageFactory configure(final URL bais) {
    return new MessageConfig(bais).factory;
  }

  /** @return a message factory able to build the message types specified by <code>config</code> */
  private MessageFactory buildFactory(final Config config) {
    final Config schema = config.getConfig("schema");
    final String id = schema.getString("id");
    final String header = schema.getString("header");
    final ContentType contentType = ContentType.valueOf(schema.getString("contentType").toUpperCase());
    final String description = schema.getString("description");
    final BitmapType bitmapType = BitmapType.valueOf(schema.getString("bitmapType").toUpperCase());
    schema.getString("contentType");
    Optional<CharEncoder> charset = Optional.absent();
    if (schema.hasPath("charset")) {
      charset = Optional.of(new CharEncoder(schema.getString("charset")));
    }
    schema.getString("charset");
    final List<MessageTemplate> messages = FluentIterable
        .from(schema.getConfigList("messages"))
        .transform(buildMessageTemplate(header, bitmapType)).toList();
    final MessageFactory result = MessageFactory.Builder()
        .id(id)
        .header(header)
        .charset(charset.orNull())
        .description(description)
        .bitmapType(bitmapType)
        .contentType(contentType)
        .templates(messages)
        .build();
    return result;
  }

  /** @return a function that can build a message template from its configuration, using the
   * supplied <code>header</code> and <code>bitmapType</code> */
  private Function<Config, MessageTemplate> buildMessageTemplate(final String header, final BitmapType bitmapType) {
    return new Function<Config, MessageTemplate>() {
      @Override
      public MessageTemplate apply(final Config input) {
        final MTI mti = MTI.create(input.getString("type"));
        final String name = input.hasPath("name") ? input.getString("name") : "";
        final Map<Integer, FieldTemplate> fields = getFields(input.getObject("fields").unwrapped());
        return  MessageTemplate.Builder()
          .name(name)
          .header(header)
          .type(mti)
          .fieldmap(fields)
          .build();
      }
    };
  }

  /** @return a map of field numbers to field templates, constructed from the supplied <code>fieldList</code> */
  private Map<Integer, FieldTemplate> getFields(final Map<String, Object> fieldList) {
    final Map<Integer, FieldTemplate> result = new HashMap<>();
    for (final Map.Entry<String, Object> entry : fieldList.entrySet()) {
      if (!Map.class.isAssignableFrom(entry.getValue().getClass())) {
        throw new ConfigException.Generic("Fields must be a map of field numbers to field definitions");
      }
      final int fieldNumber = Integer.valueOf(entry.getKey());
      @SuppressWarnings("unchecked")
      final Map<String, Object> value = (Map<String, Object>) entry.getValue();
      result.put(fieldNumber, getFieldDefinition(fieldNumber, value));
    }
    return result;
  }

  /** @return a field template configured from the supplied <code>fieldConfig</code> map */
  private FieldTemplate getFieldDefinition(final int fieldNumber, final Map<String, Object> fieldConfig) {
    return FieldTemplate.localBuilder().get()
        .f(fieldNumber)
        .name((String) fieldConfig.get("name"))
        .desc((String) fieldConfig.get("desc"))
        .type((String) fieldConfig.get("type"))
        .dim((String) fieldConfig.get("dim"))
        .build();
  }

}
