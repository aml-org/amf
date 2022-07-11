package amf.shapes.internal.spec.oas.parser

import amf.core.internal.parser.YMapOps
import amf.core.internal.plugins.syntax.SyamlAMFErrorHandler
import org.yaml.model.{YMap, YNode, YScalar}

object OasLink {
  def getLinkValue(node: YNode)(implicit eh: SyamlAMFErrorHandler) = {
    node.to[YMap] match {
      case Right(map) =>
        val ref: Option[String] = map.key("$ref").flatMap(v => v.value.asOption[YScalar]).map(_.text)
        ref match {
          case Some(url) => Left(url)
          case None      => Right(node)
        }
      case _ => Right(node)
    }
  }
}
