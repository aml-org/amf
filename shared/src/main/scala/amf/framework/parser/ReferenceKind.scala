package amf.framework.parser

/**
  * Reference kinds to be collected by the Reference Collector
  */

sealed trait ReferenceKind

object Library extends ReferenceKind

object Extension extends ReferenceKind

object Link extends ReferenceKind

object Unspecified extends ReferenceKind