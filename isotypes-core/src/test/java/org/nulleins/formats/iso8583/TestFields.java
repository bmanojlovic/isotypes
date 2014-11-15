package org.nulleins.formats.iso8583;

import org.junit.Before;
import org.junit.Test;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.FieldType;
import org.nulleins.formats.iso8583.types.MTI;

import java.math.BigInteger;
import java.text.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;


/**
 * @author phillipsr
 */
public class TestFields {
  private MessageTemplate messageTemplate;

  @Before
  public void setup() {
    messageTemplate = MessageTemplate.create("ISO015000077", MTI.create(0x0200), BitmapType.HEX);
    final MessageFactory schema = MessageFactory.Builder()
        .id("test")
        .bitmapType(BitmapType.BINARY)
        .contentType(ContentType.TEXT)
        .addTemplate(messageTemplate)
        .build();
  }

  @Test
  public void testNumeric() throws ParseException {
    final FieldTemplate target = FieldTemplate.localBuilder(messageTemplate).get()
        .f(2)
        .type(FieldType.NUMERIC)
        .dim("fixed(6)")
        .name("test")
        .build();
    final String intValue = new String(target.format(128));
    assertThat(intValue, is("000128"));

    final Object readBack = target.parse(intValue.getBytes());
    assertThat(readBack, instanceOf(BigInteger.class));
    assertThat((BigInteger)readBack, is(BigInteger.valueOf(128)));

    final String biValue = new String(target.format(BigInteger.TEN.multiply(BigInteger.TEN).add(BigInteger.TEN)));
    assertThat(biValue, is("000110"));

    final String strValue = new String(target.format(726161));
    assertThat(strValue, is("726161"));
  }

  @Test
  public void testLlvar() {
    final FieldTemplate target = FieldTemplate.localBuilder(messageTemplate).get()
        .f(2)
        .type(FieldType.ALPHANUM)
        .dim("llvar(3)")
        .name("test")
        .build();
    final String intValue = new String(target.format("128"));
    assertThat(intValue, is("128"));
  }

  @Test(expected = MessageException.class)
  public void shouldFailToFormatUnknownType() {
    final FieldTemplate subject = FieldTemplate.localBuilder(messageTemplate).get()
        .f(2)
        .type("unknown")
        .dim("llvar(3)")
        .name("test")
        .build();
    try {
      subject.format("128");
    } catch ( final MessageException e) {
      assertThat(e.getReasons().size(), is(1));
      assertThat(e.getMessage(), is("Could not format data [128] for field Field nb=2 name=test type=unknown dim=VAR2 (  3)"));
      throw e;
    }
  }

}
