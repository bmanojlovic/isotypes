/**
 *
 */
package org.nulleins.formats.iso8583;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
  protected final ImmutableMap<Integer, Optional<Object>> fields;

  public Describer(final MessageTemplate template, final Map<Integer, Optional<Object>> fields) {
    Preconditions.checkNotNull(template);
    Preconditions.checkNotNull(fields);
    this.template = template;
    this.fields = ImmutableMap.copyOf(fields);
  }

  @Override
  public String toString() {
    return Joiner.on('\n').join(this);
  }

  @Override
  public Iterator<String> iterator() {
    return new Iterator<String>() {
      private final Integer[] fieldkeys = getKeys(fields);
      private final AtomicBoolean more = new AtomicBoolean (true);
      private final AtomicInteger index = new AtomicInteger(0);

      /** is there any more desc? */
      @Override
      public boolean hasNext() {
        return more.get();
      }

      private Integer[] getKeys(final ImmutableMap<Integer, Optional<Object>> fields) {
        Preconditions.checkNotNull(fields);
        final Set<Integer> integers = fields.keySet();
        final Integer[] result = integers.toArray(new Integer[integers.size()]);
        Arrays.sort(result);
        return result;
      }

      /** @return the next formatted line of desc */
      @Override
      public String next() {
        if (index.get() == 0) {
          index.getAndIncrement();
          more.set(fieldkeys.length > 0);
          return template.toString();
        }
        if (index.get() == 1) {
          index.getAndIncrement();
          return String.format("%3.3s: %-15.15s %-37.37s %-15.15s  %s",
              "F#", "Dimension:Type", "Value (is-a)", "Name", "Description");
        }
        more.set(index.get() <= fieldkeys.length);
        final int key = fieldkeys[index.get() - 2];
        index.getAndIncrement();
        final FieldTemplate field = template.getFields().get(key);
        final Optional<Object> value = fields.containsKey(key) ? fields.get(key) : Optional.absent();
        return formatField(key, field, value);
      }

      private String formatField(final int key, final FieldTemplate field, final Optional<Object> value) {
        Preconditions.checkNotNull(field, "field #" + key + " missing from template, value: " + value.or("<absent>"));
        return String.format("%3d: %s:%-4s %-37s %-15s %s",
            key, field.getDimension(), field.getType(),
            "[" + value.or("<absent>") + "] ("
                + (value.isPresent() ? value.get().getClass().getSimpleName() : "") + ")",
            field.getName(), field.getDescription());
      }

      @Override
      public void remove() { }
    };
  }
}
