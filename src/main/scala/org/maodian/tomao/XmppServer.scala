/**
 *
 */
package org.maodian.tomao

import java.net.InetSocketAddress

import scala.xml.Elem

import org.maodian.tomao.io.xml.ElemByteStringAdapter

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.BackpressureBuffer
import akka.io.DelimiterFraming
import akka.io.IO
import akka.io.Tcp
import akka.io.Tcp.Bind
import akka.io.Tcp.Bound
import akka.io.Tcp.Connected
import akka.io.TcpPipelineHandler
import akka.io.TcpPipelineHandler.Init
import akka.io.TcpPipelineHandler.WithinActorContext
import akka.io.TcpReadWriteAdapter
import akka.util.ByteString

/**
 * @author Cole Wen
 *
 */
class XmppServer(local: InetSocketAddress) extends Actor with ActorLogging {
  import Tcp._

  implicit def system = context.system

  def receive: Receive = {
    case Start =>
      log.info("Start server...")
      IO(Tcp) ! Bind(self, local)
    case _: Bound ⇒
      log.info("The server is ready to accept connections at {}", local)
      context.become(bound(sender))
  }

  def bound(listener: ActorRef): Receive = {
    case Connected(remote, _) ⇒
      val init = TcpPipelineHandler.withLogger(log,
        new ElemByteStringAdapter("utf-8") >>
          new DelimiterFraming(maxSize = 8192, delimiter = ByteString('>'),
            includeDelimiter = true) >>
          new TcpReadWriteAdapter >>
          new BackpressureBuffer(lowBytes = 100, highBytes = 1000, maxBytes = 1000000))

      val connection = sender
      val handler = context.actorOf(Props(new TestActor(init)))
      val pipeline = context.actorOf(TcpPipelineHandler.props(
        init, connection, handler))

      connection ! Tcp.Register(pipeline)
  }
}

case object Start

class TestActor(init: Init[WithinActorContext, Elem, Elem]) extends Actor with ActorLogging {
  def receive = {
    case init.Event(data) ⇒
      log.info("akka-io Server received stanzas {} from {}", data, sender)
    /*val response = serverResponse(input)
      sender ! init.Command(response)
      log.debug("akka-io Server sent: {}", response.dropRight(1))*/
    case _: Tcp.ConnectionClosed ⇒ context.stop(self)
  }
}