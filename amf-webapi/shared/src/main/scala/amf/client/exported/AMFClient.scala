package amf.client.exported

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.client.environment.{AMFClient => InternalAMFClient}
import amf.client.convert.WebApiClientConverters._

import scala.concurrent.ExecutionContext

@JSExportAll
class AMFClient private (private val _internal: InternalAMFClient) extends AMLClient(_internal) {

  private implicit val ec: ExecutionContext = _internal.getConfiguration.getExecutionContext

  @JSExportTopLevel("AMFClient")
  def this(configuration: AMFConfiguration) = {
    this(new InternalAMFClient(configuration))
  }

  override def getConfiguration: AMFConfiguration = _internal.getConfiguration

  /**
    * parse a {@link amf.client.model.document.Document}
    * @param url of the resource to parse
    * @return a Future{@link amf.client.exported.AMFDocumentResult}
    */
  def parseDocument(url: String): ClientFuture[AMFDocumentResult] = _internal.parseDocument(url).asClient

  /**
    * parse a {@link amf.client.model.document.Module}
    * @param url of the resource to parse
    * @return a Future {@link amf.client.exported.AMFLibraryResult}
    */
  def parseLibrary(url: String): ClientFuture[AMFLibraryResult] = _internal.parseLibrary(url).asClient
}
