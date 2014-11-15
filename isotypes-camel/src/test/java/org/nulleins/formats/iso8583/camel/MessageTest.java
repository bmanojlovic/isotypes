package org.nulleins.formats.iso8583.camel;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigInteger;
import java.util.Map;

/** Test ISO8583 message marshaling and unmarshalling in a camel route
  * @author phillipsr */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MessageTest {
  /** this is the mock target of the message, a test double standing-in
      for some external server that is expecting an ISO8583 message */
  @EndpointInject(uri = "mock:receiver", context = "testContext")
  private MockEndpoint targetServer;

  /** SEDA is used as JMS stand-in: this is where we enqueue the request
      message, to kick-off the Camel route processing chain */
  @Produce(uri = "seda:queue:request", context = "testContext")
  private ProducerTemplate template;

  private static final Map<String, Object> TestMessage = ImmutableMap.<String, Object>builder()
      .put("cardNumber", 5432818929192L)
      .put("processingCode", 1010)
      .put("amount", 1200)
      .put("transDateTime", DateTime.parse("2000-03-04T05:41:33.000Z"))
      .put("transDateLocal", DateTime.parse("2001-12-12"))
      .put("transTimeLocal", LocalTime.parse("06:22:00"))
      .put("stan", 11)
      .put("acquierID", 1029)
      .put("extReference", 937278626262L)
      .put("cardTermId", "ATM-10101")
      .put("cardTermName", "DUB87")
      .put("msisdn", 353863579271L)
      .put("currencyCode", 840)
      .put("originalData", BigInteger.TEN).build();

  private static final String ExpectMessage =
      "ISO0150000770200F238000108A180000000004000000000135432818929192001010" +
          "00000000120003040541330000110622001212041029937278626262ATM-10101       " +
          "DUB87                                   12353863579271840003C10";

  /** Submit a test message to the route's input queue, and ensure that
    * the target server receives the message correctly transformed
    * @throws Exception when things don't work right */
  @Test
  public void
  testMarshalIsoMessage() throws Exception {
    targetServer.expectedMessageCount(1);
    targetServer.expectedBodiesReceived(ExpectMessage);

    template.sendBodyAndHeader("seda:queue:request", TestMessage, "ISO8583-MTI", 0x0200);

    targetServer.assertIsSatisfied();
  }

}