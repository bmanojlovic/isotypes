package org.nulleins.formats.iso8583.model;

/** Sample of a business value to include in an ISO message; this class
  * represents a credit card f that should not be transmitted in
  * the clear over the wire
  * @author phillipsr */
public final class CardNumber {
  private static final String STARS = "***************";
  private final long number;

  public CardNumber(final long number) {
    this.number = number;
  }

  /** Return the card number, obfuscating it if it looks
    * like a valid card number */
  @Override
  public String toString() {
    final String cardNum = number + "";
    final int length = cardNum.length();
    if (length <= 4) {
      return cardNum; // not a valid card number, don't bother with obfuscation
    }
    return cardNum.substring(0, 4) + STARS.substring(0, length - 6) + cardNum.substring(length - 2);
  }
}
