package org.nulleins.formats.iso8583.types;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


/** Implementation of the ISO8583 bitmap type, with facilities to create, parse and format a
  * message's bitmap in a f of formats
  * @author phillipsr */
public class Bitmap implements Iterable<Integer> {

  public static Bitmap empty() {
    return new Bitmap(new byte[] {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    });
  }

  public enum Id {
    PRIMARY, SECONDARY, TERTIARY
  }
  private static final Pattern HexMatcher = Pattern.compile("[0-9A-Fa-f]+");
  private static final int LAST_FIELD = 192;

  /** bitsets hold the three x_bitmaps available to a message, and
    * are indexed by the <code>Id.index</code> from the enum above */
  private final Map<Id,BitSet> bitmaps;

  private Bitmap(final Map<Id, BitSet> bitmaps) {
    Preconditions.checkNotNull(bitmaps);
    Preconditions.checkArgument(bitmaps.size() > 0);
    this.bitmaps = ImmutableMap.copyOf(bitmaps);
  }

  /** Create a bitmap, instantiating it from the supplied Hex string, which may
    * represent a primary, primary + secondary or primary + secondary + tertiary bitmap
    * @param value hexadecimal representation of a bitmap
    * @throws IllegalArgumentException if the bitmap is not a 16, 32 or 48 byte long
    *                                  hexadecimal string */
  public static Bitmap parse(final String value) {
    Preconditions.checkNotNull(value, "Hex bitmap must not be null");
    final String hexBitmap = value.trim();
    final int hexlength = hexBitmap.length();
    if (hexlength != 16 && hexlength != 32 && hexlength != 48) {
      throw new IllegalArgumentException(
          "Hex bitmap must be 16, 32 or 48 characters in size (got: " + hexlength + " chars)");
    }
    if (!HexMatcher.matcher(hexBitmap).matches()) {
      throw new IllegalArgumentException("Hex bitmap must contain only hexadecimal digits (0-9A-F)");
    }
    final Map<Id,BitSet> bitmaps = new HashMap<>();
    bitmaps.put(Id.PRIMARY, BitsetUtil.hex2Bitset(hexBitmap.substring(0, 16)));
    if (hexlength > 16) {
      bitmaps.put(Id.SECONDARY, BitsetUtil.hex2Bitset(hexBitmap.substring(16, 32)));
    }
    if (hexlength > 32) {
      bitmaps.put(Id.TERTIARY, BitsetUtil.hex2Bitset(hexBitmap.substring(32)));
    }
    return new Bitmap(bitmaps);
  }

  /** Construct a bitmap from <code>binBitmap</code>, an array of byte values
    * @throws IllegalArgumentException if the supplied array is less than 8 bytes */
  public Bitmap(final byte... binBitmap) {
    this(getBitmaps(binBitmap));
  }

  private static Map<Id, BitSet> getBitmaps(final byte[] binBitmap) {
    Preconditions.checkArgument(binBitmap != null && binBitmap.length >= 8, "Bin bitmap must be >= 8 bytes in size");
    final int length = binBitmap.length;
    final Map<Id,BitSet> result = new HashMap<>();
    if (length > 16) {
      result.put(Id.TERTIARY, BitsetUtil.bin2Bitset(Arrays.copyOfRange(binBitmap, 16, 24)));
    }
    if (length > 8) {
      result.put(Id.SECONDARY, BitsetUtil.bin2Bitset(Arrays.copyOfRange(binBitmap, 8, 16)));
    }
    result.put(Id.PRIMARY, BitsetUtil.bin2Bitset(Arrays.copyOfRange(binBitmap, 0, 9)));
    return result;
  }

  public String asHex(final Id map) {
    if ( bitmaps.containsKey(map)) {
      return BitsetUtil.bitset2Hex(bitmaps.get(map), 16);
    } else {
      return "0000000000000000";
    }
  }

  public byte[] asBinary(final Id map) {
    if ( bitmaps.containsKey(map)) {
      return BitsetUtil.bitset2bin(bitmaps.get(map), 8);
    } else {
      return new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    }
  }

  /** @param fieldNb
    * @throws IllegalArgumentException if the fieldNb is not {2..64} or {66..128} or {130..192} */
/*  public void setField(final int fieldNb) {
    Preconditions.checkArgument(!(fieldNb < FIRST_FIELD || fieldNb > LAST_FIELD || fieldNb == 65 || fieldNb == 129),
        "fieldNb can only be: {2..64} or {66..128} or {130..192} (fieldNb=" + fieldNb + ")");
    final Id bitmapId = getBitmapIdForField(fieldNb);
    final BitSet bitmap = x_bitmaps[bitmapId.index];
    bitmap.set(getFieldPosInBitmap(fieldNb, bitmapId));
    setBitmapPresent(bitmapId);
  } */

  /** @param fieldNb
    * @return */
  private Id getBitmapIdForField(final int fieldNb) {
    if (fieldNb <= 64) {
      return Id.PRIMARY;
    } else if (fieldNb > 64 && fieldNb <= 128) {
      return Id.SECONDARY;
    } else {
      return Id.TERTIARY;
    }
  }

  private int getFieldPosInBitmap(final int fieldNb, final Id bitmapId) {
    if (bitmapId == Id.PRIMARY) {
      return fieldNb - 1;
    } else if (bitmapId == Id.SECONDARY) {
      return fieldNb - 65;
    } else { // aliter TERTIARY:
      return fieldNb - 129;
    }
  }

  /** @param fieldNb
    * @return */
  public boolean isFieldPresent(final int fieldNb) {
    if ( fieldNb == 1 || fieldNb == 65) {
      return false;
    }
    final Id bitmapId = getBitmapIdForField(fieldNb);
    if ( !bitmaps.containsKey(bitmapId)) {
      return false;
    }
    final BitSet bitmap = bitmaps.get(bitmapId);
    final int pos = getFieldPosInBitmap(fieldNb, bitmapId);
    return bitmap.get(pos);
  }

  public boolean isBitmapPresent(final Id map) {
    if(map == Id.PRIMARY) {
      return true;
    } else if ( map == Id.SECONDARY) {
      return bitmaps.get(Id.PRIMARY).get(0);
    } else { // Id.TERTIARY
      return bitmaps.get(Id.SECONDARY).get(0);
    }
  }

  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    result.append(BitsetUtil.bitset2Hex(bitmaps.get(Id.PRIMARY), 16));
    if (isBitmapPresent(Id.SECONDARY)) {
      result.append(BitsetUtil.bitset2Hex(bitmaps.get(Id.SECONDARY), 16));
      if (isBitmapPresent(Id.TERTIARY)) {
        result.append(BitsetUtil.bitset2Hex(bitmaps.get(Id.TERTIARY), 16));
      }
    }
    return result.toString();
  }

  /** @return a copy of this bitmap with field <code>fieldNum</code> set */
  public Bitmap withField(final int fieldNum) {
    final Map<Id, BitSet> newmap = Maps.newHashMap(bitmaps);
    setField(fieldNum,newmap);
    return new Bitmap(newmap);
  }

  private void setField(final int fieldNb, final Map<Id, BitSet> bitmaps) {
    Preconditions.checkArgument(!(fieldNb < 2 || fieldNb > LAST_FIELD || fieldNb == 65 || fieldNb == 129),
        "fieldNb can only be: {2..64} or {66..128} or {130..192} (fieldNb=" + fieldNb + ")");
    final Id bitmapId = getBitmapIdForField(fieldNb);
    final BitSet bitmap = bitmaps.get(bitmapId);
    bitmap.set(getFieldPosInBitmap(fieldNb, bitmapId));
    setBitmapPresent(bitmapId, bitmaps);
  }

  private void setBitmapPresent(final Id bitmapId, final Map<Id, BitSet> bitmaps) {
    switch ( bitmapId) {
      case PRIMARY:
        return; // always present
      case SECONDARY:
        bitmaps.get(Id.PRIMARY).set(0);
        break;
      case TERTIARY:
        // if tertiary bitmap is present, then secondary is implicitly present
        bitmaps.get(Id.PRIMARY).set(0);
        bitmaps.get(Id.SECONDARY).set(0);
        break;
      default:
        //bitmaps.get(bitmapId).set(0);
    }
  }

  @Override
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      private final AtomicInteger nextFieldNum = new AtomicInteger(0);

      @Override
      public boolean hasNext() {
        while(!isFieldPresent(nextFieldNum.incrementAndGet())) {
          if(nextFieldNum.get() > LAST_FIELD) {
            return false;
          }
        }
        return true;
      }

      @Override
      public Integer next() {
        return nextFieldNum.get();
      }

      @Override
      public void remove() { }
    };
  }

}
