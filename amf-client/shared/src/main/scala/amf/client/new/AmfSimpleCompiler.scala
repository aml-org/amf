package amf.client.`new`

import amf.core.model.document.BaseUnit
import amf.core.validation.{AMFValidationReport, AMFValidationResult}

import scala.concurrent.Future

abstract class AmfSimpleCompiler {

  // parse and validate the resource. Returns the model not resolved (clone for validate).
  def compile: Future[AmfResult]
}

case class AmfResult(bu:BaseUnit, result: AMFValidationReport){
  def conforms = result.conforms
    ...
}
