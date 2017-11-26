package amf.core.resolution

import amf.core.model.domain.templates.Variable
import amf.core.utils.InflectorBase.Inflector

import scala.util.matching.Regex
import scala.util.matching.Regex.Match



object VariableReplacer {

  val VariableRegex: Regex =
    "<<([^<<>>\\s]+)(?:\\s*\\|\\s*!(singularize|pluralize|uppercase|lowercase|lowercamelcase|uppercamelcase|lowerunderscorecase|upperunderscorecase|lowerhyphencase|upperhyphencase))?>>".r

  def replaceVariables(s: String, values: Set[Variable]): String =
    VariableRegex.replaceAllIn(s, replaceMatch(values.map(v => v.name -> v.value).toMap)(_))

  private def replaceMatch(values: Map[String, String])(m: Match): String = {
    values
      .get(m.group(1))
      .map(v => Option(m.group(2)).map(variableTransformation(v, _)).getOrElse(v))
      .getOrElse(m.group(1))
  }

  protected[amf] def variableTransformation(value: String, transformation: String): String = transformation match {
    case "singularize"         => value.singularize
    case "pluralize"           => value.pluralize
    case "uppercase"           => value.toUpperCase
    case "lowercase"           => value.toLowerCase
    case "lowercamelcase"      => value.camelize().decapitalize
    case "uppercamelcase"      => value.camelize().capitalize
    case "lowerunderscorecase" => value.camelToScoreSing().toLowerCase
    case "upperunderscorecase" => value.camelToScoreSing().toUpperCase
    case "lowerhyphencase"     => value.camelToScoreSing("-").toLowerCase
    case "upperhyphencase"     => value.camelToScoreSing("-").toUpperCase
    case _                     => throw new Exception(s"Transformation '$transformation' on '$value' is not valid.")
  }
}
