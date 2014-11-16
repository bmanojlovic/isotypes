package org.nulleins.formats.iso8583;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class Functions {

  public static Function<Object,Optional<Object>> toOptional() {
    return new Function<Object,Optional<Object>>() {
      @Override
      public Optional<Object> apply(final Object input) {
        return Optional.fromNullable(input);
      }
    };
  }

  public static Function<Optional<Object>, Object> fromOptional() {
    return new Function<Optional<Object>, Object>() {
      @Override
      public Object apply(final Optional<Object> input) {
        return input.orNull();
      }
    };
  }

  private Functions() {}

}