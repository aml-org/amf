package amf.plugins.domain.webapi.contexts

import amf.parser._
import org.yaml.model.{YNode, YScalar, YType}

object RamlSpecAwareContext extends SpecAwareContext {

  override def link(node: YNode): Either[String, YNode] = {
    node match {
      case _ if isInclude(node) => Left(node.as[YScalar].text)
      case _                    => Right(node)
    }
  }

  override def ignore(shape: String, property: String): Boolean =
    (property.startsWith("(") && property.endsWith(")")) || (property.startsWith("/") && (shape == "webApi" || shape == "endPoint"))

  private def isInclude(node: YNode) = {
    node.tagType == YType.Unknown && node.tag.text == "!include"
  }
}
