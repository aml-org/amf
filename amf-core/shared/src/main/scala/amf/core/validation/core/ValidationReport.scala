package amf.core.validation.core

/**
  * Created by antoniogarrote on 18/07/2017.
  */


/**
  * Individual constraint validation failure result
  */
trait ValidationResult {
  def message: Option[String]
  def path: String
  def sourceConstraintComponent: String
  def focusNode: String
  def severity: String
  def sourceShape: String
}

/**
  * A report created after the application of a graph of shapes to a graph of ata
  */
trait ValidationReport {
  def conforms: Boolean
  def results: List[ValidationResult]
}

/**
  * Utility class for ValidationReports
  */
object ValidationReport {

  def displayString(report: ValidationReport): String = {
    var str = s"Conforms? ${report.conforms}\n"
    str += s"Number of results: ${report.results.length}\n"
    for { result <- report.results } {
      str += s"\n- Source: ${result.sourceShape}\n"
      str += s"  Path: ${result.path}\n"
      str += s"  Focus node: ${result.focusNode}\n"
      str += s"  Constraint: ${result.sourceConstraintComponent}\n"
      str += s"  Message: ${result.message}\n"
      str += s"  Severity: ${result.severity}\n"
    }
    str
  }

}
