package org.nulleins.formats.iso8583;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.nulleins.formats.iso8583.io.BCDMessageReader;
import org.nulleins.formats.iso8583.io.CharMessageReader;
import org.nulleins.formats.iso8583.io.MessageReader;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;


/**
 * Utility class used by message factory to parse an ISO8583 message
 * @author phillipsr
 */
public class MessageParser {
  private final Map<MTI, MessageTemplate> messages;
  private final String header;
  private final ContentType contentType;
  private final CharEncoder charset;
  private final BitmapType bitmapType;

  private MessageParser(
      final String header, final Map<MTI, MessageTemplate> messages,
      final ContentType contentType, final CharEncoder charset, final BitmapType bitmapType) {
    this.header = header;
    this.messages = messages;
    this.contentType = contentType;
    this.charset = charset;
    this.bitmapType = bitmapType;
  }

  public static MessageParser create(
      final String header, final Map<MTI, MessageTemplate> messages,
      final ContentType contentType, final CharEncoder charset, final BitmapType bitmapType) {
    return new MessageParser(header, messages, contentType, charset, bitmapType);
  }

  private MessageReader getMessageReader() {
    if (contentType == ContentType.TEXT) {
      return new CharMessageReader(charset);
    } else { // contentType == ContentType.BCD
      return new BCDMessageReader(charset);
    }
  }

  /**
   * read from the supplied input stream, identifying the message type and parsing the message
   * body
   * @param input stream from which an ISO8583 message can be read
   * @return a message instance representing the message received
   * @throws IOException              on errors reading from the input stream
   * @throws IllegalArgumentException if the supplied input stream is null
   */
  public Message parse(final DataInputStream input) throws IOException {
    Preconditions.checkNotNull(input, "Input stream for ISO8583 message cannot be null");
    final MessageReader reader = getMessageReader();
    final String header = validateHeader(input, reader);
    final MTI mti = reader.readMTI(input);
    final MessageTemplate template = validateMessageTemplate(mti);

    return Message.Builder()
      .template(template)
      .header(header)
      .fields(Maps.toMap(reader.readBitmap(bitmapType, input), parseMessage(input, reader, template)))
      .build();
  }

  /** @return a function that can parse a message into a map of field numbers to values */
  private static Function<Integer, Object> parseMessage(
      final DataInputStream input, final MessageReader reader, final MessageTemplate template) {
    return new Function<Integer, Object>() {
      @Override
      public Object apply(final Integer fieldNum) {
        final FieldTemplate field = template.getFields().get(fieldNum);
        try {
          System.out.print("Read field ("+fieldNum+"): " + field);
          final Object result = field.parse(reader.readField(field, input));
          System.out.println("="+result);
          return result;
          //return field.parse(reader.readField(field, input));
        } catch ( Throwable t) {
          throw Throwables.propagate(t);
        }
      }
    };
  }

  /** @return the message template fot the specified message <code>type</code>
    * @throws MessageException if no template is defined for <code>type</code> */
  private MessageTemplate validateMessageTemplate(final MTI type) {
    final MessageTemplate template = messages.get(type);
    if (template == null) {
      throw new MessageException("Message type [" + type + "] not defined in this message set");
    }
    return template;
  }

  /** @return the header field, or empty String if not required
   * @throws MessageException if the header is required but not present */
  private String validateHeader(final DataInputStream input, final MessageReader reader) throws IOException {
    final int headerLen = header != null ? header.length() : 0;
    if (headerLen > 0) {
      final String msgHeader = reader.readHeader(headerLen, input);
      if (!msgHeader.equals(header)) {
        throw new MessageException("Message should start with header: [" + header + "]");
      }
    }
    return headerLen > 0 ? header : "";
  }

}
