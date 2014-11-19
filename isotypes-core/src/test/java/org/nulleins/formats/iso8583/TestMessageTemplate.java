package org.nulleins.formats.iso8583;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.nulleins.formats.iso8583.types.Bitmap;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.FieldType;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;

/** basic test of programmatic API, creating a message template, adding fields and asserting correct values
  * @author phillipsr */
public class TestMessageTemplate {
  private static final byte[] BINARY_BITMAP1 = new byte[]{(byte) 0xf2, 0x20, 0, 0, 0, 0, 0, 1};
  private static final byte[] BINARY_BITMAP2 = new byte[]{(byte) 0xc0, 0, 0, 0, 0, 0, 0, 1};
  private static final byte[] BINARY_BITMAP3 = new byte[]{0x40, 0, 0, 0, 0, 0, 0, 1};

  private static final MTI PaymentRequest = MTI.create("0200");
  private static final Dimension FIXED6 = Dimension.parse("fixed(6)");
  private static final Dimension FIXED10 = Dimension.parse("fixed(10)");
  private MessageFactory factory;

  @Test
  public void testCreateMessageTemplate() throws IOException {
    final MessageTemplate template = MessageTemplate.create("ISO015000077", PaymentRequest, BitmapType.HEX);
    assertThat(template.getMessageType(), is(PaymentRequest));
    assertThat(template.getHeader(), is("ISO015000077"));
    final FieldTemplate.Builder builder = FieldTemplate.localBuilder(template).get();
    final Map<Integer, FieldTemplate> fields = new HashMap<Integer, FieldTemplate>() {{
      put( 2, builder.f(2).type(FieldType.ALPHANUMSYMBOL).dim("llvar(10)").name("TestField2").desc("Just a Test").build());
      put( 3, builder.f(3).type(FieldType.NUMERIC).dimension(FIXED6).name("TestField3").desc("Processing Code").build());
      put( 4, builder.f(4).type(FieldType.NUMERIC).dimension(FIXED6).name("TestField4").desc("Amount, transaction (PT - cents)").build());
      put( 7, builder.f(7).type(FieldType.DATE).dimension(FIXED10).name("TestField5").desc("Transmission Date and Time").build());
      put(11, builder.f(11).type(FieldType.NUMERIC).dimension(FIXED6).name("TestField6").desc("System Trace Audit Number").build());
    }};
    template.setFields(fields);

    template.addField(builder.f(64).type(FieldType.NUMERIC).dimension(FIXED6).name("TestField64").desc("System Trace Audit Number").build());
    template.addField(builder.f(66).type(FieldType.NUMERIC).dimension(FIXED6).name("TestField66").desc("System Trace Audit Number").build());
    template.addField(builder.f(128).type(FieldType.NUMERIC).dimension(FIXED6).name("TestField128").desc("System Trace Audit Number").build());
    template.addField(builder.f(130).type(FieldType.NUMERIC).dimension(FIXED6).name("TestField130").desc("System Trace Audit Number").build());
    template.addField(builder.f(192).type(FieldType.NUMERIC).dimension(FIXED6).name("TestField192").desc("System Trace Audit Number").build());

    // E220000000000001 becomes F220000000000001 as secondary bitmap now set
    assertThat(template.getBitmap().asHex(Bitmap.Id.PRIMARY), is("F220000000000001"));
    assertThat(template.getBitmap().asHex(Bitmap.Id.SECONDARY), is("C000000000000001"));
    assertThat(template.getBitmap().asHex(Bitmap.Id.TERTIARY), is("4000000000000001"));

    System.out.println(Joiner.on(',').join(template.getBitmap()));
    assertThat(template.getBitmap().isFieldPresent(2), is(true));
    assertThat(template.getBitmap().isFieldPresent(3), is(true));
    assertThat(template.getBitmap().isFieldPresent(4), is(true));
    assertThat(template.getBitmap().isFieldPresent(7), is(true));
    assertThat(template.getBitmap().isFieldPresent(11), is(true));
    assertThat(template.getBitmap().isFieldPresent(64), is(true));
    assertThat(template.getBitmap().isFieldPresent(65), is(false));
    assertThat(template.getBitmap().isFieldPresent(66), is(true));
    assertThat(template.getBitmap().isFieldPresent(128), is(true));
    assertThat(template.getBitmap().isFieldPresent(130), is(true));
    assertThat(template.getBitmap().isFieldPresent(192), is(true));

    Set<Integer> fieldNumbers = Sets.newHashSet(template.getBitmap());

    assertThat(fieldNumbers, containsInAnyOrder(2, 3, 4, 7, 11, 64, 66, 128, 130, 192));

    assertThat(Arrays.equals(BINARY_BITMAP1, template.getBitmap().asBinary(Bitmap.Id.PRIMARY)), is(true));
    assertThat(Arrays.equals(BINARY_BITMAP2, template.getBitmap().asBinary(Bitmap.Id.SECONDARY)), is(true));
    assertThat(Arrays.equals(BINARY_BITMAP3, template.getBitmap().asBinary(Bitmap.Id.TERTIARY)), is(true));

    final MessageFactory testFactory = MessageFactory.Builder()
        .id("tertiaryTests")
        .contentType(ContentType.TEXT)
        .bitmapType(BitmapType.HEX)
        .templates(asList(template))
        .build();

    final Map<Integer, Object> testFields = new HashMap<Integer,Object>() {{
      put(2,"Field 2");
      put(3,123456);
      put(4,789101);
      put(7, DateTime.parse("2014-11-20T12:30:00"));
      put(11,"100001");
      put(64,"200001");
      put(66,"300001");
      put(128,"400001");
      put(130,"500001");
      put(192,"600001");
    }};
    final Message message = testFactory.create(PaymentRequest, testFields);
    System.out.println(message.describe());
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    testFactory.writeToStream(message, baos);
    assertThat(baos.toString(),
        is("0200F220000000000001C000000000000001400000000000000107Field 21234567891011120123000100001200001300001400001500001600001"));


    final Map<MTI, MessageTemplate> templateMap = new HashMap<MTI,MessageTemplate>() {{
      put(PaymentRequest,template);
    }};
    final MessageParser parser = MessageParser.create(
        "", templateMap, ContentType.TEXT, CharEncoder.ASCII, BitmapType.HEX);
    final Message result = parser.parse(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
    System.out.println(result.describe());
  }

  @Before
  public void createFactory() {
    this.factory = new MessageFactory();
    factory.setId("testFactory");
    factory.setDescription("Test Message Schema");
    factory.setBitmapType(BitmapType.HEX);
    factory.setContentType(ContentType.TEXT);
    factory.setHeader("ISO015000077");
    final MessageTemplate template200 = MessageTemplate.create("ISO015000077", MTI.create(0x0200), BitmapType.HEX);
    template200.addField(FieldTemplate.localBuilder(template200).get().f(2).type(FieldType.NUMERIC).dim("fixed(6)").name("TestField").build());
    final MessageTemplate template400 = MessageTemplate.create("ISO015000077", MTI.create(0x0400), BitmapType.HEX);
    template400.addField(FieldTemplate.localBuilder(template400).get().f(2).type(FieldType.ALPHA).dim("fixed(6)").name("TestField").build());
    template400.addField(FieldTemplate.localBuilder(template400).get().f(3).type(FieldType.NUMERIC).dim("fixed(6)").name("OptField").optional().build());
    factory.addTemplates(asList(template200, template400));
    factory.initialize();
  }

  @Test
  public void validationDetectsMTIMismatch() {
    final MTI messageType = MTI.create(0x0200);
    final Message subject = Message.Builder()
        .header("ISO015000077")
        .template(factory.getTemplate(messageType))
        .build();
    final List<String> errors = factory.getTemplate(MTI.create(0x0400)).validate(subject);
    assertThat(errors.size(), is(2));
    assertThat(errors.get(0), is("Message MTI (0200) != Template MTI (0400)"));
    assertThat(errors.get(1), is("Message field missing (Field nb=2 name=TestField type=a dim=FIXED(  6))"));
  }

  @Test
  public void validationDetectsHeaderMismatch() {
    final MTI messageType = MTI.create(0x0400);
    final Message subject = Message.Builder()
        .header("ISO015000088")
        .template(factory.getTemplate(messageType))
        .build();
    final List<String> errors = factory.getTemplate(MTI.create(0x0400)).validate(subject);
    assertThat(errors.size(), is(2));
    assertThat(errors.get(0), is("Message header (ISO015000088) != Template header (ISO015000077)"));
    assertThat(errors.get(1), is("Message field missing (Field nb=2 name=TestField type=a dim=FIXED(  6))"));
  }

  @Test
  public void validationDetectsInvalidFieldValue() {
    final MTI messageType = MTI.create(0x0400);
    final HashMap<Integer, Object> fields = new HashMap<Integer,Object>() {{
      put(2, "ABC");
    }};
    final Message subject = Message.Builder()
        .header("ISO015000077")
        .template(factory.getTemplate(messageType))
        .fields(fields)
        .build();
    final List<String> errors = factory.getTemplate(MTI.create(0x0200)).validate(subject);
    assertThat(errors.size(), is(2));
    System.out.println(errors);
    assertThat(errors.get(0), is("Message MTI (0400) != Template MTI (0200)"));
    assertThat(errors.get(1), is("Message field data invalid (ABC) for field: Field nb=2 name=TestField type=n dim=FIXED(  6)"));
  }

}
