package amf.framework.parser

sealed trait ReferenceKind

object Library extends ReferenceKind

object Extension extends ReferenceKind

object Link extends ReferenceKind

object Unspecified extends ReferenceKind