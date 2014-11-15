/**
 *
 */
package org.nulleins.formats.iso8583.formatters;

import com.google.common.base.Preconditions;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.FieldType;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.nulleins.formats.iso8583.TrackData;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;


/**
 * The registry of type formatters used to parse and format the data fields in ISO8583 messages
 * <p/>
 * This class is instantiated per message factory, with the default formatters to handle the
 * standard ISO field types; these may be overridden in the schema, and custom formatters added
 * <p/>
 * Considered using DI to set-up this mapping, but unlikely to change
 * @author phillipsr
 */
public class TypeFormatters {
  private final CharEncoder charset;
  private final Map<String, TypeFormatter<?>> formatters = new HashMap<>();

  public TypeFormatters(final CharEncoder charset) {
    Preconditions.checkNotNull(charset,"charset cannot be null/empty");
    this.charset = charset;
    initializeFormatters();
  }

  /**
   * create the standard set of formatters used and map them to the types
   * they handle
   */
  private void initializeFormatters() {
    final TypeFormatter<DateTime> DateFormatter = new DateFormatter(charset);
    final TypeFormatter<LocalTime> TimeFormatter = new TimeFormatter(charset);
    final TypeFormatter<BigInteger> NumberFormatter = new NumberFormatter(charset);
    final TypeFormatter<String> AlphaFormatter = new AlphaFormatter(charset);
    final TypeFormatter<TrackData> TrackFormatter = new TrackDataFormatter(charset);

    formatters.put(FieldType.DATE, DateFormatter);
    formatters.put(FieldType.EXDATE, DateFormatter);
    formatters.put(FieldType.TIME, TimeFormatter);
    formatters.put(FieldType.NUMERIC, NumberFormatter);
    formatters.put(FieldType.ALPHA, AlphaFormatter);
    formatters.put(FieldType.ALPHASYMBOL, AlphaFormatter);
    formatters.put(FieldType.ALPHANUM, AlphaFormatter);
    formatters.put(FieldType.ALPHANUMPAD, AlphaFormatter);
    formatters.put(FieldType.ALPHANUMSYMBOL, AlphaFormatter);
    formatters.put(FieldType.NUMSYMBOL, AlphaFormatter);
    formatters.put(FieldType.NUMSIGNED, NumberFormatter);
    formatters.put(FieldType.TRACKDATA, TrackFormatter);
  }

  /**
   * Set or replace the formatter for field <code>type</code>
   * @param type
   * @param formatter
   */
  public void setFormatter(final String type, final TypeFormatter<?> formatter) {
    formatter.setCharset(charset);
    formatters.put(type, formatter);
  }

  public TypeFormatter<?> getFormatter(final String type) {
    return formatters.get(type);
  }

  @Override
  public String toString() {
    return "Registered formatters: " + formatters.keySet();
  }

}
