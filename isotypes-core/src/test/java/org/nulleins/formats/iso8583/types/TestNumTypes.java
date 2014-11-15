package org.nulleins.formats.iso8583.types;

import org.junit.Test;
import org.nulleins.formats.iso8583.formatters.NumberFormatter;
import org.nulleins.formats.iso8583.formatters.TypeFormatter;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.ParsePosition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;


/**
 * @author phillipsr
 */
public class TestNumTypes {
  private final TypeFormatter<BigInteger> formatter = new NumberFormatter(CharEncoder.ASCII);

  @Test
  public void testParseNumeric()
      throws ParseException {
    final byte[] testData = "XXXX1234567ZXY".getBytes();
    final ParsePosition pos = new ParsePosition(4);

    final byte[] data = FieldParser.getBytes(testData, pos, 5);
    final BigInteger value = formatter.parse(FieldType.NUMERIC, Dimension.parse("FIXED(5)"), 5, data);

    assertThat(value, is(BigInteger.valueOf(12345)));
    assertThat(pos.getIndex(), is(9));
  }

  @Test(expected = ParseException.class)
  public void testParseNumericBad()
      throws ParseException {
    final byte[] testData = "XXXX1234567ZXY".getBytes();
    final ParsePosition pos = new ParsePosition(4);

    try {
      final byte[] data = FieldParser.getBytes(testData, pos, 8);
      formatter.parse(FieldType.NUMERIC, Dimension.parse("FIXED(8)"), 8, data);
    } catch (final ParseException e) {
      assertThat(e.getMessage(), is("Bad f format For input string: \"1234567Z\" for type=n [1234567Z]"));
      assertThat(pos.getIndex(), is(12));

      throw e;
    }
  }

  @Test(expected = ParseException.class)
  public void testParseXNNumericBad()
      throws ParseException {
    final byte[] testData = "AXXXX1234567ZXY".getBytes();
    final ParsePosition pos = new ParsePosition(4);

    try {
      final byte[] data = FieldParser.getBytes(testData, pos, 8);
      formatter.parse(FieldType.NUMSIGNED, Dimension.parse("FIXED(8)"), 8, data);
    } catch (final ParseException e) {
      assertThat(e.getMessage(),
          is("Bad f format Bad f format for xn: must start with C or D (field data=[X1234567]) for type=xn [X1234567]"));
      assertThat(pos.getIndex(), is(12));

      throw e;
    }
  }

  @Test
  public void testParseSignedNumeric()
      throws ParseException {
    final byte[] testData = "XXXXD123456ZXY".getBytes();
    final ParsePosition pos = new ParsePosition(4);

    final byte[] data = FieldParser.getBytes(testData, pos, 5);
    final BigInteger value = formatter.parse(FieldType.NUMSIGNED, Dimension.parse("FIXED(5)"), 5, data);

    assertThat(value, is(BigInteger.valueOf(-1234)));
    assertThat(pos.getIndex(), is(9));
  }

  @Test
  public void testFormat() {
    final byte[] data = formatter.format(FieldType.NUMERIC, 123, Dimension.parse("FIXED(6)"));

    assertThat(data.length, is(6));
    assertThat(new String(data), is("000123"));
  }

  @Test
  public void testFormatXNDebit() {
    final byte[] data = formatter.format(FieldType.NUMSIGNED, -123, Dimension.parse("FIXED(5)"));

    assertThat(data.length, is(5));
    assertThat(new String(data), is("D0123"));
  }

  @Test
  public void testFormatXNDebitVar() {
    final byte[] data = formatter.format(FieldType.NUMSIGNED, -123, Dimension.parse("LLVAR(5)"));

    assertThat(data.length, is(4));
    assertThat(new String(data), is("D123"));
  }

  @Test
  public void testFormatXNCredit() {
    final byte[] data = formatter.format(FieldType.NUMSIGNED, 123, Dimension.parse("FIXED(5)"));

    assertThat(data.length, is(5));
    assertThat(new String(data), is("C0123"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatXNTooLong() {
    try {
      formatter.format(FieldType.NUMSIGNED, -123456, Dimension.parse("FIXED(5)"));
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage(), startsWith(
          "Field data length (7) exceeds field maximum (5)"));
      throw e;
    }
  }

  @Test
  public void testFormatExact() {
    final byte[] data = formatter.format(FieldType.NUMERIC, 123456, Dimension.parse("FIXED(6)"));

    assertThat(data.length, is(6));
    assertThat(new String(data), is("123456"));
  }


  @Test
  public void testFormatNumVar() {
    final byte[] testData = "123456".getBytes();
    final Dimension dim = Dimension.parse("llvar(10)");

    final byte[] result = formatter.format(FieldType.NUMERIC, testData, dim);
    assertThat(result, is(testData));
  }

  @Test
  public void testFormatNumFix() {
    final String testData = "123456";
    final Dimension dim = Dimension.parse("fixed(10)");

    final String result = new String(formatter.format(FieldType.NUMERIC, testData, dim));
    assertThat(result, is("0000" + testData));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFormatNumTooLong() {
    final byte[] testData = "1234567".getBytes();
    final Dimension dim = Dimension.parse("llvar(2)");

    try {
      formatter.format(FieldType.NUMERIC, testData, dim);
    } catch (final IllegalArgumentException e) {
      assertThat(e.getMessage(), startsWith(
          "Field data length (7) exceeds field maximum (2)"));
      throw e;
    }
  }

}
