package amf.plugins.document.webapi.validation.remote

import amf.core.model.document.PayloadFragment
import amf.core.validation.{AMFValidationResult, _}
import amf.core.vocabulary.Namespace

import scala.scalajs.js
import scala.scalajs.js.{JavaScriptException, SyntaxError}

object JsJsonSchemaValidator extends PlatformJsonSchemaValidator {

  val validator = AjvValidator()

  override type LoadedObj = js.Dynamic

  override protected def processCandidate(dataNode: js.Dynamic,
                                          jsonSchema: String,
                                          payload: PayloadFragment): Seq[AMFValidationResult] = {
    try {
      var schemaNode = loadJson(jsonSchema).asInstanceOf[js.Dictionary[js.Dynamic]]
      schemaNode -= "x-amf-fragmentType"
      schemaNode -= "example"
      schemaNode -= "examples"

    /*
        println("\n\nValidating...")
        println("  - SCHEMA:")
        println(js.JSON.stringify(schemaNode))
        println("  - DATA:")
        println(js.JSON.stringify(dataNode))
     */


      val correct = validator.validate(schemaNode.asInstanceOf[js.Object], dataNode)

      /*
        println(s"  ====> RESULT: $correct")
        println(js.JSON.stringify(validator.errors))
        println("-----------------------\n\n")
     */

      if (!correct) {
        validator.errors.toOption.getOrElse(js.Array[ValidationResult]()).map { result =>
          AMFValidationResult(
            message = makeValidationMessage(result),
            level = SeverityLevels.VIOLATION,
            targetNode = payload.encodes.id,
            targetProperty = None,
            validationId = (Namespace.AmfParser + "example-validation-error").iri(),
            position = payload.encodes.position(),
            location = payload.encodes.location(),
            source = result
          )
        }
      } else {
        Nil
      }
    } catch {
      case e: scala.scalajs.js.JavaScriptException =>
        Seq(
          AMFValidationResult(
            message = s"Internal error during validation ${e.getMessage}",
            level = SeverityLevels.VIOLATION,
            targetNode = payload.encodes.id,
            targetProperty = None,
            validationId = (Namespace.AmfParser + "example-validation-error").iri(),
            position = payload.encodes.position(),
            location = payload.encodes.location(),
            source = e
          ))
    }
  }

  private def makeValidationMessage(validationResult: ValidationResult): String ={
    var pointer = validationResult.dataPath
    if (pointer.startsWith(".")) pointer = pointer.replaceFirst("\\.", "")
    (pointer + " " + validationResult.message).trim
  }

  override protected def loadDataNodeString(payload: PayloadFragment): Option[js.Dynamic] = {
    try {
      literalRepresentation(payload) map { payloadText =>
        loadJson(payloadText)
      }
    } catch {
      case _: ExampleUnknownException                                      => None
      case e: JavaScriptException if e.exception.isInstanceOf[SyntaxError] => throw new InvalidJsonObject(e)
    }
  }

  protected def loadJson(str: String): js.Dynamic = {
    js.Dynamic.global.JSON.parse(str)
  }

}
