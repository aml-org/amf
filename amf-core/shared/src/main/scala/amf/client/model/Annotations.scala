package amf.client.model

import amf.client.convert.CoreClientConverters._
import amf.client.model.domain.DomainExtension
import amf.core.annotations._
import amf.core.parser.{Range, Annotations => InternalAnnotations}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class Annotations(_internal: InternalAnnotations) {

  @JSExportTopLevel("model.Annotations")
  def this() = this(InternalAnnotations())

  def lexical(): Range = _internal.find(classOf[LexicalInformation]).map(_.range).getOrElse(Range.NONE)

  def custom(): ClientList[DomainExtension] =
    _internal.collect({ case d: DomainExtensionAnnotation => d }).map(_.extension).asClient

  def fragmentName(): ClientOption[String] = _internal.find(classOf[ExternalFragmentRef]).map(_.fragment).asClient

  def location(): ClientOption[String] = _internal.find(classOf[SourceLocation]).map(_.location).asClient

  def isLocal: Boolean = _internal.find(classOf[LocalElement]).isDefined
}
