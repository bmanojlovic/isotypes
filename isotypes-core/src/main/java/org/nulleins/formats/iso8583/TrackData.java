package org.nulleins.formats.iso8583;


import com.google.common.base.Preconditions;

/**  Representation of the Track Data stored on a financial transaction card's magnetic strips (see ISO7813)
 * <p/>
 * @author phillipsr */
public class TrackData {
  public enum Track {TRACK1, TRACK2, TRACK3}

  private final Track type;
  private final String[] name;
  private final long primaryAccountNumber; // PAN : Primary Account Number, up to 19 digits, as defined in ISO/IEC 7812-1
  private final int expirationDate;        // ED : Expiration date, YYMM
  private final int serviceCode;           // SC : Service code, 3 digits
  private final String discretionaryData;  // DD : Discretionary data, balance of available digits

  private TrackData(final Track type, final String[] name, final long primaryAccountNumber, final int expirationDate,
                    final int serviceCode, final String discretionaryData) {
    //Preconditions.checkArgument (type == Track.TRACK1 || name != null, "Cannot set name field for " + type.toString ());
    //Preconditions.checkArgument (type != Track.TRACK1 name != null && name.length == 4, "name must be an array of four elements");
    this.name = name;
    this.type = type;
    this.primaryAccountNumber = primaryAccountNumber;
    this.expirationDate = expirationDate;
    this.serviceCode = serviceCode;
    this.discretionaryData = discretionaryData;
  }

  public long getPrimaryAccountNumber() {
    return primaryAccountNumber;
  }
  public int getExpirationDate() {
    return expirationDate;
  }
  public int getServiceCode() { return serviceCode; }
  public String getDiscretionaryData() {
    return discretionaryData;
  }

  /** @return the name field as an array of elements" {Surname, First Name or Initial, Middle Name or Initial, Title}
    * @throws IllegalStateException if this method is called on Track2 or Track3 data objects */
  public String[] getName() {
    Preconditions.checkState(type == Track.TRACK1, "No name field available for " + type.toString());
    return name;
  }

  public Track getType() {
    return type;
  }

  /** @return the canonical string representation of the track data, including field separators
    * appropriate for the variant; does not include start- and end-sentinel characters, nor the
    * calculated LRC value */
  @Override
  public String toString() {
    return (type == Track.TRACK1 ? "B" : "") +
            primaryAccountNumber + (type == Track.TRACK1 ? ("^" + formatName()) : "") +
            (type == Track.TRACK1 ? "^" : "=") +
            expirationDate + serviceCode +
            (discretionaryData != null ? discretionaryData : "");
  }

  /** @return Canonical string representing the name field according to the ISO7813 standard
    * @throws IllegalStateException if this method is called on Track2 or Track3 data objects */
  public String formatName() {
    Preconditions.checkState(type == Track.TRACK1, "No name field available for " + type.toString());
    Preconditions.checkState(name != null);
    return name[0] + "/" + name[1] + (!name[2].isEmpty() ? (" " + name[2]) : "") + "." + name[3];
  }

  public static Builder Builder() { return new Builder(); }
  public static class Builder {
    private Track type;
    private long primaryAccountNumber;
    private String[] name;
    private int expirationDate;
    private int serviceCode;
    private String discretionaryData;

    public Builder type(final Track type) {
      this.type = type;
      return this;
    }
    public Builder primaryAccountNumber(final long primaryAccountNumber) {
      this.primaryAccountNumber = primaryAccountNumber;
      return this;
    }
    public Builder name(final String[] name) {
      this.name = name;
      return this;
    }
    public Builder expirationDate(final int expirationDate) {
      this.expirationDate = expirationDate;
      return this;
    }
    public Builder serviceCode(final int serviceCode) {
      this.serviceCode = serviceCode;
      return this;
    }
    public Builder discretionaryData(final String discretionaryData) {
      this.discretionaryData = discretionaryData;
      return this;
    }
    public TrackData build() {
      return new TrackData (type,name,primaryAccountNumber,expirationDate,serviceCode,discretionaryData);
    }
  }

}
