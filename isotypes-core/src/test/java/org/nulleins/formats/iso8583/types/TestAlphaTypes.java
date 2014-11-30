package org.nulleins.formats.iso8583.types;

import org.junit.Test;
import org.nulleins.formats.iso8583.MessageException;
import org.nulleins.formats.iso8583.formatters.AlphaFormatter;
import org.nulleins.formats.iso8583.formatters.TypeFormatter;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


/**
 * @author phillipsr
 */
public class TestAlphaTypes {
  private static final TypeFormatter<String> formatter = new AlphaFormatter(CharEncoder.ASCII);

  @Test
  public void testParseAlpha()
      throws ParseException {
    final byte[] testData = "123456Hello7ZXY".getBytes();
    final ParsePosition pos = new ParsePosition(6);
    final byte[] data = FieldParser.getBytes(testData, pos, 5);
    final Object value = formatter.parse(FieldType.ALPHA, Dimension.parse("FIXED(5)"), 5, data);

    assertThat((String)value, is("Hello"));
    assertThat(pos.getIndex(), is(11));
  }


  @Test(expected = ParseException.class)
  public void testParseAlphaExhausted()
      throws ParseException {
    final byte[] testData = "123456Hell".getBytes();
    final ParsePosition pos = new ParsePosition(6);

    try {
      final byte[] data = FieldParser.getBytes(testData, pos, 5);
      formatter.parse(FieldType.ALPHA, Dimension.parse("FIXED(5)"), 5, data);
    } catch (final ParseException e) {
      assertThat(e.getMessage(), is("Data exhausted"));
      assertThat(pos.getErrorIndex(), is(6));
      throw e;
    }
  }

  @Test(expected = ParseException.class)
  public void testParseAlphaInvalid()
      throws ParseException {
    final byte[] testData = "123456H31109188".getBytes();
    final ParsePosition pos = new ParsePosition(6);

    try {
      final byte[] data = FieldParser.getBytes(testData, pos, 5);
      formatter.parse(FieldType.ALPHA, Dimension.parse("FIXED(5)"), 5, data);
    } catch (final ParseException e) {
      assertThat(e.getMessage().startsWith("Invalid data parsed"), is(true));
      throw e;
    }
  }

  @Test
  public void testFormatAlphaLVar() {
    final byte[] testData = "Data".getBytes();
    final Dimension dim = Dimension.parse("lvar(4)");

    final byte[] result = formatter.format(FieldType.ALPHA, testData, dim);
    assertThat(Arrays.equals(testData, result), is(true));
  }

  @Test
  public void testFormatAlphaLLVar() {
    final byte[] testData = "StringData".getBytes();
    final Dimension dim = Dimension.parse("llvar(10)");

    final byte[] result = formatter.format(FieldType.ALPHA, testData, dim);
    assertThat(Arrays.equals(testData, result), is(true));
  }

  @Test
  public void testFormatAlphaFix() {
    final String testData = "StringData";
    final Dimension dim = Dimension.parse("fixed(12)");

    final String result = new String(formatter.format(FieldType.ALPHA, testData, dim));
    assertThat(result, is(testData + "  "));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatAlphaInvalid() {
    formatter.format(FieldType.ALPHA, "1234", Dimension.parse("fixed(12)"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatAlphaInvalidType() {
    formatter.format(FieldType.ALPHA, "1234", Dimension.parse("broken(12)"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatAlphaInvalidDimension() {
    formatter.format(FieldType.ALPHA, "1234", Dimension.parse("fixed()"));
  }


  @Test(expected = MessageException.class)
  public void cannotGetVSizeOfFixed() {
    Dimension.parse("fixed(12)").getVSize();
  }

  @Test(expected = MessageException.class)
  public void testFormatAlphaFixTooLong() {
    formatter.format(FieldType.ALPHA, "StringData", Dimension.parse("fixed(2)"));
  }

  @Test(expected = MessageException.class)
  public void testFormatAlphaVarTooLong() {
    formatter.format(FieldType.ALPHA, "TooLong".getBytes(), Dimension.parse("llvar(2)"));
  }


}
