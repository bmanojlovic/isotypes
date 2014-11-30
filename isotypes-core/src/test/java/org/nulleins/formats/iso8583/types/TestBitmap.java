package org.nulleins.formats.iso8583.types;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;


/**
 * @author phillipsr
 */
public class TestBitmap {
  @Test
  public void testBitmap() {
		/*
		 * 4210001102C04804	Fields 2, 7, 12, 28, 32, 39, 41, 42, 50, 53, 62
		 * Explanation of Bitmap (8 BYTE Primary Bitmap = 64 Bit) field 4210001102C04804
		 * BYTE1 : 0100 0010 = 42x (fieldlist 2 and 7 are present)
		 * BYTE2 : 0001 0000 = 10x (field 12 is present)
		 * BYTE3 : 0000 0000 = 00x (no fieldlist present)
		 * BYTE4 : 0001 0001 = 11x (fieldlist 28 and 32 are present)
		 * BYTE5 : 0000 0010 = 02x (field 39 is present)
		 * BYTE6 : 1100 0000 = C0x (fieldlist 41 and 42 are present)
		 * BYTE7 : 0100 1000 = 48x (fieldlist 50 and 53 are present)
		 * BYTE8 : 0000 0100 = 04x (field 62 is present)
		 * 
		 * Expect:
		 * 01000010 00010000 00000000 00010001 00000010 11000000 01001000 00000100
		 * 42       10       00       11       02       C0       48       04
		 * 
		 * 00100000 00010010 00000011 01000000 10001000 00000000 00001000 01000010
		 */
    final Bitmap target = Bitmap.empty()
      .withField(2)
      .withField(7)
      .withField(12)
      .withField(28)
      .withField(32)
      .withField(39)
      .withField(41)
      .withField(42)
      .withField(50)
      .withField(53)
      .withField(62);

    assertThat(target.isFieldPresent(2), is(true));
    assertThat(target.isFieldPresent(7), is(true));
    assertThat(target.isFieldPresent(12), is(true));
    assertThat(target.isFieldPresent(28), is(true));
    assertThat(target.isFieldPresent(32), is(true));
    assertThat(target.isFieldPresent(39), is(true));
    assertThat(target.isFieldPresent(41), is(true));
    assertThat(target.isFieldPresent(42), is(true));
    assertThat(target.isFieldPresent(50), is(true));
    assertThat(target.isFieldPresent(53), is(true));
    assertThat(target.isFieldPresent(62), is(true));

    final String hexBitmap = target.asHex(Bitmap.Id.PRIMARY);
    assertThat(hexBitmap, is("4210001102C04804"));

    final byte[] binaryBitmap = target.asBinary(Bitmap.Id.PRIMARY);
    assertThat(binaryBitmap[0], is((byte) 0x42));
    assertThat(binaryBitmap[1], is((byte) 0x10));
    assertThat(binaryBitmap[2], is((byte) 0x00));
    assertThat(binaryBitmap[3], is((byte) 0x11));
    assertThat(binaryBitmap[4], is((byte) 0x02));
    assertThat(binaryBitmap[5], is((byte) 0xc0));
    assertThat(binaryBitmap[6], is((byte) 0x48));
    assertThat(binaryBitmap[7], is((byte) 0x04));
  }

  @Test
  public void testSecondaryBitmap() {
    final Bitmap target = Bitmap.empty().withField(2).withField(66);

    assertThat(target.isFieldPresent(2), is(true));
    assertThat(target.isFieldPresent(66), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.PRIMARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.SECONDARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.TERTIARY), is(false));

    final String hexBitmap1 = target.asHex(Bitmap.Id.PRIMARY);
    // should be "1100000..." as first bit specifies secondary bitmap present
    assertThat(hexBitmap1, is("C000000000000000"));

    final byte[] binaryBitmap1 = target.asBinary(Bitmap.Id.PRIMARY);
    assertThat(binaryBitmap1[0], is((byte) 0xC0));

    final String hexBitmap2 = target.asHex(Bitmap.Id.SECONDARY);
    assertThat(hexBitmap2, is("4000000000000000"));

    final byte[] binaryBitmap2 = target.asBinary(Bitmap.Id.SECONDARY);
    assertThat(binaryBitmap2[0], is((byte) 0x40));
  }


  @Test
  public void testTertiaryBitmap() {
    final Bitmap target = Bitmap.empty()
        .withField(2)
        .withField(140);

    assertThat(target.isFieldPresent(2), is(true));
    assertThat(target.isFieldPresent(140), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.PRIMARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.SECONDARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.TERTIARY), is(true));

    final String hexBitmap1 = target.asHex(Bitmap.Id.PRIMARY);
    // should be "1100000..." as first bit specifies secondary bitmap present
    assertThat(hexBitmap1, is("C000000000000000"));

    final byte[] binaryBitmap1 = target.asBinary(Bitmap.Id.PRIMARY);
    assertThat(binaryBitmap1[0], is((byte) 0xC0));

    final String hexBitmap3 = target.asHex(Bitmap.Id.TERTIARY);
    assertThat(hexBitmap3, is("0010000000000000"));

    final byte[] binaryBitmap3 = target.asBinary(Bitmap.Id.TERTIARY);
    assertThat(binaryBitmap3[1], is((byte) 0x10));
  }

  @Test
  public void testTertiaryBitmap2() {
    final Bitmap target = Bitmap.empty().withField(190);
    assertThat(target.isBitmapPresent(Bitmap.Id.PRIMARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.SECONDARY), is(true));
    assertThat(target.isBitmapPresent(Bitmap.Id.TERTIARY), is(true));

    final String hexBitmap2 = target.asHex(Bitmap.Id.SECONDARY);
    // should be "1000000..." as first bit specifies secondary bitmap present
    assertThat(hexBitmap2, is("8000000000000000"));

    final String hexBitmap3 = target.asHex(Bitmap.Id.TERTIARY);
    assertThat(hexBitmap3, is("0000000000000004"));

    final byte[] binaryBitmap3 = target.asBinary(Bitmap.Id.TERTIARY);
    assertThat(binaryBitmap3[7], is((byte) 0x04));
  }

  @Test
  public void canCreateTertiaryBitmapFromString() {
    // switch field 140 on
    final Bitmap bitmap = Bitmap.parse("E44000000000000880000000000000000010000000000000");
    assertThat(bitmap.isBitmapPresent(Bitmap.Id.TERTIARY), is(true));
    assertThat(bitmap.toString(), is("E44000000000000880000000000000000010000000000000"));
  }

  @Test
  public void canCreateTertiaryBitmapFromBytes() {
    // switch field 140 on
    final byte[] bytes = new byte[] {(byte) 0xe4, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08,
        (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    final Bitmap bitmap = new Bitmap(bytes);
    assertThat(bitmap.isBitmapPresent(Bitmap.Id.TERTIARY), is(true));
    assertThat(bitmap.toString(), is("E44000000000000880000000000000000010000000000000"));
  }

  private static final String HEX1 = "E440000000000008";
  private static final String HEX2 = "0000000000000040";

  @Test
  public void canIterateOverBitmap() {
    final Bitmap bitmap = Bitmap.parse("6440000000000008");
    System.out.println(bitmap);
    Set<Integer> fieldsPresent = new HashSet<>();
    for ( final int field : bitmap) {
      System.out.print(field + ",");
      fieldsPresent.add(field);
    }
    System.out.println();
    assertThat(fieldsPresent, hasSize(5));
    assertThat(fieldsPresent, contains(2,3,6,10,61));

    bitmap.iterator().remove();
  }

  @Test
  public void testBitmapParseHex1() {
    // 01100100 01000000 00000000 00000000 00000000 00000000 00000000 000010000
    final Bitmap target = Bitmap.parse("6440000000000008");

    assertThat(target.isFieldPresent(1), is(false));
    assertThat(target.isFieldPresent(2), is(true));
    assertThat(target.isFieldPresent(3), is(true));
    assertThat(target.isFieldPresent(4), is(false));
    assertThat(target.isFieldPresent(5), is(false));
    assertThat(target.isFieldPresent(6), is(true));
    assertThat(target.isFieldPresent(7), is(false));
    assertThat(target.isFieldPresent(8), is(false));
    assertThat(target.isFieldPresent(9), is(false));
    assertThat(target.isFieldPresent(10), is(true));
    assertThat(target.isFieldPresent(61), is(true));

    assertThat(target.isBitmapPresent(Bitmap.Id.SECONDARY), is(false));

    final String hexBitmap1 = target.asHex(Bitmap.Id.PRIMARY);
    assertThat(hexBitmap1, is("6440000000000008"));

    final String hexBitmap2 = target.asHex(Bitmap.Id.SECONDARY);
    assertThat(hexBitmap2, is("0000000000000000"));

    final String hexBitmap3 = target.asHex(Bitmap.Id.TERTIARY);
    assertThat(hexBitmap3, is("0000000000000000"));

  }

  @Test
  public void testBitmapParseHex2() {
    final Bitmap target = Bitmap.parse(HEX1 + HEX2);
    assertThat(target.isFieldPresent(1), is(false));
    assertThat(target.isFieldPresent(2), is(true));
    assertThat(target.isFieldPresent(3), is(true));
    assertThat(target.isFieldPresent(6), is(true));

    assertThat(target.isBitmapPresent(Bitmap.Id.SECONDARY), is(true));

    final String hexBitmap1 = target.asHex(Bitmap.Id.PRIMARY);
    assertThat(hexBitmap1, is("E440000000000008"));

    final String hexBitmap2 = target.asHex(Bitmap.Id.SECONDARY);
    assertThat(hexBitmap2, is("0000000000000040"));

    final String hexBitmap3 = target.asHex(Bitmap.Id.TERTIARY);
    assertThat(hexBitmap3, is("0000000000000000"));

    final byte[] binaryBitmap3 = target.asBinary(Bitmap.Id.TERTIARY);
    assertThat(binaryBitmap3[7], is((byte) 0x00));
  }

  private static final byte[] BIN
      = new byte[]{0x01, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08};

  @Test
  public void testBitmapParseBinary1() {
    final Bitmap target = new Bitmap(BIN);
    final byte[] binaryBitmap = target.asBinary(Bitmap.Id.PRIMARY);

    assertThat(binaryBitmap[0], is((byte) 0x01));
    assertThat(binaryBitmap[1], is((byte) 0x40));
    assertThat(binaryBitmap[2], is((byte) 0x00));
    assertThat(binaryBitmap[3], is((byte) 0x00));
    assertThat(binaryBitmap[4], is((byte) 0x00));
    assertThat(binaryBitmap[5], is((byte) 0x00));
    assertThat(binaryBitmap[6], is((byte) 0x00));
    assertThat(binaryBitmap[7], is((byte) 0x08));
  }

  // 10000010 01000000 00000000 00000000 00000000 00000000 00000000 000010000
  private static final byte[] BIN1
      = new byte[]{(byte) 0x82, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08};
  // 00001000 00000000 00000000 00000000 00000000 00000000 01000000 000000000
  private static final byte[] BIN2
      = new byte[]{0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00};

  @Test
  public void testBitmapParseBinary2() {
    final Bitmap target = new Bitmap(concatData(BIN1, BIN2));

    final byte[] primaryBitmap = target.asBinary(Bitmap.Id.PRIMARY);
    final byte[] secondaryBitmap = target.asBinary(Bitmap.Id.SECONDARY);

    assertThat(primaryBitmap[0], is((byte) 0x82));
    assertThat(primaryBitmap[1], is((byte) 0x40));
    assertThat(primaryBitmap[2], is((byte) 0x00));
    assertThat(primaryBitmap[3], is((byte) 0x00));
    assertThat(primaryBitmap[4], is((byte) 0x00));
    assertThat(primaryBitmap[5], is((byte) 0x00));
    assertThat(primaryBitmap[6], is((byte) 0x00));
    assertThat(primaryBitmap[7], is((byte) 0x08));

    assertThat(secondaryBitmap[0], is((byte) 0x08));
    assertThat(secondaryBitmap[1], is((byte) 0x00));
    assertThat(secondaryBitmap[2], is((byte) 0x00));
    assertThat(secondaryBitmap[3], is((byte) 0x00));
    assertThat(secondaryBitmap[4], is((byte) 0x00));
    assertThat(secondaryBitmap[5], is((byte) 0x00));
    assertThat(secondaryBitmap[6], is((byte) 0x40));
    assertThat(secondaryBitmap[7], is((byte) 0x00));

  }

  /**
   * @param data1
   * @param data2
   * @return
   */
  private byte[] concatData(final byte[] data1, final byte[] data2) {
    final byte[] result = new byte[data1.length + data2.length];
    int index = 0;
    for (final byte b : data1) {
      result[index++] = b;
    }
    for (final byte b : data2) {
      result[index++] = b;
    }
    return result;
  }

  @Test(expected = NullPointerException.class)
  public void testBitmapHexNull() {
    Bitmap.parse(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBitmapHexBadLength() {
    Bitmap.parse("4210001102C0484");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBitmapNonHex() {
    Bitmap.parse("4210001102G04804");
  }

  @Test//(expected=IllegalArgumentException.class)
  public void
  testBitmapTertiary() {
    Bitmap.parse("4210001102C048044210001102C048044210001102C04804");
  }

}
