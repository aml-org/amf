package amf.plugins.document.apicontract.parser.spec.oas.parser.types

import amf.core.client.scala.model.domain.{AmfArray, Shape}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.plugins.document.apicontract.parser.ShapeParserContext
import amf.plugins.document.apicontract.parser.spec.declaration.{OasTypeParser, SchemaVersion}
import amf.validations.ShapeParserSideValidations.InvalidAndType
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
                  OasTypeParser(entry, item => item.adopted(shape.id + "/and/" + index), version).parse()
              }
              .filter(_.isDefined)
              .map(_.get)
            shape.fields.setWithoutId(ShapeModel.And, AmfArray(andNodes, Annotations(entry.value)), Annotations(entry))
          case _ =>
            ctx.eh.violation(InvalidAndType,
                             shape.id,
                             "And constraints are built from multiple shape nodes",
                             entry.value)

        }
      }
    )
  }
}
