package amf.core.validation

import amf.core.model.document.PayloadFragment
import amf.core.model.domain.Shape

case class ValidationCandidate(shape: Shape, payload: PayloadFragment)

case class ValidationShapeSet(candidates: Seq[ValidationCandidate], defaultSeverity: String = SeverityLevels.VIOLATION)

object ValidationShapeSet {
  def apply(shape: Shape, payload: PayloadFragment): ValidationShapeSet =
    new ValidationShapeSet(Seq(ValidationCandidate(shape, payload)))
}
