package amf.shapes.internal.spec.oas.parser.field

import amf.core.client.scala.model.domain.{AmfArray, Shape}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.{ShapeParserContext, YMapEntryLike}
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidOrType
import org.yaml.model.{YMap, YNode}

case class OrConstraintParser(map: YMap, shape: Shape, version: SchemaVersion)(implicit ctx: ShapeParserContext) {

  def parse(): Unit = {
    map.key(
      "anyOf",
      { entry =>
        entry.value.to[Seq[YNode]] match {
          case Right(seq) =>
            val unionNodes = seq.zipWithIndex
              .flatMap { case (node, index) =>
                val entry = YMapEntryLike(node)
                OasTypeParser(entry, s"item$index", _ => Unit, version).parse()
              }
            shape.fields
              .setWithoutId(ShapeModel.Or, AmfArray(unionNodes, Annotations(entry.value)), Annotations(entry))
          case _ =>
            ctx.eh.violation(
              InvalidOrType,
              shape,
              "Or constraints are built from multiple shape nodes",
              entry.value.location
            )

        }
      }
    )
  }
}
