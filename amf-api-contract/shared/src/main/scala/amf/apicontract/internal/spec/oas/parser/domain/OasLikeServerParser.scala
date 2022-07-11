package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.{Parameter, Server}
import amf.apicontract.internal.metamodel.domain.{ParameterModel, ServerModel}
import amf.apicontract.internal.spec.oas.parser.context.OasLikeWebApiContext
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.internal.datanode.DataNodeParser
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, ScalarNode}
import amf.core.internal.utils.IdCounter
import amf.shapes.internal.spec.common.parser.{AnnotationParser, QuickFieldParserOps, YMapEntryLike}
import org.yaml.model.{YMap, YMapEntry}

/** Single server OAS-like parser
  * @param parent
  *   parent node for server
  * @param entryLike
  *   map representing server | entry representing the server and its name
  * @param ctx
  *   parsing context
  */
class OasLikeServerParser(parent: String, entryLike: YMapEntryLike)(implicit val ctx: OasLikeWebApiContext)
    extends QuickFieldParserOps {

  protected val map            = entryLike.asMap
  protected val server: Server = build()

  def parse(): Server = {
    map.key("url", ServerModel.Url in server)

    map.key("description", ServerModel.Description in server)
    map.key("variables").foreach { entry =>
      val variables = entry.value.as[YMap].entries.map(ctx.factory.serverVariableParser(_, server.id).parse())
      server.setWithoutId(ServerModel.Variables, AmfArray(variables, Annotations(entry.value)), Annotations(entry))
    }
    AnnotationParser(server, map).parse()
    ctx.closedShape(server, map, "server")
    server
  }

  private def build(): Server = {
    val s = Server(entryLike.annotations)
    entryLike.key.foreach { k =>
      val name = ScalarNode(k)
      s.setWithoutId(ServerModel.Name, name.string())
    }
    s
  }
}

class OasLikeServerVariableParser(entry: YMapEntry, parent: String)(implicit val ctx: OasLikeWebApiContext)
    extends QuickFieldParserOps {

  def parse(): Parameter = {

    val node     = ScalarNode(entry.key)
    val variable = Parameter(entry).setWithoutId(ParameterModel.Name, node.string(), Annotations(entry.key))
    variable.setWithoutId(ParameterModel.Binding, AmfScalar("path"), Annotations.synthesized())
    variable.setWithoutId(ParameterModel.ParameterName, AmfScalar(node.string()), Annotations.synthesized())
    variable.setWithoutId(ParameterModel.Required, AmfScalar(true), Annotations.synthesized())

    val map = entry.value.as[YMap]
    parseMap(variable, map)

    variable
  }

  protected def parseMap(variable: Parameter, map: YMap): Unit = {
    ctx.closedShape(variable, map, "serverVariable")
    val schema = variable
      .withScalarSchema(entry.key)
      .add(Annotations(map))
      .withDataType(DataType.String, Annotations.synthesized())
    val counter: IdCounter = new IdCounter();
    map.key("enum", ShapeModel.Values in schema using DataNodeParser.parse(counter))
    map.key(
      "default",
      entry => {
        schema.withDefaultStr(entry.value)
        schema.withDefault(DataNodeParser(entry.value).parse(), Annotations(entry.value))
      }
    )
    map.key("description", ShapeModel.Description in schema)

    AnnotationParser(schema, map).parse()
  }
}
