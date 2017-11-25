package amf.framework.parser

import amf.framework.model.document.BaseUnit

case class ParsedReference(baseUnit: BaseUnit, parsedUrl: String, referenceKind: ReferenceKind)
