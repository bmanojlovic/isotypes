package org.nulleins.formats.iso8583.formatters;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.nulleins.formats.iso8583.types.Dimension;

import java.text.ParseException;

/**
 * @author phillipsr
 */
public class NIBBSDateFormatter
    extends TypeFormatter<DateTime> {
  private final static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyMMddHHmmss");

  /**
   * {@inheritDoc}
   * @throws ParseException if the supplied data cannot be parsed as a date value
   */
  @Override
  public DateTime parse(final String type, final Dimension dim, final int length, final byte[] data)
      throws ParseException {
    try {
      return formatter.parseDateTime(decode(data));
    } catch (final Exception e) {
      throw new ParseException(
          "Cannot parse NIBBS date type: data=" + data, length);
    }
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException if the data is null or not a valid date value
   */
  @Override
  public byte[] format(final String type, final Object data, final Dimension dimension) {
    if (data == null) {
      throw new IllegalArgumentException("Date value cannot be null");
    }
    final DateTime dateTime = DateFormatter.getDateValue(data);
    if (dateTime == null) {
      throw new IllegalArgumentException("Invalid data [" + data
          + "] expected Date, got a " + data.getClass().getCanonicalName());
    }
    return formatter.print(dateTime).getBytes();
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(final Object value, final String type, final Dimension dim) {
    if (value == null) {
      return false;
    }
    if (value instanceof java.util.Date || value instanceof DateTime) {
      return true;
    }
    final String dateValue = value.toString().trim();
    if (dateValue.length() != 4 && dateValue.length() != 10) {
      return false;
    }
    try {
      formatter.parseDateTime(dateValue);
    } catch (final IllegalArgumentException e) {
      return false;
    }

    return true;
  }
}
