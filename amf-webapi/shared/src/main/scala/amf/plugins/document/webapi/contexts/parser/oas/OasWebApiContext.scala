package amf.plugins.document.webapi.contexts.parser.oas
import amf.plugins.document.webapi.parser.spec.OasWebApiDeclarations

import scala.collection.mutable
import org.yaml.model.{YMap, YScalar, YNode}
import amf.core.model.document.ExternalFragment
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.core.parser.{ErrorHandler, ParserContext, ParsedReference}
import amf.plugins.document.webapi.contexts.parser.raml.RamlWebApiContext
import amf.core.parser.YMapOps

abstract class OasWebApiContext(loc: String,
                                refs: Seq[ParsedReference],
                                private val wrapped: ParserContext,
                                private val ds: Option[OasWebApiDeclarations] = None,
                                parserCount: Option[Int] = None,
                                override val eh: Option[ErrorHandler] = None,
                                private val operationIds: mutable.Set[String] = mutable.HashSet())
  extends WebApiContext(loc, refs, wrapped, ds, parserCount, eh) {

  override val declarations: OasWebApiDeclarations =
    ds.getOrElse(
      new OasWebApiDeclarations(
        refs
          .flatMap(
            r =>
              if (r.isExternalFragment)
                r.unit.asInstanceOf[ExternalFragment].encodes.parsed.map(node => r.origin.url -> node)
              else None)
          .toMap,
        None,
        errorHandler = Some(this),
        futureDeclarations = futureDeclarations
      ))
  val factory: OasSpecVersionFactory

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
