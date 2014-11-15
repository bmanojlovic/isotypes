package org.nulleins.formats.iso8583;

import com.typesafe.config.ConfigException;
import org.junit.Test;
import org.nulleins.formats.iso8583.schema.MessageConfig;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ConfigTest {

  private static final String CONFIG_PATH = "messageTest.conf";

  @Test
  public void configuresFactory() {
    final MessageFactory factory = MessageConfig.configure(CONFIG_PATH);
    assertThat(factory.getId(), is("bankMessages"));
    assertThat(factory.getHeader(), is("ISO015000077"));
    assertThat(factory.getDescription(), is("TestBank banking messages"));
    assertThat(factory.getBitmapType(), is(BitmapType.HEX));
    assertThat(factory.getContentType(), is(ContentType.TEXT));
    assertThat(factory.getCharset(), is(CharEncoder.ASCII));
    assertThat(factory.getCharset().hashCode(), is(CharEncoder.ASCII.hashCode()));
    assertThat(factory.getTemplates().size(), is(2));
  }

  @Test
  public void configureFactoryFromUrl() throws MalformedURLException {
    final URL configUrl = this.getClass().getResource("/"+CONFIG_PATH);
    final MessageFactory factory = MessageConfig.configure(configUrl);
    final MTI type = MTI.create(0x0200);
    final Message message = factory.create(type);
    assertThat(message.getMTI(), is(type));
    assertThat(message.getHeader(), is("ISO015000077"));
  }

  private static final String SampleConfig = "schema {"+
      "  id = bankMessages, description = 'TestBank banking messages', header = ISO015000077,"+
      "  bitmapType = hex, contentType = text, charset = ascii,"+
      "  messages: ["+
      "    {"+
      "      type = 0200, name = Transaction Request,"+
      "      fields: {"+
      "          2: {name = accountNumber, desc = 'Primary Account Number', dim = llvar(19), type = n},"+
      "          3: {name = processingCode, desc = 'Processing Code', dim = fixed(6), type = n},"+
      "          4: {name = amount, desc = 'Transaction amount (cents)', dim = fixed(12), type = n},"+
      "          7: {name = transDateTime, desc = 'Transmission Date and Time', dim = fixed(10), type = date},"+
      "         11: {name = stan, desc = 'System Trace Audit Number', dim = fixed(6), type = n},"+
      "         12: {name = transTimeLocal, desc = 'Local transaction time', dim = fixed(6), type = time},"+
      "         13: {name = transDateLocal, desc = 'Local transaction date', dim = fixed(4), type = date},"+
      "         17: {name = captureDate, desc = 'Capture date', dim = fixed(4), type = date},"+
      "         28: {name = transactionFee, desc = 'Transaction fee', dim = fixed(9), type = xn},"+
      "         30: {name = processingFee, desc = 'TX processing fee', dim = fixed(9), type = xn},"+
      "         32: {name = acquierID, desc = 'Acquiring Institution ID', dim = llvar(11), type = n},"+
      "         33: {name = forwarderID, desc = 'Forwarding Institution ID', dim = llvar(11), type = n},"+
      "         37: {name = rrn, desc = 'Retrieval Reference Number', dim = fixed(12), type = anp},"+
      "         41: {name = cardTermId, desc = 'Card Acceptor Terminal ID', dim = fixed(8), type = ans},"+
      "         42: {name = cardAcceptorId, desc = 'Card Acceptor ID Code', dim = fixed(15), type = ans},"+
      "         43: {name = cardAcceptorLoc, desc = 'Card Acceptor Location Name', dim = fixed(40), type = CALf},"+
      "         49: {name = currencyCode, desc = 'Transaction Currency Code', dim = fixed(3), type = n},"+
      "         60: {name = adviceCode, desc = 'Advice/reason code', dim = lllvar(999), type = an},"+
      "        102: {name = accountId1, desc = 'Account Identification 1', dim = llvar(28), type = ans}"+
      "      }"+
      "    }]}";

  @Test
  public void configureFromStream() {
    final ByteArrayInputStream bais = new ByteArrayInputStream(SampleConfig.getBytes());
    final MessageFactory factory = MessageConfig.configure(bais);
    final MTI type = MTI.create(0x0200);
    final Message message = factory.create(type);
    assertThat(message.getMTI(), is(type));
    assertThat(message.getHeader(), is("ISO015000077"));
  }

  @Test(expected = ConfigException.Generic.class)
  public void failsWithBadConfig() {
    MessageConfig.configure("badConfigTest.conf");
  }
}
