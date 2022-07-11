package amf.shapes.internal.spec.raml.parser

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.internal.plugins.syntax.SYamlAMFParserErrorHandler
import org.yaml.model.{IllegalTypeHandler, YNode, YScalar, YType}

object RamlLink {
  def link(node: YNode)(implicit eh: AMFErrorHandler): Either[String, YNode] = {
    implicit val errorHandler: IllegalTypeHandler = new SYamlAMFParserErrorHandler(eh)

    node match {
      case _ if isInclude(node) => Left(node.as[YScalar].text)
      case _                    => Right(node)
    }
  }

  private def isInclude(node: YNode) = node.tagType == YType.Include
}
