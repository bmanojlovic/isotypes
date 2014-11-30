package org.nulleins.formats.iso8583.types;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * Utilities to manipulate (Packed) Binary Coded Decimal values, as no standard
 * third-party library found (mail me if you know of one...)
 * @author Converted from an old C library (author unknown)
 */
public class BCD {
  /** regex to validate values passed in as strings */
  private static final Pattern NumberMatcher = Pattern.compile("[0-9]*");

  /** @return a byte array being the BCD representation of the numeric <code>value </code>string supplied
    * @throws IllegalArgumentException if the supplied value is not a valid numeric string */
  public static byte[] valueOf(final String value) {
    Preconditions.checkNotNull(value,"Cannot convert <null> to BCD");
    final String candidate = value.trim();
    Preconditions.checkArgument (NumberMatcher.matcher(candidate).matches(),"Can only convert strings of digits to BCD");

    byte[] result = valueOf(new BigInteger(candidate));
    final int vlength = candidate.length() / 2;
    while (result.length < vlength) {
      result = ArrayUtils.add(result, 0, (byte) 0);
    }
    return result;
  }

  /** @return a byte array being the BCD representation of the long <code>value</code>supplied */
  public static byte[] valueOf(final long value) {
    final int nbDigits = (int) (Math.log10(value) + 1);
    final int bytes = nbDigits % 2 == 0 ? nbDigits / 2 : (nbDigits + 1) / 2;
    final byte[] result = new byte[bytes];
    long candidate = value;
    for (int i = 0; i < nbDigits; i++) {
      byte register = (byte) (candidate % 10);
      candidate /= 10;
      if ((i % 2 == 0) || (i == nbDigits - 1 && (nbDigits % 2 != 0))) {
        result[i / 2] = register;
        continue;
      }
      register = (byte) (register << 4);
      result[i / 2] |= register;
    }

    swapOrder(bytes, result);
    return result;
  }

  /**
   * Answer with a byte array being the BCD representation of the BigInteger supplied
   * @param value
   * @return
   * @throws IllegalArgumentException if the value is null
   */
  public static byte[] valueOf(final BigInteger value) {
    Preconditions.checkNotNull(value,"Cannot convert <null> to BCD");
    BigInteger candidate = value;
    final int nbDigits = (int) (Math.log10(candidate.doubleValue()) + 1);
    final int bytes = nbDigits % 2 == 0 ? nbDigits / 2 : (nbDigits + 1) / 2;
    final byte[] result = new byte[bytes];

    for (int i = 0; i < nbDigits; i++) {
      byte register = candidate.mod(BigInteger.TEN).byteValue();
      candidate = candidate.divide(BigInteger.TEN);
      if ((i % 2 == 0) || (i == nbDigits - 1 && (nbDigits % 2 != 0))) {
        result[i / 2] = register;
        continue;
      }
      register = (byte) (register << 4);
      result[i / 2] |= register;
    }

    swapOrder(bytes, result);
    return result;
  }

  private static void swapOrder(final int bytes, final byte[] result) {
    for (int i = 0; i < bytes / 2; i++) {
      final byte register = result[i];
      result[i] = result[bytes - i - 1];
      result[bytes - i - 1] = register;
    }
  }

  /**
   * Answer with a string representation of the BCD
   * (byte) value supplied
   * @param value
   * @return
   */
  public static String toString(final byte value) {
    final byte[] nibbles = new byte[2];
    nibbles[1] = (byte) ((value & 0x0f));
    byte b = (byte) (value & 0xf0);
    b >>>= 4;
    nibbles[0] = (byte) ((b & 0x0f));
    return "" + nibbles[0] + nibbles[1];
  }

  public static String toString(final byte[] bcd) {
    final StringBuilder result = new StringBuilder();
    for (final byte b : bcd) {
      result.append(toString(b));
    }

    return result.toString();
  }

}