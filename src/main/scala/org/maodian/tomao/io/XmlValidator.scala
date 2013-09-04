/**
 *
 */
package org.maodian.tomao.io

import akka.io.SymmetricPipelineStage
import akka.io.PipelineContext
import akka.util.ByteString
import scala.xml.NodeSeq
import akka.io.SymmetricPipePair

/**
 * @author Cole Wen
 *
 */
class XmlValidator extends SymmetricPipelineStage[PipelineContext, ByteString, ByteString] {
  override def apply(ctx: PipelineContext) = new SymmetricPipePair[ByteString, ByteString] {
    override def eventPipeline = { bs: ByteString =>
      import XmlValidator._
      invalidXmlPairs map {
        _ match {
          case Pair(None, Some(tail)) if bs.takeRight(tail.length) == tail =>
            throw new IllegalArgumentException
          case Pair(Some(head), Some(tail)) if (bs.take(head.length) == head && bs.takeRight(tail.length) == tail) =>
            throw new IllegalArgumentException
          case Pair(Some(head), None) if bs.take(head.length) == head =>
            throw new IllegalArgumentException
          case _ =>
        }
      }
      ctx.singleEvent(bs)
    }
    override def commandPipeline = { bs: ByteString =>
      ctx.singleCommand(bs)
    }
  }
}

object XmlValidator {
  val invalidXmlPairs = Vector(
    Pair(None, Some("-->")),
    Pair(None, Some("]]>")),
    Pair(Some("<?xml "), Some("?>")),
    Pair(Some("<!"), None))
}