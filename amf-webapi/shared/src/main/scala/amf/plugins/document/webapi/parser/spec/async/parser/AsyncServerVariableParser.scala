package amf.plugins.document.webapi.parser.spec.async.parser

import amf.core.utils.IdCounter
import amf.core.parser._
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.domain.OasLikeServerVariableParser
import amf.plugins.domain.shapes.models.Example
import amf.plugins.domain.webapi.models.Parameter
import amf.validations.ParserSideValidations
import org.yaml.model.{YMap, YMapEntry, YSequence, YType}

case class AsyncServerVariableParser(entry: YMapEntry, parent: String)(implicit override val ctx: AsyncWebApiContext)
    extends OasLikeServerVariableParser(entry, parent) {

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
                .withName(idCounter.genId("example"))
                .withValue(node)
            }
            variable.withExamples(examples)
          case _ =>
            ctx.violation(ParserSideValidations.ExamplesMustBeASeq,
                          variable.id,
                          "Examples facet must be an array of strings")
        }
      }
    )
  }
}
