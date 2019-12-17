package amf.plugins.document.webapi.contexts.parser

import amf.core.parser.{YMapOps, ErrorHandler, ParserContext, ParsedReference}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.plugins.document.webapi.contexts.{WebApiContext, SpecVersionFactory}
import amf.plugins.document.webapi.parser.spec.OasLikeWebApiDeclarations
import amf.plugins.document.webapi.parser.spec.domain.OasLikeServerVariableParser
import amf.plugins.domain.webapi.models.Server
import org.yaml.model.{YMap, YScalar, YNode, YMapEntry}

import scala.collection.mutable

trait OasLikeSpecVersionFactory extends SpecVersionFactory {
  def serverVariableParser(entry: YMapEntry, server: Server): OasLikeServerVariableParser
}

abstract class OasLikeWebApiContext(loc: String,
                                    refs: Seq[ParsedReference],
                                    private val wrapped: ParserContext,
                                    private val ds: Option[OasLikeWebApiDeclarations] = None,
                                    parserCount: Option[Int] = None,
                                    override val eh: Option[ErrorHandler] = None,
                                    private val operationIds: mutable.Set[String] = mutable.HashSet())
    extends WebApiContext(loc, refs, wrapped, ds, parserCount, eh) {

  val factory: OasLikeSpecVersionFactory

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
