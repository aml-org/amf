package amf.client

import amf.parser.Document

/**
  * Created by pedro.colunga on 5/26/17.
  */
trait Handler {
    def success(document: Document)
    def error(exception: Throwable)
}
