package org.nulleins.formats.iso8583.formatters;

import com.google.common.base.Preconditions;
import org.nulleins.formats.iso8583.MessageException;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;

import java.text.ParseException;

/** Generic definition of a type formatter, defining the format and parse
  * methods that concrete formatters need to support
  * @author phillipsr */
public abstract class TypeFormatter<T> {
  private CharEncoder charset;

  /** @return string representation of the data supplied, interpreted according to the
    * field type and dim specification supplied
    * @param type      variant of the type specified for the field
    * @param dimension specifies if fixed or variable and the required size
    * @param position  of field value in the input
    * @param data      the bytes to be parsed
    * @throws IllegalArgumentException if the data is null or invalid the field type
    * @throws ParseException           if the data cannot be parsed to the specified type */
  public abstract T parse(String type, Dimension dimension, int position, byte[] data) throws ParseException;

  /** @return with a byte array representing the data supplied, formatted according to the
    * field type and dim specified
    * @param type      variant of the alpha type specified for the field
    * @param data      the object to be formatted
    * @param dimension specifies if fixed or variable and the required size
    * @throws IllegalArgumentException if the data is null or invalid as an alpha string
    * @throws MessageException         if the data supplied results in the maximum field length being exceeded */
  public abstract byte[] format(String type, Object data, Dimension dimension);

  /** @return true if the supplied value a valid instance of the type/dim specified
    * @param value     candidate value to store in field
    * @param type      (sub-type) of the field
    * @param dimension storage type & size information */
  public abstract boolean isValid(Object value, String type, Dimension dimension);

  /** Specify the charset to be used when reading or writing character data
    * @param charset to be used when formatting alpha-type field values
    *                (see {@link java.nio.charset.Charset})
    * @throws IllegalArgumentException if the charset is null, or not supported by the JVM */
  protected void setCharset(final CharEncoder charset) {
    Preconditions.checkNotNull(charset,"charset cannot be null/empty");
    this.charset = charset;
  }

  /** @return A string representation of the <code>data</code> supplied, decode the supplied data using
    * the configured charset
    * @throw RuntimeException if data cannot be translated to the appropriate charset */
  protected String decode(final byte[] data) {
    Preconditions.checkNotNull(charset);
    return charset.getString(data);
  }

}
