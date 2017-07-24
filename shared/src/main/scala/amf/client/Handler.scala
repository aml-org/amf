package amf.client

import amf.document.BaseUnit
import amf.domain.WebApi

/**
  *
  */
trait Handler {
  def success(document: BaseUnit)
  def error(exception: Throwable)
}

trait WebApiHandler {
  def success(document: WebApi)
  def error(exception: Throwable)
}
