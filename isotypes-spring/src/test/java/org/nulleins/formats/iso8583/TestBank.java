package org.nulleins.formats.iso8583;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nulleins.formats.iso8583.types.CardAcceptorLocation;
import org.nulleins.formats.iso8583.types.MTI;
import org.nulleins.formats.iso8583.types.PostilionAddAmount;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;


/**
 * @author phillipsr
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestBank {
  @Resource
  private MessageFactory bankMessages;

  private static final String ExpectRequest =
      "0200F238801588E0801000000000040000001950611891871625134610110000000020000001206091818"
          + "00983110181812061206D00010000C00000000065890190611111100006702034117014641000000000000322"
          + "PH Rumukrushi          Porthar      PHNG5660162012201220122012100551031385";
  private static final String ExpectResponse =
      "0210F238801588E0841000000000040000001950611891871625134610110000000020000001206091818"
          + "00983110181812061206D00010000C00000000065890190611111100006702034117014641000000000000322"
          + "PH Rumukrushi          Porthar      PHNG5660601002566C0000024260261003566"
          + "C0000000000001001566C0000024260260162012201220122012100551031385";

  @Test
  public void testCreateMessage()
      throws ParseException, IOException {
    Map<String, Object> params = new HashMap<String, Object>() {{
      put("accountNumber", 5061189187162513461L);
      put("processingCode", "011000");
      put("amount", 2000000);
      put("transDateTime", "1206091818");
      put("stan", "009831");
      put("transTimeLocal", "101818");
      put("transDateLocal", 1206);
      put("captureDate", "1206");
      put("transactionFee", -10000);
      put("processingFee", 0);
      put("acquierID", 589019);
      put("forwarderID", 111111);
      put("rrn", "000067020341");
      put("cardTermId", "17014641");
      put("cardAcceptorId", "000000000000322");
      put("cardAcceptorLoc", new CardAcceptorLocation("PH Rumukrushi", "Porthar", "PH", "NG"));
      put("currencyCode", 566);
      put("adviceCode", "2012201220122012");
      put("accountId1", "0551031385");
    }};

    Message message = bankMessages.createByNames(MTI.create("0200"), params);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bankMessages.writeToStream(message, baos);
    String messageText = baos.toString();

    assertThat(message.validate(), empty());
    assertThat(messageText, is(ExpectRequest));

    // parse the message back as a map:
    Message readback = bankMessages.parse(messageText.getBytes());
    Map<Integer, Object> outParams = new HashMap<>(Maps.transformValues(readback.getFields(), MessageFactory.fromOptional()));
    assertThat(readback.validate(), empty());

    assertThat((BigInteger)outParams.get(2), is(BigInteger.valueOf(5061189187162513461L)));
    assertThat((BigInteger)outParams.get(3), is(BigInteger.valueOf(11000)));
    assertThat((BigInteger)outParams.get(4), is(BigInteger.valueOf(2000000)));
    assertThat(outParams.get(7).toString(), is("2000-12-06T09:18:18.000Z"));
    assertThat(outParams.get(11).toString(), is("9831"));
    assertThat(outParams.get(12).toString(), is("10:18:18.000"));
    assertThat(outParams.get(13).toString(), is("2000-12-06T00:00:00.000Z"));
    assertThat(outParams.get(17).toString(), is("2000-12-06T00:00:00.000Z"));
    assertThat((BigInteger)outParams.get(28), is(BigInteger.valueOf(10000).negate()));
    assertThat((BigInteger)outParams.get(30), is(BigInteger.ZERO));
    assertThat(outParams.get(32).toString(), is("589019"));
    assertThat(outParams.get(33).toString(), is("111111"));
    assertThat(outParams.get(37).toString(), is("000067020341"));
    assertThat(outParams.get(41).toString(), is("17014641"));
    assertThat(outParams.get(42).toString(), is("000000000000322"));
    Object f43 = outParams.get(43);
    assertThat(f43, instanceOf(CardAcceptorLocation.class));
    CardAcceptorLocation cal = (CardAcceptorLocation) f43;
    assertThat(cal.getLocation(), is("PH Rumukrushi"));
    assertThat(cal.getCity(), is("Porthar"));
    assertThat(cal.getState(), is("PH"));
    assertThat(cal.getCountry(), is("NG"));
    assertThat(outParams.get(49).toString(), is("566"));
    assertThat(outParams.get(60).toString(), is("2012201220122012"));
    assertThat(outParams.get(102).toString(), is("0551031385"));

    MTI responseType = MTI.create("0210");
    PostilionAddAmount[] addAmount = new PostilionAddAmount[3];
    addAmount[0] = new PostilionAddAmount(10, 2, 566, new BigInteger("2426026"));
    addAmount[1] = new PostilionAddAmount(10, 3, 566, BigInteger.ZERO);
    addAmount[2] = new PostilionAddAmount(10, 1, 566, new BigInteger("2426026"));
    //10 02 566 C 000002426026 10 03 566 C000000000000 10 01 566 C 000002426026

    outParams.put(54, addAmount);

    Message response = bankMessages.createByNumbers(responseType, outParams);
    assertThat(response.validate(), empty());
    baos = new ByteArrayOutputStream();
    bankMessages.writeToStream(response, baos);
    String responseMessage = baos.toString();
    assertThat(responseMessage, is(ExpectResponse));
  }


}
