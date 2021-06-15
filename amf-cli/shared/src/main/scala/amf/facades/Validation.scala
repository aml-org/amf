package amf.facades

import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.platform.execution.BaseExecutionEnvironment
import amf.core.internal.remote.Platform
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.validation.EffectiveValidations
import amf.core.internal.validation.core.ValidationSpecification
import amf.plugins.domain.VocabulariesRegister
import amf.plugins.features.validation.emitters.ShaclJsonLdShapeGraphEmitter

import scala.concurrent.{ExecutionContext, Future}

class Validation(platform: Platform) {

  def init()(implicit executionContext: ExecutionContext): Future[Unit] = {
//    amf.core.AMF.registerPlugin(PayloadValidatorPlugin)
    // Remod registering
    VocabulariesRegister.register(platform)
    Future.successful {}
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
