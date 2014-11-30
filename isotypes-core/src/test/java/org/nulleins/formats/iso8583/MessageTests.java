package org.nulleins.formats.iso8583;

import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;
import org.nulleins.formats.iso8583.types.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class MessageTests {

  private MessageFactory factory;
  private Message message400;
  private Message message410;
  private Message message210;
  private Message message200;
  private Message message200_2;

  @Before
  public void createFactory() {

    final MessageTemplate template400 = MessageTemplate.Builder()
        .header("ISO015000077")
        .type(MTI.create(0x0400))
        .fieldlist(asList(
            FieldTemplate.localBuilder().get().f(2).type(FieldType.NUMERIC).dim("fixed(3)").name("TestField").build(),
            FieldTemplate.localBuilder().get().f(3).type(FieldType.NUMERIC).dim("fixed(3)").name("TestField").build()))
        .build();

    final MessageTemplate template410 = MessageTemplate.Builder()
        .header("ISO015000077")
        .type(MTI.create(0x0410))
        .fieldlist(asList(
            FieldTemplate.localBuilder().get().f(2).type(FieldType.NUMERIC).dim("fixed(3)").name("TestField").build()))
        .build();
    final MessageTemplate template200 = MessageTemplate.Builder().header("ISO015000077").type(MTI.create(0x0200)).fieldlist(
        asList(FieldTemplate.localBuilder().get().f(2).type(FieldType.NUMERIC).dim("fixed(3)").name("TestField").build())).build();
    final MessageTemplate template210 = MessageTemplate.Builder().header("ISO015000077").type(MTI.create(0x0210)).fieldlist(
        asList(FieldTemplate.localBuilder().get().f(2).type(FieldType.NUMERIC).dim("fixed(3)").name("TestField").build())).build();

    this.factory = MessageFactory.Builder()
    .id("testFactory")
    .description("Test Message Schema")
    .bitmapType(BitmapType.HEX)
    .contentType(ContentType.TEXT)
        .charset(CharEncoder.ASCII)
    .header("ISO015000077")
    .addTemplate(template200)
    .addTemplate(template210)
    .addTemplate(template400)
    .addTemplate(template410)
    .build();

    final MTI mt1 = MTI.create(0x0400);
    message400 = Message.Builder()
        .template(factory.getTemplate(mt1))
        .build();
    final MTI mt2 = MTI.create(0x0410);
    message410 = Message.Builder()
        .template(factory.getTemplate(mt2))
        .build();
    final MTI mt3 = MTI.create(0x0210);
    message210 = Message.Builder()
        .template(factory.getTemplate(mt3))
        .build();
    final MTI mt4 = MTI.create(0x0200);
    message200 = Message.Builder()
        .template(factory.getTemplate(mt4))
        .build();
    message200_2 = Message.Builder()
        .header("ISO015000077")
        .template(factory.getTemplate(mt4))
        .build();
  }


  @Test
  public void messagesSorted() {
    final Collection<Message> messageList = asList(message400, message410, message210, message200, message200_2);
    final TreeSet<Message> sortedSet = new TreeSet<>(messageList);
    assertThat(sortedSet.size(), is(4));
    assertThat ( Iterables.getFirst(sortedSet, null), is(message200));
    assertThat ( Iterables.getLast(sortedSet), is(message410));
  }

  @Test
  public void fieldEquality() {
    final MTI mti = MTI.create(0x0400);
    final Map<Integer,Object> field1 = new HashMap<Integer,Object>() {{
      put(2,1);
    }};
    final Map<Integer,Object> field2 = new HashMap<Integer,Object>() {{
      put(2,1);
    }};
    final Message first = Message.Builder()
        .header("ISO015000077")
        .template(factory.getTemplate(mti))
        .fields(field1)
        .build();
    final Message second = Message.Builder()
        .header("ISO015000077")
        .template(factory.getTemplate(mti))
        .fields(field2)
        .build();
    assertThat(first, is(second));
    assertThat(first.hashCode(), is(second.hashCode()));
  }

  @Test
  public void fieldInequality() {
    final MTI mti = MTI.create(0x0400);
    final Map<Integer,Object> field1 = new HashMap<Integer,Object>() {{
      put(2,1);
    }};
    final Map<Integer,Object> field2 = new HashMap<Integer,Object>() {{
      put(2,2);
    }};
    final Map<Integer,Object> field3 = new HashMap<Integer,Object>() {{
      put(3,1);
    }};
    final Message first = Message.Builder()
        .header("ISO015000077")
        .template(factory.getTemplate(mti))
        .fields(field1)
        .build();
    final Message second = Message.Builder()
        .header("ISO015000077")
        .template(factory.getTemplate(mti))
        .fields(field2)
        .build();
    assertThat(first, is(not(second)));
    assertThat(first.hashCode(), is(not(second.hashCode())));

    assertThat(first, not(nullValue(Message.class)));
    assertThat(first.equals("some string"), is(false));
    field1.putAll(field3);
    final Message third = Message.Builder()
        .header("ISO015000077")
        .template(factory.getTemplate(mti))
        .fields(field1)
        .build();
    assertThat(first, is(not(third)));
    assertThat(third.isFieldPresent(2), is(true));
    assertThat(third.isFieldPresent(3), is(true));

    final Message forth = Message.Builder()
        .header("ISO015000077")
        .template(factory.getTemplate(MTI.create(0x410)))
        .fields(field1)
        .build();
    assertThat(first, is(not(forth)));
  }

  @Test
  public void describesMessage() {
    final Iterable<String> description = message200.describe();
    assertThat(Iterables.getFirst(description, ""),
        is("MTI: 0200 (version=ISO 8583-1:1987 class=Financial Message function=Request origin=Acquirer) name: \"null\" header: [ISO015000077] #fieldlist: 1"));
    description.iterator().remove();
  }

}
