package org.nulleins.formats.iso8583.config;

import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.MessageTemplate;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.MTI;

import static java.util.Arrays.asList;

public class CharMessageConfiguration {

  public static MessageFactory createMessageFactory() {

    final MessageTemplate template = MessageTemplate.create("ISO015000077", MTI.create(0x0200), BitmapType.HEX);
    template.setName("Acquirer Payment Request");
    final FieldTemplate.Builder builder = FieldTemplate.localBuilder(template).get();
    template.addFields(asList(
        builder.f(2).name("cardNumber").desc("Payment Card Number").dim("llvar(40)").type("n").build(),
        builder.f(3).name("processingCode").desc("Processing Code").dim("fixed(6)").type("n").build(),
        builder.f(4).name("amount").desc("Amount, transaction (cents)").dim("fixed(12)").type("n").build(),
        builder.f(7).name("transDateTime").desc("Transmission Date and Time").dim("fixed(10)").type("date").build(),
        builder.f(11).name("stan").desc("System Trace Audit Number").dim("fixed(6)").type("n").build(),
        builder.f(12).name("transTimeLocal").desc("Time, local transaction").dim("fixed(6)").type("time").build(),
        builder.f(13).name("transDateLocal").desc("Date, local transaction").dim("fixed(4)").type("date").build(),
        builder.f(32).name("acquierID").desc("Acquiring Institution ID").dim("llvar(4)").type("n").defaultValue("0000").build(),
        builder.f(37).name("extReference").desc("Retrieval Reference Number").dim("fixed(12)").type("n").build(),
        builder.f(41).name("cardTermId").desc("Card Acceptor Terminal ID").dim("fixed(16)").type("ans").build(),
        builder.f(43).name("cardTermName").desc("Card Acceptor Terminal Name").dim("fixed(40)").type("ans").build(),
        builder.f(48).name("msisdn").desc("Additional Data (MSISDN)").dim("llvar(14)").type("n").build(),
        builder.f(49).name("currencyCode").desc("Currency Code, Transaction").dim("fixed(3)").type("n").build(),
        builder.f(90).name("originalData").desc("Original data elements").dim("lllvar(4)").type("xn").build()));

    return MessageFactory.Builder()
        .id("charMessageSet")
        .contentType(ContentType.TEXT)
        .bitmapType(BitmapType.HEX)
        .header("ISO015000077")
        .charset(new CharEncoder("cp1047"))
        .templates(asList(template))
        .build();
  }

}
