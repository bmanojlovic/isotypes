package org.nulleins.formats.iso8583.io;

import org.apache.commons.lang3.ArrayUtils;
import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.types.BCD;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.FieldType;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.DataOutputStream;
import java.io.IOException;


/**
 * MessageWriter that encodes numeric fields as packed BCD values
 * @author phillipsr
 */
public class BCDMessageWriter
    extends MessageWriter {
  /**
   * Instantiate a message writer that encodes numeric values as BCD, and
   * character data in the specified character set
   * @param charset to be used when writing character data
   */
  public BCDMessageWriter(final CharEncoder charset) {
    super.charCodec = charset;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void appendMTI(final MTI type, final DataOutputStream output)
      throws IOException {
    final int mti = type.intValue();
    write(new byte[]{(byte) (mti >> 8), (byte) mti}, output);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void appendField(final FieldTemplate field, final Object data, final DataOutputStream output)
      throws IOException {
    final Dimension dim = field.getDimension();
    final byte[] inputValue = field.format(data);
    String fieldValue = charCodec.getString(inputValue);
    if (dim.getType() == Dimension.Type.VARIABLE) {
      write(getVarLengthSpecifier(dim.getVSize(), fieldValue), output);
    }
    if (field.getType().equals(FieldType.NUMSIGNED)) {
      write((byte) (inputValue[0] - 0x37), output); // 'C' => 0xC and 'D' => 0xD
      fieldValue = fieldValue.substring(1);
      //$FALL-THROUGH$
    }
    final byte[] encodedValue;
    if (field.getType().equals(FieldType.NUMERIC) ||
        field.getType().equals(FieldType.NUMSIGNED) ||
        field.getType().equals(FieldType.DATE) ||
        field.getType().equals(FieldType.TIME) ||
        field.getType().equals(FieldType.EXDATE)) {
      encodedValue = BCD.valueOf(fieldValue);
    } else {
      encodedValue = fieldValue.getBytes();
    }
    write(encodedValue, output);
  }

  /**
   * returns a BCD byte array that specifies the length of the
   * supplied value for a variable-width field to the output stream
   * @param vsize size of the variable width specifier (1, 2 or 3)
   * @param value the value to be stored in the field
   * @return field size as a byte array
   */
  private byte[] getVarLengthSpecifier(final int vsize, final String value) {
    int length = value.length();
    if (length % 2 != 0) { // is odd
      length++;
    }
    byte[] vspecifier = BCD.valueOf(length);
    if (vsize > 2 && vspecifier.length < 2) { // LLLVAR: needs filler to make 2-byte length specifier
      vspecifier = ArrayUtils.add(vspecifier, 0, (byte) 0);
    }
    return vspecifier;
  }

}
