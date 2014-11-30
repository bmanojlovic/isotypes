/**
 *
 */
package org.nulleins.formats.iso8583.formatters;

import com.google.common.base.Preconditions;
import org.nulleins.formats.iso8583.TrackData;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.FieldType;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formatter/parser capable of interpreting the data representation used on financial transaction card's
 * magnetic strips (ISO/IEC 7813)
 * <p/>
 * Note, this data hs already been read and parsed by the card reader hardware before being submitted via
 * ISO8583, so usually, the start- and end-sentinels and LRC fieldlist are not present in an ISO8583 field<p/>
 * <b>Track 1:</b>
 * <pre>
 * %B1234567890123445^EARIBUG/H.                ^99011200000000000000**XXX******?*
 * ^^^               ^^                         ^^   ^       ^         ^        ^^
 * |||_ Card f  ||_ Card holder            ||   |       |         |_ CVV** ||_ LRC
 * ||_ Format code   |_ Field separator         ||   |       |                  |_ End sentinel
 * |_ Start sentinel           Field separator _||   |       |_ Discretionary data
 *                                   Expiration _|   |_ Service code
 * </pre>
 * <b>Track 1, Format B:</b>
 * <table>
 * <tr><td>STX</td><td>Start sentinel � one character (generally '%')</td></tr>
 * <tr><td>FMT</td><td>Format code="B" � one character (alpha only)</td></tr>
 * <tr><td>PAN</td><td>Primary account f (PAN) � up to 19 characters.
 * Usually, but not always, matches the credit card f printed on the front of the card.</td></tr>
 * <tr><td>FS</td><td>Field Separator � one character (generally '^')</td></tr>
 * <tr><td>NAME*</td><td>Name � two to 26 characters</td></tr>
 * <tr><td>FS</td><td>Field Separator � one character (generally '^')</td></tr>
 * <tr><td>ED</td><td>Expiration date � four characters in the form YYMM.</td></tr>
 * <tr><td>SC</td><td>Service code � three characters</td></tr>
 * <tr><td>DD</td><td>Discretionary data � may include Pin Verification Key Indicator (PVKI, 1 character),
 * PIN Verification Value (PVV, 4 characters), Card Verification Value or Card Verification Code
 * (CVV or CVC, 3 characters)</td></tr>
 * <tr><td>ETX</td><td>End sentinel � one character (generally '?')</td></tr>
 * <tr><td>LRC</td><td>Longitudinal redundancy check (LRC) � it is one character and a validity character
 * calculated from other data on the track.</td></tr>
 * </table>
 * (*)<b>NAME</b> can have the following internal structure:
 * <pre>
 * Surname "/" First Name or Initial " " Middle Name or Initial "." Title
 * </pre>
 * <b>Track 2:</b>
 * <pre>
 * ;1234567890123445=99011200XXXX00000000?*
 * ^^               ^^   ^   ^           ^^
 * ||_ Card f  ||   |   |_ Encrypted||_ LRC
 * |_ Start sentinel||   |      PIN***   |_ End sentinel
 *                  ||   |_ Service code
 * Field separator _||_ Expiration
 * </pre>
 * The Track 2 structure is specified as:
 * <table>
 * <tr><td>STX</td><td>Start sentinel ";"</td></tr>
 * <tr><td>PAN</td><td>Primary Account Number, up to 19 digits, as defined in ISO/IEC 7812-1</td></tr>
 * <tr><td>FS</td><td>Separator "="</td></tr>
 * <tr><td>ED</td><td>Expiration date, YYMM or "=" if not present</td></tr>
 * <tr><td>SC</td><td>Service code, 3 digits or "=" if not present</td></tr>
 * <tr><td>DD</td><td>Discretionary data, balance of available digits</td></tr>
 * <tr><td>ETX</td><td>End sentinel "?"</td></tr>
 * <tr><td>LRC</td><td>Longitudinal redundancy check, calculated according to ISO/IEC 7811-2</td></tr>
 * </table>
 * The maximum record length is 40 numeric digits.
 * E.g.,
 * <code>;1234567890123456789=1503=001?</code>
 * @author phillipsr
 */
public class TrackDataFormatter extends TypeFormatter<TrackData> {
  /** Track1 pattern: 5 groups are 0: whole field, 1: PAN, 2: NAME, 3: ED, 4: SC */
  private static final Pattern Track1Matcher
      = Pattern.compile("^%%?([A-Z])(\\d{1,19})\\^([^\\^]{2,26})\\^(\\d{4}|\\^)(\\d{3}|\\^)([^\\?]+)\\??$");
  private static final Pattern Track2Matcher
      /** Track2 pattern: 4 groups are 0: whole field, 1: PAN, 2: ED, 2: SC */
      = Pattern.compile("^;?(\\d{1,19})[=|D](\\d{4}|[=|D])(\\d{3}|[=|D])(\\d*)\\??$");
  /** Name pattern: 5 groups are 0: while field, 1: Surname, 2: First Name or Initial, 3: Middle Name or Initial, 4: Title */
  private static final Pattern NameFieldMatcher
      = Pattern.compile("([^/]+)/([A-Za-z]*)([ ][A-Za-z]+)?\\.([A-Za-z]+)*");

  private final TypeFormatter<String> alphaFormatter;

  public TrackDataFormatter(final CharEncoder charset) {
    setCharset(charset);
    alphaFormatter = new AlphaFormatter(charset);
  }

  /**
   * {@inheritDoc}
   * <p/>Parse Track1 or Track2 data
   * @throws ParseException if the supplied data does not match Track1 or Track2 data specification
   */
  @Override
  public TrackData parse(final String type, final Dimension dim, final int length, final byte[] data)
      throws ParseException {
    final String value = decode(data);
    final Matcher t1matcher = Track1Matcher.matcher(value);
    if (t1matcher.matches()) {
      return parseTrack1(t1matcher);
    }
    final Matcher t2matcher = Track2Matcher.matcher(value);
    if (t2matcher.matches()) {
      return parseTrack2(t2matcher);
    }

    throw new ParseException("Could not understand track data (type=" + type + "): [" + value + "]", length);
  }

  /* (non-Javadoc)
   * @see TypeFormatter#format(java.lang.String, java.lang.Object, Dimension)
   */
  @Override
  public byte[] format(final String type, final Object data, final Dimension dimension) {
    Preconditions.checkNotNull(data,"TrackData value cannot be null");
    Preconditions.checkArgument (isValid(data, type, dimension),
        "Cannot format invalid value for [" + type + "] field: '"
          + data + "', is-a " + data.getClass().getSimpleName());

    final TrackData trackData = (TrackData) data;

    final StringBuilder buffer = new StringBuilder();
    final String format = trackData.getType() == TrackData.Track.TRACK1 ? "B%d^%-26.26s%04d%03d%s" : "%d%s=%04d%03d%s";
    buffer.append(String.format(format,
        trackData.getPrimaryAccountNumber(),
        trackData.getType() == TrackData.Track.TRACK1 ? trackData.formatName() : "",
        trackData.getExpirationDate(),
        trackData.getServiceCode(),
        trackData.getDiscretionaryData()));
    final String result = buffer.toString();

    return alphaFormatter.format(FieldType.ALPHANUMSYMBOL, result, dimension);
  }

  /**
   * {@inheritDoc}
   * <p/>Check that the supplied value is-a <code>TrackData</code> object, of type Track1 or Track2,
   * and that the mandatory fieldlist are set
   */
  @Override
  public boolean isValid(final Object value, final String type, final Dimension dimension) {
    if (!(value instanceof TrackData)) {
      return false;
    }
    final TrackData trackData = (TrackData) value;
    return !((trackData.getType() != TrackData.Track.TRACK1 && trackData.getType() != TrackData.Track.TRACK2)
              || trackData.getExpirationDate() == 0 || trackData.getServiceCode() == 0)
        && (trackData.getType() == TrackData.Track.TRACK2 || trackData.getPrimaryAccountNumber() != 0);
  }

  /**
   * Interpret the supplied field value as Track 1 data
   * @param matcher that has matcher the Track1 pattern
   * @return TrackData object initialized from the sub-fieldlist of <code>value/code>,
   * with the <code>name</code> property set
   * @throws AssertionError if the matcher has not matched the 5 groups in the Track1 pattern
   */
  private TrackData parseTrack1(final Matcher matcher) {
    Preconditions.checkArgument(matcher.groupCount() == 6, "Track1 Data requires 6 fieldlist (" + matcher.groupCount() + ")");
    Preconditions.checkArgument(matcher.group(1).charAt(0) == 'B', "Only Track1 type 'B' format supported");
    return TrackData.Builder()
      .type(TrackData.Track.TRACK1)
      .primaryAccountNumber(Long.parseLong(matcher.group(2).trim()))
      .name(getNameFields(matcher.group(3).trim()))
      .expirationDate(Integer.parseInt(matcher.group(4).trim()))
      .serviceCode(Integer.parseInt(matcher.group(5).trim()))
      .discretionaryData(matcher.group(6).trim()).build ();
  }

  /**
   * Interpret the supplied field value as Track 2 data
   * @param matcher that has matcher the Track2 pattern
   * @return TrackData object initialized from the sub-fields of <code>value</code>
   * without the <code>name</code> property set (not present in Track 2)
   * @throws AssertionError if the matcher has not matched the 4 groups in the Track2 pattern
   */
  private TrackData parseTrack2(final Matcher matcher) {
    Preconditions.checkArgument(matcher.groupCount() == 4,
        "Track2 Data requires 4 fieldlist (" + matcher.groupCount() + ")");

    return TrackData.Builder()
        .type(TrackData.Track.TRACK2)
    .primaryAccountNumber(Long.parseLong(matcher.group(1)))
    .expirationDate(Integer.parseInt(matcher.group(2)))
    .serviceCode(Integer.parseInt(matcher.group(3)))
    .discretionaryData(matcher.group(4)).build ();
  }

  /**
   * Parse an encoded Track2 name field into its four elements
   * @param nameField formatted according to ISO8583 Track2 Data Name format
   * @return array of {Surname, First Name or Initial, Middle Name or Initial, Title}
   */
  private String[] getNameFields(final String nameField) {
    final Matcher matcher = NameFieldMatcher.matcher(nameField);
    if (!matcher.matches()) {
      return new String[]{nameField, "", "", ""};
    } else {
      return new String[]{
          matcher.group(1), matcher.group(2),
          matcher.group(3), matcher.group(4)};
    }
  }

}
