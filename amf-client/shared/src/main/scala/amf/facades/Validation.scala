package amf.facades

import amf.client.execution.BaseExecutionEnvironment
import amf.core.remote.Platform
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.EffectiveValidations
import amf.core.validation.core.ValidationSpecification
import amf.plugins.document.graph.AMFGraphPlugin
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.document.webapi.{Oas20Plugin, PayloadPlugin, Raml08Plugin, Raml10Plugin, _}
import amf.plugins.domain.VocabulariesRegister
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.APIDomainPlugin
import amf.plugins.features.validation.{AMFValidatorPlugin, CoreValidations}
import amf.plugins.features.validation.emitters.ShaclJsonLdShapeGraphEmitter
import amf.plugins.syntax.SYamlSyntaxPlugin
import amf.validation.DialectValidations
import amf.validations._
import amf.{ProfileName, Raml10Profile}

import scala.concurrent.{ExecutionContext, Future}

class Validation(platform: Platform) {

  def init()(implicit executionContext: ExecutionContext): Future[Unit] = {
    amf.core.AMF.registerPlugin(PayloadValidatorPlugin)
    // Remod registering
    VocabulariesRegister.register(platform)
    amf.core.registries.AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml10Plugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Raml08Plugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas20Plugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Oas30Plugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(Async20Plugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(PayloadPlugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(AMFGraphPlugin)
    amf.core.registries.AMFPluginsRegistry.registerDocumentPlugin(JsonSchemaPlugin)
    amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(APIDomainPlugin)
    amf.core.registries.AMFPluginsRegistry.registerDomainPlugin(DataShapesDomainPlugin)
    amf.core.registries.AMFPluginsRegistry.registerFeaturePlugin(AMFValidatorPlugin)
    amf.core.AMF.init()
  }

  def shapesGraph(validations: EffectiveValidations, profileName: ProfileName = Raml10Profile): String =
    new ShaclJsonLdShapeGraphEmitter(profileName).emit(customValidations(validations.effective.values.toSeq))

  def customValidations(validations: Seq[ValidationSpecification]): Seq[ValidationSpecification] =
    validations.filter(s => !s.isParserSide)
}

object Validation extends PlatformSecrets {
  def apply(platform: Platform,
            exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): Future[Validation] = {
    implicit val executionContext: ExecutionContext = exec.executionContext
    val validation                                  = new Validation(platform)
    validation.init().map(_ => validation)
  }
}
