package amf.validation

import amf.core.validation
import amf.core.parser
import amf.core.parser.Position.ZERO

import scala.collection.JavaConverters._

class Position(position: parser.Position) {
  val line: Int = position.line
  val column: Int = position.column
}

class Range(range: parser.Range) {
  val start: Position = new Position(range.start)
  val end: Position = new Position(range.end)
}

class AMFValidationResult(result: validation.AMFValidationResult) {
  val message: String = result.message
  val level: String = result.level
  val targetNode: String = result.targetNode
  val targetProperty: String = result.targetProperty.orNull
  val validationId: String = result.validationId
  val position: Range = result.position match {
    case Some(lexicalInformation) => new Range(lexicalInformation.range)
    case _                        => new Range(new parser.Range(ZERO, ZERO))
  }
  val source = result.source
}

class AMFValidationReport(report: validation.AMFValidationReport) {
  val conforms: Boolean = report.conforms
  val model: String = report.model
  val profile: String = report.profile
  val results: java.util.List[AMFValidationResult] = report.results.map(new AMFValidationResult(_)).asJava
}
