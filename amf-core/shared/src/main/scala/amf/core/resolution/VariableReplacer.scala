package amf.core.resolution

import amf.core.annotations.{ErrorRegistered, SourceAST}
import amf.core.model.domain.templates.Variable
import amf.core.model.domain.{DataNode, ScalarNode}
import amf.core.resolution.stages.ResolvedLinkNode
import amf.core.utils.InflectorBase.Inflector
import org.yaml.model.{QuotedMark, YScalar}
import org.yaml.render.YamlRender

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object VariableReplacer {

  private val Transformations =
    "singularize|pluralize|uppercase|lowercase|lowercamelcase|uppercamelcase|lowerunderscorecase|upperunderscorecase|lowerhyphencase|upperhyphencase"
  private val TransformationsRegex = Transformations.r

  val VariableRegex: Regex = s"<<\\s*([^<<>>|\\s]+)((?:\\s*\\|\\s*!(?:$Transformations)\\s*)*)>>".r

  def replaceNodeVariables(s: ScalarNode, values: Set[Variable], errorFunction: String => Unit): DataNode = {
    s.value match {
      case VariableRegex(name, transformations) =>
        values.find(_.name == name) match {
          case Some(Variable(_, scalar: ScalarNode))
              if scalar.dataType.isEmpty || scalar.dataType.get.endsWith("#string") =>
            s.value = VariableRegex.replaceAllIn(
              s.value,
              replaceMatch(values.map(v => v.name -> v.value).toMap, strict = false, isKey = false)(_, errorFunction))
            s
          case Some(_) if transformations.nonEmpty =>
            errorFunction(s"Cannot apply transformations '$transformations' to variable '$name'.")
            s
          case Some(Variable(_, scalar: ScalarNode)) => scalar
          case Some(Variable(_, node))               => node
          case None =>
            if (s.annotations.find(classOf[ErrorRegistered]).isEmpty) {
              errorFunction(s"Cannot find variable '$name'.")
              s.annotations += ErrorRegistered()
            }
            s
        }

      case text =>
        s.value = VariableRegex.replaceAllIn(
          text,
          replaceMatch(values.map(v => v.name -> v.value).toMap, strict = false, isKey = false)(_, errorFunction))
        s
    }
  }

  def replaceVariables(s: String, values: Set[Variable], errorFunction: String => Unit): String =
    VariableRegex.replaceAllIn(
      s,
      replaceMatch(values.map(v => v.name -> v.value).toMap, strict = false, isKey = false)(_, errorFunction))

  def replaceVariablesInKey(key: String, values: Set[Variable], errorFunction: String => Unit): String =
    VariableRegex.replaceAllIn(
      key,
      replaceMatch(values.map(v => v.name -> v.value).toMap, strict = true, isKey = true)(_, errorFunction))

  private def replaceMatch(values: Map[String, DataNode], strict: Boolean, isKey: Boolean)(
      m: Match,
      errorFunction: String => Unit): String = {
    val nameWithChevrons = m.group(0)
    val name             = m.group(1)
    var emptyVariable    = false
    val textOption = values
      .get(name)
      .flatMap {
        case v: ScalarNode =>
          v.annotations
            .find(classOf[SourceAST])
            .map(_.ast)
            .collectFirst({
              case s: YScalar if s.mark.isInstanceOf[QuotedMark] => {
                val variableValue = YamlRender.render(YScalar(s.text))
                if (variableValue.matches(" *") && isKey && strict) {
                  errorFunction(s"Variable '$name' cannot have an empty value")
                  None
                }
                variableValue
              }
              /* this calls quotedmark.marktext*/
            })
            .orElse {
              if (v.value.matches(" *") && isKey && strict) {
                errorFunction(s"Variable '$name' cannot have an empty value")
                emptyVariable = true
                None
              } else
                Some(v.value)
            }

        case r: ResolvedLinkNode => Some(r.source.alias)
        case node =>
          errorFunction(s"Variable '$name' cannot be replaced with type ${node.getClass.getName}")
          None
      }

    textOption
      .flatMap { text =>
        Option(m.group(2))
          .map { transformations =>
            TransformationsRegex.findAllIn(transformations).foldLeft(text)(variableTransformation(errorFunction))
          }
          .orElse(Some(text))
      }
      .getOrElse(
        if (strict && !emptyVariable) {
          errorFunction(s"Cannot find variable '$name'.")
          nameWithChevrons
        } else {
          nameWithChevrons
        }
      )
      .replace("$", "\\$")
  }

  protected[amf] def variableTransformation(
      errorFunction: String => Unit)(value: String, transformation: String): String = transformation match {
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
    case _ =>
      errorFunction(s"Transformation '$transformation' on '$value' is not valid.")
      value
  }
}
