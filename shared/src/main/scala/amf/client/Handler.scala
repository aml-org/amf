package amf.client

import amf.parser.AMFUnit

/**
  * Created by pedro.colunga on 5/26/17.
  */
trait Handler {
  def success(document: AMFUnit)
  def error(exception: Throwable)
}
