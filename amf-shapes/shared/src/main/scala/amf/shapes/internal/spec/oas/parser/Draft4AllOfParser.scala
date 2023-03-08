package amf.shapes.internal.spec.oas.parser

import amf.core.client.scala.model.domain.{AmfArray, Shape}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.spec.common.parser.{ShapeParserContext, YMapEntryLike}
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidAndType
import org.yaml.model.{YMap, YMapEntry, YNode}

case class AndConstraintParser(map: YMap, shape: Shape, adopt: Shape => Unit, version: SchemaVersion)(implicit
    ctx: ShapeParserContext
) {

  def parse(): Unit = {
    map.key(
      "allOf",
      entry => {
        adopt(shape)
        entry.value.to[Seq[YNode]] match {
          case Right(seq) =>
            val andNodes = seq.zipWithIndex
              .flatMap { case (node, index) =>
                val entry = YMapEntryLike(node)
                OasTypeParser(entry, s"item$index", _ => Unit, version).parse()
              }
            shape.fields.setWithoutId(ShapeModel.And, AmfArray(andNodes, Annotations(entry.value)), Annotations(entry))
          case _ =>
            ctx.eh.violation(
              InvalidAndType,
              shape,
              "And constraints are built from multiple shape nodes",
              entry.value.location
            )

        }
      }
    )
  }
}
