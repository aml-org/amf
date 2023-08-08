package amf.apicontract.internal.spec.oas.parser.domain

import amf.aml.client.scala.model.domain.DialectDomainElement
import amf.aml.internal.registries.AMLRegistry
import amf.aml.internal.semantic.CachedExtensionDialectFinder
import amf.aml.internal.semantic.SemanticExtensionOps.findExtensionMapping
import amf.aml.internal.semantic.SemanticExtensionParser.{
  createCustomDomainProperty,
  createDomainExtension,
  mergeAnnotationIntoExtension
}
import amf.apicontract.client.scala.model.domain.Operation
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.apicontract.internal.spec.oas.parser.context.AwsOas3WebApiContext
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.ScalarNode
import amf.shapes.internal.spec.common.parser.AnnotationParser.setExtensions
import org.yaml.model.{YMap, YMapEntry, YScalar}

class AwsOas30OperationParser(entry: YMapEntry, adopt: Operation => Operation)(
    override implicit val ctx: AwsOas3WebApiContext
) extends Oas30OperationParser(entry, adopt) {

  private val integrationExtensionName          = "amazon-apigateway-integration"
  private def extensionIdFor(operation: Operation) = s"${operation.id}/integration"
  private val operationIri                      = OperationModel.`type`.head.iri()
  private val integrationAnnotationMapping = {
    val finder = CachedExtensionDialectFinder(ctx.config.registryContext.getRegistry.asInstanceOf[AMLRegistry])
    findExtensionMapping(integrationExtensionName, Seq(operationIri), finder).map(_._1).get
  }

  override def entryKey: AmfScalar = {
    entry.key.toString match {
      case "x-amazon-apigateway-any-method" => ScalarNode("any").string()
      case _                                => ScalarNode(entry.key).string()
    }
  }

  override def parse(): Operation = {
    val operation = super.parse()
    val map       = entry.value.as[YMap]

    map.key(s"x-$integrationExtensionName", parseIntegration(_, operation))

    operation
  }

  private def parseIntegration(entry: YMapEntry, operation: Operation): Unit = {
    val extensionId = extensionIdFor(operation)
    val integrationOption = entry.value.value match {
      case map: YMap if isRef(map) => resolveRef(entry, extensionId)
      case _                       => parseInlineIntegration(entry, extensionId)
    }

    integrationOption match {
      case Some(integration) => setExtensions(operation, Seq(integration))
      case None              => // ignore, should throw error
    }
  }

  private def isRef(map: YMap): Boolean = map.key("$ref").isDefined
  private def resolveRef(extensionEntry: YMapEntry, extensionId: String): Option[DomainExtension] = {
    val entryValue = extensionEntry.value.value.asInstanceOf[YMap]

    entryValue
      .key("$ref")
      .flatMap(entry => {
        val plainRef        = entry.value.value.asInstanceOf[YScalar].text
        val integrationName = plainRef.stripPrefix("#/components/x-amazon-apigateway-integrations/")
        ctx.declarations.findIntegration(integrationName)
      })
      .map { integrationValue =>
        val integrationExtension = DialectDomainElement()
          .withObjectField(integrationAnnotationMapping, integrationValue, Right(entry))

        val property  = createCustomDomainProperty(integrationExtension, integrationExtensionName, extensionEntry)
        val extension = createDomainExtension(integrationExtensionName, extensionEntry, extensionId, property)
        mergeAnnotationIntoExtension(integrationExtension, extension)
      }
  }

  private def parseInlineIntegration(extensionEntry: YMapEntry, extensionId: String): Option[DomainExtension] = {
    ctx.extensionsFacadeBuilder
      .extensionName(integrationExtensionName)
      .parse(Seq(operationIri), extensionEntry, ctx, extensionId)
  }

}
