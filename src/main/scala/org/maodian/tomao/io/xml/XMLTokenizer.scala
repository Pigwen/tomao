package org.maodian.tomao.io.xml

import org.maodian.tomao.io.XmlTokenizer
import java.nio.charset.CharsetDecoder
import akka.util.ByteString
import akka.util.ByteStringBuilder
import java.nio.charset.CharacterCodingException
import java.nio.CharBuffer
import org.apache.vysper.charset.CharsetUtil
import org.xml.sax.SAXException
import org.apache.vysper.xml.sax.impl.XMLTokenizer.TokenListener

object XMLTokenizer2 {
  val NO_CHAR: Int = -1;
}

class XMLTokenizer2(listener: TokenListener) extends XmlTokenizer {
  import XMLTokenizer2._

  val buffer = ByteString.newBuilder
  private var state: State = State.START

  trait State
  object State {
    object START extends State
    object IN_TAG extends State
    object IN_STRING extends State
    object IN_DOUBLE_ATTRIBUTE_VALUE extends State
    object IN_SINGLE_ATTRIBUTE_VALUE extends State
    object IN_TEXT extends State
    object CLOSED extends State

  }

  def parse(byteBuffer: ByteString, decoder: CharsetDecoder): Unit = {
    //while (byteBuffer.hasRemaining() && state != State.CLOSED) {
    for (c <- byteBuffer if state != State.CLOSED) {
      if (state == State.START) {
        if (c == '<') {
          emit(c);
          state = State.IN_TAG;
        } else if (Character.isWhitespace(c)) {
          // ignore
        } else {
          state = State.IN_TEXT;
          buffer.putByte(c)
        }
      } else if (state == State.IN_TEXT) {
        if (c == '<') {
          emit(decoder);
          emit(c);
          state = State.IN_TAG;
        } else {
          buffer.putByte(c);
        }
      } else if (state == State.IN_TAG) {
        if (c == '>') {
          emit(c);
          state = State.START;
        } else if (c == '"') {
          emit(c);
          state = State.IN_DOUBLE_ATTRIBUTE_VALUE;
        } else if (c == '\'') {
          emit(c);
          state = State.IN_SINGLE_ATTRIBUTE_VALUE;
        } else if (c == '-') {
          emit(c);
        } else if (isControlChar(c)) {
          emit(c);
        } else if (Character.isWhitespace(c)) {
          buffer.clear();
        } else {
          state = State.IN_STRING;
          buffer.putByte(c);
        }
      } else if (state == State.IN_STRING) {
        if (c == '>') {
          emit(CharsetUtil.getDecoder());
          emit(c);
          state = State.START;
        } else if (isControlChar(c)) {
          emit(CharsetUtil.getDecoder());
          emit(c);
          state = State.IN_TAG;
        } else if (Character.isWhitespace(c)) {
          emit(CharsetUtil.getDecoder());
          state = State.IN_TAG;
        } else {
          buffer.putByte(c);
        }
      } else if (state == State.IN_DOUBLE_ATTRIBUTE_VALUE) {
        if (c == '"') {
          emit(decoder);
          emit(c);
          state = State.IN_TAG;
        } else {
          buffer.putByte(c);
        }
      } else if (state == State.IN_SINGLE_ATTRIBUTE_VALUE) {
        if (c == '\'') {
          emit(decoder);
          emit(c);
          state = State.IN_TAG;
        } else {
          buffer.putByte(c);
        }
      }
    }
  }

  def close: Unit = {
    state = State.CLOSED;
    buffer.clear();
  }

  def restart: Unit = {
    buffer.clear();
  }

  private def isControlChar(c: Byte): Boolean = {
    return c == '<' || c == '>' || c == '!' || c == '/' || c == '?' || c == '=';
  }

  private def emit(token: Byte): Unit = {
    // method will only be called for control chars, thus the cast to char should be safe
    listener.token(token.toChar, null);
  }

  private def emit(decoder: CharsetDecoder): Unit = {
    try {
      //buffer.flip();
      //CharBuffer charBuffer = decoder.decode(buffer.buf());
      listener.token(NO_CHAR.toChar, buffer.result.decodeString("utf-8"));
      buffer.clear();
    } catch {
      case e: CharacterCodingException =>
        throw new SAXException(e);
    }
  }
}