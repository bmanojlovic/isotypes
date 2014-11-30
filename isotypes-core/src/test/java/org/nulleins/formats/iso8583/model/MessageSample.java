package org.nulleins.formats.iso8583.model;

import org.nulleins.formats.iso8583.Message;
import org.nulleins.formats.iso8583.MessageException;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.PaymentRequestBean;
import org.nulleins.formats.iso8583.TrackData;
import org.nulleins.formats.iso8583.TrackData.Track;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Sample class demonstrating the isotypes API
 * <p/>
 * @author phillipsr
 */
public class MessageSample {

  private final MessageFactory factory; // defined in the iso8583.xml context
  private final OutputStream output = new OutputStream() {
    @Override
    public void write(final int b) { /* throw it all away */ }
  };

  public MessageSample(final MessageFactory factory) {
    this.factory = factory;
  }

  public void sendMessage(final int mti, final PaymentRequestBean request) throws IOException, ParseException {
    final Date dateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse("01-01-2013 10:15:30");
    final TrackData track1data = TrackData.Builder()
    .type(Track.TRACK1)
    .primaryAccountNumber(123456789L)
    .name(new String[]{"Bugg", "Harry", "H", "Mr"})
    .expirationDate(1212)
    .serviceCode(120).build ();
    final TrackData track2data = TrackData.Builder()
    .type(Track.TRACK2)
    .primaryAccountNumber(track1data.getPrimaryAccountNumber())
    .expirationDate(track1data.getExpirationDate())
    .serviceCode(track1data.getServiceCode()).build ();

    // instantiate request from business object
    final Message message = factory.createFromBean(MTI.create(mti), request,
        new HashMap<Integer,Object>() {{ // additional fields used by the ISO8583 protocol/server
          put(3, 101010);     // processing code
          put(7, dateTime);  // transmission date and time
          put(11, 4321);     // trace (correlation) f
          put(12, dateTime); // transaction time
          put(13, dateTime); // transaction date
          put(45, track1data);
          put(35, track2data);
        }});

    // log the message content:
    for (final String line : message.describe()) {
      System.out.println("INFO: " + line);
    }

    // check the message is good-to-go:
    final List<String> errors = message.validate();
    if (!errors.isEmpty()) {
      throw new MessageException(errors.toString ());
    }

    // write it to the dummy output stream:
    factory.writeToStream(message, output);
  }

}
