package org.nulleins.formats.iso8583.camel.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.MessageTemplate;
import org.nulleins.formats.iso8583.camel.ISO8583Format;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.MTI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static java.util.Arrays.asList;

@Configuration
public class TestConfiguration extends SingleRouteCamelConfiguration {

  @Override
  public RouteBuilder route() {
    return new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        from("seda:queue:request")
            .unmarshal().custom("iso8583")
            .to("mock:receiver");
      }
    };
  }

  @Bean
  public MessageFactory factory() {
    final FieldTemplate.Builder builder = FieldTemplate.localBuilder().get();
    final List<FieldTemplate> fields = asList(
        builder.f(2).name("accountNumber").desc("Primary Account Number").dim("llvar(19)").type("n").build(),
        builder.f(3).name("processingCode").desc("Processing Code").dim("fixed(6)").type("n").build(),
        builder.f(4).name("amount").desc("Amount, transaction (cents)").dim("fixed(12)").type("n").build(),
        builder.f(7).name("transDateTime").desc("Transmission Date and Time").dim("fixed(10)").type("date").build(),
        builder.f(11).name("stan").desc("System Trace Audit Number").dim("fixed(6)").type("n").build(),
        builder.f(12).name("transTimeLocal").desc("Time, local transaction").dim("fixed(6)").type("time").build(),
        builder.f(13).name("transDateLocal").desc("Date, local transaction").dim("fixed(4)").type("date").build(),
        builder.f(17).name("captureDate").desc("Date, capture").dim("fixed(4)").type("date").build(),
        builder.f(28).name("transactionFee").desc("Amount, transaction fee").dim("fixed(9)").type("xn").build(),
        builder.f(30).name("processingFee").desc("Amount, tx processing fee").dim("fixed(9)").type("xn").build(),
        builder.f(32).name("acquierID").desc("Acquiring Institution ID").dim("llvar(11)").type("n").build(),
        builder.f(33).name("forwarderID").desc("Forwarding Institution ID").dim("llvar(11)").type("n").build(),
        builder.f(37).name("rrn").desc("Retrieval Reference Number").dim("fixed(12)").type("an").build(),
        builder.f(41).name("cardTermId").desc("Card Acceptor Terminal ID").dim("fixed(8)").type("ans").build(),
        builder.f(42).name("cardAcceptorId").desc("Card Acceptor ID Code").dim("fixed(15)").type("ans").build(),
        builder.f(43).name("cardAcceptorLoc").desc("Card Acceptor Location Name").dim("fixed(40)").type("ans").build(),
        builder.f(49).name("currencyCode").desc("Currency Code, Transaction").dim("fixed(3)").type("n").build(),
        builder.f(54).name("addAmounts").desc("Additional Amounts").dim("lllvar(120)").type("ans").build(),
        builder.f(60).name("adviceCode").desc("Advice/reason code").dim("lllvar(120)").type("an").build(),
        builder.f(102).name("accountId1").desc("Account Identification 1").dim("llvar(28)").type("ans").build());
    final MessageTemplate template = MessageTemplate.Builder().header("ISO015000077").type(MTI.create(0x0210)).fieldlist(fields).build();
    return MessageFactory.Builder()
        .id("testMessages")
        .header("ISO015000077")
        .contentType(ContentType.TEXT)
        .templates(asList(template))
        .charset (CharEncoder.ASCII)
        .bitmapType (BitmapType.HEX).build();
  }

  @Bean
  public ISO8583Format iso8583() {
    return new ISO8583Format(factory());
  }

}
