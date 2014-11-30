package org.nulleins.formats.iso8583;

import org.junit.Before;
import org.junit.Test;
import org.nulleins.formats.iso8583.formatters.TypeFormatters;
import org.nulleins.formats.iso8583.types.*;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
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
    final FieldTemplate.Builder requestBuilder = FieldTemplate.localBuilder().get();
    final List<FieldTemplate> requestFields = asList (
        requestBuilder.f (2).dim ("fixed(6)").type (FieldType.NUMERIC).build (),
        requestBuilder.f (3).dim ("llvar(3)").type (FieldType.ALPHANUM).build (),
        requestBuilder.f (4).dim ("fixed(6)").type ("unknown").build ());
    messageTemplate = MessageTemplate.Builder()
        .header("ISO015000077")
        .type(MTI.create(0x0200))
        .fieldlist(requestFields)
        .build().with (new TypeFormatters (CharEncoder.ASCII));
  }

  @Test
  public void testNumeric() throws ParseException {
    final FieldTemplate target = messageTemplate.getFields ().get(2);
    final String intValue = new String(target.format(128, messageTemplate.getFormatter(target.getType())));
    assertThat(intValue, is("000128"));

    final Object readBack = messageTemplate.parse(intValue.getBytes(),target);
    assertThat(readBack, instanceOf(BigInteger.class));
    assertThat((BigInteger)readBack, is(BigInteger.valueOf(128)));

    final String biValue = new String(target.format(BigInteger.TEN.multiply(BigInteger.TEN).add(BigInteger.TEN),
        messageTemplate.getFormatter(target.getType())));
    assertThat(biValue, is("000110"));

    final String strValue = new String(target.format(726161,
        messageTemplate.getFormatter(target.getType())));
    assertThat(strValue, is("726161"));
  }

  @Test
  public void testLlvar() {
    final FieldTemplate target = messageTemplate.getFields ().get (3);
    final String intValue = new String(target.format("128",
        messageTemplate.getFormatter(target.getType())));
    assertThat(intValue, is("128"));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailToFormatUnknownType() {
    final FieldTemplate target = messageTemplate.getFields ().get (4);
    try {
      target.format("128", messageTemplate.getFormatter(target.getType()));
    } catch ( final IllegalStateException e) {
      assertThat(e.getMessage(), is("Template must have a formatter for field type: unknown"));
      throw e;
    }
  }

}
