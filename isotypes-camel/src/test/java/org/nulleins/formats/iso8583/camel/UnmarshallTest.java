package org.nulleins.formats.iso8583.camel;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nulleins.formats.iso8583.Message;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.camel.config.TestConfiguration;
import org.nulleins.formats.iso8583.types.MTI;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.math.BigInteger;

/** Test ISO8583 message marshaling and unmarshalling in a camel route
  * @author phillipsr */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestConfiguration.class})
public class UnmarshallTest {
  /** this is the mock target of the message, a test double standing-in
      for some external server that is expecting an ISO8583 message */
  @EndpointInject(uri = "mock:receiver")
  private MockEndpoint responseEndpoint;

  /** SEDA is used as JMS stand-in: this is where we enqueue the request
      message, to kick-off the Camel route processing chain */
  @Produce(uri = "seda:queue:request")
  private ProducerTemplate template;

  @Resource
  private MessageFactory factory;

  private Message getResponseMessage() {
    final MTI messageType = MTI.create(0x0210);
    final Message result = Message.Builder()
        .messageTypeIndicator(messageType)
        .header("ISO015000077")
        .template(factory.getTemplate(messageType))
        .build();
    result.setFieldValue(2, new BigInteger("10101"));
    result.setFieldValue(4, new BigInteger("420"));
    result.setFieldValue(3, new BigInteger("456"));
    result.setFieldValue(7, DateTime.parse("2000-10-10T12:30+01:00"));
    result.setFieldValue(11, new BigInteger("123"));
    result.setFieldValue(12, LocalTime.parse("13:00:00"));
    result.setFieldValue(13, DateTime.parse("2000-10-10T00:00+01:00"));
    result.setFieldValue(17, DateTime.parse("2000-10-10T00:00+01:00"));
    result.setFieldValue(28, BigInteger.TEN);
    result.setFieldValue(30, BigInteger.ONE);
    result.setFieldValue(32, new BigInteger("1827271711"));
    result.setFieldValue(33, new BigInteger("827277722"));
    result.setFieldValue(37, "717266621");
    result.setFieldValue(41, "2001919");
    result.setFieldValue(42, "978817112");
    result.setFieldValue(43, "1000");
    result.setFieldValue(49, new BigInteger("400"));
    result.setFieldValue(54, "2");
    result.setFieldValue(60, "0");
    result.setFieldValue(102, "91817233372");
    return result;
  }

  private static final String TestMessage =
      "ISO0150000770210F238801588E0841000000000040000000510101000456000000000" +
          "420101012300000012313000010101010C00000010C00000001101827271711098" +
          "27277722717266621   2001919 978817112      1000                   " +
          "                 400001200101191817233372";
  /** Submit a test message to the route's input queue, and ensure that
    * the target server receives the message correctly transformed
    * @throws Exception when things don't work right */
  @Test
  public void
  testUnmarshalIsoMessage() throws Exception {
    responseEndpoint.expectedMessageCount(1);
    responseEndpoint.expectedBodiesReceived(getResponseMessage());

    template.sendBody("seda:queue:request", TestMessage);

    responseEndpoint.assertIsSatisfied();
  }

}