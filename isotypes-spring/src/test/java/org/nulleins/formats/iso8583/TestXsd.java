package org.nulleins.formats.iso8583;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nulleins.formats.iso8583.model.CardNumber;
import org.nulleins.formats.iso8583.model.PaymentRequestBean;
import org.nulleins.formats.iso8583.types.Bitmap;
import org.nulleins.formats.iso8583.types.MTI;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;


/**
 * @author phillipsr
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestXsd {
  @Resource
  private MessageFactory messages;

  @Test
  public void
  testListXsdSchema() {
    assertThat(messages, notNullValue());

    final MessageTemplate msg0200 = messages.getTemplate(MTI.create(0x0200));
    assertThat(msg0200, notNullValue());
    assertThat(msg0200.getFields().size(), is(14));

    final MessageTemplate msg0400 = messages.getTemplate(MTI.create(0x0400));
    assertThat(msg0400, notNullValue());
    assertThat(msg0400.getFields().size(), is(12));
  }

  @Test
  public void testCreateMessage()
      throws ParseException, IOException {
    // create the test message and set field values:
    final Message message = messages.createByNames(MTI.create(0x0200), new HashMap<String,Object>() {{
        put("cardNumber", new CardNumber(5432818929192L));
        put("processingCode", 1010);
        put("amount", new BigInteger("1200"));
        put("transDateTime", (new SimpleDateFormat("MMddHHmmss")).parse("1212121212"));
        put("stan", 666666);
        put("transTimeLocal", (new SimpleDateFormat("HHmmss")).parse("121212"));
        put("transDateLocal", (new SimpleDateFormat("MMdd")).parse("1212"));
        put("acquierID", 1029);
        put("extReference", 937278626262L);
        put("cardTermId", "ATM-10101");
        put("cardTermName", "DUB87");
        put("msisdn", 353863579271L);
        put("currencyCode", 840);
        put("originalData", BigInteger.TEN);
      }});

    // check the message has been correctly created:
    assertThat(message.validate(), empty());

    // convert the message into its wire format:
    final byte[] messageData = messages.getMessageData(message);

    // parse the message back into a new message instance
    final Message readback = messages.parse(messageData);
    // ensure the message context is the same as the original (allowing for type promotions):
    final DateTime dateTime = DateTimeFormat.forPattern("MMddHHmmss").parseDateTime("1212121212");
    final DateTime date = DateTimeFormat.forPattern("MMdd").parseDateTime("1212");
    final LocalTime localTime = DateTimeFormat.forPattern("HHmmss").parseLocalTime("121212");
    final Map<Integer, Object> results = Maps.transformValues(readback.getFields(), Functions.fromOptional());
    assertThat((String)results.get(2), is("5432*******92"));
    assertThat((BigInteger)results.get(3), is(BigInteger.valueOf(1010)));
    assertThat((BigInteger)results.get(4), is(BigInteger.valueOf(1200)));
    assertThat((DateTime)results.get(7), is(dateTime));
    assertThat((BigInteger)results.get(11), is(BigInteger.valueOf(666666)));
    assertThat((LocalTime)results.get(12), is(localTime));
    assertThat((DateTime)results.get(13), is(date));
    assertThat((BigInteger)results.get(32), is(BigInteger.valueOf(1029)));
    assertThat((BigInteger)results.get(37), is(BigInteger.valueOf(937278626262L)));
    assertThat((String)results.get(41), is("ATM-10101"));
    assertThat((String)results.get(43), is("DUB87"));
    assertThat((BigInteger)results.get(48), is(BigInteger.valueOf(353863579271L)));
    assertThat((BigInteger)results.get(49), is(BigInteger.valueOf(840)));
    assertThat((BigInteger)results.get(90), is(BigInteger.TEN));

    // check the describer by comparing the desc of the original message
    // with that of the read-back message; as the data-types can change in translation,
    // only the first 21 chars are compared (the field definitions)
    //
    final Set<String> original = new HashSet<>();
    for (final String line : message.describe()) {
      original.add(line.substring(0, 21));
    }
    final Set<String> derived = new HashSet<>();
    for (final String line : readback.describe()) {
      derived.add(line.substring(0, 21));
    }
    assertThat(original, is(derived));
  }

  @Test(expected = MessageException.class)
  public void testCreateBadMessage() {
    // create the test message and set field values:
    final Message message = messages.create(MTI.create(0x0200), Collections.<Integer,Object>emptyMap());
    final List<String> errors = message.validate();
    if (!errors.isEmpty()) {
      throw new MessageException(errors);
    }
  }

  private static final String ExpectedBeanMessage =
      "ISO0150000770200F238000108A180000000004000000000" +
          "135432*******920010100000000000120923000000618172" +
          "1011121117041029937278626262ATM-10101       DUB87" +
          "                                   12353863579271840004C999";

  @Test
  public void testCreateMessageFromBean()
      throws ParseException {
    // this bean represent the business data in the transaction
    final PaymentRequestBean bean = new PaymentRequestBean();
    bean.setCardNumber(new CardNumber(5432818929192L));
    bean.setAmount(new BigInteger("12"));
    bean.setAcquierID(1029);
    bean.setExtReference(937278626262L);
    bean.setCardTermId("ATM-10101");
    bean.setCardTermName("DUB87");
    bean.setMsisdn(353863579271L);
    bean.setCurrencyCode(840);
    bean.setOriginalData(999);

    // this map contains the technical/protocol fields
    final DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    final DateFormat tf = new SimpleDateFormat("HH:mm:ss");
    final Map<Integer, Object> params = new HashMap<Integer, Object>() {{
      put(3, 1010);
      put(7, df.parse("23-09-2015"));
      put(11, 618172);
      put(12, tf.parse("10:11:12"));
      put(13, df.parse("17-11-2016"));
    }};

    final Message message = messages.createFromBean(MTI.create("0200"), bean, params);

    assertThat(message.validate(), empty());

    final String messageText = new String(messages.getMessageData(message));

    assertThat(messageText, is(ExpectedBeanMessage));
    final Message response = messages.transform(MTI.create(0x0400), message,
        new HashMap<String, Object>() {{
          put("currencyCode2", 885);
          put("currencyCode3", 350);
        }});
    System.out.println(response.describe());
    for ( final String error : response.validate()) {
      System.out.println(error);
    }
    assertThat(response.isValid(), is(true));
  }

  @Test
  public void testParseBeanMessageAsMap()
      throws ParseException, IOException {
    final Message message = messages.parse(ExpectedBeanMessage.getBytes());

    assertThat(message.validate(), empty());
    final Map<Integer, Object> result = Maps.transformValues(message.getFields(), new Function<Optional<Object>, Object>() {
          @Override
          public Object apply(final Optional<Object> input) {
            return input.orNull();
          }
        });

    assertThat(message.validate(), empty());
    assertThat(message.getMTI(), is(MTI.create("0200")));

    assertThat((String)result.get(2), is("5432*******92"));
    assertThat((BigInteger)result.get(3), is(new BigInteger("1010")));
    assertThat((BigInteger) result.get(4), is(new BigInteger("12")));
    assertThat(result.get(7).toString(), is("2000-09-23T00:00:00.000+01:00"));
    assertThat((BigInteger)result.get(11), is(new BigInteger("618172")));
    assertThat(result.get(12).toString(), is("10:11:12.000"));
    assertThat(result.get(13).toString(), is("2000-11-17T00:00:00.000Z"));
    assertThat((BigInteger)result.get(32), is(new BigInteger("1029")));
    assertThat((BigInteger)result.get(37), is(new BigInteger("937278626262")));
    assertThat((String)result.get(41), is("ATM-10101"));
    assertThat((String)result.get(43), is("DUB87"));
    assertThat((BigInteger)result.get(48), is(new BigInteger("353863579271")));
    assertThat((BigInteger)result.get(49), is(new BigInteger("840")));
    assertThat((BigInteger)result.get(90), is(new BigInteger("999")));
  }

  @Test
  public void testBitmap() {
    /*
		 * 4210001102C04804	Fields 2, 7, 12, 28, 32, 39, 41, 42, 50, 53, 62
		 * Explanation of Bitmap (8 BYTE Primary Bitmap = 64 Bit) field 4210001102C04804
		 * BYTE1 : 0100 0010 = 42x (fields 2 and 7 are present)
		 * BYTE2 : 0001 0000 = 10x (field 12 is present)
		 * BYTE3 : 0000 0000 = 00x (no fields present)
		 * BYTE4 : 0001 0001 = 11x (fields 28 and 32 are present)
		 * BYTE5 : 0000 0010 = 02x (field 39 is present)
		 * BYTE6 : 1100 0000 = C0x (fields 41 and 42 are present)
		 * BYTE7 : 0100 1000 = 48x (fields 50 and 53 are present)
		 * BYTE8 : 0000 0100 = 04x (field 62 is present)
		 */
    final MessageTemplate template = messages.getTemplate(MTI.create("0400"));

    final byte[] binaryBitmap = template.getBitmap().asBinary(Bitmap.Id.PRIMARY);
    assertThat(binaryBitmap[0], is((byte) 0x42));
    assertThat(binaryBitmap[1], is((byte) 0x38));
    assertThat(binaryBitmap[2], is((byte) 0x00));
    assertThat(binaryBitmap[3], is((byte) 0x01));
    assertThat(binaryBitmap[4], is((byte) 0x08));
    assertThat(binaryBitmap[5], is((byte) 0xa1));
    assertThat(binaryBitmap[6], is((byte) 0x08));
    assertThat(binaryBitmap[7], is((byte) 0x04));

    final String hexBitmap = template.getBitmap().asHex(Bitmap.Id.PRIMARY);
    assertThat(hexBitmap, is("4238000108A10804"));
  }

  private static final String Payment_Request =
      "ISO01500007702007238000108A18000165264**********02305700000000032000"
          + "121022021393716600021312111181800601368034522937166CIB08520263     CIB-57357"
          + "HOSPITAL     CAIRO          EG01120167124377818";

  @Test
  public void testParseMessage()
      throws ParseException, IOException {
    final Map<Integer, Object> params = Maps.transformValues(
        messages.parse(Payment_Request.getBytes()).getFields(), Functions.fromOptional());

    assertThat((String) params.get(2), is("5264**********02"));
    assertThat((BigInteger)params.get(3), is(BigInteger.valueOf(305700)));
    assertThat((BigInteger)params.get(4), is(BigInteger.valueOf(32000)));
    assertThat(params.get(7).toString(), is("2000-12-10T22:02:13.000Z"));
    assertThat((BigInteger)params.get(11), is(BigInteger.valueOf(937166)));
    assertThat(params.get(12).toString(), is("00:02:13.000"));
    assertThat(params.get(13).toString(), is("2000-12-11T00:00:00.000Z"));
    assertThat((BigInteger)params.get(32), is(BigInteger.valueOf(81800601368L)));
    assertThat((BigInteger)params.get(37), is(BigInteger.valueOf(34522937166L)));
    assertThat((String)params.get(41), is("CIB08520263"));
    assertThat((String)params.get(43), is("CIB-57357HOSPITAL     CAIRO          EG0"));
    assertThat((BigInteger)params.get(48), is(BigInteger.valueOf(20167124377L)));
    assertThat((BigInteger)params.get(49), is(BigInteger.valueOf(818)));
  }

  private static final String ExpectMessage =
      "ISO0150000770200F238000108A180000000004000000000135432818929192"
          + "00101000000000120012121212006666661212001212041029937278626262"
          + "ATM-10101       DUB87                                   12353863579271840003C10";

  @Test
  public void
  testCreateMessageAPI()
      throws IOException, ParseException {
    final Date testDate = (new SimpleDateFormat("ddMMyyyy:HHmmss")).parse("12122012:121200");
    final Message request = messages.create(MTI.create(0x0200), new HashMap<Integer,Object>() {{
        put(2, 5432818929192L);
        put(3, 1010);
        put(4, new BigInteger("1200"));
        put(7, testDate);
        put(11, 666666);
        put(12, testDate);
        put(13, testDate);
        put(32, 1029);
        put(37, 937278626262L);
        put(41, "ATM-10101");
        put(43, "DUB87");
        put(48, 353863579271L);
        put(49, 840);
        put(90, BigInteger.TEN);
      }});

    assertThat(request.isValid(), is(true));

    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    messages.writeToStream(request, baos);
    final Message message = messages.parse(new ByteArrayInputStream(baos.toByteArray()));
    assertThat(new String(messages.getMessageData(message)), is(ExpectMessage));
  }

}
