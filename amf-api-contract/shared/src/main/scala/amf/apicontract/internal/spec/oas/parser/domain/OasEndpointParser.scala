package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.security.SecurityRequirement
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Parameter}
import amf.apicontract.internal.metamodel.domain.{EndPointModel, OperationModel}
import amf.apicontract.internal.spec.common.Parameters
import amf.apicontract.internal.spec.common.parser.{
  OasLikeSecurityRequirementParser,
  OasParametersParser,
  RamlParametersParser
}
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.spec.raml.parser.domain.ParametrizedDeclarationParser
import amf.apicontract.internal.spec.spec.toRaml
import amf.core.client.scala.model.domain.AmfArray
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.utils.{AmfStrings, IdCounter}
import org.yaml.model.{YMap, YMapEntry, YNode}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

abstract class OasEndpointParser(entry: YMapEntry, parentId: String, collector: List[EndPoint])(
    override implicit val ctx: OasWebApiContext
) extends OasLikeEndpointParser(entry, parentId, collector) {
  protected val operationsRegex = "get|patch|put|post|delete|options|head|connect|trace"
  override protected def parseEndpointMap(endpoint: EndPoint, map: YMap): EndPoint = {
    super.parseEndpointMap(endpoint, map)

    // PARAM PARSER

    var parameters = Parameters()
    val entries    = ListBuffer[YMapEntry]()
    // This are the rest of the parameters, this must be simple to be supported by OAS.
    map
      .key("parameters")
      .foreach { entry =>
        entries += entry
        parameters = parameters.add(OasParametersParser(entry.value.as[Seq[YNode]], endpoint.id).parse(true))
      }
    // This is because there may be complex path parameters coming from RAML1
    map.key("uriParameters".asOasExtension).foreach { entry =>
      entries += entry
      val uriParameters =
        RamlParametersParser(entry.value.as[YMap], (p: Parameter) => Unit, binding = "path")(toRaml(ctx))
          .parse()
      parameters = parameters.add(Parameters(path = uriParameters))
    }
    parameters match {
      case Parameters(query, path, header, cookie, _, _)
          if query.nonEmpty || path.nonEmpty || header.nonEmpty || cookie.nonEmpty =>
        endpoint.setWithoutId(
          EndPointModel.Parameters,
          AmfArray(query ++ path ++ header ++ cookie, Annotations(entries.head.value)),
          Annotations(entries.head)
        )
      case _ =>
    }
    if (parameters.body.nonEmpty)
      endpoint.setWithoutId(EndPointModel.Payloads, AmfArray(parameters.body), Annotations(entries.head))

    // PARAM PARSER

    map.key("displayName".asOasExtension, EndPointModel.Name in endpoint)

    map.key(
      "is".asOasExtension,
      (EndPointModel.Extends in endpoint using ParametrizedDeclarationParser
        .parse(endpoint.withTrait)).allowingSingleValue
    )

    map.key(
      "type".asOasExtension,
      entry =>
        ParametrizedDeclarationParser(
          entry.value,
          endpoint.withResourceType,
          ctx.declarations.findResourceTypeOrError(entry.value)
        )
          .parse()
    )

    ctx.factory.serversParser(map, endpoint).parse()

    map.key(
      "security".asOasExtension,
      entry => {
        // TODO check for empty array for resolution ?
        val idCounter = new IdCounter()
        val securedBy = entry.value
          .as[Seq[YNode]]
          .flatMap(s =>
            OasLikeSecurityRequirementParser(s, (se: SecurityRequirement) => Unit, idCounter)
              .parse()
          )

        endpoint.setWithoutId(
          OperationModel.Security,
          AmfArray(securedBy, Annotations(entry.value)),
          Annotations(entry)
        )
      }
    )

    map.regex(
      operationsRegex,
      entries => {
        val operations = parseOperations(entries)
        endpoint.setWithoutId(
          EndPointModel.Operations,
          AmfArray(operations, Annotations.inferred()),
          Annotations.inferred()
        )
      }
    )

    endpoint
  }
}
