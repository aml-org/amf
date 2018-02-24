package amf.plugins.document.vocabularies2.parser.common

import amf.core.parser.ErrorHandler
import amf.core.validation.core.ValidationSpecification
import amf.core.vocabulary.Namespace
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.YPart

trait SyntaxErrorReporter { this: ErrorHandler =>

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

  def closedNodeViolation(id: String, property: String, nodeType: String, ast: YPart) = {
    violation(
      ParserSideValidations.ClosedShapeSpecification.id(),
      id,
      s"Property: '$property' not supported in a $nodeType node",
      ast
    )
  }
}
