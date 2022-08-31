package amf.shapes.internal.spec.common.parser

import amf.aml.client.scala.model.document.Dialect
import amf.aml.internal.registries.AMLRegistry
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.datanode.DataNodeParserContext
import amf.core.internal.parser.domain.{DotQualifiedNameExtractor, FragmentRef, SearchScope}
import org.yaml.model.YNode

import scala.collection.mutable

abstract class ExtensionsContext(
    val loc: String,
    refs: Seq[ParsedReference],
    val options: ParsingOptions,
    wrapped: ParserContext,
    val declarationsOption: Option[ShapeDeclarations] = None,
    val nodeRefIds: mutable.Map[YNode, String] = mutable.Map.empty
) extends ParserContext(loc, refs, wrapped.futureDeclarations, wrapped.config)
    with DataNodeParserContext {

  protected def computeExtensions: Map[String, Dialect] = wrapped.config.registryContext.getRegistry match {
    case amlRegistry: AMLRegistry => amlRegistry.getExtensionRegistry
    case _                        => Map.empty
  }

  val declarations: ShapeDeclarations = declarationsOption.getOrElse(
    ShapeDeclarations.empty(eh, futureDeclarations, DotQualifiedNameExtractor).withExtensions(computeExtensions)
  )

  override def findAnnotation(key: String, scope: SearchScope.Scope): Option[CustomDomainProperty] =
    declarations.findAnnotation(key, scope)

  override def getMaxYamlReferences: Option[Int] = options.getMaxYamlReferences

  override def fragments: Map[String, FragmentRef] = declarations.fragments
}
