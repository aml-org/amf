package amf.core.annotations

import amf.core.model.domain.Annotation
import amf.core.model.domain.extensions.BaseDomainExtension

/** Amf annotation for custom domain properties (raml annotations). */
case class DomainExtensionAnnotation(extension: BaseDomainExtension) extends Annotation
