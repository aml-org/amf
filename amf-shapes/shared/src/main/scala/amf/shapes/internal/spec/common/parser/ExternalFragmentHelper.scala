package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.ExternalDomainElement
import amf.shapes.internal.spec.ShapeParserContext
import org.yaml.model.YNode.MutRef
import org.yaml.model.{YNode, YScalar}

object ExternalFragmentHelper {

  def searchForAlreadyParsedNodeInFragments(contentNode: YNode)(implicit ctx: ShapeParserContext): Option[YNode] = {
    val nodeLocation: String = locationOfNode(contentNode)
    ctx.fragments.values
      .find(_.location.contains(nodeLocation))
      .map(_.encoded)
      .flatMap {
        case e: ExternalDomainElement =>
          // TODO: leaves room for improvement, content nested within the parsed external domain element can be searched
          if (sameContent(e.raw, contentNode)) e.parsed else None
        case _ => None
      }
  }

  private def sameContent(str: StrField, contentNode: YNode): Boolean = {
    contentNode.asOption[YScalar].exists(_.text == str.value())
  }

  private def locationOfNode(contentNode: YNode): String = {
    val targetLocation = contentNode match {
      case r: MutRef => r.target.map(_.location)
      case _         => None
    }
    targetLocation.getOrElse(contentNode.location).sourceName
  }

}
