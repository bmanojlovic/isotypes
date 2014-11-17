package org.nulleins.formats.iso8583;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.nulleins.formats.iso8583.config.BankMessageConfiguration;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class MessageBuilderTest {

  private final MessageFactory factory = BankMessageConfiguration.createMessageFactory();

  @Test
  public void builderCreatesMessage() throws IOException {
    final Map<Integer, Object> fields = new HashMap<Integer,Object>() {{
      put(2, 10101);
      put(4, new BigDecimal("420.50"));
      put(3, "456");
      put(7, DateTime.parse("2014-10-10T12:30:00Z"));
      put(11, 123);
      put(12, LocalTime.parse("13:00:00"));
      put(13, DateTime.parse("2014-10-10T13:30:00Z"));
      put(17, DateTime.parse("2014-10-10T14:00:00Z"));
      put(28, BigDecimal.TEN);
      put(30, BigDecimal.ONE);
      put(32, 1827271711);
      put(33, 827277722);
      put(37, 717266621);
      put(41, 2001919);
      put(42, 978817112);
      put(43, "1000");
      put(49, 400);
      put(54, 2);
      put(60, 0);
      put(102, 91817233372L);
    }};
    final MTI messageType = MTI.create(0x0210);
    final Message subject = Message.Builder()
        .template(factory.getTemplate(messageType))
        .header("ISO015000077")
        .fields(fields)
        .build();


    System.out.println(subject.describe());

    System.out.print("\n[");
    factory.writeToStream(subject, System.out);
    System.out.println("]");
  }
}
