package amf.shapes.internal.validation.payload

case class MaxNestingValueReached(limit: Long) extends Exception(s"Reached maximum nesting value of $limit in JSON")
