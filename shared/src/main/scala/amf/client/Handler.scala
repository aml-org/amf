package amf.client

import amf.domain.APIDocumentation
import amf.parser.AMFUnit

/**
  *
  */
trait Handler {
  def success(document: AMFUnit)
  def error(exception: Throwable)
}

trait WebApiHandler {
  def success(document: APIDocumentation)
  def error(exception: Throwable)
}
