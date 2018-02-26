package amf.plugins.document.vocabularies2.parser.common

import amf.core.annotations.LexicalInformation
import amf.core.parser.ErrorHandler
import amf.core.validation.core.ValidationSpecification
import amf.core.vocabulary.Namespace
import amf.plugins.document.vocabularies2.metamodel.domain.PropertyMappingModel
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.YPart

trait SyntaxErrorReporter { this: ErrorHandler =>

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


  def missingTermViolation(term: String, node: String, ast: YPart) = {
    violation(MissingTermSpecification.id(), node, s"Cannot find vocabulary term $term", ast)
  }

  def missingPropertyRangeViolation(term: String, node: String, lexical: Option[LexicalInformation]) = {
    violation(
      MissingPropertyRangeSpecification.id(),
      node,
      Some(PropertyMappingModel.ObjectRange.value.iri()),
      s"Cannot find property range term $term",
      lexical)
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
