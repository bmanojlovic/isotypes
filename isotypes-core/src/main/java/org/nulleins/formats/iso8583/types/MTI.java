package org.nulleins.formats.iso8583.types;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Representation of an ISO8583 Message Type Indicator
  * @author phillipsr */
public final class MTI implements Comparable<MTI> {
  // regex defining valid MTI values
  private static final String ValidMTI = "[0129][123456789][0123489][012345]";

  private static final Pattern ValidMTIMatcher = Pattern.compile(ValidMTI);

  private final String value;

  /** Create an MTI instance from the supplied <code>code</code> character array
    * @throws IllegalArgumentException if the code is null, not 4 chars in length,
    *                                  or does not conform to the valid MTI pattern: <code>[0129][123456789][0123489][012345]</code> */
  private MTI(final char[] code) {
    Preconditions.checkNotNull(code);
    final String value = new String(code).trim();
    Preconditions.checkArgument(value.length() == 4,"MTI must not be null and four chars long [" + Arrays.toString(code) + "]");
    Preconditions.checkArgument(ValidMTIMatcher.matcher(value).matches(),
        "MTI must be numeric and conform to pattern: [" + ValidMTI + "]: got [" + value + "]");
    this.value = value;
  }

  /** @return an MTI object from <code>code</code>. its string representation */
  public static MTI create(final String code) {
    return new MTI(code.toCharArray());
  }

  /** @return an MTI object from <code>code</code>, its numeric representation */
  public static MTI create(final int code) {
    final char[] mti = new char[]{
        (char) (((code & 0xf000) >> 12) + 0x30),
        (char) (((code & 0x0f00) >> 8) + 0x30),
        (char) (((code & 0x00f0) >> 4) + 0x30),
        (char) (((code & 0x000f)) + 0x30)};
    return new MTI(mti);
  }

  private static final Map<Character, String> versions = new HashMap<Character, String>() {{
    put('0', "ISO 8583-1:1987");
    put('1', "ISO 8583-1:1993");
    put('2', "ISO 8583-1:2003");
    put('9', "Private");
  }};

  private static final Map<Character, String> messageClasses = new HashMap<Character, String>() {{
    put('1', "Authorization Message");
    put('2', "Financial Message");
    put('3', "File Actions Message");
    put('4', "Reversal Message");
    put('5', "Reconciliation Message");
    put('6', "Administrative Message");
    put('7', "Fee Collection Messages");
    put('8', "Network Management Message");
    put('9', "Reserved by ISO");
  }};

  private static final Map<Character, String> messageFunctions = new HashMap<Character, String>() {{
    put('0', "Request");
    put('1', "Request Response");
    put('2', "Advice");
    put('3', "Advice Response");
    put('4', "Notification");
    put('8', "Response acknowledgment");
    put('9', "Negative acknowledgment");
  }};

  private static final Map<Character, String> messageOrigins = new HashMap<Character, String>() {{
    put('0', "Acquirer");
    put('1', "Acquirer Repeat");
    put('2', "Issuer");
    put('3', "Issuer Repeat");
    put('4', "Other");
    put('5', "Other Repeat");
  }};

  private static final int VERSION_POS = 0;
  private static final int CLASS_POS = 1;
  private static final int FUNCTION_POS = 2;
  private static final int ORIGIN_POS = 3;

  public String getVersion() {
    return versions.get(value.charAt(VERSION_POS));
  }

  public String getMessageClass() {
    return messageClasses.get(value.charAt(CLASS_POS));
  }

  public String getMessageFunction() {
    return messageFunctions.get(value.charAt(FUNCTION_POS));
  }

  public String getMessageOrigin() {
    return messageOrigins.get(value.charAt(ORIGIN_POS));
  }

  public String describe() {
    return value + " (version=" + getVersion()
        + " class=" + getMessageClass()
        + " function=" + getMessageFunction()
        + " origin=" + getMessageOrigin() + ")";
  }


  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean equals(final Object other) {
    return this == other || !(other == null || other.getClass() != this.getClass()) && value.equals(((MTI) other).value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  public int intValue() {
    return Integer.parseInt(value, 16);
  }

  @Override
  public int compareTo(final MTI other) { return value.compareTo(other.value); }

}
