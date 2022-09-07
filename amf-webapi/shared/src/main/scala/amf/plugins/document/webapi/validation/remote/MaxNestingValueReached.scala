package amf.plugins.document.webapi.validation.remote

case class MaxNestingValueReached(limit: Long) extends Exception(s"Reached maximum nesting value of $limit in JSON")
