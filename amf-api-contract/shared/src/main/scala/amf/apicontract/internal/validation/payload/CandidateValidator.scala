package amf.apicontract.internal.validation.payload

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.internal.validation.{ValidationCandidate, ValidationConfiguration}
import org.mulesoft.common.core.CachedFunction
import org.mulesoft.common.functional.MonadInstances.identityMonad

import scala.concurrent.{ExecutionContext, Future}

object CandidateValidator {

  def validateAll(candidates: Seq[ValidationCandidate], config: ValidationConfiguration)(
      implicit executionContext: ExecutionContext): Future[AMFValidationReport] = {

    val client = config.amfConfig.elementClient()

    val pluginLookupFunc = (candidate: ValidationCandidate) => client.payloadValidatorFor(candidate.shape, candidate.payload)

    val validatorLookup = CachedFunction.from(pluginLookupFunc)

    val futures: Seq[Future[AMFValidationReport]] = candidates.map { candidate =>
      val validator = validatorLookup.runCached(candidate)
      validator.validate(candidate.payload)
    }

    Future.sequence(futures).map { f =>
      val seq = f.flatMap { _.results.sorted }
      AMFValidationReport("", ProfileName(""), seq)
    }
  }
}
