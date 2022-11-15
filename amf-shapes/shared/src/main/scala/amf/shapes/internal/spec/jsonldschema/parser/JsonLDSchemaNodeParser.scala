package amf.shapes.internal.spec.jsonldschema.parser

import amf.core.internal.parser.domain.Annotations
import amf.core.client.scala.model.domain.Shape
import amf.shapes.internal.spec.jsonldschema.parser.builder.{JsonLDElementBuilder, JsonLDErrorBuilder}
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.{
  UnsupportedRootLevel,
  UnsupportedScalarRootLevel
}
import org.yaml.model._

object JsonPath {
  val empty: JsonPath = JsonPath()
}
case class JsonPath private (segments: List[String] = Nil) {

  private lazy val path       = segments.mkString("/")
  override def toString       = path
  def concat(segment: String) = copy(segments :+ segment)
  def last                    = JsonPath(segments.lastOption.toList)
  def lastSegment             = segments.lastOption
}
case class JsonLDSchemaNodeParser(shape: Shape, node: YNode, key: String, path: JsonPath, isRoot: Boolean = false)(
    implicit ctx: JsonLDParserContext
) {

  // TODO native-jsonld: key is only used to generate default class term for objects. Shall we analyse another way?
  def parse(): JsonLDElementBuilder = {
    node.tagType match {
      case YType.Map => JsonLDObjectElementParser(node.as[YMap], key, path)(ctx).parse(shape)
      case YType.Seq => JsonLDArrayElementParser(node.as[YSequence], path)(ctx).parse(shape)
      case _ if isScalarNode && !isRoot =>
        JsonLDScalarElementParser(node.as[YScalar], node.tagType, path).parse(shape)
      case _ if isScalarNode && isRoot =>
        ctx.eh.violation(UnsupportedScalarRootLevel, shape, UnsupportedScalarRootLevel.message, Annotations(node))
        JsonLDErrorBuilder(path)
      case _ =>
        ctx.eh.violation(UnsupportedRootLevel, shape, UnsupportedRootLevel.message, Annotations(node))
        JsonLDErrorBuilder(path)
    }
  }

  private def isScalarNode = {
    node.value.isInstanceOf[YScalar]
  }
}
