package org.maodian.tomao.io.xml

import scala.xml.Elem
import scala.xml.XML
import akka.io.PipelineContext
import akka.io.SymmetricPipePair
import akka.io.SymmetricPipelineStage
import akka.util.ByteString
import scala.collection.immutable.Nil

class ElemByteStringAdapter(val charset: String) extends SymmetricPipelineStage[PipelineContext, Elem, ByteString] {
  private val openStream = "<stream:stream "
  private val closeStream = "</stream:stream>"
    private val xmlDeclaration = "<?xml "
  private var streamNode = "";

  override def apply(ctx: PipelineContext) =
    new SymmetricPipePair[Elem, ByteString] {
      override def eventPipeline = { bs: ByteString =>
        println(bs.decodeString(charset))
        val withoutHeadingSpace = bs.dropWhile(Character.isWhitespace(_))
        if (withoutHeadingSpace.startsWith(ByteString(xmlDeclaration, charset))) {
          Nil
        } else if (withoutHeadingSpace.startsWith(ByteString(openStream, charset))) {
          streamNode = withoutHeadingSpace.decodeString(charset) + closeStream
          ctx.singleEvent(XML.loadString(streamNode))
        } else {
          ctx.singleEvent(XML.loadString(streamNode + withoutHeadingSpace.decodeString(charset) + closeStream))
        }
      }

      override def commandPipeline = { el: Elem =>
        ctx.singleCommand(ByteString(el.toString, charset))
      }
    }
}