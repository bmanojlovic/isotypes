package org.nulleins.formats.iso8583;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nulleins.formats.iso8583.types.MTI;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * @author phillipsr
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TestBinary {
  @Resource
  private MessageFactory factory;

  @Test
  public void testCreateBinaryMessage()
      throws IOException, ParseException {
    assertThat(factory, notNullValue());
    final Date testDate = (new SimpleDateFormat("ddMMyyyy:HHmmss")).parse("12122012:121200");
    // create a request message with binary bitmap and bcd encoded content
    // as specified in the iso:schema (see associated TestBinary-context.xml)
    final Message request = factory.create(MTI.create(0x0200));
    request.setFieldValue(2, 5432818929192L);
    request.setFieldValue(3, 1010);
    request.setFieldValue(4, new BigInteger("1200"));
    request.setFieldValue(7, testDate);
    request.setFieldValue(11, 666666);
    request.setFieldValue(12, testDate);
    request.setFieldValue(13, testDate);
    request.setFieldValue(32, 1029);
    request.setFieldValue(37, 937278626262L);
    request.setFieldValue(41, "ATM-10101");
    request.setFieldValue(43, "DUB87");
    request.setFieldValue(48, 353863579271L);
    request.setFieldValue(49, 840);
    request.setFieldValue(90, BigInteger.TEN);

    final byte[] expectData = {
        0x02, 0x00, (byte) 0xf2, 0x38, 0x00, 0x01, 0x08, (byte) 0xa1, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00,
        0x00, 0x00, 0x14, 0x05, 0x43, 0x28, 0x18, (byte) 0x92, (byte) 0x91, (byte) 0x92, 0x00, 0x10, 0x10, 0x00, 0x00, 0x00,
        0x00, 0x12, 0x00, 0x12, 0x12, 0x12, 0x12, 0x00, 0x66, 0x66, 0x66, 0x12, 0x12, 0x00, 0x12, 0x12,
        0x04, 0x10, 0x29, (byte) 0x93, 0x72, 0x78, 0x62, 0x62, 0x62, 0x41, 0x54, 0x4d, 0x2d, 0x31, 0x30, 0x31,
        0x30, 0x31, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x44, 0x55, 0x42, 0x38, 0x37, 0x20, 0x20,
        0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
        0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
        0x20, 0x12, 0x35, 0x38, 0x63, 0x57, (byte) 0x92, 0x71, 0x08, 0x40, 0x00, 0x04, 0x0c, 0x10
    };

    final byte[] data = factory.getMessageData(request);
    System.out.println(HexDumper.getHexDump(data));
    assertThat(data, is(expectData));

    final Message message = factory.parse(new ByteArrayInputStream(data));

    final byte[] odata = factory.getMessageData(message);
    assertThat(odata, is(expectData));
  }

  @Test
  public void testBcdEncoding()
      throws IOException, ParseException {
    /*
		 * this test uses examples from a web site, to correlate the format created, see:
		 * http://www.chileoffshore.com/en/interesting-articles/115-about-iso8583
		 */

    final Message request = factory.create(MTI.create(0x0220));
    request.setFieldValue(2, "1234567890123456");
    request.setFieldValue(7, "0609173030");
    request.setFieldValue(22, "ABC123");
    request.setFieldValue(63, "012345678901234567890123456789012345678901234567890"
        + "1234567890123456789012345678901234567890123456789");

    final byte[] data = factory.getMessageData(request);

    final byte[] expectData = {
        0x02, 0x20, 0x42, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x02, 0x16, 0x12, 0x34, 0x56, 0x78, (byte) 0x90,
        0x12, 0x34, 0x56, 0x06, 0x09, 0x17, 0x30, 0x30, 0x41, 0x42, 0x43, 0x31, 0x32, 0x33, 0x20, 0x20,
        0x20, 0x20, 0x20, 0x20, 0x01, 0x00, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35,
        0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31,
        0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
        0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33,
        0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,};
    final Message message = factory.parse(new ByteArrayInputStream(data));

    final byte[] odata = factory.getMessageData(message);
    assertThat(odata, is(expectData));
  }


  @Test(expected = IllegalArgumentException.class)
  public void testExceedFieldSize()
      throws Throwable {
    final Map<Integer, Object> params = new HashMap<Integer, Object>() {{
      put(2, 5432818929192L);
      put(3, 1010);
      put(4, new BigInteger("1200"));
      put(7, Calendar.getInstance().getTime());
      put(11, 666666);
      put(12, Calendar.getInstance().getTime());
      put(13, Calendar.getInstance().getTime());
      put(32, 999999); // field is LLVAR(4), this will fail
      put(37, 937278626262L);
      put(41, "ATM-10101");
      put(43, "DUB87");
      put(48, 353863579271L);
      put(49, 840);
      put(90, new BigInteger("1000"));
    }};

    final Message request = factory.createByNumbers(MTI.create(0x0200), params);
    try {
      final ByteArrayOutputStream output = new ByteArrayOutputStream();
      factory.writeToStream(request, output);
    } catch (final Exception e) {
      assertThat(e.getCause().getMessage(), startsWith("Field data length (6) exceeds field maximum (4)"));
      throw e.getCause();
    }
  }

}
