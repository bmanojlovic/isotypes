package org.nulleins.formats.iso8583;

import java.math.BigInteger;

public class PaymentRequestBean {
  public CardNumber getCardNumber() {
    return cardNumber;
  }

  public void setCardNumber(final CardNumber cardNumber) {
    this.cardNumber = cardNumber;
  }

  public BigInteger getAmount() {
    return amount;
  }

  public void setAmount(final BigInteger amount) {
    this.amount = amount;
  }

  public int getAcquierID() {
    return acquierID;
  }

  public void setAcquierID(final int acquierID) {
    this.acquierID = acquierID;
  }

  public long getExtReference() {
    return rrn;
  }

  public void setExtReference(final long rrn) {
    this.rrn = rrn;
  }

  public String getCardTermId() {
    return cardTermId;
  }

  public void setCardTermId(final String cardTermId) {
    this.cardTermId = cardTermId;
  }

  public String getCardTermName() {
    return cardTermName;
  }

  public void setCardTermName(final String cardTermName) {
    this.cardTermName = cardTermName;
  }

  public long getMsisdn() {
    return msisdn;
  }

  public void setMsisdn(final long msisdn) {
    this.msisdn = msisdn;
  }

  public int getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(final int currencyCode) {
    this.currencyCode = currencyCode;
  }

  private CardNumber cardNumber;
  private BigInteger amount;
  private int acquierID;
  private long rrn;
  private String cardTermId;
  private String cardTermName;
  private long msisdn;
  private int currencyCode;
  private int originalData;

  public int getOriginalData() {
    return originalData;
  }

  /**
   * @param i
   */
  public void setOriginalData(final int data) {
    this.originalData = data;
  }
}