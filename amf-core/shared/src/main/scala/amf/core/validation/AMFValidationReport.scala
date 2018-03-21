package amf.core.validation

case class AMFValidationReport(conforms: Boolean, model: String, profile: String, results: Seq[AMFValidationResult]) {

  override def toString: String = {
    var str = s"Model: $model\n"
    str += s"Profile: $profile\n"
    str += s"Conforms? $conforms\n"
    str += s"Number of results: ${results.length}\n"
    results.sorted.groupBy(_.level) foreach {
      case (level, results) =>
        str += s"\nLevel: $level\n"
        for { result <- results } {
          str += result
        }
    }
    str
  }
}
