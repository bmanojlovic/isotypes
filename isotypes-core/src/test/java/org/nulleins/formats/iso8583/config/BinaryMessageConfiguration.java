package org.nulleins.formats.iso8583.config;

import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.MessageTemplate;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.MTI;

import static java.util.Arrays.asList;

public class BinaryMessageConfiguration {

  public static MessageFactory createMessageFactory() {

    final MessageTemplate requestMessageTemplate = MessageTemplate.create("ISO015000077", MTI.create(0x0200), BitmapType.BINARY);
    final FieldTemplate.Builder requestBuilder = FieldTemplate.localBuilder(requestMessageTemplate).get();
    requestMessageTemplate.addFields(asList(
        requestBuilder.f(2).name("cardNumber").desc("Payment Card Number").dim("llvar(40)").type("n").build(),
        requestBuilder.f(3).name("processingCode").desc("Processing Code").dim("fixed(6)").type("n").build(),
        requestBuilder.f(4).name("amount").desc("Amount, transaction (cents)").dim("fixed(12)").type("n").build(),
        requestBuilder.f(7).name("transDateTime").desc("Transmission Date and Time").dim("fixed(10)").type("date").build(),
        requestBuilder.f(11).name("stan").desc("System Trace Audit Number").dim("fixed(6)").type("n").build(),
        requestBuilder.f(12).name("transTimeLocal").desc("Time, local transaction").dim("fixed(6)").type("time").build(),
        requestBuilder.f(13).name("transDateLocal").desc("Date, local transaction").dim("fixed(4)").type("date").build(),
        requestBuilder.f(32).name("acquierID").desc("Acquiring Institution ID").dim("llvar(4)").type("n").defaultValue("0000").build(),
        requestBuilder.f(37).name("rrn").desc("Retrieval Reference Number").dim("fixed(12)").type("n").build(),
        requestBuilder.f(41).name("cardTermId").desc("Card Acceptor Terminal ID").dim("fixed(16)").type("ans").build(),
        requestBuilder.f(43).name("cardTermName").desc("Card Acceptor Terminal Name").dim("fixed(40)").type("ans").build(),
        requestBuilder.f(48).name("msisdn").desc("Additional Data (MSISDN)").dim("llvar(14)").type("n").build(),
        requestBuilder.f(49).name("currencyCode").desc("Currency Code, Transaction").dim("fixed(3)").type("n").build(),
        requestBuilder.f(90).name("originalData").desc("Original data elements").dim("lllvar(999)").type("xn").build()));

    final MessageTemplate reversalMessageTemplate = MessageTemplate.create("ISO015000077", MTI.create(0x0400), BitmapType.BINARY);
    final FieldTemplate.Builder reversalBuilder = FieldTemplate.localBuilder(reversalMessageTemplate).get();
    reversalMessageTemplate.addFields(asList(
        reversalBuilder.f(2).name("cardNumber").desc("Payment Card Number").dim("llvar(2)").type("n").build(),
        reversalBuilder.f(7).name("transDateTime").desc("Transmission Date and Time").dim("fixed(10)").type("date").build(),
        reversalBuilder.f(12).name("transTimeLocal").desc("Time, local transaction").dim("fixed(6)").type("time").build(),
        reversalBuilder.f(28).name("transDateLocal").desc("Date, local transaction").dim("fixed(4)").type("date").build(),
        reversalBuilder.f(32).name("acquierID").desc("Acquiring Institution ID").dim("llvar(2)").type("n").build(),
        reversalBuilder.f(39).name("rrn").desc("Retrieval Reference Number").dim("fixed(12)").type("n").build(),
        reversalBuilder.f(41).name("cardTermId").desc("Card Acceptor Terminal ID").dim("fixed(6)").type("ans").build(),
        reversalBuilder.f(42).name("cardTermName").desc("Card Acceptor Terminal Name").dim("fixed(40)").type("ans").build(),
        reversalBuilder.f(50).name("msisdn").desc("Additional Data (MSISDN)").dim("lllvar(3)").type("n").build(),
        reversalBuilder.f(53).name("currencyCode2").desc("Currency Code, Transaction").dim("fixed(3)").type("n").build(),
        reversalBuilder.f(62).name("currencyCode3").desc("Currency Code, Transaction").dim("fixed(3)").type("n").build()));

    final MessageTemplate txAdviceMessageTemplate = MessageTemplate.create("ISO015000077", MTI.create(0x0220), BitmapType.BINARY);
    final FieldTemplate.Builder txAdviceBuilder = FieldTemplate.localBuilder(txAdviceMessageTemplate).get();
    txAdviceMessageTemplate.addFields(asList(
        txAdviceBuilder.f(2).name("cardNumber").desc("Payment Card Number").dim("llvar(20)").type("n").build(),
        txAdviceBuilder.f(7).name("transDateTime").desc("Transmission Date/Time").dim("fixed(10)").type("date").build(),
        txAdviceBuilder.f(22).name("posEntryMode").desc("Date, local transaction").dim("fixed(12)").type("an").build(),
        txAdviceBuilder.f(63).name("privateResv").desc("Private, reserved").dim("lllvar(120)").type("an").build()));

    return MessageFactory.Builder()
        .id("binaryMessageSet")
        .contentType(ContentType.BCD)
        .bitmapType(BitmapType.BINARY)
        .templates(asList(requestMessageTemplate,reversalMessageTemplate,txAdviceMessageTemplate))
        .build();
  }

}
