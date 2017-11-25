package amf.core.parser

/**
  * Reference kinds to be collected by the Reference Collector
  */

sealed trait ReferenceKind

object LibraryReference extends ReferenceKind

object ExtensionReference extends ReferenceKind

object LinkReference extends ReferenceKind

object UnspecifiedReference extends ReferenceKind