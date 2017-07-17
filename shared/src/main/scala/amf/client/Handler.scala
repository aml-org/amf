package amf.client

import amf.document.BaseUnit
import amf.domain.APIDocumentation

/**
  *
  */
trait Handler {
  def success(document: BaseUnit)
  def error(exception: Throwable)
}

trait WebApiHandler {
  def success(document: APIDocumentation)
  def error(exception: Throwable)
}
