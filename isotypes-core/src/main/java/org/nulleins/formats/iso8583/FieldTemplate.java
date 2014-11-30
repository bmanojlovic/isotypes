package org.nulleins.formats.iso8583;

import com.google.common.base.Preconditions;
import org.nulleins.formats.iso8583.formatters.TypeFormatter;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.MTI;

/** Definition of an ISO8583 messageTemplate field, capable for formatting and parsing messageTemplate
 * fields, based upon its configuration
 * <p/>
 * Example:</br>
 * <code>
 * &lt;field f="2" type="LLVAR" name="CardNumber" desc="Payment Card Number" /&gt;
 * </code>
 * @author phillipsr */
public class FieldTemplate {
  private final String type;
  private final Dimension dimension;
  private final int number;
  private final String name;
  private final String description;
  private final String defaultValue;
  private final String autogenSpec;
  private final boolean optional;

  private FieldTemplate (
      final int number, final String type, final Dimension dimension,
      final String name, final String description, final String defaultValue,
      final boolean optional, final String autogenSpec) {
    Preconditions.checkNotNull (type);
    Preconditions.checkNotNull (dimension);
    this.number = number;
    this.type = type;
    this.dimension = dimension;
    this.name = name;
    this.description = description != null ? description : "";
    this.optional = optional;
    this.defaultValue = defaultValue;
    this.autogenSpec = autogenSpec;
  }

  public int getNumber() { return number; }
  public String getType() { return type; }
  public Dimension getDimension() {
    return dimension;
  }
  public String getName() { return name; }
  public String getDescription() {
    return description;
  }
  public String getDefaultValue() {
    return defaultValue;
  }
  public String getAutogen() {
    return autogenSpec;
  }
  public boolean isOptional() {
    return optional;
  }

   /**
   * Use this field definition to format the data supplied
   * @param value
   * @param formatter
   * @return data
   * @throws MessageException if the formatter failed to create a field of the correct size
   */
  public byte[] format(final Object value, final TypeFormatter<?> formatter) {
    final String result;
    try {
      result = new String(formatter.format(type, value, this.dimension));
    } catch (final Exception e) {
      throw new MessageException("Could not format data [" + value + "] for field " + this, e);
    }

    if (this.dimension.getType() == Dimension.Type.FIXED && result.length() != dimension.getLength()) {
      throw new MessageException(this + ": Formatter did not format fixed field to specified length, value=[" + result + "]");
    }
    if (this.dimension.getType() == Dimension.Type.VARIABLE && result.length() > dimension.getLength() + dimension.getVSize()) {
      throw new MessageException(this + ": Formatter exceeded maximum length for variable field; value=[" + result + "]");
    }
    return result.getBytes();
  }

  @Override
  public String toString() {
    return "Field nb=" + this.getNumber()
        + " name=" + this.getName()
        + " type=" + this.getType()
        + " dim=" + dimension
        + (autogenSpec != null ? (" autogen=[" + autogenSpec + "]") : "")
        + (defaultValue != null ? (" default=[" + defaultValue + "]") : "");
  }

  /** thread-safe builder, to de-clutter message building (create one and reuse safely) */
  public static ThreadLocal<Builder> localBuilder() {
    return new ThreadLocal<Builder>() {
      @Override
      protected Builder initialValue() {
        return new Builder();
      }
    };
  }

  public static class Builder {
    private Integer number;
    private String type;
    private Dimension dimension;
    private String name;
    private String description = "";
    private String defaultValue;
    private String autogenSpec;
    private boolean optional;

    public Builder f(final int number) {
      this.number = number;
      return this;
    }

    public Builder type(final String type) {
      this.type = type;
      return this;
    }

    public Builder dimension(final Dimension dimension) {
      this.dimension = dimension;
      return this;
    }

    public Builder dim(final String dimension) {
      return dimension(Dimension.parse(dimension));
    }

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder desc(final String description) {
      this.description = description;
      return this;
    }

    public Builder defaultValue(final String value) {
      this.defaultValue = value;
      return this;
    }

    public Builder autogenSpec(final String autogenSpec) {
      this.autogenSpec = autogenSpec;
      return this;
    }

    public Builder optional() {
      optional = true;
      return this;
    }

    public FieldTemplate build() {
      final FieldTemplate result = new FieldTemplate(number, type, dimension, name, description,defaultValue,optional,autogenSpec);
      this.number = null;
      this.name = null;
      this.description = "";
      this.autogenSpec = null;
      this.defaultValue = null;
      this.dimension = null;
      this.optional = false;
      return result;
    }
  }
}
