/**
 *
 */
package org.maodian.tomao

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.io.TcpPipelineHandler
import akka.actor.Props
import akka.io._
import akka.actor.ActorRef
import akka.util.ByteString
import akka.actor.ActorLogging
import akka.io.TcpPipelineHandler._
import akka.actor.Deploy
import org.maodian.tomao.io.XmlValidator
import org.maodian.tomao.io.XmlTokenizer

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
        new StringByteStringAdapter("utf-8") >>
          new DelimiterFraming(maxSize = 8192, delimiter = ByteString('>'),
            includeDelimiter = true) >>
          new XmlTokenizer >>
          new TcpReadWriteAdapter >>
          // new SslTlsSupport(sslEngine(remote, client = false)) >>
          new BackpressureBuffer(lowBytes = 100, highBytes = 1000, maxBytes = 1000000))

      val connection = sender
      val handler = context.actorOf(Props(new TestActor(init)))
      val pipeline = context.actorOf(TcpPipelineHandler.props(
        init, connection, handler))

      connection ! Tcp.Register(pipeline)
  }
}

case object Start

class TestActor(init: Init[WithinActorContext, String, String]) extends Actor with ActorLogging {
  def receive = {
    case init.Event(data) ⇒
      log.info("akka-io Server received {} from {}", data, sender)
    /*val response = serverResponse(input)
      sender ! init.Command(response)
      log.debug("akka-io Server sent: {}", response.dropRight(1))*/
    case _: Tcp.ConnectionClosed ⇒ context.stop(self)
  }
}