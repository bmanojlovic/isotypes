/**
 *
 */
package org.nulleins.formats.iso8583.types;

import java.math.BigInteger;

/**
 * @author phillipsr
 */
public class PostilionAddAmount {
  private Integer accountType;
  private Integer amountType;
  private Integer currencyCode;
  private BigInteger amount;

  public PostilionAddAmount(final int accountType, final int amountType, final int currencyCode, final BigInteger amount) {
    this.accountType = accountType;
    this.amountType = amountType;
    this.currencyCode = currencyCode;
    this.amount = amount;
  }

  @Override
  public String toString() {
    return String.format("%02d %02d %03d %d", accountType, amountType, currencyCode, amount);
  }

  public Integer getAccountType() {
    return accountType;
  }

  public Integer getAmountType() {
    return amountType;
  }

  public Integer getCurrencyCode() {
    return currencyCode;
  }

  public BigInteger getAmount() {
    return amount;
  }

  public void setAccountType(final int accountType) {
    this.accountType = accountType;
  }

  public void setAmountType(final int amountType) {
    this.amountType = amountType;
  }

  public void setCurrencyCode(final int currencyCode) {
    this.currencyCode = currencyCode;
  }

  public void setAmount(final BigInteger amount) {
    this.amount = amount;
  }

}
