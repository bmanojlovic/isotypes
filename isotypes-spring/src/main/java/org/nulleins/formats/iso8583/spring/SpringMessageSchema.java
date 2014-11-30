package org.nulleins.formats.iso8583.spring;

import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.ContentType;

import java.util.List;

public class SpringMessageSchema {
  private String id;
  private String header;
  private boolean strict;
  private BitmapType bitmapType;
  private ContentType contentType;

  //public List<SpringMessageTemplate> getMessages () { return messages; }

  //public void setMessages (List<SpringMessageTemplate> messages) { this.messages = messages; }

  private List<SpringMessageTemplate> messages;

  public String getId () {
    return id;
  }

  public void setId (String id) {
    this.id = id;
  }

  public String getHeader () {
    return header;
  }

  public void setHeader (String header) {
    this.header = header;
  }

  public boolean isStrict () {
    return strict;
  }

  public void setStrict (boolean strict) {
    this.strict = strict;
  }

  public BitmapType getBitmapType () {
    return bitmapType;
  }

  public void setBitmapType (BitmapType bitmapType) {
    this.bitmapType = bitmapType;
  }

  public ContentType getContentType () {
    return contentType;
  }

  public void setContentType (ContentType contentType) {
    this.contentType = contentType;
  }

  public String getCharset () {
    return charset;
  }

  public void setCharset (String charset) {
    this.charset = charset;
  }

  private String charset;

}
