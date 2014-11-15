package org.nulleins.formats.iso8583.formatters;

import com.google.common.base.Preconditions;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.FieldType;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;


/**
 * Formatter that can format and parse ISO8583 date field formats
 * @author phillipsr
 */
public class DateFormatter extends TypeFormatter<DateTime> {

  private final static Map<String, DateTimeFormatter> Formatters
      = new HashMap<String, DateTimeFormatter>(3) {{
    put(FieldType.DATE + ":10", DateTimeFormat.forPattern("MMddHHmmss"));
    put(FieldType.DATE + ":4", DateTimeFormat.forPattern("MMdd"));
    put(FieldType.EXDATE + ":4", DateTimeFormat.forPattern("yyMM"));
  }};

  public DateFormatter(final CharEncoder charset) {
    setCharset(charset);
  }

  /**
   * {@inheritDoc}
   * @throws ParseException if the supplied data cannot be parsed as a date value
   */
  @Override
  public DateTime parse(final String type, final Dimension dim, final int length, final byte[] data)
      throws ParseException {
    final DateTimeFormatter formatter = Formatters.get(type + ":" + length);
    Preconditions.checkArgument(formatter != null,
        "Formatter not found for date field, type=(" + type + ":" + length + ") data=" + data, length);
    try {
      return formatter.parseDateTime(decode(data));
    } catch (final Exception e) {
      final ParseException rethrow = new ParseException("Cannot parse date field value, type=("
          + type + ":" + length + ") data=" +data
          + " [decoded=" + decode(data) + "]", length);
      rethrow.initCause(e);
      throw rethrow;
    }
  }

  /**
   * {@inheritDoc}
   * @throws IllegalArgumentException if the data is null or not a valid date value
   */
  @Override
  public byte[] format(final String type, final Object data, final Dimension dimension) {
    Preconditions.checkNotNull(data,"Date value cannot be null");
    final DateTime dateTime = getDateValue(data);
    Preconditions.checkArgument(dateTime != null,
        "Invalid data [" + data + "] expected Date, got a " + data.getClass().getCanonicalName());
    return Formatters.get(type + ":" + dimension.getLength()).print(dateTime).getBytes();
  }

  public static DateTime getDateValue(final Object data) {
    if (data instanceof DateTime) {
      return (DateTime) data;
    } else if (data instanceof java.sql.Date) {
      return new DateTime(((java.sql.Date) data).getTime());
    } else if (data instanceof java.util.Date) {
      return new DateTime(((java.util.Date) data).getTime());
    }

    final String dateString = data.toString().trim();
    final String key = FieldType.DATE + ":" + dateString.length();
    final DateTimeFormatter formatter = Formatters.get(key);
    if (formatter == null) {
      throw new IllegalArgumentException("Invalid data [" + data + "]: cannot convert to date");
    }
    return formatter.parseDateTime(dateString);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isValid(final Object value, final String type, final Dimension dim) {
    return validate(value, type);
  }

  public static boolean validate(final Object value, final String type) {
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
      Formatters.get(type + ":" + dateValue.length()).parseDateTime(dateValue);
    } catch (final IllegalArgumentException e) {
      return false;
    }

    return true;
  }

}
