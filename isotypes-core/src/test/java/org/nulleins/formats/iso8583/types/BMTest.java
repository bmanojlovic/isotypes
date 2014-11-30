package org.nulleins.formats.iso8583.types;

import org.junit.Test;

import java.util.BitSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


/**
 * @author phillipsr
 */
public class BMTest {
  @Test
  public void testBitmapHex() {
    final BitSet target = BitsetUtil.hex2Bitset("4210001102C04804");

    String result = BitsetUtil.bitset2bitstring(target, 64);
    assertThat(result, is("0100001000010000000000000001000100000010110000000100100000000100"));

    result = BitsetUtil.bitset2Hex(target, 16);
    assertThat(result, is("4210001102C04804"));
  }

  @Test
  public void testBitmapBin() {
    final BitSet target = BitsetUtil.bin2Bitset(
        new byte[]{(byte) 0x80, 0x04, 0x22, (byte) 0xf1, 0x01, 0x10, (byte) 0xc1, 0x01});

    // 10000000 00000100 00100010 11110001 00000001 00010000 11000001 00000001
    final String result = BitsetUtil.bitset2Hex(target, 16);
    assertThat(result, is("800422F10110C101"));
  }

  @Test
  public void testBitmap2Bin() {
    final BitSet target = BitsetUtil.hex2Bitset("4210001102C04804");

    final byte[] result = BitsetUtil.bitset2bin(target, 8);
    final BitSet bs = BitsetUtil.bin2Bitset(result);
    final String string = BitsetUtil.bitset2Hex(bs, 16);
    assertThat(string, is("4210001102C04804"));
  }

  @Test
  public void testBitmapSetters() {
    final BitSet target = new BitSet();
    target.set(0);
    target.set(63);

    final String result = BitsetUtil.bitset2Hex(target, 16);
    assertThat(result, is("8000000000000001"));
  }

  @Test
  public void testBitmapGetters() {
    final BitSet target = BitsetUtil.hex2Bitset("4210001102C04804");
    // 2, 7, 12, 28, 32, 39, 41, 42, 50, 53, 62
    assertThat(target.get(0), is(false));
    assertThat(target.get(1), is(true));
    assertThat(target.get(2), is(false));
    assertThat(target.get(3), is(false));
    assertThat(target.get(4), is(false));
    assertThat(target.get(5), is(false));
    assertThat(target.get(6), is(true));
    assertThat(target.get(7), is(false));
    assertThat(target.get(8), is(false));
    assertThat(target.get(9), is(false));
    assertThat(target.get(10), is(false));
    assertThat(target.get(11), is(true));
    assertThat(target.get(27), is(true));
    assertThat(target.get(31), is(true));
    assertThat(target.get(38), is(true));
    assertThat(target.get(40), is(true));
    assertThat(target.get(41), is(true));
    assertThat(target.get(49), is(true));
    assertThat(target.get(52), is(true));
    assertThat(target.get(61), is(true));
  }

}
