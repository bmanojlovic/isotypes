package org.nulleins.formats.iso8583;

import org.junit.Test;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.MTI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class AutogenTest {

  private static final MessageTemplate template = MessageTemplate.create("ISO199", MTI.create(0x0200), BitmapType.HEX);
  private static final FieldTemplate field = FieldTemplate.localBuilder(template).get()
      .f(11).name("stan").desc("System Trace Audit Number").dim("fixed(6)").type("n")
      .build();

  @Test
  public void canAutogenStan() {
    final AutoGenerator<Integer> generator = new StanGenerator(1,100);

    for ( int i = 0; i < 5; i++) {
      generator.generate("", field);
    }
    final Integer v = generator.generate("", field);
    assertThat(v, is(6));
  }

  @Test
  public void factoryErrors() {
    final AutoGeneratorFactory factory = new AutoGeneratorFactory(null);
    assertThat(factory.generate("", field), is(nullValue()));
    assertThat(factory.generate("x", field), is(nullValue()));
    assertThat(factory.generate("#", field), is(nullValue()));
  }
}
