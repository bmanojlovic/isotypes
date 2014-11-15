package org.nulleins.formats.iso8583;

import org.junit.Test;
import org.nulleins.formats.iso8583.formatters.TypeFormatters;
import org.nulleins.formats.iso8583.types.CharEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class FormatterTests {

  @Test
  public void registersDefaultFormatters() {
    final TypeFormatters formatters = new TypeFormatters(CharEncoder.ASCII);
    assertThat(formatters.toString(), is("Registered formatters: [ns, time, xn, an, a, anp, n, as, exdate, date, z, ans]"));
  }
}
