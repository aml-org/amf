package amf.core.validation.core

import amf.core.vocabulary.Namespace
import org.yaml.model.YDocument.EntryBuilder


case class FunctionConstraint(message: Option[String],
                              code: Option[String] = None,
                              libraries: Seq[String] = Seq(),
                              functionName: Option[String] = None
                             ) {

  def constraintId(validationId: String) = s"${validationId}Constraint"
  def validatorId(validationId: String) = s"${validationId}Validator"
  def validatorPath(validationId: String) = s"${validationId}Path"
  def validatorArgument(validationId: String) = {
    "$" + validatorPath(validationId)
      .split("#")
      .last
      .replace("-","_")
      .replace(".","_")
  }
  def computeFunctionName(validationId: String) = functionName match {
    case Some(fnName) => fnName
    case _            => {
      val localName = validationId.split("/").last.split("#").last
      s"${localName.replace("-","_").replace(".","_")}FnName"
    }
  }
}

case class NodeConstraint(constraint: String, value: String)

case class PropertyConstraint(ramlPropertyId: String,
                              name: String,
                              message: Option[String] = None,
                              pattern: Option[String] = None,
                              maxCount: Option[String] = None,
                              minCount: Option[String] = None,
                              minLength: Option[String] = None,
                              maxLength: Option[String] = None,
                              minExclusive: Option[String] = None,
                              maxExclusive: Option[String] = None,
                              minInclusive: Option[String] = None,
                              maxInclusive: Option[String] = None,
                              node: Option[String] = None,
                              datatype: Option[String] = None,
                              `class`: Seq[String] = Seq(),
                              in: Seq[String] = Seq.empty,
                              custom: Option[(EntryBuilder, String) => Unit] = None
                             ) {}

case class ValidationSpecification(name: String,
                                   message: String,
                                   ramlMessage: Option[String] = None,
                                   oasMessage: Option[String] = None,
                                   targetInstance: Seq[String] = Seq.empty,
                                   targetClass: Seq[String] = Seq.empty,
                                   targetObject: Seq[String] = Seq.empty,
                                   unionConstraints: Seq[String] = Seq.empty,
                                   propertyConstraints: Seq[PropertyConstraint] = Seq.empty,
                                   nodeConstraints: Seq[NodeConstraint] = Seq.empty,
                                   closed: Option[Boolean] = None,
                                   functionConstraint: Option[FunctionConstraint] = None,
                                   custom: Option[(EntryBuilder, String) => Unit] = None
                                  ) {

  def id(): String = {
    if (name.startsWith("http://") || name.startsWith("https://") || name.startsWith("file:")) {
      name
    } else {
      Namespace.expand(name).iri() match {
        case s if s.startsWith("http://") || s.startsWith("https://") || s.startsWith("file:") => s
        case s  => (Namespace.Data + s).iri()
      }
    }
  }

  def isParserSide() = targetClass.nonEmpty && targetClass.head == ValidationSpecification.PARSER_SIDE_VALIDATION
}

object ValidationSpecification {
  val PARSER_SIDE_VALIDATION: String = (Namespace.Shapes + "ParserShape").iri()
}
