package org.nulleins.formats.iso8583.formatters;

import org.nulleins.formats.iso8583.formatters.NumberFormatter;
import org.nulleins.formats.iso8583.formatters.TypeFormatter;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.FieldType;
import org.nulleins.formats.iso8583.types.PostilionAddAmount;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * Information on up to 6 amounts and related account data for which specific data elements have not been defined.
 * Each amount is a fixed length field consisting of 5 data elements:
 * <p/>
 * 1002566C0000024260261003566C0000000000001001566C000002426026
 * 10 02 566 C 000,002,426,026 10 03 566 C000000000000 10 01 566 C 000002426026
 * Account type (positions 1 - 2)
 * Amount type (positions 3 - 4)
 * Currency code (positions 5 - 7)
 * Amount sign (position 8) - "C" or "D"
 * Amount (position 9 - 20)
 * @author phillipsr
 */
public class AddAmountsFormatter
    extends TypeFormatter<PostilionAddAmount[]> {
  private static final int Segmentlength = 20; // size of the recurring fields segment
  private final TypeFormatter<BigInteger> numberFormatter;

  public AddAmountsFormatter() {
    setCharset(CharEncoder.ASCII);
    numberFormatter = new NumberFormatter(CharEncoder.ASCII);
  }

  @Override
  public PostilionAddAmount[] parse(final String type, final Dimension dimension, final int length, final byte[] data)
      throws ParseException {
    final String field = new String(data);
    final List<PostilionAddAmount> result = new ArrayList<>(6);
    // split into 20-char fields
    for (int p = 0; p < field.length(); p += Segmentlength) {
      final String segment = field.substring(p, p + Segmentlength);
      final int accountType = numberFormatter.parse(
          FieldType.NUMERIC, dimension, 2, segment.substring(0, 2).getBytes()).intValue();
      final int amountType = numberFormatter.parse(
          FieldType.NUMERIC, dimension, 2, segment.substring(2, 4).getBytes()).intValue();
      final int currencyCode = numberFormatter.parse(
          FieldType.NUMERIC, dimension, 3, segment.substring(4, 7).getBytes()).intValue();
      final BigInteger amount = numberFormatter.parse(
          FieldType.NUMSIGNED, dimension, 13, segment.substring(7).getBytes());
      result.add(new PostilionAddAmount(accountType, amountType, currencyCode, amount));
    }
    return result.toArray(new PostilionAddAmount[result.size()]);
  }


  @Override
  public byte[] format(final String type, final Object data, final Dimension dimension) {
    if (data == null) {
      throw new IllegalArgumentException("Data to format cannot be null");
    }
    if (!(data instanceof PostilionAddAmount[])) {
      throw new IllegalArgumentException("Data must be an array of PostilionAddAmount values");
    }
    final PostilionAddAmount[] addAmounts = (PostilionAddAmount[]) data;
    if (addAmounts.length < 1 || addAmounts.length > 6) {
      throw new IllegalArgumentException("Array of PostilionAddAmount must contain 1 to 6 values");
    }
    final StringBuilder result = new StringBuilder(addAmounts.length * 20);
    for (final PostilionAddAmount item : addAmounts) {
      if (item == null) {
        throw new IllegalArgumentException("PostilionAddAmount null in data array");
      }
      result.append(new String(
          numberFormatter.format(FieldType.NUMERIC, item.getAccountType(), Dimension.parse("fixed(2)"))));
      result.append(new String(
          numberFormatter.format(FieldType.NUMERIC, item.getAmountType(), Dimension.parse("fixed(2)"))));
      result.append(new String(
          numberFormatter.format(FieldType.NUMERIC, item.getCurrencyCode(), Dimension.parse("fixed(3)"))));
      result.append(new String(
          numberFormatter.format(FieldType.NUMSIGNED, item.getAmount(), Dimension.parse("fixed(13)"))));
    }
    return result.toString().getBytes();
  }


  @Override
  public boolean isValid(final Object value, final String type, final Dimension dimension) {
    if (!(value instanceof PostilionAddAmount[])) {
      return false;
    }
    final PostilionAddAmount[] addAmounts = (PostilionAddAmount[]) value;
    if (addAmounts.length < 1 || addAmounts.length > 6) {
      return false;
    }
    for (final PostilionAddAmount item : addAmounts) {
      if (item == null) {
        return false;
      }
      if (item.getAccountType() == null) {
        return false;
      }
      if (item.getAmountType() == null) {
        return false;
      }
      if (item.getAmount() == null) {
        return false;
      }
      if (item.getCurrencyCode() == null) {
        return false;
      }
    }
    return true;
  }

}
