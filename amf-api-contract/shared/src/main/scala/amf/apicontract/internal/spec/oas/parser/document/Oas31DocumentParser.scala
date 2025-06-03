package amf.apicontract.internal.spec.oas.parser.document

import amf.aml.internal.parse.common.DeclarationKey
import amf.aml.internal.parse.dialects.DialectAstOps.DialectYMapOps
import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.oas.parser.domain.Oas30EndpointParser
import amf.apicontract.internal.validation.definitions.ParserSideValidations.NonEmptyOasApi
import amf.core.client.scala.model.domain.{AmfArray, AmfObject}
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Spec
import amf.shapes.internal.spec.common.parser.AnnotationParser
import org.yaml.model.YMap

class Oas31DocumentParser(root: Root, spec: Spec = Spec.OAS31)(implicit override val ctx: OasWebApiContext)
    extends Oas3DocumentParser(root, spec) {

  private def checkRootNodes(map: YMap, api: WebApi): Unit = {
    val requiredRootKeys = Seq("paths", "components", "webhooks")
    if (requiredRootKeys.map(map.key).forall(_.isEmpty))
      ctx.violation(
        NonEmptyOasApi,
        api.id,
        "OAS API should have at least a 'components', 'paths', or 'webhooks' property",
        map.location
      )
  }

  override def parseDeclarations(root: Root, map: YMap, parentObj: AmfObject): Unit = {
    super.parseDeclarations(root, map, parentObj)
    map.key("components").foreach { components =>
      val parent = root.location + "#/declarations"
      val map    = components.value.as[YMap]

      parsePathItemsDeclarations(map, parent)
    }
    AnnotationParser(parentObj, map).parseOrphanNode("components")
  }

  private def parsePathItemsDeclarations(map: YMap, parent: String): Unit = {
    map.key(
      "pathItems",
      endpoints => {
        addDeclarationKey(DeclarationKey(endpoints))
        endpoints.value
          .as[YMap]
          .entries
          .foreach(endpointMapEntry => {
            val maybeEndPoint = Oas30EndpointParser(endpointMapEntry, parent, List()).parse()
            maybeEndPoint.foreach(endpoint => ctx.declarations += endpoint)
          })
      }
    )
  }

  override def parseWebApi(map: YMap): WebApi = {
    val api = super.parseWebApi(map)
    map
      .key("webhooks")
      .foreach(webhooksEntry => {
        val webhooksYMap = webhooksEntry.value.as[YMap]
        val endpoints = webhooksYMap.entries.foldLeft(List[EndPoint]())((acc, entry) => {
          acc ++ ctx.factory.endPointParser(entry, api.id, acc).parse()
        })
        api.setWithoutId(
          WebApiModel.Webhooks,
          AmfArray(endpoints, Annotations(webhooksEntry.value)),
          Annotations(webhooksEntry)
        )
      })
    map.key("jsonSchemaDialect", WebApiModel.DefaultSchema in api)
    checkRootNodes(map, api)
    api
  }
}

object Oas31DocumentParser {
  def apply(root: Root, spec: Spec = Spec.OAS31)(implicit ctx: OasWebApiContext): Oas31DocumentParser =
    new Oas31DocumentParser(root, spec)
}
