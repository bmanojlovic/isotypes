package org.nulleins.formats.iso8583;

import org.junit.Test;
import org.nulleins.formats.iso8583.formatters.AddAmountsFormatter;
import org.nulleins.formats.iso8583.formatters.CardAcceptorLocationFormatter;
import org.nulleins.formats.iso8583.types.CardAcceptorLocation;
import org.nulleins.formats.iso8583.types.Dimension;
import org.nulleins.formats.iso8583.types.PostilionAddAmount;

import java.math.BigInteger;
import java.text.ParseException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author phillipsr
 */
public class TestCustomFields {
  private static final CardAcceptorLocationFormatter calFormatter = new CardAcceptorLocationFormatter();
  private static final AddAmountsFormatter aaFormatter = new AddAmountsFormatter();

  @Test
  public void testCustomerFormat() {
    final CardAcceptorLocation target = new CardAcceptorLocation("PH Rumukrushi", "Porthar", "PH", "NG");

    final byte[] data = calFormatter.format("CALf", target, Dimension.parse("FIXED(40)"));
    assertThat(new String(data), is("PH Rumukrushi          Porthar      PHNG"));
  }

  @Test
  public void testCustomerParse()
      throws ParseException {
    final byte[] data = "PH Rumukrushi          Porthar      PHNG".getBytes();
    final CardAcceptorLocation response = calFormatter.parse("CALf", Dimension.parse("FIXED(40)"), 40, data);
    assertThat(response.getLocation(), is("PH Rumukrushi"));
    assertThat(response.getCity(), is("Porthar"));
    assertThat(response.getState(), is("PH"));
    assertThat(response.getCountry(), is("NG"));
  }

  @Test
  public void testCustomerFormat2() {
    final PostilionAddAmount[] target = new PostilionAddAmount[3];
    target[0] = new PostilionAddAmount(10, 2, 566, new BigInteger("2426026"));
    target[1] = new PostilionAddAmount(10, 3, 566, BigInteger.ZERO);
    target[2] = new PostilionAddAmount(10, 1, 566, new BigInteger("2426026"));
    final byte[] data = aaFormatter.format("AAf", target, Dimension.parse("lllvar(10)"));
    assertThat(data.length , is(60));
    assertThat(new String(data), is("1002566C0000024260261003566C0000000000001001566C000002426026"));
  }

  @Test
  public void testCustomerParse2()
      throws ParseException {
    //             | . .  .            | . .  .            | . .  .
    final byte[] data = "1002566C0000024260261003566C0000000000001001566D000002426026".getBytes();
    final PostilionAddAmount[] response = aaFormatter.parse("AAf", Dimension.parse("FIXED(40)"), data.length, data);

    assertThat(response.length, is(3));

    assertThat(response[0].getAccountType(), is(10));
    assertThat(response[0].getAmount(), is(BigInteger.valueOf(2426026)));
    assertThat(response[0].getAmountType(), is(2));
    assertThat(response[0].getCurrencyCode(), is(566));

    assertThat(response[1].getAccountType(), is(10));
    assertThat(response[1].getAmount(), is(BigInteger.ZERO));
    assertThat(response[1].getAmountType(), is(3));
    assertThat(response[1].getCurrencyCode(), is(566));

    assertThat(response[2].getAccountType(), is(10));
    assertThat(response[2].getAmount(), is(BigInteger.valueOf(2426026).negate()));
    assertThat(response[2].getAmountType(), is(1));
    assertThat(response[2].getCurrencyCode(), is(566));
  }

  @Test
  public void twoWayTest()
      throws ParseException {
    final String data = "1002566C0000024260261003566C0000000000001001566C000002426026";
    final PostilionAddAmount[] target = aaFormatter.parse("AAf", Dimension.parse("LLVAR(120)"), data.length(), data.getBytes());
    final String response = new String(aaFormatter.format("AAf", target, Dimension.parse("LLVAR(120)")));
    assertThat(response, is(data));
  }

}
