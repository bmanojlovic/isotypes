package org.nulleins.formats.iso8583;

import org.junit.Test;
import org.nulleins.formats.iso8583.config.CharMessageConfiguration;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Test message transformation using EBCDIC character set encoding for text fieldlist
 * @author phillipsr
 */
public class EncodingTest {
  private static final String EBCDIC_CHARSET = "IBM1047";
  private static final String ExpectMessage =
      "ISO0150000770200F238000108A180000000004000000000135432818929192"
          + "00101000000000120012121212006666661212001212041029937278626262"
          + "ATM-10101       DUB87                                   12353863579271840003C10";

  private final MessageFactory factory = CharMessageConfiguration.createMessageFactory();

  @Test
  public void codecTest()
      throws IOException {
    assertThat(Charset.isSupported(EBCDIC_CHARSET), is(true));
    final Charset ebcdicCodec = Charset.forName(EBCDIC_CHARSET);
    assertThat(ebcdicCodec, notNullValue());

    final byte[] ebcdta = "Hello World".getBytes(EBCDIC_CHARSET);
    final byte[] expect = new byte[]{(byte) 0xc8, (byte) 0x85, (byte) 0x93, (byte) 0x93, (byte) 0x96,
        0x40, (byte) 0xe6, (byte) 0x96, (byte) 0x99, (byte) 0x93, (byte) 0x84};

    assertThat(ebcdta, is(expect));
  }

  @Test
  public void testCreateMessageEbcdic() throws ParseException, IOException {
    assertThat(factory.getCharset().toString(), is("IBM1047"));
    final Message message = getTestMessage();

    assertThat(message.validate(), empty());

    final byte[] messageData = factory.getMessageData(message);
    assertThat(new String(messageData, EBCDIC_CHARSET), is(ExpectMessage));

    final Message readback = factory.parse(messageData);
    assertThat(new String(factory.getMessageData(readback), EBCDIC_CHARSET), is(ExpectMessage));
  }

  private Message getTestMessage() throws ParseException {
    final Date testDate = (new SimpleDateFormat("ddMMyyyy:HHmmss")).parse("12122012:121200");
    return factory.createByNames(MTI.create(0x0200), new HashMap<String, Object>() {{
        put("cardNumber", 5432818929192L);
        put("processingCode", 1010);
        put("amount", new BigInteger("1200"));
        put("transDateTime", testDate);
        put("stan", 666666);
        put("transTimeLocal", testDate);
        put("transDateLocal", testDate);
        put("acquierID", 1029);
        put("extReference", 937278626262L);
        put("cardTermId", "ATM-10101");
        put("cardTermName", "DUB87");
        put("msisdn", 353863579271L);
        put("currencyCode", 840);
        put("originalData", BigInteger.TEN);
      }});
  }
}
