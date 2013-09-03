package org.maodian.tomao

import akka.actor.ActorSystem
import akka.actor.Props
import java.net.InetSocketAddress

object Main extends App {
  val system = ActorSystem("xmpp-server");
  system.actorOf(Props(new XmppServer(new InetSocketAddress(5227)))) ! Start
}