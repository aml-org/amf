package amf.core.resolution

import amf.core.model.domain.{DataNode, ScalarNode}
import amf.core.model.domain.templates.Variable
import amf.core.utils.InflectorBase.Inflector

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object VariableReplacer {

  private val Transformations =
    "singularize|pluralize|uppercase|lowercase|lowercamelcase|uppercamelcase|lowerunderscorecase|upperunderscorecase|lowerhyphencase|upperhyphencase"
  private val TransformationsRegex = Transformations.r

  val VariableRegex: Regex = s"<<\\s*([^<<>>|\\s]+)((?:\\s*\\|\\s*!(?:$Transformations)\\s*)*)>>".r

  def replaceVariables(s: ScalarNode, values: Set[Variable]): DataNode = {
    s.value match {
      case VariableRegex(name, transformations) =>
        values.find(_.name == name) match {
          case Some(Variable(_, scalar: ScalarNode)) if scalar.dataType.isEmpty || scalar.dataType.get.endsWith("#string") =>
            s.value = VariableRegex.replaceAllIn(s.value, replaceMatch(values.map(v => v.name -> v.value).toMap)(_))
            s
          case Some(_) if transformations.nonEmpty =>
            throw new Exception(s"Cannot apply transformations '$transformations' to variable '$name'.")
          case Some(Variable(_, scalar: ScalarNode))=> scalar
          case Some(Variable(_, node)) => node
          case None                    => throw new Exception(s"Cannot find variable '$name'.")
        }

      case text =>
        s.value = VariableRegex.replaceAllIn(text, replaceMatch(values.map(v => v.name -> v.value).toMap)(_))
        s
    }
  }

  def replaceVariables(s: String, values: Set[Variable]): String =
    VariableRegex.replaceAllIn(s, replaceMatch(values.map(v => v.name -> v.value).toMap)(_))

  private def replaceMatch(values: Map[String, DataNode])(m: Match): String = {
    val name = m.group(1)
    values
      .get(name)
      .map {
        case v: ScalarNode =>
          val text = v.value
          Option(m.group(2))
            .map { transformations =>
              TransformationsRegex.findAllIn(transformations).foldLeft(text)(variableTransformation)
            }
            .getOrElse(text)
        case node => throw new Exception(s"Variable '$name' cannot be replaced with type $node")
      }
      .getOrElse(name)
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
