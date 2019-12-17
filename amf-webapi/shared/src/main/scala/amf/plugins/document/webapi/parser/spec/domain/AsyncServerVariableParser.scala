package amf.plugins.document.webapi.parser.spec.domain
import amf.core.utils.IdCounter
import amf.plugins.domain.shapes.models.Example
import org.yaml.model.{YType, YMap, YSequence, YMapEntry}
import amf.plugins.domain.webapi.models.{Parameter, Server}
import amf.validations.ParserSideValidations
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.core.parser.YMapOps

case class AsyncServerVariableParser(entry: YMapEntry, server: Server)(implicit override val ctx: AsyncWebApiContext)
    extends OasLikeServerVariableParser(entry, server) {

  override protected def parseMap(variable: Parameter, map: YMap): Unit = {
    super.parseMap(variable, map)
    map.key(
      "examples",
      examplesEntry => {
        examplesEntry.value.tagType match {
          case YType.Seq =>
            val idCounter = new IdCounter()
            val examples = examplesEntry.value.as[YSequence].nodes.map { node =>
              Example()
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
