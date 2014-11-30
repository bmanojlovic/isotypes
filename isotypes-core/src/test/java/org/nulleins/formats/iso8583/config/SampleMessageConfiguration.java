package org.nulleins.formats.iso8583.config;

import org.nulleins.formats.iso8583.AutoGeneratorFactory;
import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.MessageTemplate;
import org.nulleins.formats.iso8583.StanGenerator;
import org.nulleins.formats.iso8583.formatters.AddAmountsFormatter;
import org.nulleins.formats.iso8583.formatters.CardAcceptorLocationFormatter;
import org.nulleins.formats.iso8583.types.*;

import java.util.List;

import static java.util.Arrays.asList;

public class SampleMessageConfiguration {

  public static MessageFactory createMessageFactory() {

    final FieldType AAfType = new FieldType("AAf");
    final FieldType CALfType = new FieldType("CALf");


    final FieldTemplate.Builder requestBuilder = FieldTemplate.localBuilder().get();
    final List<FieldTemplate> requestFields = asList(
        requestBuilder.f(2).name("cardNumber").desc("Payment Card Number").dim("llvar(40)").type("ns").build(),
        requestBuilder.f(3).name("processingCode").desc("Processing Code").dim("fixed(6)").type("n").defaultValue("101010").build(),
        requestBuilder.f(4).name("amount").desc("Amount, transaction (cents)").dim("fixed(12)").type("n").build(),
        requestBuilder.f(7).name("transDateTime").desc("Transmission Date and Time").dim("fixed(10)").type("date").build(),
        requestBuilder.f(11).name("stan").desc("System Trace Audit Number").dim("fixed(6)").type("n").build(),
        requestBuilder.f(12).name("transTimeLocal").desc("Time, local transaction").dim("fixed(6)").type("time").build(),
        requestBuilder.f(13).name("transDateLocal").desc("Date, local transaction").dim("fixed(4)").type("date").build(),
        requestBuilder.f(32).name("acquierID").desc("Acquiring Institution ID").dim("llvar(4)").type("n").build(),
        requestBuilder.f(35).name("track2").desc("Track 2 Data").dim("llvar(37)").type("z").build(),
        requestBuilder.f(37).name("extReference").desc("Retrieval Reference Number").dim("fixed(12)").type("n").build(),
        requestBuilder.f(41).name("cardTermId").desc("Card Acceptor Terminal ID").dim("fixed(16)").type("ans").build(),
        requestBuilder.f(43).name("cardTermName").desc("Card Acceptor Terminal Name").dim("fixed(40)").type("ans").build(),
        requestBuilder.f(45).name("track1").desc("Track 1 Data").dim("llvar(76)").type("z").build(),
        requestBuilder.f(48).name("msisdn").desc("Additional Data (MSISDN)").dim("llvar(14)").type("n").build(),
        requestBuilder.f(49).name("currencyCode").desc("Currency Code, Transaction").dim("fixed(3)").type("n").build(),
        requestBuilder.f(90).name("originalData").desc("Original data elements").dim("lllvar(4)").type("xn").build());
    final MessageTemplate requestMessageTemplate = MessageTemplate.Builder().name("Acquirer Payment Request").header("ISO015000077").type(MTI.create(0x0200)).fieldlist(requestFields).build();

    final FieldTemplate.Builder responseBuilder = FieldTemplate.localBuilder().get();
    final List<FieldTemplate> responseFields = asList(
        responseBuilder.f(2).name("accountNumber").desc("Primary Account Number").dim("llvar(19)").type("n").build(),
        responseBuilder.f(2).name("cardNumber").desc("Payment Card Number").dim("llvar(14)").type("n").build(),
        responseBuilder.f(7).name("transDateTime").desc("Transmission Date and Time").dim("fixed(10)").type("date").build(),
        responseBuilder.f(11).name("stan").desc("System Trace Audit Number").dim("fixed(6)").type("n").build(),
        responseBuilder.f(12).name("transTimeLocal").desc("Time, local transaction").dim("fixed(6)").type("time").build(),
        responseBuilder.f(13).name("transDateLocal").desc("Date, local transaction").dim("fixed(4)").type("date").build(),
        responseBuilder.f(32).name("acquierID").desc("Acquiring Institution ID").dim("llvar(4)").type("n").build(),
        responseBuilder.f(37).name("extReference").desc("Retrieval Reference Number").dim("fixed(12)").type("n").build(),
        responseBuilder.f(41).name("cardTermId").desc("Card Acceptor Terminal ID").dim("fixed(16)").type("ans").build(),
        responseBuilder.f(43).name("cardTermName").desc("Card Acceptor Terminal Name").dim("fixed(40)").type("ans").build(),
        responseBuilder.f(48).name("msisdn").desc("Additional Data (MSISDN)").dim("lllvar(14)").type("n").build(),
        responseBuilder.f(53).name("currencyCode2").desc("Currency Code, Transaction").dim("fixed(3)").type("n").build(),
        responseBuilder.f(62).name("currencyCode3").desc("Currency Code, Transaction").dim("fixed(3)").type("n").build());

    final MessageTemplate responseMessageTemplate = MessageTemplate.Builder()
    .name("Reversal Request").header("ISO015000077").type(MTI.create(0x0400)).fieldlist(requestFields).build();

    return MessageFactory.Builder()
        .id("messageSet")
        .header("ISO015000077")
        .contentType(ContentType.TEXT)
        .bitmapType(BitmapType.HEX)
        .charset(CharEncoder.ASCII)
        .autogen(new AutoGeneratorFactory(new StanGenerator(1, 999)))
        .templates(asList(requestMessageTemplate, responseMessageTemplate))
        .addFormatter(AAfType.getCode(), new AddAmountsFormatter())
        .addFormatter(CALfType.getCode(), new CardAcceptorLocationFormatter())
        .build();

  }

}
