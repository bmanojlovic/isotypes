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
import java.util.HashMap;
import java.util.Map;

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
    final Map<Integer, Object> fields = new HashMap<Integer,Object>() {{
      put(2, new BigInteger("10101"));
      put(4, new BigInteger("420"));
      put(3, new BigInteger("456"));
      put(7, DateTime.parse("2000-10-10T12:30+01:00"));
      put(11, new BigInteger("123"));
      put(12, LocalTime.parse("13:00:00"));
      put(13, DateTime.parse("2000-10-10T00:00+01:00"));
      put(17, DateTime.parse("2000-10-10T00:00+01:00"));
      put(28, BigInteger.TEN);
      put(30, BigInteger.ONE);
      put(32, new BigInteger("1827271711"));
      put(33, new BigInteger("827277722"));
      put(37, "717266621");
      put(41, "2001919");
      put(42, "978817112");
      put(43, "1000");
      put(49, new BigInteger("400"));
      put(54, "2");
      put(60, "0");
      put(102, "91817233372");
    }};
    return Message.Builder()
        .header("ISO015000077")
        .template(factory.getTemplate(messageType))
        .fields(fields)
        .build();
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