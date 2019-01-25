package amf.client.handler

import amf.client.convert.CoreClientConverters._
import amf.client.resolve.ClientErrorHandler
import amf.core.parser

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("ErrorHandler")
@JSExportAll
object ErrorHandler {

  def handler(obj: JsErrorHandler): ClientErrorHandler =
    (id: String,
     node: String,
     property: ClientOption[String],
     message: String,
     range: ClientOption[parser.Range],
     level: String,
     location: ClientOption[String]) => obj.reportConstraint(id, node, property, message, range, level, location)

}

@js.native
trait JsErrorHandler extends js.Object {

  def reportConstraint(id: String,
                       node: String,
                       property: ClientOption[String],
                       message: String,
                       range: ClientOption[amf.core.parser.Range],
                       level: String,
                       location: ClientOption[String]): Unit = js.native
}
