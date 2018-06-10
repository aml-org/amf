package amf.validation

import amf.ProfileNames
import amf.core.unsafe.PlatformSecrets
import amf.facades.Validation
import amf.plugins.features.validation.PlatformValidator
import amf.plugins.features.validation.emitters.{JSLibraryEmitter, ValidationJSONLDEmitter}
import org.scalatest.AsyncFunSuite

class FromJsonLDPayloadValidationTest extends AsyncFunSuite with PlatformSecrets {

  val path = "file://amf-client/shared/src/test/resources/validations/"

  val testValidations = Map(
    "bad_domain/valid.jsonld"                 -> ExpectedReport(conforms = true, 0, ProfileNames.OAS),
    "endpoint/amf.jsonld"                     -> ExpectedReport(conforms = false, 1, ProfileNames.AMF),
    "endpoint/valid.jsonld"                   -> ExpectedReport(conforms = true, 0, ProfileNames.AMF),
    "operation/amf.jsonld"                    -> ExpectedReport(conforms = false, 1, ProfileNames.AMF),
    "operation/valid.jsonld"                  -> ExpectedReport(conforms = true, 0, ProfileNames.AMF),
    "parameters/amf_properties.jsonld"        -> ExpectedReport(conforms = false, 4, ProfileNames.AMF),
    "parameters/amf_empty.jsonld"             -> ExpectedReport(conforms = false, 4, ProfileNames.AMF),
    "parameters/amf_valid.jsonld"             -> ExpectedReport(conforms = true, 0, ProfileNames.AMF),
    "shapes/enum_amf.jsonld"                  -> ExpectedReport(conforms = false, 2, ProfileNames.OAS),
    "shapes/enum_valid.jsonld"                -> ExpectedReport(conforms = true, 0, ProfileNames.OAS),
    "webapi/amf.jsonld"                       -> ExpectedReport(conforms = false, 1, ProfileNames.OAS),
    "webapi/valid.jsonld"                     -> ExpectedReport(conforms = false, 1, ProfileNames.OAS),
    "webapi/valid.jsonld"                     -> ExpectedReport(conforms = true, 0, ProfileNames.RAML),
    "webapi/bad_protocol.jsonld"              -> ExpectedReport(conforms = false, 1, ProfileNames.RAML),
    "types/scalars/missing_type.jsonld"       -> ExpectedReport(conforms = false, 1, ProfileNames.RAML),
    "types/scalars/missing_type_valid.jsonld" -> ExpectedReport(conforms = true, 0, ProfileNames.RAML),
    "types/scalars/wrong_facet.jsonld"        -> ExpectedReport(conforms = false, 2, ProfileNames.RAML),
    "types/scalars/valid_facet.jsonld"        -> ExpectedReport(conforms = true, 0, ProfileNames.RAML),
    //   we commentated the range of items validation
    //    "types/arrays/wrong_items.jsonld"         -> ExpectedReport(conforms = false, 1, ProfileNames.RAML),
    //    "types/arrays/right_items.jsonld"         -> ExpectedReport(conforms = true, 0, ProfileNames.RAML),
    "types/arrays/empty_items.jsonld"         -> ExpectedReport(conforms = true, 0, ProfileNames.RAML),
    "types/arrays/empty_items.jsonld"         -> ExpectedReport(conforms = false, 1, ProfileNames.OAS),
    "annotationTypes/invalid.jsonld"          -> ExpectedReport(conforms = false, 1, ProfileNames.RAML),
    "annotationTypes/valid.jsonld"            -> ExpectedReport(conforms = true, 0, ProfileNames.RAML)
  )

  for {
    (file, expectedReport) <- testValidations
  } yield {
    test(s"SHACL Validator $file") {
      validate(file, expectedReport)
    }
  }

  private def validate(file: String, expectedReport: ExpectedReport) = {
    platform.resolve(path + file).flatMap { data =>
      val model = data.stream.toString
      Validation(platform).flatMap { validation =>
        val effectiveValidations = validation.computeValidations(expectedReport.profile)
        val shapes               = validation.shapesGraph(effectiveValidations)
        val jsLibrary            = new JSLibraryEmitter(None).emitJS(effectiveValidations.effective.values.toSeq)

        jsLibrary match {
          case Some(code) =>
            PlatformValidator.instance.registerLibrary(ValidationJSONLDEmitter.validationLibraryUrl, code)
          case _ => // ignore
        }
        PlatformValidator.instance.report(
          model,
          "application/ld+json",
          shapes,
          "application/ld+json"
        ) flatMap { report =>
          assert(expectedReport == ExpectedReport(report.conforms, report.results.length, expectedReport.profile))
        }
      }
    }
  }

}
