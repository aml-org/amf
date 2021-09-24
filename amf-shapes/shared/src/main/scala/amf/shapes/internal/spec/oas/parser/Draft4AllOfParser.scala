package amf.shapes.internal.spec.oas.parser

import amf.core.client.scala.model.domain.{AmfArray, Shape}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.common.SchemaVersion
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.InvalidAndType
import org.yaml.model.{YMap, YMapEntry, YNode}

case class AndConstraintParser(map: YMap, shape: Shape, adopt: Shape => Unit, version: SchemaVersion)(
    implicit ctx: ShapeParserContext) {

  def parse(): Unit = {
    map.key(
      "allOf",
      entry => {
        adopt(shape)
        entry.value.to[Seq[YNode]] match {
          case Right(seq) =>
            val andNodes = seq.zipWithIndex
              .map {
                case (node, index) =>
                  val entry = YMapEntry(YNode(s"item$index"), node)
                  OasTypeParser(entry, item => Unit, version).parse()
              }
              .filter(_.isDefined)
              .map(_.get)
            shape.fields.setWithoutId(ShapeModel.And, AmfArray(andNodes, Annotations(entry.value)), Annotations(entry))
          case _ =>
            ctx.eh.violation(InvalidAndType,
                             shape,
                             "And constraints are built from multiple shape nodes",
                             entry.value.location)

        }
      }
    )
  }
}
