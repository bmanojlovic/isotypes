package org.nulleins.formats.iso8583.config;

import org.nulleins.formats.iso8583.AutoGeneratorFactory;
import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.MessageTemplate;
import org.nulleins.formats.iso8583.StanGenerator;
import org.nulleins.formats.iso8583.formatters.AddAmountsFormatter;
import org.nulleins.formats.iso8583.formatters.CardAcceptorLocationFormatter;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.FieldType;
import org.nulleins.formats.iso8583.types.MTI;

import static java.util.Arrays.asList;

public class BankMessageConfiguration {

  public static MessageFactory createMessageFactory() {

    final FieldType AAfType = new FieldType("AAf");
    final FieldType CALfType = new FieldType("CALf");

    final MessageTemplate requestMessageTemplate = MessageTemplate.create("ISO015000077", MTI.create(0x0200), BitmapType.HEX);
    requestMessageTemplate.setName("Transaction Request");
    final FieldTemplate.Builder requestBuilder = FieldTemplate.localBuilder(requestMessageTemplate).get();
    requestMessageTemplate.setFields(asList(
        requestBuilder.f(2).name("accountNumber").desc("Primary Account Number").dim("llvar(19)").type("n").build(),
        requestBuilder.f(3).name("processingCode").desc("Processing Code").dim("fixed(6)").type("n").build(),
        requestBuilder.f(4).name("amount").desc("Amount, transaction (cents)").dim("fixed(12)").type("n").build(),
        requestBuilder.f(7).name("transDateTime").desc("Transmission Date and Time").dim("fixed(10)").type("date").build(),
        requestBuilder.f(11).name("stan").desc("System Trace Audit Number").dim("fixed(6)").type("n").build(),
        requestBuilder.f(12).name("transTimeLocal").desc("Time, local transaction").dim("fixed(6)").type("time").build(),
        requestBuilder.f(13).name("transDateLocal").desc("Date, local transaction").dim("fixed(4)").type("date").build(),
        requestBuilder.f(17).name("captureDate").desc("Date, capture").dim("fixed(4)").type("date").build(),
        requestBuilder.f(28).name("transactionFee").desc("Amount, transaction fee").dim("fixed(9)").type("xn").build(),
        requestBuilder.f(30).name("processingFee").desc("Amount, tx processing fee").dim("fixed(9)").type("xn").build(),
        requestBuilder.f(32).name("acquierID").desc("Acquiring Institution ID").dim("llvar(11)").type("n").build(),
        requestBuilder.f(33).name("forwarderID").desc("Forwarding Institution ID").dim("llvar(11)").type("n").build(),
        requestBuilder.f(37).name("rrn").desc("Retrieval Reference Number").dim("fixed(12)").type("anp").build(),
        requestBuilder.f(41).name("cardTermId").desc("Card Acceptor Terminal ID").dim("fixed(8)").type("ans").build(),
        requestBuilder.f(42).name("cardAcceptorId").desc("Card Acceptor ID Code").dim("fixed(15)").type("ans").build(),
        requestBuilder.f(43).name("cardAcceptorLoc").desc("Card Acceptor Location Name").dim("fixed(40)").type("ans").build(),
        requestBuilder.f(49).name("currencyCode").desc("Currency Code, Transaction").dim("fixed(3)").type("n").build(),
        requestBuilder.f(60).name("adviceCode").desc("Advice/reason code").dim("lllvar(999)").type("an").build()));

    final MessageTemplate responseMessageTemplate = MessageTemplate.create("ISO015000077", MTI.create(0x0210), BitmapType.HEX);
    final FieldTemplate.Builder responseBuilder = FieldTemplate.localBuilder(responseMessageTemplate).get();
    responseMessageTemplate.setFields(asList(
        responseBuilder.f(2).name("accountNumber").desc("Primary Account Number").dim("llvar(19)").type("n").build(),
        responseBuilder.f(3).name("processingCode").desc("Processing Code").dim("fixed(6)").type("n").build(),
        responseBuilder.f(4).name("amount").desc("Amount, transaction (cents)").dim("fixed(12)").type("n").build(),
        responseBuilder.f(7).name("transDateTime").desc("Transmission Date and Time").dim("fixed(10)").type("date").build(),
        responseBuilder.f(11).name("stan").desc("System Trace Audit Number").dim("fixed(6)").type("n").build(),
        responseBuilder.f(12).name("transTimeLocal").desc("Time, local transaction").dim("fixed(6)").type("time").build(),
        responseBuilder.f(13).name("transDateLocal").desc("Date, local transaction").dim("fixed(4)").type("date").build(),
        responseBuilder.f(17).name("captureDate").desc("Date, capture").dim("fixed(4)").type("date").build(),
        responseBuilder.f(28).name("transactionFee").desc("Amount, transaction fee").dim("fixed(9)").type("xn").build(),
        responseBuilder.f(30).name("processingFee").desc("Amount, tx processing fee").dim("fixed(9)").type("xn").build(),
        responseBuilder.f(32).name("acquierID").desc("Acquiring Institution ID").dim("llvar(11)").type("n").build(),
        responseBuilder.f(33).name("forwarderID").desc("Forwarding Institution ID").dim("llvar(11)").type("n").build(),
        responseBuilder.f(37).name("rrn").desc("Retrieval Reference Number").dim("fixed(12)").type("an").build(),
        responseBuilder.f(41).name("cardTermId").desc("Card Acceptor Terminal ID").dim("fixed(8)").type("ans").build(),
        responseBuilder.f(42).name("cardAcceptorId").desc("Card Acceptor ID Code").dim("fixed(15)").type("ans").build(),
        responseBuilder.f(43).name("cardAcceptorLoc").desc("Card Acceptor Location Name").dim("fixed(40)").type("ans").build(),
        responseBuilder.f(49).name("currencyCode").desc("Currency Code, Transaction").dim("fixed(3)").type("n").build(),
        responseBuilder.f(54).name("addAmounts").desc("Additional Amounts").dim("lllvar(120)").type("ans").build(),
        responseBuilder.f(60).name("adviceCode").desc("Advice/reason code").dim("lllvar(120)").type("an").build(),
        responseBuilder.f(102).name("accountId1").desc("Account Identification 1").dim("llvar(28)").type("ans").build()));

    return MessageFactory.Builder()
        .id("messageSet")
        .header("ISO015000077")
        .contentType(ContentType.TEXT)
        .bitmapType(BitmapType.HEX)
        .autogen(new AutoGeneratorFactory(new StanGenerator(1, 999)))
        .templates(asList(requestMessageTemplate, responseMessageTemplate))
        .addFormatter(AAfType.getCode(), new AddAmountsFormatter())
        .addFormatter(CALfType.getCode(), new CardAcceptorLocationFormatter())
        .build();

  }

}
