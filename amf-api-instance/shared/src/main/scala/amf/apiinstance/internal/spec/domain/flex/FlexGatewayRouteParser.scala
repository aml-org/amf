package amf.apiinstance.internal.spec.domain.flex

import amf.apiinstance.client.scala.model.domain.Route
import amf.apiinstance.internal.spec.context.FlexGWConfigContext
import amf.apiinstance.internal.utils.NodeTraverser
import amf.core.client.scala.errorhandling.AMFErrorHandler
import org.yaml.model.YMap


case class FlexGatewayRouteParser(routeNode: YMap)(implicit ctx: FlexGWConfigContext) extends NodeTraverser  {
  override def error_handler: AMFErrorHandler = ctx.eh
  val route = Route(routeNode)

  def parse(adopt: Route => Unit): Unit = {
    parseDestinationPath()
    adopt(route)
    parseRules()
  }

  private def parseRules() = {
    FlexRuleParser(routeNode).parse(route.withRule)
  }

  private def parseDestinationPath(): Unit = {
    traverse(routeNode).fetch("config").fetch("destinationPath").string() match {
      case Some(destinationPath) => route.withPath(destinationPath)
      case _                     => // ignore
    }
  }
}
