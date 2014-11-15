/**
 *
 */
package org.nulleins.formats.iso8583.formatters;

import org.nulleins.formats.iso8583.types.CardAcceptorLocation;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.FieldType;

import java.text.ParseException;


/**
 * [PH Rumukrushi          Porthar      PHNG]
 * <p/>
 * The location information (positions 1 - 23), exclusive of city, state and country
 * The city (positions 24 - 36) in which the Point-of-Service is located
 * The state (positions 37 - 38) in which the Point-of-Service is located
 * The country (positions 39 - 40) in which the Point-of-Service is located
 * @author phillipsr
 */
public class CardAcceptorLocationFormatter
    extends TypeFormatter<CardAcceptorLocation> {
  private final TypeFormatter<String> formatter;

  public CardAcceptorLocationFormatter() {
    setCharset(CharEncoder.ASCII);
    formatter = new AlphaFormatter(CharEncoder.ASCII);
  }

  @Override
  public CardAcceptorLocation parse(final String type, final Dimension dimension, final int length, final byte[] data)
      throws ParseException {
    final String segment = new String(data);
    final String location = formatter.parse(
        FieldType.ALPHANUMSYMBOL, dimension, 23, segment.substring(0, 23).getBytes()).trim();
    final String city = formatter.parse(
        FieldType.ALPHANUMSYMBOL, dimension, 13, segment.substring(23, 36).getBytes()).trim();
    final String state = formatter.parse(
        FieldType.ALPHANUMSYMBOL, dimension, 2, segment.substring(36, 38).getBytes()).trim();
    final String country = formatter.parse(
        FieldType.ALPHANUMSYMBOL, dimension, 2, segment.substring(38, 40).getBytes()).trim();
    return new CardAcceptorLocation(location, city, state, country);
  }

  @Override
  public byte[] format(final String type, final Object data, final Dimension dimension) {
    if (!(data instanceof CardAcceptorLocation)) {
      throw new IllegalArgumentException("data must be a CardAcceptorLocation instance");
    }
    final StringBuilder result = new StringBuilder();
    final CardAcceptorLocation item = (CardAcceptorLocation) data;
    result.append(new String(formatter.format(FieldType.ALPHANUMPAD, item.getLocation(), Dimension.parse("fixed(23)"))));
    result.append(new String(formatter.format(FieldType.ALPHA, item.getCity(), Dimension.parse("fixed(13)"))));
    result.append(new String(formatter.format(FieldType.ALPHA, item.getState(), Dimension.parse("fixed(2)"))));
    result.append(new String(formatter.format(FieldType.ALPHA, item.getCountry(), Dimension.parse("fixed(2)"))));
    assert dimension.getType() == Dimension.Type.FIXED
        ? result.length() == dimension.getLength()
        : result.length() <= dimension.getLength();
    return result.toString().getBytes();
  }

  @Override
  public boolean isValid(final Object value, final String type, final Dimension dimension) {
    if (!(value instanceof CardAcceptorLocation)) {
      return false;
    }
    final CardAcceptorLocation item = (CardAcceptorLocation) value;
    return item.getLocation() != null && item.getCity() != null && item.getState() != null && item.getCountry() != null;
  }

}
