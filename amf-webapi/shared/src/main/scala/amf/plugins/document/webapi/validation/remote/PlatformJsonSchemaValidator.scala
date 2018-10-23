package amf.plugins.document.webapi.validation.remote
import amf.core.model.domain.Shape
import amf.core.validation.{AMFValidationReport, AMFValidationResult, ValidationCandidate}
import amf.core.validation.core.ValidationProfile
import amf.plugins.domain.shapes.models.{AnyShape, FileShape}

import scala.collection.mutable
import scala.concurrent.Future

/* trait for platform natives validations of paylodas */
trait PlatformJsonSchemaValidator {

  case class JsonSchemaCandidate(candidate: ValidationCandidate, validator: PlatformPayloadValidator)

  val validatorsCache: mutable.Map[Shape, PlatformPayloadValidator] = mutable.Map()

  def validate(validationCandidates: Seq[ValidationCandidate],
               profile: ValidationProfile): Future[AMFValidationReport] = Future.successful {

    val jsonSchemaCandidates: Seq[JsonSchemaCandidate] = computeJsonSchemaValidators(validationCandidates)

    val results: Seq[AMFValidationResult] = jsonSchemaCandidates flatMap { v =>
      v.validator.validate(v.candidate.payload).results
    }

    AMFValidationReport(
      conforms = results.isEmpty,
      model = "http://test.com/paylaod#validations",
      profile = profile.name, // profiles.headOption.map(_.name).getOrElse(ProfileNames.AMF)
      results = results
    )
  }

  def computeJsonSchemaValidators(validationCandidates: Seq[ValidationCandidate]): Seq[JsonSchemaCandidate] = {
    // already caching in generated json schema annotation? how to ignore the parsed json schema annotation?
    validationCandidates.map { vc =>
      if (vc.shape.isInstanceOf[FileShape]) { // duplicated logic???
        None
      } else {
        vc.shape match {
          case anyS: AnyShape =>
            val validator = validatorsCache.get(vc.shape) match {
              case Some(v) => v // todo: add method for recieve a data node or fragment? or serialize??
              case _ =>
                val validator = validatorForShape(anyS)
                validatorsCache.put(anyS, validator)
                validator
            }
            Some(JsonSchemaCandidate(vc, validator))
          case _ => None // what can i do when is a not valid any shape??
        }

      }
    } collect { case Some(v) => v }
  }

  protected def validatorForShape(s: AnyShape): PlatformPayloadValidator
  // todo: move to json schema serializer? remove header and examples using comparator by json schema context
}
