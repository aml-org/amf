package amf.core.annotations

import amf.core.model.domain.Annotation
import amf.core.parser.Annotations

case class BaseUriAnnotation(original: String, extensions: Annotations) extends Annotation
