package amf.client.exported

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import amf.client.environment.{AMFClient => InternalAMFClient}
import amf.client.convert.ApiClientConverters._
import amf.core.client.platform.AMFResult
import amf.core.client.platform.model.document.BaseUnit

/**
  * The AMF Client contains common AMF operations.
  * For more complex uses see [[AMFParser]] or [[amf.client.remod.AMFRenderer]]
  */
@JSExportAll
class AMFClient private (private val _internal: InternalAMFClient) extends BaseAMLClient(_internal) {

  @JSExportTopLevel("AMFClient")
  def this(configuration: AMFConfiguration) = {
    this(new InternalAMFClient(configuration))
  }

  override def getConfiguration(): AMFConfiguration = _internal.getConfiguration

  /**
    * parse a [[amf.client.model.document.Document]]
    * @param url of the resource to parse
    * @return a Future [[AMFDocumentResult]]
    */
  def parseDocument(url: String): ClientFuture[AMFDocumentResult] = _internal.parseDocument(url).asClient

  /**
    * parse a [[amf.client.model.document.Module]]
    * @param url of the resource to parse
    * @return a Future [[AMFLibraryResult]]
    */
  def parseLibrary(url: String): ClientFuture[AMFLibraryResult] = _internal.parseLibrary(url).asClient

  /**
    * Transforms a [[BaseUnit]] with using pipeline with default id.
    * @param bu [[BaseUnit]] to transform
    * @param targetMediaType Provide a specification for obtaining the correct pipeline.
    *                        Must be <code>"application/spec"</code> or <code>"application/spec+syntax"</code>.
    *                        Examples: <code>"application/raml10"</code> or <code>"application/raml10+yaml"</code>
    * @return An [[AMFResult]] with the transformed BaseUnit and it's validation results
    */
  def transformDefault(bu: BaseUnit, targetMediaType: String): AMFResult =
    _internal.transformDefault(bu, targetMediaType)

  /**
    * Transforms a [[BaseUnit]] with using pipeline with editing id.
    * @param bu [[BaseUnit]] to transform
    * @param targetMediaType Provide a specification for obtaining the correct pipeline.
    *                        Must be <code>"application/spec"</code> or <code>"application/spec+syntax"</code>.
    *                        Examples: <code>"application/raml10"</code> or <code>"application/raml10+yaml"</code>
    * @return An [[AMFResult]] with the transformed BaseUnit and it's validation results
    */
  def transformEditing(bu: BaseUnit, targetMediaType: String): AMFResult =
    _internal.transformEditing(bu, targetMediaType)

  /**
    * Transforms a [[BaseUnit]] with using pipeline with compatibility id.
    * @param bu [[BaseUnit]] to transform
    * @param targetMediaType Provide a specification for obtaining the correct pipeline.
    *                        Must be <code>"application/spec"</code> or <code>"application/spec+syntax"</code>.
    *                        Examples: <code>"application/raml10"</code> or <code>"application/raml10+yaml"</code>
    * @return An [[AMFResult]] with the transformed BaseUnit and it's validation results
    */
  def transformCompatibility(bu: BaseUnit, targetMediaType: String): AMFResult =
    _internal.transformCompatibility(bu, targetMediaType)

  /**
    * Transforms a [[BaseUnit]] with using pipeline with cache id.
    * @param bu [[BaseUnit]] to transform
    * @param targetMediaType Provide a specification for obtaining the correct pipeline.
    *                        Must be <code>"application/spec"</code> or <code>"application/spec+syntax"</code>.
    *                        Examples: <code>"application/raml10"</code> or <code>"application/raml10+yaml"</code>
    * @return An [[AMFResult]] with the transformed BaseUnit and it's validation results
    */
  def transformCache(bu: BaseUnit, targetMediaType: String): AMFResult = _internal.transformCache(bu, targetMediaType)
}
