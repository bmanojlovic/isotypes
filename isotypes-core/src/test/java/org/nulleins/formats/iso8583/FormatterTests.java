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
    assertThat(formatters.hasFormatter("time"), is(true));
    assertThat(formatters.hasFormatter("xn"), is(true));
    assertThat(formatters.hasFormatter("an"), is(true));
    assertThat(formatters.hasFormatter("a"), is(true));
    assertThat(formatters.hasFormatter("anp"), is(true));
    assertThat(formatters.hasFormatter("n"), is(true));
    assertThat(formatters.hasFormatter("as"), is(true));
    assertThat(formatters.hasFormatter("exdate"), is(true));
    assertThat(formatters.hasFormatter("date"), is(true));
    assertThat(formatters.hasFormatter("z"), is(true));
    assertThat(formatters.hasFormatter("ans"), is(true));
  }
}
