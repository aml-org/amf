package amf.apicontract.client.platform

import amf.aml.client.platform.BaseAMLBaseUnitClient
import amf.apicontract.client.scala.{AMFBaseUnitClient => InternalAMFBaseUnitClient}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.JSExportAll

/** The AMF Client contains common AMF operations. For more complex uses see [[amf.core.client.scala.parse.AMFParser]]
  * or [[amf.core.client.scala.render.AMFRenderer]]
  */
@JSExportAll
class AMFBaseUnitClient private[amf] (private val _internal: InternalAMFBaseUnitClient)
    extends BaseAMLBaseUnitClient(_internal) {

  private[amf] def this(configuration: AMFConfiguration) = {
    this(new InternalAMFBaseUnitClient(configuration))
  }

  override def getConfiguration(): AMFConfiguration = _internal.getConfiguration

  /** parse a [[amf.core.client.scala.model.document.Document]]
    * @param url
    *   of the resource to parse
    * @return
    *   a Future [[AMFDocumentResult]]
    */
  def parseDocument(url: String): ClientFuture[AMFDocumentResult] = _internal.parseDocument(url).asClient

  /** parse a [[amf.core.client.scala.model.document.Module]]
    * @param url
    *   of the resource to parse
    * @return
    *   a Future [[AMFLibraryResult]]
    */
  def parseLibrary(url: String): ClientFuture[AMFLibraryResult] = _internal.parseLibrary(url).asClient
}
