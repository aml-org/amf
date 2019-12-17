package amf.plugins.document.webapi.parser.spec.domain
import org.yaml.model.{YType, YMap, YMapEntry}
import amf.core.metamodel.Field
import amf.core.metamodel.domain.ShapeModel
import amf.plugins.document.webapi.parser.spec.common.{SpecParserOps, AnnotationParser, DataNodeParser}
import amf.core.parser.{Annotations, YMapOps}
import amf.core.model.domain.{DomainElement, AmfArray}
import amf.core.utils.IdCounter
import amf.plugins.document.webapi.contexts.parser.OasLikeWebApiContext
import amf.plugins.document.webapi.parser.spec.oas.Oas3Syntax
import amf.plugins.domain.webapi.metamodel.ServerModel
import amf.plugins.domain.webapi.models.{Parameter, Server}
import amf.validations.ParserSideValidations.ServerVariableMissingDefault

/**
  * Single server OAS-like parser
  * @param parent parent node for server
  * @param map map representing server
  * @param ctx parsing context
  */
class OasLikeServerParser(parent: String, map: YMap)(implicit val ctx: OasLikeWebApiContext) extends SpecParserOps {

  def parse(): Server = {
    val server = Server(map)

    server.adopted(parent)

    ctx.closedShape(server.id, map, "server")

    map.key("url", ServerModel.Url in server)
    map.key("description", ServerModel.Description in server)
    map.key("variables").foreach { entry =>
      val variables = entry.value.as[YMap].entries.map(ctx.factory.serverVariableParser(_, server).parse())
      server.set(ServerModel.Variables, AmfArray(variables, Annotations(entry.value)), Annotations(entry))
    }
    AnnotationParser(server, map).parse()
    server
  }
}

class OasLikeServerVariableParser(entry: YMapEntry, server: Server)(implicit val ctx: OasLikeWebApiContext)
    extends SpecParserOps {
  def parse(): Parameter = {

    val variable = server
      .withVariable(entry.key)
      .withParameterName(entry.key)
      .withBinding("path")

    // Parse map
    entry.value.tagType match {
      case YType.Map =>
        parseMap(variable, entry.value.as[YMap])
    }

    variable
  }

  protected def parseMap(variable: Parameter, map: YMap): Unit = {
    ctx.closedShape(variable.id, map, "serverVariable")

    val schema  = variable.withScalarSchema(entry.key)
    val counter = new IdCounter()
    map.key("enum", ShapeModel.Values in schema using DataNodeParser.parse(Some(schema.id), counter))
    map.key("default", entry => {
      schema.withDefaultStr(entry.value)
      schema.withDefault(DataNodeParser(entry.value).parse())
    })
    map.key("description", ShapeModel.Description in schema)
  }
}
