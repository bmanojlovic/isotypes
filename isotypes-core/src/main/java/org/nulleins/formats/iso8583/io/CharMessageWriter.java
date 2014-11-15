package org.nulleins.formats.iso8583.io;

import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.DataOutputStream;
import java.io.IOException;


/**
 * MessageWriter that encodes numeric fields as text (in defined Charset)
 * @author phillipsr
 */
public class CharMessageWriter
    extends MessageWriter {
  /**
   * Instantiate a character message writer that encodes the character
   * data in the specified character set
   * @param codec to be used when writing character data
   */
  public CharMessageWriter(final CharEncoder codec) {
    super.charCodec = codec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void appendMTI(final MTI type, final DataOutputStream output)
      throws IOException {
    write(type.toString(), output);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void appendField(final FieldTemplate field, final Object data, final DataOutputStream output)
      throws IOException {
    final byte[] fieldValue = charCodec.getBytes(field.format(data));
    final Dimension dim = field.getDimension();
    if (dim.getType() == Dimension.Type.VARIABLE) {
      final String vsize = String.format("%0" + dim.getVSize() + "d", fieldValue.length);
      output.write(charCodec.getBytes(vsize));
    }
    write(fieldValue, output);
  }

}
