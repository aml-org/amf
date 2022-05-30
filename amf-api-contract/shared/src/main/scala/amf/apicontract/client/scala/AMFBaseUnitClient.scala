package amf.apicontract.client.scala

import amf.core.client.scala.AMFParseResult
import amf.core.client.scala.model.document.{Document, Module}
import amf.core.client.scala.parse.{AMFParser, InvalidBaseUnitTypeException}
import amf.core.internal.metamodel.document.{DocumentModel, ModuleModel}
import amf.shapes.client.scala.ShapesBaseUnitClient

import scala.concurrent.{ExecutionContext, Future}

/** The AMF Client contains common AMF operations associated to base unit and documents. For more complex uses see
  * [[AMFParser]] or [[amf.core.client.scala.render.AMFRenderer]]
  */
class AMFBaseUnitClient private[amf] (override protected val configuration: AMFConfiguration)
    extends ShapesBaseUnitClient(configuration) {

  override implicit val exec: ExecutionContext = configuration.getExecutionContext

  override def getConfiguration: AMFConfiguration = configuration

  /** parse a [[amf.core.client.platform.model.document.Document]]
    * @param url
    *   of the resource to parse
    * @return
    *   a Future [[AMFDocumentResult]]
    */
  def parseDocument(url: String): Future[AMFDocumentResult] = AMFParser.parse(url, configuration).map {
    case result: AMFParseResult if result.baseUnit.isInstanceOf[Document] =>
      new AMFDocumentResult(result.baseUnit.asInstanceOf[Document], result.results)
    case other =>
      throw InvalidBaseUnitTypeException.forMeta(other.baseUnit.meta, DocumentModel)
  }

  /** parse a [[amf.core.client.scala.model.document.Module]]
    * @param url
    *   of the resource to parse
    * @return
    *   a Future [[AMFLibraryResult]]
    */
  def parseLibrary(url: String): Future[AMFLibraryResult] = AMFParser.parse(url, configuration).map {
    case result: AMFParseResult if result.baseUnit.isInstanceOf[Module] =>
      new AMFLibraryResult(result.baseUnit.asInstanceOf[Module], result.results)
    case other =>
      throw InvalidBaseUnitTypeException.forMeta(other.baseUnit.meta, ModuleModel)
  }
}
