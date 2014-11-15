package org.nulleins.formats.iso8583.formatters;

import com.google.common.base.Preconditions;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;


/**
 * Formatter that can format and parse ISO8583 time format field value
 * @author phillipsr
 */
public class TimeFormatter
    extends TypeFormatter<LocalTime> {
  private final static DateTimeFormatter Formatter = DateTimeFormat.forPattern("HHmmss");

  public TimeFormatter(final CharEncoder charset) {
    setCharset(charset);
  }

  /**
   * {@inheritDoc}
   * @throws ParseException if the data cannot be parsed as a valid time value
   */
  @Override
  public LocalTime parse(final String type, final Dimension dimension, final int length, final byte[] data)
      throws ParseException {
    try {
      return Formatter.parseLocalTime(decode(data));
    } catch (final Exception e) {
      throw new ParseException("Cannot parse time for dim: '" + type + ":" + length + "'", length);
    }
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException if the data is null
   */
  @Override
  public byte[] format(final String type, final Object data, final Dimension dimension) {
    Preconditions.checkNotNull(data,"Time value cannot be null");
    return Formatter.print(getTime(data)).getBytes();
  }

  /**
   * Answer with a time representation of the data object supplied
   * @param data to convert to a time
   * @return LocalTime set from data supplied
   * @throws IllegalArgumentException if the data is null or not a valid time value
   */
  private LocalTime getTime(final Object data) {
    if (data instanceof LocalTime) {
      return (LocalTime) data;
    } else if (data instanceof java.sql.Date) {
      return new LocalTime(((java.sql.Date) data).getTime());
    } else if (data instanceof java.util.Date) {
      return new LocalTime(((java.util.Date) data).getTime());
    }
    final String timeString = String.format("%6.6s", data.toString().trim()).replaceAll(" ", "0");
    return Formatter.parseLocalTime(timeString);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(final Object value, final String type, final Dimension dimension) {
    if (value == null) {
      return false;
    }
    if (value instanceof java.util.Date || value instanceof LocalTime) {
      return true;
    }
    final String timeValue = value.toString().trim();
    if (timeValue.length() != 6) {
      return false;
    }
    try {
      Formatter.parseLocalTime(timeValue);
    } catch (final IllegalArgumentException e) {
      return false;
    }
    return true;
  }

}
