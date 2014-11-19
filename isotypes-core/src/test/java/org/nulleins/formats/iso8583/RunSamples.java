package org.nulleins.formats.iso8583;

import org.junit.Test;
import org.nulleins.formats.iso8583.config.SampleMessageConfiguration;
import org.nulleins.formats.iso8583.model.CardNumber;
import org.nulleins.formats.iso8583.model.MessageSample;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;

import static org.junit.Assert.fail;

/**
 * @author phillipsr
 */
public class RunSamples {
  private final MessageFactory factory = SampleMessageConfiguration.createMessageFactory();

  /** Business-significant fields to include in message */
  private static final PaymentRequestBean Request = new PaymentRequestBean() {{
    setCardNumber(new CardNumber(12345678901234L));
    setAmount(BigInteger.TEN);
    setCurrencyCode(978);
    setMsisdn(353863447681L);
    setExtReference(1234);
    setCardTermId("ATM-1234");
    setCardTermName("BOI/ATM/D8/SJG");
    setOriginalData(0);
    setAcquierID(3031);
  }};


  @Test
  public void runSamples() throws IOException, ParseException {
    final MessageSample sample = new MessageSample(factory);

    try {
      sample.sendMessage(0x0200, Request);
    } catch (final MessageException e) {
      for (final String line : e.getReasons()) {
        System.err.println(line);
      }
      e.printStackTrace();
      fail(e.getMessage());
    } catch (final Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
