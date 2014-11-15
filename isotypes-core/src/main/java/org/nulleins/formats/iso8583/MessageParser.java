package org.nulleins.formats.iso8583;

import com.google.common.base.Preconditions;
import org.nulleins.formats.iso8583.io.BCDMessageReader;
import org.nulleins.formats.iso8583.io.CharMessageReader;
import org.nulleins.formats.iso8583.io.MessageReader;
import org.nulleins.formats.iso8583.types.Bitmap;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.DataInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
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

  public static MessageParser create (
      final String header, final Map<MTI, MessageTemplate> messages,
      final ContentType contentType, final CharEncoder charset, final BitmapType bitmapType) {
    return new MessageParser(header,messages,contentType,charset,bitmapType);
  }

  private MessageReader getMessageReader() {
    switch (contentType) {
      case TEXT:
        return new CharMessageReader(charset);
      case BCD:
        return new BCDMessageReader(charset);
    }
    return null;
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
    Preconditions.checkNotNull(input,"Input stream for ISO8583 message cannot be null");
    final MessageReader reader = getMessageReader();

    // if the header field is required, check that it is present
    final int headerLen = header != null ? header.length() : 0;
    if (headerLen > 0) {
      final String msgHeader = reader.readHeader(headerLen, input);
      if (!msgHeader.equals(header)) {
        throw new MessageException("Message should start with header: [" + header + "]");
      }
    }

    // read the message type (MTI)
    final MTI type = reader.readMTI(input);
    final MessageTemplate template = messages.get(type);
    if (template == null) {
      throw new MessageException("Message type [" + type + "] not defined in this message set");
    }

    // create resulting message
    final Message result = new Message(template.getMessageTypeIndicator(), headerLen > 0 ? header : "");

    final Bitmap bitmap = reader.readBitmap(bitmapType, input);

    // iterate across all possible fields, parsing if present:
    final Map<Integer, Object> fields = new HashMap<>();
    for ( int fieldNum = 2; fieldNum <= 192; fieldNum++) {
      if (!bitmap.isFieldPresent(fieldNum)) {
        continue;
      }
      final FieldTemplate field = template.getFields().get(fieldNum);
      final byte[] fieldData = reader.readField(field, input);
      try {
        final Object value = field.parse(fieldData);
        fields.put(field.getNumber(), value);
      } catch (final ParseException e) {
        throw new MessageException("Failed to parse field: " + field.toString(), e);
      }
    }
    result.setFields(fields);

    return result;
  }

}
