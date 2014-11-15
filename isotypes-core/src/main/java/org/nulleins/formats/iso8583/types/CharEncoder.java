package org.nulleins.formats.iso8583.types;

import com.google.common.base.Preconditions;

import java.nio.charset.Charset;

/** Holds the character set that should be used for text encoding and decoding,
  * and provides the methods for encoding/decoding to/from byte[]/String
  * @author phillipsr */
public class CharEncoder {
  public static final CharEncoder ASCII = new CharEncoder("US-ASCII");
  private final Charset charset;

  /** Set the charset that should be used for writing text field values
    * @param charsetName JVM name of charset (see {@link java.nio.charset.Charset})
    * @throws IllegalArgumentException if the charset is null, or not supported by the JVM */
  public CharEncoder(final String charsetName) {
    Preconditions.checkArgument(charsetName != null && !charsetName.isEmpty(),"charset cannot be null/empty");
    Preconditions.checkArgument(Charset.isSupported(charsetName),
        "charset [" + charsetName + "] not supported by JVM");
    this.charset = Charset.forName(charsetName);
  }

  @Override
  public String
  toString() {
    return charset.name();
  }

  /** @return a String in this character encoding, initialized from the byte data supplied
    * @param data bytes to be converted */
  public String getString(final byte[] data) {
    return new String(data, charset);
  }

  /** @return a byte array in this character encoding, initialized from the byte data supplied
    * @param data String to be converted a byte character using this character encoding */
  public byte[] getBytes(final String data) {return data.getBytes(charset);
  }

  /** @return a byte array in this character encoding, initialized from the byte data supplied
    * @param data bytes to be converted */
  public byte[] getBytes(final byte[] data) {
    return getBytes(new String(data));
  }

  @Override
  public boolean equals(final Object o) {
    return this == o || !(o == null || getClass() != o.getClass()) && charset.equals(((CharEncoder) o).charset);
  }

  @Override
  public int hashCode() {
    return charset.hashCode();
  }
}
