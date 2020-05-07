package amf.plugins.document.webapi.parser.spec.domain
import amf.core.annotations.SynthesizedField
import org.yaml.model.{YMap, YMapEntry}
import amf.core.metamodel.domain.ShapeModel
import amf.core.model.DataType
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, DataNodeParser, SpecParserOps}
import amf.core.parser.{Annotations, ScalarNode, YMapOps}
import amf.core.model.domain.{AmfArray, AmfScalar}
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.domain.webapi.metamodel.{ParameterModel, ServerModel}
import amf.plugins.domain.webapi.models.{Parameter, Server}

/**
  * Single server OAS-like parser
  * @param parent parent node for server
  * @param map map representing server
  * @param ctx parsing context
  */
class OasLikeServerParser(parent: String, map: YMap)(implicit val ctx: OasLikeWebApiContext) extends SpecParserOps {

  def parse(): Server = {
    val server = Server(map)
    map.key("url", ServerModel.Url in server)
    server.adopted(parent)

    map.key("description", ServerModel.Description in server)
    map.key("variables").foreach { entry =>
      val variables = entry.value.as[YMap].entries.map(ctx.factory.serverVariableParser(_, server.id).parse())
      server.set(ServerModel.Variables, AmfArray(variables, Annotations(entry.value)), Annotations(entry))
    }
    AnnotationParser(server, map).parse()
    ctx.closedShape(server.id, map, "server")
    server
  }
}

class OasLikeServerVariableParser(entry: YMapEntry, parent: String)(implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {
  def parse(): Parameter = {

    val node     = ScalarNode(entry.key)
    val variable = Parameter(entry).set(ParameterModel.Name, node.string(), Annotations(entry.key))
    variable.adopted(parent)
    variable.set(ParameterModel.Binding, AmfScalar("path"), Annotations() += SynthesizedField())
    variable
      .withParameterName(entry.key)
      .withRequired(true) // default value of path parameter to avoid raw validation

    val map = entry.value.as[YMap]
    parseMap(variable, map)

    variable
  }

  protected def parseMap(variable: Parameter, map: YMap): Unit = {
    ctx.closedShape(variable.id, map, "serverVariable")

    val schema  = variable.withScalarSchema(entry.key).add(Annotations(map)).withDataType(DataType.String)
    val counter = new IdCounter()
    map.key("enum", ShapeModel.Values in schema using DataNodeParser.parse(Some(schema.id), counter))
    map.key("default", entry => {
      schema.withDefaultStr(entry.value)
      schema.withDefault(DataNodeParser(entry.value).parse())
    })
    map.key("description", ShapeModel.Description in schema)
  }
}
