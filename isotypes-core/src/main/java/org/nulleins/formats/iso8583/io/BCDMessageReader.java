package org.nulleins.formats.iso8583.io;

import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.types.BCD;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.FieldType;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/** MessageReader that expects numeric field value to be encoded in BCD
  * @author phillipsr */
public class BCDMessageReader extends MessageReader {

  /** Reader capable of reading BCD-encoded message data */
  public BCDMessageReader(final CharEncoder charset) {
    super.charCodec = charset;
  }

  private static final Set<String> NumericTypes = new HashSet<String>() {{
        add(FieldType.NUMSIGNED);
        add(FieldType.NUMERIC);
        add(FieldType.DATE);
        add(FieldType.TIME);
        add(FieldType.EXDATE);
      }};

  /** {@inheritDoc} */
  @Override
  public byte[] readField(final FieldTemplate field, final DataInputStream input) throws IOException {
    int length = field.getDimension().getLength();
    if (field.getDimension().getType() == Dimension.Type.VARIABLE) {
      // LVAR and LLVAR: 1 byte length specifier, LLLVAR: 2 bytes required:
      final byte[] var = new byte[(int) Math.ceil(field.getDimension().getVSize() / 2.0)];
      input.readFully(var);
      length = Integer.parseInt(BCD.toString(var));
    }
    if (field.getType().equals(FieldType.TRACKDATA)) {
      return readTrackData(length, input);
    }
    if (NumericTypes.contains(field.getType())) {
      boolean negative = false;
      if (field.getType().equals(FieldType.NUMSIGNED)) {
        final byte sign = input.readByte();
        length -= 2; // read two nibbles
        negative = sign == 0x0d;
      }
      return readNumeric(field, length, negative, input);
    }
    return readBytes(length, input);
  }

  /** Read a BCD-encoded numeric field value from the input stream,
    * returning a character representation of the numeric value
    * @param field    template describing the field to be read
    * @param length   of the field in the input
    * @param negative flag: is the field to be interpreted as a negative value?
    * @return a byte array representing the numeric value read, as characters
    * @throws IOException if the required amount of data could not be read */
  private byte[] readNumeric(final FieldTemplate field, final int length, final boolean negative, final DataInputStream input) throws IOException {
    // packed BCD, half length (rounded-up):
    final byte[] data = readBytes((int) Math.ceil(length / 2.0), input);
    String result = BCD.toString(data);
    final int rlen = result.length();
    if (rlen > length) // got left-padded zero when converted to BCD
    {
      result = result.substring(rlen - length, rlen);
    }
    if (field.getType().equals(FieldType.NUMSIGNED)) {
      result = (negative ? "D" : "C") + result;
    }
    return result.getBytes();
  }

  /** Read binary track data from the input stream
    * @param length of the field in the input
    * @return character representation of the track data
    * @throws IOException if the required amount of data could not be read */
  private byte[] readTrackData(final int length, final DataInputStream input) throws IOException {
    final String name = "";
    String pan = "";
    final String exdate;
    final String scode;
    final String descr = "";
    final byte[] data = new byte[(int) Math.ceil(length / 2.0)];
    input.readFully(data);
    int pos = 0;
    if (data[0] == 0x37) { // start sentinel
      pos++;
    }
    for (; pos < data.length; pos++) {
      final byte b = data[pos];
      if ((b & 0xd0) == 0xd0) {
        pan = BCD.toString(Arrays.copyOfRange(data, 0, pos));
        break;
      }
      if ((b & 0x0d) == 0x0d) {
        pan = BCD.toString(Arrays.copyOfRange(data, 0, pos))
            + ((b & 0xf0) >> 4);
        break;
      }
    }
    exdate = BCD.toString(Arrays.copyOfRange(data, pos + 1, pos + 3));
    scode = BCD.toString(Arrays.copyOfRange(data, pos + 3, pos + 3 + 2)).substring(0, 3);

    return (pan + name + "=" + exdate + scode + descr).getBytes();
  }

  /** {@inheritDoc} */
  @Override
  public MTI readMTI(final DataInputStream input) throws IOException {
    final byte[] data = readBytes(2, input);
    return MTI.create(
        String.valueOf((char) (((data[0] & 0xf0) >> 4) + 0x30))
            + (char) ((data[0] & 0x0f) + 0x30)
            + (char) (((data[1] & 0xf0) >> 4) + 0x30)
            + (char) ((data[1] & 0x0f) + 0x30));
  }

}
