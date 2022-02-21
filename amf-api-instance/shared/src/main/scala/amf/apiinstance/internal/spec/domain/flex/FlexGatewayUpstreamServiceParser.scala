package amf.apiinstance.internal.spec.domain.flex

import amf.apiinstance.client.scala.model.domain.UpstreamService
import amf.apiinstance.internal.spec.context.FlexGWConfigContext
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import org.yaml.model.YMap

case class FlexGatewayUpstreamServiceParser(serviceMap: YMap)(implicit ctx: FlexGWConfigContext) extends NodeTraverser {
  override def error_handler: AMFErrorHandler = ctx.eh
  val upstream = UpstreamService(serviceMap)

  def parse(adopt: UpstreamService => Unit): Unit = {
    parseHosts()
    adopt(upstream)
    parseRoutes()
  }

  private def parseHosts() = {
    traverse(serviceMap).errorFor(upstream).fetch("address").string() match {
      case Some(address) => upstream.withHosts(List(address))
      case _             => // ignore
    }
  }

  private def parseRoutes() {
    traverse(serviceMap).fetch("routes").arrayOr(()) { case routesNode =>
      routesNode.nodes.foreach { routeNode =>
        traverse(routeNode).map {
          case Some(routeNodeMap) =>
            FlexGatewayRouteParser(routeNodeMap).parse { parsedRoute =>
              upstream.withRoute(parsedRoute)
            }
          case _                  => // record violation
        }
      }
    }
  }
}
