package org.nulleins.formats.iso8583;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.FieldType;
import org.nulleins.formats.iso8583.types.MTI;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;


/**
 * @author phillipsr
 */
public class TestMessageFactory {
  private static final MTI RequestMessage = MTI.create(0x0200);
  private static final HashMap<Integer, Object> FIELDS = new HashMap<Integer,Object>() {{
    put(2, BigInteger.TEN);
  }};
  private MessageFactory factory;

  @Test
  public void testListSchema() {
    final Set<MTI> schema = new HashSet<>();
    for (final MessageTemplate message : factory.getTemplates()) {
      schema.add(message.getMessageType());
    }
    assertThat(schema.contains(MTI.create(0x0200)), is(true));
  }

  @Test
  public void testCreateMessageFromTemplate() {
    assertThat(factory.toString(), is(MESSAGE_FACTORY_DESCRIPTION));

    final Message message = factory.create(RequestMessage, FIELDS);
    assertThat(message.toString(), is("Message mti=0200 header=ISO015000077 #field=1"));
    assertThat(message.getFieldValue(2).get(), Is.<Object>is(BigInteger.TEN));
    assertThat((BigInteger) message.getFieldValue("TestField").get(), is(BigInteger.TEN));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadCharset() {
    MessageFactory.Builder()
        .charset(new CharEncoder("tlhIngan-pIqaD")); // try to set Klingon charset
  }

  @Test(expected = NoSuchFieldError.class)
  public void missingGetFieldByNumberTest() {
    final Message message = factory.create(RequestMessage, FIELDS);
    message.getFieldValue(3);
  }

  @Test(expected = NoSuchFieldError.class)
  public void missingGetFieldByNameTest() {
    final Message message = factory.create(RequestMessage, FIELDS);
    message.getFieldValue("Frogmella");
  }

  @Test(expected = IllegalArgumentException.class)
  public void createFactoryFails() {
    try {
      final MessageFactory subject = MessageFactory.Builder ()
          .id ("testFactory")
          .description ("Test Message Schema")
          .bitmapType (BitmapType.HEX)
          .contentType (ContentType.TEXT)
          .header ("ISO015000077").build ();
    } catch ( IllegalArgumentException e) {
      assertThat(e.getMessage (), is("Factory must have message template definitions"));
      throw e;
    }
  }

  private static final String MESSAGE_FACTORY_DESCRIPTION =
      "MessageFactory id=testFactory desc='Test Message Schema' "
          + "header=ISO015000077 contentType=TEXT charset=US-ASCII bitmapType=HEX templates# 1";

  @Before
  public void createFactory() {
    final FieldTemplate field = FieldTemplate.localBuilder().get().f(2).type(FieldType.NUMERIC).dim("fixed(6)").name("TestField").build();
    final MessageTemplate template = MessageTemplate.Builder()
        .header("ISO015000077")
        .type(RequestMessage)
        .fieldlist(asList(field))
        .build();
    this.factory = MessageFactory.Builder()
        .id("testFactory")
        .description("Test Message Schema")
        .bitmapType(BitmapType.HEX)
        .contentType(ContentType.TEXT)
        .charset(CharEncoder.ASCII)
        .addTemplate(template)
        .header("ISO015000077").build();
  }

}
