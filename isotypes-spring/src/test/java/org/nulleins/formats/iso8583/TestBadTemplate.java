package org.nulleins.formats.iso8583;

import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.text.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

public class TestBadTemplate {

  private static final String SampleContextPath = "classpath:org/nulleins/formats/iso8583/TestBadTemplate-context.xml";

  @Test(expected = BeanCreationException.class)
  public void failsToDefineTemplate() throws IOException, ParseException {
    final ApplicationContext context = new ClassPathXmlApplicationContext(SampleContextPath);
    try {
      new MessageSample(context.getBean("badMessages", MessageFactory.class));
    } catch ( final Exception e) {
      assertThat(e, instanceOf(BeanCreationException.class));
      assertThat(e.getCause(), instanceOf(IllegalStateException.class));
      assertThat(e.getCause().getMessage(), is("duplicate field f: 2 defined for Message type 0200"));
      throw e;
    }
  }
}
