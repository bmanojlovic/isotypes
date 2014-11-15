package org.nulleins.formats.iso8583.types;

import com.google.common.base.Preconditions;

import java.util.BitSet;

/** helper methods to convert bitsets to- and from hex format, and to
  * convert to- and from appropriately ordered byte arrays, consistent
  * with the ISO8583 bitmap usage (i.e., big-endian)
  * @author phillipsr */
public class BitsetUtil {

  /** @return a bitmap equivalent of <code>hex</code>, thestring representation of
    * an ISO8583 hexadecimal bitmap
    * @throws IllegalArgumentException if the hex string is null, empty or not even-sized */
  static BitSet hex2Bitset(final String hex) {
    Preconditions.checkNotNull(hex,"Hex string must be non-null");
    Preconditions.checkArgument(!(hex.length() == 0 || hex.length() % 2 != 0),
        "Hex string must be even-sized (length=" + hex.length() + ")");
    final BitSet result = new BitSet();
    final int length = hex.length();
    int bytenum = (length / 2) - 1;
    for (int index = length; index >= 2; index -= 2) {
      final int bytevalue = Integer.valueOf(hex.substring(index - 2, index), 16);
      for (int bit = 0, mask = 0x80; mask >= 0x01; bit++, mask /= 2) {
        if ((mask & bytevalue) == mask) {
          result.set((bytenum * 8) + bit);
        }
      }
      bytenum--;
    }
    return result;
  }

  /** @return hexadecimal string version of the <code>bitset</code>supplied, as required
    * by ISO8583 bitmap (hex) format, of the specified <code>minLength</code>
    * (right-padded with "00" if required) */
  static String bitset2Hex(final BitSet bitset, final int minLength) {
    final StringBuilder result = new StringBuilder();
    for (int bytenum = 0; bytenum < minLength / 2; bytenum++) {
      byte v = 0;
      for (int bit = 0, mask = 0x80; mask >= 0x01; bit++, mask /= 2) {
        if (bitset.get((bytenum * 8) + bit)) {
          v |= mask;
        }
      }
      result.append(String.format("%02X", v));
    }

    return result.toString();
  }

  /** @return a string representing the supplied <code>bitset</code>, as a String of zero and ones
    * @throws IllegalArgumentException if the bitset is null */
  static String bitset2bitstring(final BitSet bitset, final int length) {
    Preconditions.checkNotNull(bitset, "Bitset must not be null");
    final StringBuilder result = new StringBuilder();
    for (int i = 0; i < length; i++) {
      result.append(bitset.get(i) ? "1" : "0");
    }
    return result.toString();
  }

  /** @return a BitSet initialized from a byte array representation of <code>binBitmap</code> */
  static BitSet bin2Bitset(final byte[] binBitmap) {
    final BitSet result = new BitSet();
    for (int bytenum = 0; bytenum < binBitmap.length; bytenum++) {
      for (int bit = 0, mask = 0x80; mask >= 0x01; bit++, mask /= 2) {
        if ((mask & binBitmap[bytenum]) == mask) {
          result.set((bytenum * 8) + bit);
        }
      }
    }

    return result;
  }

  /** @return a byte array representation of the supplied <code>BitSet</code>
    * @throws IllegalArgumentException if the bitset is null */
  static byte[] bitset2bin(final BitSet bitSet, final int length) {
    Preconditions.checkNotNull(bitSet,"bitSet must be non-null");

    final byte[] result = new byte[length];
    for (int bytenum = length - 1; bytenum >= 0; bytenum--) {
      result[bytenum] = 0;
      for (int bit = 0, mask = 0x80; mask >= 0x01; bit++, mask /= 2) {
        if (bitSet.get((bytenum * 8) + bit)) {
          result[bytenum] |= mask;
        }
      }
    }
    return result;
  }

  private BitsetUtil() {}

}
