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
    final Map<Integer, Object> fields = new HashMap<>();
    final MTI messageType = MTI.create(0x0210);
    final Message subject = Message.Builder()
        .messageTypeIndicator(messageType)
        .header("ISO015000077")
        .template(factory.getTemplate(messageType))
        .fields(fields)
        .build();
    subject.setFieldValue(2, 10101);
    subject.setFieldValue(4, new BigDecimal("420.50"));
    subject.setFieldValue(3, "456");
    subject.setFieldValue(7, DateTime.parse("2014-10-10T12:30:00Z"));
    subject.setFieldValue(11, 123);
    subject.setFieldValue(12, LocalTime.parse("13:00:00"));
    subject.setFieldValue(13, DateTime.parse("2014-10-10T13:30:00Z"));
    subject.setFieldValue(17, DateTime.parse("2014-10-10T14:00:00Z"));
    subject.setFieldValue(28, BigDecimal.TEN);
    subject.setFieldValue(30, BigDecimal.ONE);
    subject.setFieldValue(32, 1827271711);
    subject.setFieldValue(33, 827277722);
    subject.setFieldValue(37, 717266621);
    subject.setFieldValue(41, 2001919);
    subject.setFieldValue(42, 978817112);
    subject.setFieldValue(43, "1000");
    subject.setFieldValue(49, 400);
    subject.setFieldValue(54, 2);
    subject.setFieldValue(60, 0);
    subject.setFieldValue(102, 91817233372L);

    System.out.println(subject.describe());

    System.out.print("\n[");
    factory.writeToStream(subject, System.out);
    System.out.println("]");
  }
}
