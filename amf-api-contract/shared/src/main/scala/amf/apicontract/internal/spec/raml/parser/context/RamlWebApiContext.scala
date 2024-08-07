package amf.apicontract.internal.spec.raml.parser.context

import amf.apicontract.internal.spec.common.RamlWebApiDeclarations
import amf.apicontract.internal.spec.common.parser.{ParsingHelpers, WebApiContext}
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.domain.{AmfObject, Shape}
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.plugins.syntax.SYamlAMFParserErrorHandler
import amf.core.internal.remote.Spec
import amf.core.internal.validation.CoreValidations.DeclarationNotFound
import amf.shapes.internal.spec.raml.parser.RamlWebApiContextType.{DEFAULT, RamlWebApiContextType}
import amf.shapes.internal.spec.common.parser.{IgnoreCriteria, SpecSettings, SpecSyntax}
import amf.shapes.internal.spec.raml.parser.{Raml08Settings, Raml10Settings, RamlWebApiContextType}
import org.yaml.model._

import scala.collection.mutable

abstract class RamlWebApiContext(
    override val loc: String,
    refs: Seq[ParsedReference],
    options: ParsingOptions,
    val wrapped: ParserContext,
    private val ds: Option[RamlWebApiDeclarations] = None,
    val specSettings: SpecSettings
) extends WebApiContext(loc, refs, options, wrapped, ds, specSettings = specSettings)
    with RamlSpecAwareContext {

  var globalMediatype: Boolean                                     = false
  val operationContexts: mutable.Map[AmfObject, RamlWebApiContext] = mutable.Map()

  def contextType: RamlWebApiContextType = specSettings.ramlContextType.getOrElse(DEFAULT)
  def setContextType(contextType: RamlWebApiContextType) = specSettings match {
    case settings: Raml10Settings => settings.setRamlContextType(contextType)
    case settings: Raml08Settings => settings.setRamlContextType(contextType)
    case _                        => // ignore
  }

  def mergeOperationContext(operation: AmfObject): Unit = {
    val contextOption = operationContexts.get(operation)
    contextOption.foreach(mergeContext)
    operationContexts.remove(operation)
  }

  def mergeAllOperationContexts(): Unit = {
    operationContexts.values.foreach(mergeContext)
    operationContexts.keys.foreach(operationContexts.remove)
  }
  def mergeContext(subContext: RamlWebApiContext): Unit = {
    declarations.absorb(subContext.declarations)
    subContext.declarations.futureDeclarations.promises.foreach(fd => declarations.futureDeclarations.promises += fd)
    subContext.futureDeclarations.promises.foreach(fd => futureDeclarations.promises += fd)
  }

  override val declarations: RamlWebApiDeclarations =
    ds.getOrElse(new RamlWebApiDeclarations(alias = None, futureDeclarations = futureDeclarations, errorHandler = eh))
  protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext

  /** Adapt this context for a nested library, used when evaluating resource type / traits Using a path to the library
    * whose context is going to be looked up, e.g. lib.TypeA
    */
  def adapt[T](path: String)(k: RamlWebApiContext => T): T = {
    val pathElements        = path.split("\\.").dropRight(1)
    val adaptedDeclarations = findDeclarations(pathElements, declarations)
    k(clone(declarations.merge(adaptedDeclarations)))
  }

  protected def findDeclarations(path: Seq[String], declarations: RamlWebApiDeclarations): RamlWebApiDeclarations = {
    if (path.isEmpty) {
      declarations
    } else {
      val nextLibrary = path.head
      declarations.libraries.get(nextLibrary) match {
        case Some(library: RamlWebApiDeclarations) =>
          findDeclarations(path.tail, library)
        case _ =>
          violation(DeclarationNotFound, "", s"Cannot find declarations in context '${path.mkString(".")}")
          declarations
      }
    }
  }

  val factory: RamlSpecVersionFactory

  override def autoGeneratedAnnotation(s: Shape): Unit = ParsingHelpers.ramlAutoGeneratedAnnotation(s)
}

class PayloadContext(
    loc: String,
    refs: Seq[ParsedReference],
    override val wrapped: ParserContext,
    private val ds: Option[RamlWebApiDeclarations] = None,
    contextType: RamlWebApiContextType = RamlWebApiContextType.DEFAULT,
    options: ParsingOptions = ParsingOptions()
) extends RamlWebApiContext(
      loc,
      refs,
      options,
      wrapped,
      ds,
      new Raml10Settings(Raml10Syntax, contextType)
    ) {
  override protected def clone(declarations: RamlWebApiDeclarations): RamlWebApiContext = {
    new PayloadContext(loc, refs, wrapped, Some(declarations), options = options)
  }
  override val factory: RamlSpecVersionFactory = new Raml10VersionFactory()(this)
  override def syntax: SpecSyntax = new SpecSyntax {
    override val nodes: Map[String, Set[String]] = Map()
  }
  override def spec: Spec = Spec.PAYLOAD
}
