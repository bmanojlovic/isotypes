package org.nulleins.formats.iso8583;

import com.google.common.base.Optional;
import org.junit.Test;
import org.nulleins.formats.iso8583.types.MTI;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class AutogenTest {

  private static final FieldTemplate field = FieldTemplate.localBuilder().get()
    .f(11).name("stan").desc("System Trace Audit Number").dim("fixed(6)").type("n").build();
  private static final MessageTemplate template = MessageTemplate.Builder()
      .header ("ISO199")
      .type (MTI.create (0x0200))
      .fieldlist (asList (field))
      .build ();

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
    assertThat(factory.generate("x", field), is(Optional.absent()));
    assertThat(factory.generate("#", field), is(nullValue()));
  }
}
