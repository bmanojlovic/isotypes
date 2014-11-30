package org.nulleins.formats.iso8583.io;

import org.apache.commons.lang3.ArrayUtils;
import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.types.Bitmap;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;


/** Generic class to read ISO8583 message from a data stream; interpretation of
  * numeric fieldlist is deferred to specific subclasses (e.g., BCD, character)
  * @author phillipsr */
public abstract class MessageReader {
  protected CharEncoder charCodec;

  /** Read the value of the supplied field from the input stream
    * @param field template describing the next field in the input
    * @return the value of the field as a byte array
    * @throws IOException if the required amount of data cannot be read */
  public abstract byte[] readField(FieldTemplate field, DataInputStream input) throws IOException;

  /** Read the Message Type Indicator from the input stream
    * @return an MTI object representing the message type being read
    * @throws IOException if the required amount of data cannot be read */
  public abstract MTI readMTI(DataInputStream input) throws IOException;

  /** Read an ISO8583 bitmap from the input stream
    * @param bitmapType specifies if the bitmap is binary or hex (character data)
    * @return a Bitmap object initialized from the input data
    * @throws IOException if the required amount of data cannot be read */
  public Bitmap readBitmap(final BitmapType bitmapType, final DataInputStream input)
      throws IOException {
    if (bitmapType == BitmapType.BINARY) {
      return readBinaryBitmap(input);
    } else {
      return readHexBitmap(input);
    }
  }

  /** Read a binary bitmap from the input stream
    * @return a Bitmap object initialized from the input data
    * @throws IOException if the required amount of data cannot be read */
  private Bitmap readBinaryBitmap(final DataInputStream input) throws IOException {
    // read the first bitmap
    byte[] bitmap1 = readBytes(8, input);
    // read secondary bitmap (if present):
    if ((bitmap1[0] & (byte) 0x80) != 0) {
      final byte[] bitmap2 = readBytes(8, input);
      bitmap1 = ArrayUtils.addAll(bitmap1, bitmap2);
      // read tertiary bitmap (if present):
      if ((bitmap2[0] & (byte) 0x80) != 0) {
        final byte[] bitmap3 = readBytes(8, input);
        bitmap1 = ArrayUtils.addAll(bitmap1, bitmap3);
      }
    }
    return new Bitmap(bitmap1);
  }

  /** Read a hex string bitmap from the input stream
    * @return a Bitmap object initialized from the input data
    * @throws IOException if the required amount of data cannot be read */
  private Bitmap readHexBitmap(final DataInputStream input) throws IOException {
    // read the first bitmap
    final String bitmap1 = charCodec.getString(readBytes(16, input));

    Bitmap result = Bitmap.parse(bitmap1);

    // read secondary bitmap (if present):
    if (result.isBitmapPresent(Bitmap.Id.SECONDARY)) {
      final String bitmap2 = charCodec.getString(readBytes(16, input));
      result = Bitmap.parse(bitmap1 + bitmap2);
      // read tertiary bitmap (if present):
      if (result.isBitmapPresent(Bitmap.Id.TERTIARY)) {
        final String bitmap3 = charCodec.getString(readBytes(16, input));
        result = Bitmap.parse(bitmap1 + bitmap2 + bitmap3);
      }
    }
    return result;
  }

  /** Read the header field from the input stream
    * @param size of the header to be read
    * @return the header as a string
    * @throws IOException if the required amount of data cannot be read */
  public String readHeader(final int size, final DataInputStream input) throws IOException {
    final byte[] data = new byte[size];
    input.readFully(data);
    return charCodec.getString(data);
  }

  /** Helper method to read fully a f of bytes
    * @param size f of bytes to be read
    * @return a new byte array containing the data read
    * @throws IOException if the required amount of data cannot be read */
  protected byte[] readBytes(final int size, final DataInputStream input)
      throws IOException {
    try {
      final byte[] data = new byte[size];
      input.readFully(data);
      return data;
    } catch (final EOFException e) {
      throw new IOException("Failed to read fully " + size + " bytes from input stream", e);
    }
  }

}
