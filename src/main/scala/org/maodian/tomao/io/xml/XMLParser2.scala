package org.maodian.tomao.io.xml

import org.apache.vysper.xml.sax.impl._
import org.apache.vysper.xml.decoder._
import org.xml.sax.ContentHandler
import org.xml.sax.ErrorHandler
import java.util.{Map => JMap}
import java.lang.{Boolean => JBoolean}
import akka.util.ByteString
import java.nio.charset.CharsetDecoder
import org.xml.sax.SAXException
import org.apache.vysper.xml.sax.impl.XMLParser._

class XMLParser2(contentHandler: ContentHandler, errorHandler: ErrorHandler, features: JMap[String, JBoolean],
  properties: JMap[String, Object]) extends XMLParser(contentHandler, errorHandler, features, properties) {

  def parse(byteBuffer: ByteString, charsetDecoder: CharsetDecoder): Unit = {
        if (state == State.CLOSED)
            throw new SAXException("Parser is closed");

        try {
            tokenizer.parse(byteBuffer, charsetDecoder);
        } catch {
          case e: RuntimeException =>
            e.printStackTrace();
            fatalError(e.getMessage());
        }
    }
}