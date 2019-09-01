package amf.plugins.document.webapi.parser.spec.domain

import amf.core.parser.{Annotations, ScalarNode, _}
import amf.plugins.document.webapi.contexts.OasWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.OasTypeParser
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax
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

    val map = entry.value.as[YMap]

    val name      = entry.key.as[YScalar].text
    val parameter = producer(name).add(Annotations(entry))
      .set(ParameterModel.Name, ScalarNode(entry.key).string())

    map.key("description", ParameterModel.Description in parameter)

    if (ctx.syntax == Oas3Syntax) {
      parseOas3Header(parameter, map)
    } else {
      parseOas2Header(parameter, name, map)
    }


    parameter.withBinding("header") // we need to add the binding in order to conform all parameters validations
    AnnotationParser(parameter, map).parse()

    parameter
  }

  protected def parseOas2Header(parameter: Parameter, name: String, map: YMap): Unit = {
    parameter.set(ParameterModel.Required, !name.endsWith("?"))

    map.key("x-amf-required", (ParameterModel.Required in parameter).explicit)

    map.key(
      "type",
      _ => {
        OasTypeParser(entry, (shape) => shape.withName("schema").adopted(parameter.id))
          .parse()
          .map(s => parameter.set(ParameterModel.Schema, tracking(s, parameter.id), Annotations(entry)))
      }
    )
  }

  protected def parseOas3Header(parameter: Parameter, map: YMap): Unit = {
    map.key("required", (ParameterModel.Required in parameter).explicit)
    map.key("deprecated", (ParameterModel.Deprecated in parameter).explicit)
    map.key("allowEmptyValue", (ParameterModel.AllowEmptyValue in parameter).explicit)

    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, (shape) => shape.withName("schema").adopted(parameter.id))
          .parse()
          .map(s => parameter.set(ParameterModel.Schema, tracking(s, parameter.id), Annotations(entry)))
      }
    )

    ctx.closedShape(parameter.id, map, "header")
  }
}
