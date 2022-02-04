package amf.apiinstance.internal.spec.domain.envoy

import amf.apiinstance.client.scala.model.domain.ProtocolListener
import amf.apiinstance.internal.spec.context.AWSAPIGWConfigContext
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.parser.domain.Annotations
import org.yaml.model.YMap

case class EnvoyConfigProtocolListenerParser(ast: YMap)(implicit val ctx: AWSAPIGWConfigContext) extends NodeTraverser {

  def parse(adopt: ProtocolListener => Unit): Unit = {
    traverse(ast).fetch("listeners").arrayOr(()) { listeners =>
      listeners.nodes.zipWithIndex.foreach { case (listener,i) =>
        parseListener(listener.as[YMap], s"listener_$i", adopt)
      }
    }
  }

  def parseSocket(node: YMap, listener: ProtocolListener) = {
    traverse(node).errorFor(listener).fetch("address").string().foreach(listener.withAddress)
    traverse(node).fetch("port_value").string() match {
      case Some(port) =>
        listener.withPort(port)
      case _          =>
        traverse(node).errorFor(listener).fetch("named_port").string().foreach(listener.withNamedPort)
    }
  }

  def parsePipe(node: YMap, listener: ProtocolListener) = {
    traverse(node).errorFor(listener).fetch("path").string().foreach(listener.withPipe)
    traverse(node).fetch("mode").string().foreach(listener.withPipeMode)
  }

  private def parseAddress(node: YMap, listener: ProtocolListener, adopt: ProtocolListener => Unit): ProtocolListener = {
    traverse(node).fetch("socket_address").map {
      case Some(socket) =>
        parseSocket(socket, listener)
      case None         =>
        traverse(node).errorFor(listener).fetch("pipe").mapOr(()) { pipe =>
          parsePipe(pipe, listener)
        }
    }
    adopt(listener)
    listener
  }

  private def parseListener(node: YMap, defaultName: String, adopt: ProtocolListener => Unit): ProtocolListener = {
    val listener = ProtocolListener(Annotations(node))
    val t = traverse(node)
    listener.withName(t.fetch("name").stringOr(defaultName))
    t.errorFor(listener).fetch("address").mapOr(listener)(parseAddress(_, listener, adopt))
  }

  override def error_handler: AMFErrorHandler = ctx.eh
}
