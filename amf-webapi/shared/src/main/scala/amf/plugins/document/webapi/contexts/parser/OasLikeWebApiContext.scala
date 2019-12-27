package amf.plugins.document.webapi.contexts.parser

import amf.core.model.document.ExternalFragment
import amf.core.parser.{ParsedReference, ParserContext, YMapOps}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.contexts.{SpecVersionFactory, WebApiContext}
import amf.plugins.document.webapi.parser.spec.OasLikeWebApiDeclarations
import amf.plugins.document.webapi.parser.spec.domain.{
  OasLikeEndpointParser,
  OasLikeOperationParser,
  OasLikeServerVariableParser
}
import amf.plugins.domain.webapi.models.{EndPoint, Operation, Server, WebApi}
import org.yaml.model.{YMap, YMapEntry, YNode, YScalar}

import scala.collection.mutable

trait OasLikeSpecVersionFactory extends SpecVersionFactory {
  def serverVariableParser(entry: YMapEntry, server: Server): OasLikeServerVariableParser
  // TODO ASYNC complete this
  def operationParser(entry: YMapEntry, producer: String => Operation): OasLikeOperationParser
  def endPointParser(entry: YMapEntry,
                     producer: String => EndPoint,
                     collector: mutable.ListBuffer[EndPoint]): OasLikeEndpointParser
}

abstract class OasLikeWebApiContext(loc: String,
                                    refs: Seq[ParsedReference],
                                    private val wrapped: ParserContext,
                                    private val ds: Option[OasLikeWebApiDeclarations] = None,
                                    private val operationIds: mutable.Set[String] = mutable.HashSet())
    extends WebApiContext(loc, refs, wrapped, ds) {

  val factory: OasLikeSpecVersionFactory

  override val declarations: OasLikeWebApiDeclarations =
    ds.getOrElse(
      new OasLikeWebApiDeclarations(
        refs
          .flatMap(
            r =>
              if (r.isExternalFragment)
                r.unit.asInstanceOf[ExternalFragment].encodes.parsed.map(node => r.origin.url -> node)
              else None)
          .toMap,
        None,
        errorHandler = eh,
        futureDeclarations = futureDeclarations
      ))

  override def link(node: YNode): Either[String, YNode] = {
    node.to[YMap] match {
      case Right(map) =>
        val ref: Option[String] = map.key("$ref").flatMap(v => v.value.asOption[YScalar]).map(_.text)
        ref match {
          case Some(url) => Left(url)
          case None      => Right(node)
        }
      case _ => Right(node)
    }
  }

  val linkTypes: Boolean = wrapped match {
    case _: RamlWebApiContext => false
    case _                    => true
  }

  override def ignore(shape: String, property: String): Boolean =
    property.startsWith("x-") || property == "$ref" || (property.startsWith("/") && (shape == "webApi" || shape == "paths"))

  /** Used for accumulating operation ids.
    * returns true if id was not present, and false if operation being added is already present. */
  def registerOperationId(id: String): Boolean = operationIds.add(id)
}
