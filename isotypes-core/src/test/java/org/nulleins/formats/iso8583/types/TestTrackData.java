/**
 *
 */
package org.nulleins.formats.iso8583.types;

import org.junit.Test;
import org.nulleins.formats.iso8583.TrackData;
import org.nulleins.formats.iso8583.formatters.TrackDataFormatter;
import org.nulleins.formats.iso8583.formatters.TypeFormatter;

import java.text.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.Is.is;

/**
 * @author phillipsr
 */
public class TestTrackData {
  private final TypeFormatter<TrackData> formatter = new TrackDataFormatter(CharEncoder.ASCII);

  private static final byte[] TestDataT1 =
      "%B1234567890123445^EARIBUG/HUW.              ^99011200000000000000**XXX******?".getBytes();
  private static final byte[] TestDataT2 = "1234567890123456789=1015123".getBytes();
  private static final byte[] TestDataT3 =
      "011234567890123445=724724100000000000030300XXXX040400099010=************************==1=0000000000000000".getBytes();

  @Test
  public void testParseT1()
      throws ParseException {
    final TrackData value = formatter.parse(FieldType.TRACKDATA, Dimension.parse("FIXED(50)"), 0, TestDataT1);

    assertThat(value.getType(), is(TrackData.Track.TRACK1));
    assertThat(value.getName()[0], is("EARIBUG"));
    assertThat(value.getName()[1], is("HUW"));
    assertThat(value.getPrimaryAccountNumber(), is(1234567890123445L));
    assertThat(value.getExpirationDate(), is(9901));
    assertThat(value.getServiceCode(), is(120));
    assertThat(value.getDiscretionaryData(), is("0000000000000**XXX******"));
  }



  @Test
  public void testParseT2()
      throws ParseException {
    final TrackData value = formatter.parse(FieldType.TRACKDATA, Dimension.parse("FIXED(50)"), 0, TestDataT2);

    assertThat(value.getType(), is(TrackData.Track.TRACK2));
    assertThat(value.getPrimaryAccountNumber(), is(1234567890123456789L));
    assertThat(value.getExpirationDate(), is(1015));
    assertThat(value.getServiceCode(), is(123));
    assertThat(value.getDiscretionaryData(), isEmptyString());
  }

  /**
   * Track3 data not supported
   * @throws ParseException
   */
  @Test(expected = ParseException.class)
  public void testParseT3()
      throws ParseException {

    final TrackData value = formatter.parse(FieldType.TRACKDATA, Dimension.parse("FIXED(50)"), 0, TestDataT3);

    assertThat(value.getType(), is(TrackData.Track.TRACK3));
  }

  @Test
  public void testFormatT1() {
    final TrackData value = new TrackData(TrackData.Track.TRACK1);
    value.setName(new String[]{"EARIBUG", "HUW", "", ""});
    value.setPrimaryAccountNumber(1234567890123445L);
    value.setExpirationDate(9901);
    value.setServiceCode(120);
    value.setDiscretionaryData("0000000000000**XXX******");
    final byte[] result = formatter.format(FieldType.TRACKDATA, value, Dimension.parse("LLVAR(80)"));
    assertThat(new String(result),
        is("B1234567890123445^EARIBUG/HUW.              99011200000000000000**XXX******"));
  }

  @Test
  public void testFormatT2() {
    final TrackData value = new TrackData(TrackData.Track.TRACK2);
    value.setPrimaryAccountNumber(1234567890123456789L);
    value.setExpirationDate(1015);
    value.setServiceCode(123);
    value.setDiscretionaryData("");
    final byte[] result = formatter.format(FieldType.TRACKDATA, value, Dimension.parse("LLVAR(80)"));
    assertThat(new String(result), is("1234567890123456789=1015123"));
  }

}
