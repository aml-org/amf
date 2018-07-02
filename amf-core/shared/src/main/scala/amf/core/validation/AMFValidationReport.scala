package amf.core.validation

import amf.ProfileName

case class AMFValidationReport(conforms: Boolean,
                               model: String,
                               profile: ProfileName,
                               results: Seq[AMFValidationResult]) {

  private val DefaultMax = 30

  def toString(max: Int): String = {
    val str = StringBuilder.newBuilder
    str.append(s"Model: $model\n")
    str.append(s"Profile: ${profile.profile}\n")
    str.append(s"Conforms? $conforms\n")
    str.append(s"Number of results: ${results.length}\n")
    for { (level, results) <- results.take(max).sorted.groupBy(_.level) } {
      str.append(s"\nLevel: $level\n")
      for { result <- results } {
        str.append(result)
      }
    }
    str.toString
  }

  override def toString: String = toString(DefaultMax)
}
