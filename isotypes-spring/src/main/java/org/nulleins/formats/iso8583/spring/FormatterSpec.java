package org.nulleins.formats.iso8583.spring;

import org.nulleins.formats.iso8583.formatters.TypeFormatter;

/** Specification holder for data formatter during parsing, will be used to instantiate
 * or reference the appropriate formatter bean when the MessageFactory is itself created
 * @author phillipsr */
public class FormatterSpec {
  private String type; // the type that this formatter will format (equates to the <field> type attribute)
  private Object spec; // either the full name of the formatter class, or a reference to a bean

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public Object getSpec() {
    return spec;
  }

  public void setSpec(final Object spec) {
    this.spec = spec;
  }

  TypeFormatter<?> getFormatter() throws InstantiationException, IllegalAccessException {
    final Object fmtr = this.getSpec();
    if (java.lang.Class.class.isInstance(fmtr)) { // action specifies a class to be instantiated
      final Class<?> fmtrClass = (Class<?>) fmtr;
      return (TypeFormatter<?>) fmtrClass.newInstance();
    } else { // formatter is either a bean reference or class name
      return (TypeFormatter<?>) fmtr;
    }
  }
}
