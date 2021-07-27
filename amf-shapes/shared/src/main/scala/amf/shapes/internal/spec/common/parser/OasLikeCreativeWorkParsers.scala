package amf.shapes.internal.spec.common.parser

import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.{Oas, Raml}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.CreativeWork
import amf.shapes.internal.domain.metamodel.CreativeWorkModel
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.UnexpectedVendor
import amf.shapes.internal.vocabulary.VocabularyMappings
import org.yaml.model.{YMap, YNode}

object OasLikeCreativeWorkParser {
  def parse(node: YNode, parentId: String)(implicit ctx: ShapeParserContext): CreativeWork =
    OasLikeCreativeWorkParser(node, parentId).parse()
}

case class OasLikeCreativeWorkParser(node: YNode, parentId: String)(implicit val ctx: ShapeParserContext)
    extends QuickFieldParserOps {
  def parse(): CreativeWork = {

    val map          = node.as[YMap]
    val creativeWork = CreativeWork(node)

    map.key("url", CreativeWorkModel.Url in creativeWork)
    map.key("description", CreativeWorkModel.Description in creativeWork)
    map.key("title".asOasExtension, CreativeWorkModel.Title in creativeWork)

    creativeWork.adopted(parentId)
    AnnotationParser(creativeWork, map).parse()
    if (ctx.isOas2Syntax || ctx.isOas3Context)
      ctx.closedShape(creativeWork.id, map, "externalDoc")
    creativeWork
  }
}

case class RamlCreativeWorkParser(node: YNode)(implicit val ctx: ShapeParserContext) extends QuickFieldParserOps {
  def parse(): CreativeWork = {

    val map           = node.as[YMap]
    val documentation = CreativeWork(Annotations.valueNode(node))

    map.key("title", (CreativeWorkModel.Title in documentation).allowingAnnotations)
    map.key("content", (CreativeWorkModel.Description in documentation).allowingAnnotations)

    val url = ctx.spec match {
      case _: Oas  => "url"
      case _: Raml => "url".asRamlAnnotation
      case other =>
        ctx.eh.violation(UnexpectedVendor, documentation.id, s"Unexpected spec '$other'", node.location)
        "url"
    }

    map.key(url, CreativeWorkModel.Url in documentation)

    AnnotationParser(documentation, map, List(VocabularyMappings.documentationItem)).parse()

    documentation
  }
}
