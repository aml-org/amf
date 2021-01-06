package amf.plugins.document.webapi.parser.spec.common

import amf.core.model.domain.ExternalDomainElement
import amf.plugins.document.webapi.contexts.WebApiContext
import org.yaml.model.YNode
import org.yaml.model.YNode.MutRef
import org.yaml.parser.JsonParser

object ExternalFragmentHelper {

  def searchNodeInFragments(contentNode: YNode)(implicit ctx: WebApiContext): Option[YNode] = {
    val nodeLocation: String = locationOfNode(contentNode)
    ctx.declarations.fragments.values
      .find(_.location.contains(nodeLocation))
      .map(_.encoded)
      .flatMap {
        case e: ExternalDomainElement =>
          e.parsed
        case _ => None
      }
  }

  private def locationOfNode(contentNode: YNode): String = {
    val targetLocation = contentNode match {
      case r: MutRef => r.target.map(_.location)
      case _         => None
    }
    targetLocation.getOrElse(contentNode.location).sourceName
  }

}
