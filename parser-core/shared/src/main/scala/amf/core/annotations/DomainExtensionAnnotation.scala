package amf.core.annotations

import amf.core.model.domain.Annotation
import amf.core.model.domain.extensions.DomainExtension

/** Amf annotation for custom domain properties (raml annotations). */
case class DomainExtensionAnnotation(extension: DomainExtension) extends Annotation
