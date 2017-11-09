package amf.validation

import amf.domain.Annotation.LexicalInformation
import amf.parser.Range
import amf.validation.SeverityLevels.VIOLATION
import amf.validation.model.ParserSideValidations.ParsingErrorSpecification
import org.mulesoft.lexer.InputRange
import org.yaml.model._

/**
  * Validation aware: requires an implicit validation.
  */
trait ValidationAware {}
