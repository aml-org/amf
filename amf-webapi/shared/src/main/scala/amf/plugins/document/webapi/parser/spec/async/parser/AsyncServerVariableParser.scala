package amf.plugins.document.webapi.parser.spec.async.parser

import amf.core.metamodel.domain.ExternalSourceElementModel
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.utils.IdCounter
import amf.core.parser._
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.domain.OasLikeServerVariableParser
import amf.plugins.domain.shapes.metamodel.common.ExamplesField
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.models.Parameter
import amf.validations.ParserSideValidations
import amf.validations.ShapeParserSideValidations.ExamplesMustBeASeq
import org.yaml.model.{YMap, YMapEntry, YSequence, YType}

case class AsyncServerVariableParser(entry: YMapEntry, parent: String)(implicit override val ctx: AsyncWebApiContext)
    extends OasLikeServerVariableParser(entry, parent)(ctx) {

  override protected def parseMap(variable: Parameter, map: YMap): Unit = {
    super.parseMap(variable, map)
    map.key(
      "examples",
      examplesEntry => {
        examplesEntry.value.tagType match {
          case YType.Seq =>
            val idCounter = new IdCounter()
            val examples = examplesEntry.value.as[YSequence].nodes.map { node =>
              Example(node)
                .withName(idCounter.genId("example"), Annotations.synthesized())
                .set(ExternalSourceElementModel.Raw,
                     AmfScalar(node.asScalar.map(_.text).getOrElse(node.toString), Annotations(node)),
                     Annotations.inferred())
            }
            variable.fields.set(variable.id,
                                ExamplesField.Examples,
                                AmfArray(examples, Annotations(examplesEntry.value)),
                                Annotations(examplesEntry))
          case _ =>
            ctx.violation(ExamplesMustBeASeq, variable.id, "Examples facet must be an array of strings")
        }
      }
    )
  }
}
