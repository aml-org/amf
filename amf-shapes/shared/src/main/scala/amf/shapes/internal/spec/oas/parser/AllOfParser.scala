package amf.shapes.internal.spec.oas.parser

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.parser.{YMapOps, YNodeLikeOps}
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.{ShapeParserContext, YMapEntryLike}
import org.yaml.model.{YMap, YNode}

case class AllOfParser(array: Seq[YNode], adopt: Shape => Unit, version: SchemaVersion)(implicit
    ctx: ShapeParserContext
) {
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
