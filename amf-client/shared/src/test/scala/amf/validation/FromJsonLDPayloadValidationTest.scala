package amf.validation

import amf.core.unsafe.PlatformSecrets
import amf.facades.Validation
import amf.plugins.features.validation.PlatformValidator
import amf.plugins.features.validation.emitters.{JSLibraryEmitter, ShaclJsonLdShapeGraphEmitter}
import amf.{AmfProfile, Oas20Profile, Oas30Profile, Raml10Profile}
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class FromJsonLDPayloadValidationTest extends AsyncFunSuite with PlatformSecrets {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val path = "file://amf-client/shared/src/test/resources/validations/"

  val testValidations = Map(
    "bad_domain/valid.jsonld"                                -> ExpectedReport(conforms = true, 0, Oas20Profile),
    "endpoint/amf.jsonld"                                    -> ExpectedReport(conforms = true, 0, AmfProfile),
    "endpoint/valid.jsonld"                                  -> ExpectedReport(conforms = true, 0, AmfProfile),
    "operation/amf.jsonld"                                   -> ExpectedReport(conforms = true, 0, AmfProfile),
    "operation/valid.jsonld"                                 -> ExpectedReport(conforms = true, 0, AmfProfile),
    "parameters/amf_properties.jsonld"                       -> ExpectedReport(conforms = false, 2, AmfProfile),
    "parameters/amf_empty.jsonld"                            -> ExpectedReport(conforms = false, 2, AmfProfile),
    "parameters/amf_valid.jsonld"                            -> ExpectedReport(conforms = true, 0, AmfProfile),
    "shapes/enum_amf.jsonld"                                 -> ExpectedReport(conforms = false, 1, Oas20Profile),
    "shapes/enum_valid.jsonld"                               -> ExpectedReport(conforms = true, 0, Oas20Profile),
    "webapi/amf.jsonld"                                      -> ExpectedReport(conforms = false, 1, Oas20Profile),
    "webapi/valid.jsonld"                                    -> ExpectedReport(conforms = false, 1, Oas20Profile),
    "webapi/valid.jsonld"                                    -> ExpectedReport(conforms = true, 0, Raml10Profile),
    "webapi/bad_protocol.jsonld"                             -> ExpectedReport(conforms = false, 1, Raml10Profile),
    "types/scalars/missing_type.jsonld"                      -> ExpectedReport(conforms = false, 1, Raml10Profile),
    "types/scalars/missing_type_valid.jsonld"                -> ExpectedReport(conforms = true, 0, Raml10Profile),
    "types/scalars/wrong_facet.jsonld"                       -> ExpectedReport(conforms = false, 1, Raml10Profile),
    "types/scalars/valid_facet.jsonld"                       -> ExpectedReport(conforms = true, 0, Raml10Profile),
    "types/scalars/invalid_xml_attribute_non_scalar.jsonld"  -> ExpectedReport(conforms = false, 1, Raml10Profile),
    "types/scalars/invalid_xml_wrapped_scalar.jsonld"        -> ExpectedReport(conforms = false, 1, Raml10Profile),
    "types/arrays/empty_items.jsonld"                        -> ExpectedReport(conforms = true, 0, Raml10Profile),
    "types/arrays/empty_items.jsonld"                        -> ExpectedReport(conforms = false, 1, Oas20Profile),
    "annotationTypes/invalid.jsonld"                         -> ExpectedReport(conforms = false, 1, Raml10Profile),
    "annotationTypes/valid.jsonld"                           -> ExpectedReport(conforms = true, 0, Raml10Profile),
    "path-parameter-required/required-is-not-present.jsonld" -> ExpectedReport(conforms = false, 1, Oas20Profile),
    "path-parameter-required/required-set-to-true.jsonld"    -> ExpectedReport(conforms = true, 0, Oas20Profile),
    "file-parameter/invalid.jsonld"                          -> ExpectedReport(conforms = false, 1, Oas20Profile),
    "../upanddown/oas3/basic-content.jsonld"                 -> ExpectedReport(conforms = false, 1, Oas30Profile)
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
            PlatformValidator.instance.registerLibrary(ShaclJsonLdShapeGraphEmitter.validationLibraryUrl, code)
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
