package amf.core.parser

import amf.core.model.document.BaseUnit

case class ParsedReference(baseUnit: BaseUnit, parsedUrl: String, referenceKind: ReferenceKind)
