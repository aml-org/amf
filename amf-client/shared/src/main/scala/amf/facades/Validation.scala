package amf.facades

import amf.client.execution.BaseExecutionEnvironment
import amf.core.remote.Platform
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.EffectiveValidations
import amf.core.validation.core.ValidationSpecification
import amf.plugins.document.webapi.validation.PayloadValidatorPlugin
import amf.plugins.domain.VocabulariesRegister
import amf.plugins.domain.shapes.DataShapesDomainPlugin
import amf.plugins.domain.webapi.APIDomainPlugin
import amf.plugins.features.validation.AMFValidatorPlugin
import amf.plugins.features.validation.emitters.ShaclJsonLdShapeGraphEmitter
import amf.plugins.syntax.SYamlSyntaxPlugin
import amf.{ProfileName, Raml10Profile}

import scala.concurrent.{ExecutionContext, Future}

class Validation(platform: Platform) {

  def init()(implicit executionContext: ExecutionContext): Future[Unit] = {
    amf.core.AMF.registerPlugin(PayloadValidatorPlugin)
    // Remod registering
    VocabulariesRegister.register(platform)
    amf.core.registries.AMFPluginsRegistry.registerSyntaxPlugin(SYamlSyntaxPlugin)
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
