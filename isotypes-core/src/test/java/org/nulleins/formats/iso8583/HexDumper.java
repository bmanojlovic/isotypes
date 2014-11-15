package org.nulleins.formats.iso8583;


/**
 * @author phillipsr
 */
public class HexDumper {

  public static String getHexDump(final byte[] data) {
    final StringBuilder result = new StringBuilder();
    for (int i = 0; i < data.length; i++) {
      result.append(formatHex(data[i], i));
    }
    return result.toString();
  }

  private static String formatHex(final byte value, final int position) {
    final String sep;
    if (position > 0) {
      if (position % 16 == 0) {
        sep="\n";
      } else {
        sep=", ";
      }
    } else {
      sep = "";
    }
    return String.format("%s0x%02x", sep, value);
  }

  private HexDumper() {}

}
