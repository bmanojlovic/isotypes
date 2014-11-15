package org.nulleins.formats.iso8583.types;

import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;


/**
 * @author phillipsr
 */
public class TestMTI {
  @Test
  public void testGoodMTI() {
    final MTI mti = MTI.create("0200");
    assertThat(mti.toString(), is("0200"));
    assertThat(mti.getVersion(), is("ISO 8583-1:1987"));
    assertThat(mti.getMessageClass(), is("Financial Message"));
    assertThat(mti.getMessageFunction(), is("Request"));
    assertThat(mti.getMessageOrigin(), is("Acquirer"));
  }

  @Test
  public void testGoodMTIBinary() {
    final MTI mti = MTI.create(0x0200);
    assertThat(mti.toString(), is("0200"));
    assertThat(mti.getVersion(), is("ISO 8583-1:1987"));
    assertThat(mti.getMessageClass(), is("Financial Message"));
    assertThat(mti.getMessageFunction(), is("Request"));
    assertThat(mti.getMessageOrigin(), is("Acquirer"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMTINonNumeric() {
    MTI.create("A200");
  }

  @Test
  public void testAcquirerReversalAdviceRepeatMessage() {
    final MTI mti = MTI.create("0421");
    assertThat(mti.toString(), is("0421"));
  }

  @Test
  public void equalityConsistent() {
    final MTI mti1 = MTI.create(0x0200);
    final MTI mti2 = MTI.create("0200");
    assertThat(mti1, is(mti2));
    assertThat(mti1.hashCode(), is(mti2.hashCode()));
  }

  @Test
  public void inequalityConsistent() {
    final MTI mti1 = MTI.create(0x0200);
    final MTI mti2 = MTI.create("0210");
    assertThat(mti1, is(not(mti2)));
    assertThat(mti1.hashCode(), is(not(mti2.hashCode())));
  }

  @Test
  public void valueSortOrder() {
    final List<MTI> keys = asList(
        MTI.create(0x0200), MTI.create(0x0210), MTI.create(0x0400),
        MTI.create(0x0410), MTI.create(0x0200));
    final Set<MTI> sorted = new TreeSet<>(keys);
    assertThat(sorted, hasSize(4));
    assertThat(Iterables.getFirst(sorted,null), is(MTI.create(0x0200)));
    assertThat(Iterables.getLast(sorted), is(MTI.create(0x0410)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMTIWrongFormat() {
    MTI.create("0206"); // '6' is not allowed in final position
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMTITooShort() {
    MTI.create("200");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMTITooLong() {
    MTI.create("02000");
  }

}
