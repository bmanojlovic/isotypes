package org.nulleins.formats.iso8583.types;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class BitsetTest {

  @Test
  public void creationTest() {
    final byte[] bytes = new byte[] {(byte) 0xe4, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08,
        (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    BitSet subject = BitSet.valueOf(bytes);
    assertThat(BitsetUtil.bitset2Hex(subject, 32), is("27020000000000100100000000000000"));
  }

  @Test
  public void cannotCreateUtilInstance() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
    Constructor<BitsetUtil> c = BitsetUtil.class.getDeclaredConstructor();
    c.setAccessible(true);
    final BitsetUtil subject = c.newInstance();
    assertThat(subject, not(nullValue(BitsetUtil.class)));
  }
}
