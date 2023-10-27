package amf.shapes.internal.spec.oas.parser.field

import amf.aml.internal.parse.dialects.DialectAstOps.DialectYMapOps
import amf.core.client.scala.model.domain.{AmfArray, Shape}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.{ShapeParserContext, YMapEntryLike}
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidXoneType
import org.yaml.model.{YMap, YNode}

case class XoneConstraintParser(map: YMap, shape: Shape, adopt: Shape => Unit, version: SchemaVersion)(implicit
    ctx: ShapeParserContext
) {

  def parse(): Unit = {
    map.key(
      "oneOf",
      { entry =>
        adopt(shape)
        entry.value.to[Seq[YNode]] match {
          case Right(seq) =>
            val nodes = seq.zipWithIndex
              .flatMap { case (node, index) =>
                val entry = YMapEntryLike(node)
                OasTypeParser(entry, s"item$index", _ => Unit, version).parse()
              }
            shape.fields.setWithoutId(ShapeModel.Xone, AmfArray(nodes, Annotations(entry.value)), Annotations(entry))
          case _ =>
            ctx.eh.violation(
              InvalidXoneType,
              shape,
              "Xone constraints are built from multiple shape nodes",
              entry.value.location
            )

        }
      }
    )
  }
}
