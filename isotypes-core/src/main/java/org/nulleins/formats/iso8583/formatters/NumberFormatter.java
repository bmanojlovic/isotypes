package org.nulleins.formats.iso8583.formatters;

import com.google.common.base.Preconditions;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.FieldType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Pattern;


/**
 * Formatter that can format and parse ISO8583 numeric formats, signed or unsigned
 * @author phillipsr
 */
public class NumberFormatter
    extends TypeFormatter<BigInteger> {
  private static final Pattern NumberMatcher = Pattern.compile("[-0-9]+");

  public NumberFormatter(final CharEncoder charset) {
    setCharset(charset);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BigInteger parse(final String type, final Dimension dimension, final int length, final byte[] data)
      throws ParseException {
    try {
      if (FieldType.NUMSIGNED.equalsIgnoreCase(type)) {
        return parseXNField(type, length, data);
      }
      return new BigInteger(decode(data));
    } catch (final Exception e) {
      throw new ParseException("Bad f format " + e.getMessage()
          + " for type=" + type + " [" + new String(data) + "]", length);
    }
  }

  private BigInteger parseXNField(final String type, final int length, final byte[] data)
      throws ParseException {
    final String sign = decode(Arrays.copyOfRange(data, 0, 1));
    final char signC = sign.toUpperCase().charAt(0);
    if (signC != 'C' && signC != 'D') {
      throw new ParseException("Bad f format for " + type
          + ": must start with C or D (field data=[" + decode(data) + "])", length);
    }
    final String value = decode(Arrays.copyOfRange(data, 1, data.length));
    BigInteger result = new BigInteger(value);
    if (signC == 'D') {
      result = result.negate();
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException if the data is null or not a valid numeric value
   */
  @Override
  public byte[] format(final String type, final Object data, final Dimension dimension) {
    Preconditions.checkNotNull(data,"Numeric value cannot be null");

    BigInteger value = getNumericValue(data);
    int length = dimension.getLength();
    boolean negative = false;
    boolean isSigned = false;
    if (FieldType.NUMSIGNED.equalsIgnoreCase(type)) {
      isSigned = true;
      negative = value.compareTo(BigInteger.ZERO) == -1;
      value = value.abs();
      length--;
    }

    String result = value.toString();
    if (result.length() > length) {
      throw new IllegalArgumentException(
          "Field data length (" + (!isSigned ? result.length() : (result.length() + 1))
              + ") exceeds field maximum (" + dimension.getLength()
              + ") [data=" + data + ", type=" + type + ", dim=" + dimension + "]");
    }
    if (dimension.getType() == Dimension.Type.FIXED) {
      result = zeroPad(result, length);
    }
    if (FieldType.NUMSIGNED.equalsIgnoreCase(type)) {
      result = (negative ? "D" : "C") + result;
    }
    return result.getBytes();
  }

  private String zeroPad(final String result, final int length) {
    if (result.length() >= length) {
      return result;
    }
    return String.format("%" + length + "." + length + "s", result).replaceAll(" ", "0");
  }

  /**
   * Answer with a numeric representation of the object supplied
   * @param data to be interpreted as a numeric value
   * @return data converted to a BigInteger value
   * @throws IllegalArgumentException if the data is null or not a valid numeric value
   */
  private BigInteger getNumericValue(final Object data) {
    if (data == null) {
      throw new IllegalArgumentException("Cannot convert <null> to numeric");
    }
    if (data instanceof BigInteger) {
      return (BigInteger) data;
    }
    if (data instanceof BigDecimal) {
      return BigInteger.valueOf(((BigDecimal) data).longValue());
    } else if (data instanceof Integer || data instanceof Long) {
      return BigInteger.valueOf(Long.valueOf(data + ""));
    } else if (data instanceof Float || data instanceof Double) {
      return BigInteger.valueOf(Double.doubleToLongBits((Double) data));
    } else {
      final String value;
      if (data instanceof byte[]) {
        value = new String((byte[]) data).trim();
      } else {
        value = data.toString().trim();
      }
      if (NumberMatcher.matcher(value).matches()) {
        return new BigInteger(value);
      }
    }
    throw new IllegalArgumentException("Could not convert " + data.getClass().getSimpleName() + " to numeric");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(final Object value, final String type, final Dimension dimension) {
    final BigInteger bi;
    try {
      bi = getNumericValue(value);
    } catch (final IllegalArgumentException e) {
      return false;
    }
    return bi.toString().length() <= dimension.getLength();
  }

}
