/**
 *
 */
package org.maodian.tomao

import akka.kernel.Bootable
import akka.actor.ActorSystem
import akka.actor.Props
import java.net.InetSocketAddress

/**
 * @author Cole Wen
 *
 */
class AppKernel extends Bootable {
  val system = ActorSystem("hellokernel")

  def startup = {
    system.actorOf(Props(new XmppServer(new InetSocketAddress(5227)))) ! Start
  }

  def shutdown = {
    system.shutdown()
  }
}