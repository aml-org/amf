package amf.apicontract.internal.spec.oas.parser.document

import amf.aml.client.scala.model.domain.DialectDomainElement
import amf.apicontract.internal.spec.oas.parser.context.AwsOas3WebApiContext
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{AmfArray, AmfObject, AmfScalar}
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.Value
import amf.core.internal.remote.{AwsOas30, Spec}
import org.yaml.model.YMap

class AwsOas3DocumentParser(override val root: Root)(implicit override val ctx: AwsOas3WebApiContext)
    extends Oas3DocumentParser(root) {

  override val spec: Spec        = AwsOas30
  private val integrationsIri    = "http://a.ml/vocabularies/aws#integrations"
  private val integrationNameIri = "http://a.ml/vocabularies/aws#name"

  override def parseDeclarations(root: Root, map: YMap, parentObj: AmfObject): Unit = {
    super.parseDeclarations(root, map, parentObj)

    for {
      customDomainProperties <- parentObj.fields.getValueAsOption(DomainElementModel.CustomDomainProperties)
      extension              <- findIntegrationsExtension(customDomainProperties)
      integrations <- extension.fields
        .getValueAsOption(integrationsIri)
        .map(_.value.asInstanceOf[AmfArray])
    } yield {
      integrations.values.foreach {
        case integration: DialectDomainElement => addIntegrationToDeclarations(integration)
        case _                                 => // ignore
      }
    }
  }

  private def findIntegrationsExtension(customDomainProperties: Value): Option[DomainExtension] = {
    val extensions = customDomainProperties.value.asInstanceOf[AmfArray].values
    extensions
      .find {
        case extension: DomainExtension =>
          extension.fields.getValueAsOption(DomainExtensionModel.Name) match {
            case Some(name) => name.value.asInstanceOf[AmfScalar].toString == "amazon-apigateway-integrations"
            case None       => false
          }
        case _ => false
      }
      .map(_.asInstanceOf[DomainExtension])
  }

  private def addIntegrationToDeclarations(integration: DialectDomainElement) = {
    integration.fields.getValueAsOption(integrationNameIri) match {
      case Some(nameField) =>
        val name = nameField.value.asInstanceOf[AmfScalar].toString
        ctx.declarations.addIntegration(name, integration)
      case _ => // ignore, should throw error
    }
  }

}
