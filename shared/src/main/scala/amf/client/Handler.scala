package amf.client

import amf.parser.Container

/**
  * Created by pedro.colunga on 5/26/17.
  */
trait Handler {
  def success(document: Container)
  def error(exception: Throwable)
}
