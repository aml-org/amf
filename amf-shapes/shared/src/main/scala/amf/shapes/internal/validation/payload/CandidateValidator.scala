package amf.shapes.internal.validation.payload

import amf.core.client.common.validation.ProfileName
import amf.core.client.scala.validation.AMFValidationReport
import amf.core.client.scala.validation.payload.AMFShapePayloadValidator
import amf.core.internal.validation.{ValidationCandidate, ValidationConfiguration}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

object CandidateValidator {
  private type ValidatorKey = (String, String) // shape id + payload MediaType

  def validateAll(candidates: Seq[ValidationCandidate], config: ValidationConfiguration)(implicit
      executionContext: ExecutionContext
  ): Future[AMFValidationReport] = {

    val client = config.amfConfig.elementClient()
    val cache  = mutable.Map[ValidatorKey, AMFShapePayloadValidator]()

    val futures: Seq[Future[AMFValidationReport]] = candidates.map { candidate =>
      val key: ValidatorKey = (candidate.shape.id, candidate.payload.mediaType.value())
      val validator = cache.getOrElse(
        key, {
          val foundValidator = client.payloadValidatorFor(candidate.shape, candidate.payload)
          cache.put(key, foundValidator)
          foundValidator
        }
      )
      validator.validate(candidate.payload)
    }

    Future.sequence(futures).map { f =>
      val seq = f.flatMap { _.results.sorted }
      AMFValidationReport("", ProfileName(""), seq)
    }
  }
}
