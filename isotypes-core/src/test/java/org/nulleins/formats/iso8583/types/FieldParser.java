package org.nulleins.formats.iso8583.types;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Arrays;

/**
 * Parser to read byte data from ISO8583 field data item
 * @author phillipsr
 */
public class FieldParser {
  /**
   * @param data
   * @param pos
   * @param size
   * @param dimension
   * @return
   * @throws ParseException
   */
  public static byte[] getBytes(final byte[] data, final ParsePosition pos, final int length)
      throws ParseException {
    final int start = pos.getIndex();
    if (start + length > data.length) {
      pos.setErrorIndex(start);
      throw new ParseException("Data exhausted", start);
    }
    final byte[] result = Arrays.copyOfRange(data, start, start + length);
    pos.setIndex(start + length);
    return result;
  }

  /**
   * Read the Hollerith field length value from
   * the data
   * @param data
   * @param pos
   * @param vSize of the hollerithian specifier
   * @return
   */
  public static int getLength(final byte[] data, final ParsePosition pos, final int vSize) {
    final int start = pos.getIndex();
    final String result = new String(Arrays.copyOfRange(data, start, start + vSize));
    pos.setIndex(start + vSize);
    return Integer.parseInt(result);
  }

}
