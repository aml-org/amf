package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.AmfScalar
import amf.core.parser.{Annotations, ScalarNode, YMapOps}
import amf.plugins.document.webapi.contexts.parser.async.AsyncWebApiContext
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.{JSONSchemaDraft7SchemaVersion, OasTypeParser}
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter
import org.yaml.model.{YMap, YMapEntry}

case class AsyncParametersParser(parentId: String, map: YMap)(implicit val ctx: AsyncWebApiContext) {

  def parse(): Seq[Parameter] = {
    map.entries.map(AsyncParameterParser(parentId, _).parse())
  }
}

case class AsyncParameterParser(parentId: String, definitionEntry: YMapEntry)(implicit val ctx: AsyncWebApiContext)
    extends SpecParserOps {

  private def nameAndAdopt(param: Parameter): Parameter = {
    param
      .set(ParameterModel.Name, ScalarNode(definitionEntry.key).string())
      .adopted(parentId)
      .add(Annotations(definitionEntry))
  }

  def parse(): Parameter = {
    val map = definitionEntry.value.as[YMap]

    val param = nameAndAdopt(Parameter())
    parseSchema(map, param)
    map.key("description", ParameterModel.Description in param)
    map.key("location", ParameterModel.Binding in param)

    if (param.binding.isNullOrEmpty) {
      param.set(ParameterModel.Binding, AmfScalar("path"), Annotations() += SynthesizedField())
    }

    AnnotationParser(param, map).parse()
    ctx.closedShape(param.id, map, "parameter")
    param
  }

  def parseSchema(map: YMap, param: Parameter): Unit = {
    map.key(
      "schema",
      entry => {
        OasTypeParser(entry, shape => shape.withName("schema").adopted(param.id), JSONSchemaDraft7SchemaVersion)
          .parse()
          .foreach { schema =>
            param.set(ParameterModel.Schema, schema, Annotations(entry))
          }
      }
    )
  }
}
