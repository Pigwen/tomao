/**
 *
 */
package org.maodian.tomao.io

import akka.io.SymmetricPipelineStage
import akka.io.PipelineContext
import akka.util.ByteString
import scala.xml.NodeSeq
import akka.io.SymmetricPipePair
import org.maodian.tomao.Start
import akka.util.ByteStringBuilder

/**
 * @author Cole Wen
 *
 */
class XmlTokenizer extends SymmetricPipelineStage[PipelineContext, ByteString, ByteString] {
  import XmlValidator._
  
  private var state: State = StartTag
  val buffer: ByteStringBuilder = new ByteStringBuilder
  
  override def apply(ctx: PipelineContext) = new SymmetricPipePair[ByteString, ByteString] {
    override def eventPipeline = { bs: ByteString =>
      bs foreach { b: Byte =>
        state match {
          case StartTag =>
            if (Character.isWhitespace(b)) {
              // ignore
            }
            b match {
              case '<' =>
                state = InTag
            }
          case InTag =>
          case EndTag =>
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
  sealed abstract class State
  case object StartTag extends State
  case object EndTag extends State
  case object InTag extends State
}