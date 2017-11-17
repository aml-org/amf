package amf.exception

case class UnableToResolveUnitException(location: String) extends RuntimeException(s"Unable to resolve unit: $location")
