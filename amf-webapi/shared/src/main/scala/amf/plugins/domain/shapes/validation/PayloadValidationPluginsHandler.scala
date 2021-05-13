package amf.plugins.domain.shapes.validation

import amf.ProfileName
import amf.client.execution.BaseExecutionEnvironment
import amf.client.plugins._
import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape
import amf.core.registries.AMFPluginsRegistry
import amf.core.unsafe.PlatformSecrets
import amf.core.utils._
import amf.core.validation._
import amf.internal.environment.Environment
import amf.remod.PayloadValidationPluginFinder
import org.mulesoft.common.core.CachedFunction
import org.mulesoft.common.functional.MonadInstances.identityMonad

import scala.concurrent.{ExecutionContext, Future}

object CandidateValidator extends PayloadValidationPluginFinder {
  def validateAll(candidates: Seq[ValidationCandidate], severity: String, env: Environment)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {

    val pluginLookupFunc = (candidate: ValidationCandidate) =>
      lookupPluginFor(candidate, env, severity)
        .validator(candidate.shape, candidate.payload.mediaType.value(), env)

    val validatorLookup = CachedFunction.from(pluginLookupFunc)

    val futures: Seq[Future[AMFValidationReport]] = candidates.map { candidate =>
      val validator = validatorLookup.runCached(candidate)
      validator.validate(candidate.payload)
    }

    Future.sequence(futures).map { f =>
      val seq = f.flatMap { _.results.sorted }
      AMFValidationReport(!seq.exists(_.severityLevel == SeverityLevels.VIOLATION), "", ProfileName(""), seq)
    }
  }
}

object PayloadValidationPluginsHandler extends PlatformSecrets with PayloadValidationPluginFinder {

  def validateFragment(
      shape: Shape,
      fragment: PayloadFragment,
      severity: String,
      env: Environment = Environment(),
      validationMode: ValidationMode = StrictValidationMode,
      exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): Future[AMFValidationReport] = {
    implicit val executionContext: ExecutionContext = exec.executionContext
    val p                                           = lookupPluginFor(fragment.mediaType.value(), shape, env, severity)

    p.validator(shape, fragment.mediaType.value(), env, validationMode).validate(fragment)
  }

  def validateWithGuessing(
      shape: Shape,
      payload: String,
      severity: String,
      env: Environment = Environment(),
      validationMode: ValidationMode = StrictValidationMode,
      exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): Future[AMFValidationReport] =
    validate(shape, payload.guessMediaType(isScalar = false), payload, severity, env, validationMode, exec)

  def validate(shape: Shape,
               mediaType: String,
               payload: String,
               severity: String,
               env: Environment = Environment(),
               validationMode: ValidationMode = StrictValidationMode,
               exec: BaseExecutionEnvironment = platform.defaultExecutionEnvironment): Future[AMFValidationReport] = {
    implicit val executionContext: ExecutionContext = exec.executionContext
    val p                                           = lookupPluginFor(mediaType, shape, env, severity)

    p.validator(shape, mediaType, env, validationMode).validate(payload)
  }

  def payloadValidator(shape: Shape,
                       mediaType: String,
                       env: Environment,
                       validationMode: ValidationMode): Option[PayloadValidator] =
    searchPlugin(mediaType, shape, env).map(_.validator(shape, mediaType, env, validationMode))
}
