package amf.plugins.document.apicontract.parser.spec.oas.parser.types

import amf.core.model.domain.Shape
import amf.core.parser.{Annotations, ScalarNode, YMapOps, YNodeLikeOps}
import amf.plugins.document.apicontract.parser.ShapeParserContext
import amf.plugins.document.apicontract.parser.spec.declaration.common.YMapEntryLike
import amf.plugins.document.apicontract.parser.spec.declaration.{OasTypeParser, SchemaVersion}
import amf.plugins.domain.shapes.models.AnyShape
import org.yaml.model.{YMap, YNode}

case class AllOfParser(array: Seq[YNode], adopt: Shape => Unit, version: SchemaVersion)(
    implicit ctx: ShapeParserContext) {
  def parse(): Seq[Shape] =
    array
      .flatMap(n => {
        n.toOption[YMap]
          .flatMap(declarationsRef)
          .orElse(OasTypeParser(YMapEntryLike(n), "", adopt, version).parse())
      })

  private def declarationsRef(entries: YMap): Option[Shape] = {
    entries
      .key("$ref")
      .flatMap { entry =>
        ctx.shapes.get(entry.value.as[String].stripPrefix("#/definitions/")) map { declaration =>
          declaration
            .link(ScalarNode(entry.value), Annotations(entry))
            .asInstanceOf[AnyShape]
            .withName(declaration.name.option().getOrElse("schema"), Annotations())
        }
      }
  }
}
