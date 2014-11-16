package org.nulleins.formats.iso8583;

import org.junit.Test;
import org.nulleins.formats.iso8583.config.BankMessageConfiguration;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

public class ParserTests {

  private static final MessageFactory factory = BankMessageConfiguration.createMessageFactory();
  private static final String GOOD_MESSAGE =
        "ISO0150000770210F238801588E0841000000000040000000510101000456000000000"+
            "420101012300000012313000010101010C00000010C00000001101827271711098"+
            "27277722717266621   2001919 978817112      1000"+
            "                                    400001200101191817233372";
  private static final String SHORT_MESSAGE =
      "ISO0150000770210F238801588E0841000000000040000000510101000456000000000"+
          "420101012300000012313000010101010C00000010C00000001101827271711098"+
          "27277722717266621   2001919 978817112      1000";

  @Test
  public void canParseMessageStream() throws IOException {
    final Message result = createTestParser().parse(new DataInputStream(new ByteArrayInputStream(GOOD_MESSAGE.getBytes())));
    assertThat(result.getFields().entrySet(), hasSize(20));
  }

  @Test(expected = IOException.class)
  public void failsToParseTruncatedMessageStream() throws Throwable {
    try {
      createTestParser().parse(new DataInputStream(new ByteArrayInputStream(SHORT_MESSAGE.getBytes())));
    } catch ( final Exception e) {
      assertThat(e, instanceOf(RuntimeException.class));
      assertThat(e.getCause(), instanceOf(IOException.class));
      assertThat(e.getCause().getMessage(), is("Failed to read fully 40 bytes from input stream"));
      throw e.getCause();
    }

  }

  private static MessageParser createTestParser() {
    final MTI messageType = MTI.create(0x0210);
    final MessageTemplate template = factory.getTemplate(messageType);
    final Map<MTI, MessageTemplate> messages = new HashMap<>(1);
    messages.put(messageType,template);

    return MessageParser.create(
        "ISO015000077", messages, ContentType.TEXT, CharEncoder.ASCII, BitmapType.HEX);
  }
}
