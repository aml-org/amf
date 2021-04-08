package amf.plugins.document.webapi.validation

import amf.core.validation.core.ShaclSeverityUris._
import amf.core.validation.core.{FunctionConstraint, NodeConstraint, PropertyConstraint, ValidationSpecification}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.document.webapi.validation.AMFRawValidations.AMFValidation

object RawValidationAdapter extends ImportUtils {

  private val SHACL_PATH_IRI              = "http://www.w3.org/ns/shacl#path"
  private val SHACL_TARGET_OBJECTS_OF_IRI = "http://www.w3.org/ns/shacl#targetObjectsOf"

  def apply(validation: AMFValidation): Seq[ValidationSpecification] = parseValidation(validation)

  private def parseValidation(validation: AMFValidation) = {
    val spec = createSpecificationFrom(validation)

    expandValidationTargetIri(validation) match {
      case SHACL_PATH_IRI =>
        val valueType = buildValueType(validation)
        valueType match {
          case sh @ ValueType(Namespace.Shacl, _) =>
            Seq(spec.copy(propertyConstraints = Seq(parsePropertyConstraint(spec, validation, sh))))
          case sh @ ValueType(Namespace.Shapes, _) =>
            findComplexShaclConstraint(sh)
              .getOrElse(
                Seq(specWithFunctionConstraint(validation, spec, sh))
              )

          case _ => Seq(spec)
        }

      case SHACL_TARGET_OBJECTS_OF_IRI =>
        Seq(
          spec.copy(
            targetObject = Seq(validation.owlProperty),
            nodeConstraints = Seq(NodeConstraint(validation.constraint, validation.value))
          ))
      case _ => throw new Exception(s"Unknown validation target ${validation.target}")
    }
  }

  private def createSpecificationFrom(validation: AMFValidation): ValidationSpecification =
    ValidationSpecification(
      name = computeValidationId(validation),
      message = validation.message.getOrElse(""),
      severity = amfToShaclSeverity(validation.severity),
      ramlMessage = Some(validation.ramlErrorMessage),
      oasMessage = Some(validation.openApiErrorMessage),
      targetClass = Seq(validation.owlClass)
    )

  private def specWithFunctionConstraint(validation: AMFValidation, spec: ValidationSpecification, sh: ValueType) = {
    spec.copy(functionConstraint = Option(parseFunctionConstraint(validation, sh)))
  }

  private def buildValueType(validation: AMFValidation) = {
    if (constraintHasUriFragment(validation)) {
      val strings = validation.constraint.trim.split("#")
      ValueType(Namespace.find(strings.head).get, strings.last)
    } else Namespace.staticAliases.expand(validation.constraint)
  }

  private def constraintHasUriFragment(validation: AMFValidation) = validation.constraint.trim.contains("#")

  private def expandValidationTargetIri(validation: AMFValidation) = {
    Namespace.staticAliases.expand(validation.target.trim).iri()
  }

  private def computeValidationId(validation: AMFValidation) = {
    validation.uri match {
      case Some(s) => s.trim
      case _       => validationId(validation)
    }
  }

  private def parsePropertyConstraint(spec: ValidationSpecification,
                                      validation: AMFValidation,
                                      sh: ValueType): PropertyConstraint = {

    val constraint = PropertyConstraint(
      ramlPropertyId = validation.owlProperty,
      name = s"${spec.name}/prop",
      message = validation.message
    )
    sh.iri() match {
      case "http://www.w3.org/ns/shacl#minCount"     => constraint.copy(minCount = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxCount"     => constraint.copy(maxCount = Some(validation.value))
      case "http://www.w3.org/ns/shacl#pattern"      => constraint.copy(pattern = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minExclusive" => constraint.copy(minExclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxExclusive" => constraint.copy(maxExclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minInclusive" => constraint.copy(minInclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxInclusive" => constraint.copy(maxInclusive = Some(validation.value))
      case "http://www.w3.org/ns/shacl#minLength"    => constraint.copy(minLength = Some(validation.value))
      case "http://www.w3.org/ns/shacl#maxLength"    => constraint.copy(maxLength = Some(validation.value))
      case "http://www.w3.org/ns/shacl#in"           => constraint.copy(in = validation.value.split("\\s*,\\s*"))
      case "http://www.w3.org/ns/shacl#node"         => constraint.copy(node = Some(validation.value))
      case "http://www.w3.org/ns/shacl#datatype"     => constraint.copy(datatype = Some(validation.value))
      case "http://www.w3.org/ns/shacl#class"        => constraint.copy(`class` = Seq(validation.value))
      case _                                         => throw new Exception(s"Unsupported constraint ${validation.constraint}")
    }
  }

  private def parseFunctionConstraint(validation: AMFValidation, sh: ValueType): FunctionConstraint = {
    FunctionConstraint(
      message = validation.message,
      functionName = None, // i have to ignore the function name so it will be taken from the generated js library
      code = JsCustomValidations(sh.name),
      internalFunction = Some(sh.name)
    )
  }

  private def findComplexShaclConstraint(sh: ValueType): Option[Seq[ValidationSpecification]] = {
    ComplexShaclCustomValidations.defintions.get(sh.name)
  }
}
