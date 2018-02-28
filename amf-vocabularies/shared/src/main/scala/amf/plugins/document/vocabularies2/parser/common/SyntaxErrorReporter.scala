package amf.plugins.document.vocabularies2.parser.common

import amf.core.annotations.LexicalInformation
import amf.core.parser.{Annotations, ErrorHandler}
import amf.core.validation.core.ValidationSpecification
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies2.metamodel.domain.PropertyMappingModel
import amf.plugins.document.vocabularies2.model.domain.PropertyMapping
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.{YNode, YPart}

trait SyntaxErrorReporter { this: ErrorHandler =>

  protected val InconsistentPropertyRangeValueSpecification = ValidationSpecification(
    (Namespace.AmfParser + "inconsistent-property-range-value").iri(),
    "Range value does not match the expectd type",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  protected val MissingPropertyRangeSpecification = ValidationSpecification(
    (Namespace.AmfParser + "missing-node-mapping-range-term").iri(),
    "Missing property range term",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )


  protected val MissingTermSpecification = ValidationSpecification(
    (Namespace.AmfParser + "missing-vocabulary-term").iri(),
    "Missing vocabulary term",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )

  protected val MissingFragmentSpecification = ValidationSpecification(
    (Namespace.AmfParser + "missing-dialect-fragment").iri(),
    "Missing dialect fragment",
    None,
    None,
    Seq(ValidationSpecification.PARSER_SIDE_VALIDATION)
  )


  def missingTermViolation(term: String, node: String, ast: YPart) = {
    violation(MissingTermSpecification.id(), node, s"Cannot find vocabulary term $term", ast)
  }

  def missingFragmentViolation(fragment: String, node: String, ast: YPart) = {
    violation(MissingFragmentSpecification.id(), node, s"Cannot find fragment $fragment", ast)
  }

  def missingPropertyRangeViolation(term: String, node: String, lexical: Option[LexicalInformation]) = {
    violation(
      MissingPropertyRangeSpecification.id(),
      node,
      Some(PropertyMappingModel.ObjectRange.value.iri()),
      s"Cannot find property range term $term",
      lexical)
  }

  def inconsistentPropertyRangeValueViolation(node: String, property: PropertyMapping, expected: String, found: String, valueNode: YNode) = {
    violation(
      InconsistentPropertyRangeValueSpecification.id(),
      node,
      Some(property.nodePropertyMapping()),
      s"Cannot find expected range for property ${property.nodePropertyMapping()} (${property.name()})",
      Annotations(valueNode).find(classOf[LexicalInformation]))
  }

  def closedNodeViolation(id: String, property: String, nodeType: String, ast: YPart) = {
    violation(
      ParserSideValidations.ClosedShapeSpecification.id(),
      id,
      s"Property: '$property' not supported in a $nodeType node",
      ast
    )
  }
}
