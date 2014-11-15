package org.nulleins.formats.iso8583;

import org.junit.Test;
import org.nulleins.formats.iso8583.model.CardNumber;
import org.nulleins.formats.iso8583.model.PaymentRequestBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;

import static org.junit.Assert.fail;

/**
 * @author phillipsr
 */
public class RunSamples {
  private static final String SampleContextPath = "classpath:org/nulleins/formats/iso8583/samples/MessageSample-context.xml";

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
    final ApplicationContext context = new ClassPathXmlApplicationContext(SampleContextPath);
    final MessageSample sample = new MessageSample(context.getBean("sampleMessages",MessageFactory.class));

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
