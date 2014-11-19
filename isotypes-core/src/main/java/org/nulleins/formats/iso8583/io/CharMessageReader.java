package org.nulleins.formats.iso8583.io;

import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.DataInputStream;
import java.io.IOException;


/** MessageReader that reads numeric fields as text (in the defined Charset)
 * @author phillipsr */
public class CharMessageReader extends MessageReader {
  /** Instantiate a character message reader
    * @param charset character set that character data is expected to be encoded with */
  public CharMessageReader(final CharEncoder charset) {
    super.charCodec = charset;
  }

  /** {@inheritDoc} */
  @Override
  public MTI readMTI(final DataInputStream input) throws IOException {
    final byte[] data = readBytes(4, input);
    return MTI.create(charCodec.getString(data));
  }

  /** {@inheritDoc} */
  @Override
  public byte[] readField(final FieldTemplate field, final DataInputStream input) throws IOException {
    final int length;
    if (field.getDimension().getType() == Dimension.Type.VARIABLE) {
      final byte[] data = readBytes(field.getDimension().getVSize(), input);
      length = Integer.parseInt(charCodec.getString(data));
    } else {
      length = field.getDimension().getLength();
    }
    return readBytes(length, input);
  }

}
