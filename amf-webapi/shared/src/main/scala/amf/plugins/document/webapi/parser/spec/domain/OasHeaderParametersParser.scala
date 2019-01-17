package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, ScalarNode, _}
import amf.plugins.document.webapi.contexts.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.domain.shapes.models.ExampleTracking.tracking
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter
import org.yaml.model.{YMap, YMapEntry, YScalar}

case class OasHeaderParametersParser(map: YMap, producer: String => Parameter)(implicit ctx: OasWebApiContext) {
  def parse(): Seq[Parameter] = {
    map.entries
      .map(entry => OasHeaderParameterParser(entry, producer).parse())
  }
}

case class OasHeaderParameterParser(entry: YMapEntry, producer: String => Parameter)(implicit ctx: OasWebApiContext)
    extends SpecParserOps {
  def parse(): Parameter = {

    val name      = entry.key.as[YScalar].text
    val parameter = producer(name).add(Annotations(entry))

    parameter
      .set(ParameterModel.Required, !name.endsWith("?"))
      .set(ParameterModel.Name, ScalarNode(entry.key).string())

    val map = entry.value.as[YMap]

    map.key("description", ParameterModel.Description in parameter)
    map.key("x-amf-required", (ParameterModel.Required in parameter).explicit)

    map.key(
      "type",
      _ => {
        OasTypeParser(entry, (shape) => shape.withName("schema").adopted(parameter.id))
          .parse()
          .map(s => parameter.set(ParameterModel.Schema, tracking(s, parameter.id), Annotations(entry)))
      }
    )

    parameter.withBinding("header") // we need to add the binding in order to conform all parameters validations
    AnnotationParser(parameter, map).parse()

    parameter
  }
}
