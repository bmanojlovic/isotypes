package org.nulleins.formats.iso8583.formatters;

import com.google.common.base.Preconditions;
import org.nulleins.formats.iso8583.MessageException;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.FieldType;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Formatter that can format and parse alpha field values
 * (includes alphanumeric, alpha+symbol, etc., e.g., non-numeric fields)
 * @author phillipsr
 */
public class AlphaFormatter extends TypeFormatter<String> {
  public AlphaFormatter(final CharEncoder charset) {
    setCharset(charset);
  }

  /**
   * {@inheritDoc}
   * @param type variant of the alpha type specified for the field
   * @throws IllegalArgumentException if the data is null or invalid as an alpha string
   * @throws ParseException           if data cannot be translated to the appropriate charset
   */
  @Override
  public String parse(final String type, final Dimension dimension, final int position, final byte[] data)
      throws ParseException {
    final String result;
    try {
      result = decode(data).trim();
    } catch (final Exception e) {
      throw new ParseException(
          "Decoding error " + e.getMessage() + " for " + type + " field: " + Arrays.toString(data), position);
    }

    if (!isValid(result, type, dimension)) {
      throw new ParseException("Invalid data parsed for field (" + type + ") value=[" + result + "]", position);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] format(final String type, final Object data, final Dimension dimension) {
    Preconditions.checkNotNull(data,"Alpha values cannot be null");
    final String value = data instanceof byte[] ? new String((byte[]) data) : data.toString();

    if (!isValid(value, type, dimension)) {
      throw new IllegalArgumentException("Cannot format invalid value for [" + type + "] field: '"
          + value + "', is-a " + value.getClass().getSimpleName());
    }

    if (dimension.getType() == Dimension.Type.FIXED) {
      final int length = dimension.getLength();
      if (value.length() > length) {
        throw new MessageException("Fixed field data length ("
            + value.length() + ") exceeds field maximum (" + dimension.getLength() + "): data=[" + value + "]");
      }
      // for fixed width fieldlist, pad right with spaces
      return String.format("%-" + length + "." + length + "s", value).getBytes();
    }
    // Variable field: dim length is the maximum length:
    if (value.length() > dimension.getLength()) {
      throw new MessageException("Variable field data length ("
          + value.length() + ") exceeds field maximum (" + dimension.getLength() + ")");
    }
    return value.getBytes();
  }

  /* set of pattern matchers for the various alpha-based type fieldlist */
  private static final Map<String, Pattern> Validators = new HashMap<String, Pattern>(3) {{
    put(FieldType.ALPHA, Pattern.compile("[a-zA-Z]*"));      // zero or more alphabetic
    put(FieldType.ALPHANUM, Pattern.compile("[a-zA-Z0-9]*"));   // zero or more alphabetic or digit
    put(FieldType.ALPHANUMPAD, Pattern.compile("[a-zA-Z 0-9]*"));  // zero or more alphabetic, digit or space
    put(FieldType.ALPHASYMBOL, Pattern.compile("[ -~&&[^0-9]]*")); // zero or more alphabetic or symbol
    put(FieldType.ALPHANUMSYMBOL, Pattern.compile("[ -~]*"));      // zero or more any character
    put(FieldType.NUMSYMBOL, Pattern.compile("[ -~&&[^a-zA-Z]]*")); // zero or more symbol
    put(FieldType.TRACKDATA, Pattern.compile("[ -~]*"));        // zero or more any character
  }};

  /**
   * {@inheritDoc}
   * <p/>checks the string representation of <code>value</code> against the
   * pattern matcher for the supplied <code>type</code>
   */
  @Override
  public boolean isValid(final Object value, final String type, final Dimension dim) {
    return value != null && Validators.get(type).matcher(value.toString().trim()).matches();
  }

}
