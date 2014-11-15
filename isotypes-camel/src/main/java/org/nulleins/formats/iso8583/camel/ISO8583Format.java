package org.nulleins.formats.iso8583.camel;

import com.google.common.base.Preconditions;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.nulleins.formats.iso8583.Message;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.types.MTI;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/** DataFormat capable to marshaling and unmarshalling ISO8583 messages
  * from and to a map of field values
  * @author phillipsr  */
public class ISO8583Format implements DataFormat
{
  private static final String MTI_HEADER = "ISO8583-MTI";
  private final MessageFactory factory;

  public ISO8583Format(final MessageFactory factory) {
    Preconditions.checkNotNull(factory);
    this.factory = factory;
  }

  @Override
  public void marshal ( final Exchange exchange, final Object graph, final OutputStream out) throws Exception {
    @SuppressWarnings("unchecked")
    final Map<String,Object> fieldMap = exchange.getContext()
                .getTypeConverter()
                .mandatoryConvertTo ( Map.class, graph);
        
    final MTI messageType = MTI.create ( exchange.getIn().getHeader(MTI_HEADER, Integer.class));
    final Message message = factory.createByNames ( messageType, fieldMap);
        
    factory.writeToStream ( message, out);
  }

  @Override
  public Object unmarshal ( final Exchange exchange, final InputStream stream) throws Exception {
    return factory.parse ( stream);
  }
}