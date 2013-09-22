package org.maodian.tomao.io.xml

import scala.xml.Elem
import scala.xml.XML
import akka.io.PipelineContext
import akka.io.SymmetricPipePair
import akka.io.SymmetricPipelineStage
import akka.util.ByteString
import scala.collection.immutable.Nil

class ElemByteStringAdapter(val charset: String) extends SymmetricPipelineStage[PipelineContext, String, ByteString] {
  private val openStream = "<stream:stream "
  private val closeStream = "</stream:stream>"
  private val xmlDeclaration = "<?xml "
  private var streamNode = "";
  private var depth = 0;
  private val buffer = ByteString.newBuilder

  override def apply(ctx: PipelineContext) =
    new SymmetricPipePair[String, ByteString] {
      override def eventPipeline = { bs: ByteString =>
        println(bs.decodeString(charset))
        val withoutHeadingSpace = bs.dropWhile(Character.isWhitespace(_))
        if (withoutHeadingSpace.startsWith(ByteString(xmlDeclaration, charset))) {
          buffer.append(withoutHeadingSpace)
          Nil
        } else if (withoutHeadingSpace.startsWith(ByteString(openStream, charset))) {
          buffer.append(withoutHeadingSpace).append(ByteString(closeStream, charset))
          ctx.singleEvent(buffer.result.decodeString(charset))
        } else if (withoutHeadingSpace.startsWith(ByteString("</", charset))) {
          depth = depth - 1
          buffer.append(withoutHeadingSpace)
        } else if (withoutHeadingSpace.startsWith(ByteString("<", charset))) {
          depth = depth + 1
          buffer.append(withoutHeadingSpace)
        }

        if (depth == 0) {
          ctx.singleEvent(buffer.result.decodeString(charset))
        } else {
          Nil
        }
      }

      override def commandPipeline = { str: String =>
        ctx.singleCommand(ByteString(str, charset))
      }
    }
}