package amf.apicontract.internal.spec.async.parser.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.oas.parser.OasLikeServerVariableParser
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.metamodel.domain.ExternalSourceElementModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.IdCounter
import amf.shapes.client.scala.domain.models.Example
import amf.shapes.internal.domain.metamodel.common.ExamplesField
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.ExamplesMustBeASeq
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
