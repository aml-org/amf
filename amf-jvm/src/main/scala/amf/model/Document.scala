package amf.model

import java.util

import amf.domain

import scala.collection.JavaConverters._

/**
  * JVM Document model class.
  */
case class Document(private[amf] val document: amf.document.BaseUnit)
    extends BaseUnit
    with DeclaresModel
    with EncodesModel {

  def this() = this(amf.document.Document())

  def this(webApi: WebApi) = this(amf.document.Document().withEncodes(webApi.element))

  /** List of references to other [[DomainElement]]s. */
  override val references: java.util.List[BaseUnit] =
    document.references
      .map(d => amf.model.Document(d.asInstanceOf[amf.document.Document]).asInstanceOf[BaseUnit])
      .asJava

  /** Uri that identifies the document. */
  override val location: String = document.location

  /** Encoded [[DomainElement]] described in the document element. */
  val encodes: WebApi = Option(document)
    .filter(_.isInstanceOf[amf.document.Document])
    .map(d => WebApi(d.asInstanceOf[amf.document.Document].encodes))
    .orNull

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  val declares: java.util.List[amf.domain.DomainElement] =
    document match {
      case d: amf.document.Document => d.declares.asJava
      case _                        => new util.ArrayList[domain.DomainElement]()
    }

  override def unit: amf.document.BaseUnit = document
}
