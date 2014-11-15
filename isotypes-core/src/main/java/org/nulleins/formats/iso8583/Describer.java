/**
 *
 */
package org.nulleins.formats.iso8583;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Utility class that can describe an ISO8583 message in readable form, used for
 * logging and debugging purposes
 * <p/>
 * A Describer provides its message desc as an Iterable, to make it easy to
 * output multiple line descriptions in log files, UI, etc.
 * @author phillipsr
 */
class Describer implements Iterable<String> {
  protected final MessageTemplate template;
  protected final Map<Integer, Object> fields;

  public Describer(final MessageTemplate template, final Map<Integer, Object> fields) {
    Preconditions.checkNotNull(template);
    Preconditions.checkNotNull(fields);
    this.template = template;
    this.fields = fields;
  }

  @Override
  public String toString() {
    return Joiner.on('\n').join(this);
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {
      private final Integer[] fieldkeys = getKeys(fields);
      private boolean more = true;
      private int index;

      /** is there any more desc? */
      @Override
      public boolean hasNext() {
        return more;
      }

      private Integer[] getKeys(final Map<Integer, Object> fields) {
        Preconditions.checkNotNull(fields);
        final Set<Integer> integers = fields.keySet();
        final Integer[] result = integers.toArray(new Integer[integers.size()]);
        Arrays.sort(result);
        return result;
      }

      /** @return the next formatted line of desc */
      @Override
      public String next() {
        if (index == 0) {
          index++;
          more = fieldkeys.length > 0;
          return template.toString();
        }
        if (index == 1) {
          index++;
          return String.format("%3.3s: %-15.15s %-37.37s %-15.15s  %s",
              "F#", "Dimension:Type", "Value (is-a)", "Name", "Description");
        }
        more = index <= fieldkeys.length;
        final int key = fieldkeys[index - 2];
        final FieldTemplate field = template.getFields().get(key);
        final Object value = fields.get(key);
        index++;
        return String.format("%3d: %s:%-4s %-37s %-15s %s",
            key, field.getDimension(), field.getType(), "[" + value + "] ("
                + (value != null ? value.getClass().getSimpleName() : "") + ")",
            field.getName(), field.getDescription());
      }

      @Override
      public void remove() {
        throw new NoSuchMethodError("'remove' not implemented");
      }
    };
  }
}
