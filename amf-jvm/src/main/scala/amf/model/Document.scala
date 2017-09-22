package amf.model

import java.util

import amf.domain
import amf.spec.dialects.DomainEntity

import scala.collection.JavaConverters._

/**
  * JVM Document model class.
  */
case class Document(private[amf] val document: amf.document.BaseUnit)
    extends BaseUnit
    with DeclaresModel
    with EncodesModel {

  def this() = this(amf.document.Document())

  def this(webApi: DomainElement) = this(amf.document.Document().withEncodes(webApi.element))

  /** List of references to other [[DomainElement]]s. */
  override val references: java.util.List[BaseUnit] =
    document.references
      .map(d => amf.model.Document(d.asInstanceOf[amf.document.Document]).asInstanceOf[BaseUnit])
      .asJava

  /** Uri that identifies the document. */
  override val location: String = document.location

  /** Encoded [[DomainElement]] described in the document element. */
  val encodes: DomainElement = Option(document)
    .filter(_.isInstanceOf[amf.document.Document])
    .map({
      case doc: amf.document.Document => {
        doc.encodes match {
          case webapi: amf.domain.WebApi => WebApi(webapi)
          // case domain: DomainEntity        => new amf.dialects.DialectEntityModel(domain)

          case _ => throw new Exception("Only WebAPI and vocabularies supported at the moment")
        }
      }
      case _ => throw new Exception("Only documents supported at the moment")
    }).get

  /** Declared [[DomainElement]]s that can be re-used from other documents. */
  val declares: java.util.List[amf.domain.DomainElement] =
    document match {
      case d: amf.document.Document => d.declares.asJava
      case _                        => new util.ArrayList[domain.DomainElement]()
    }

  override def unit: amf.document.BaseUnit = document

  override def usage: String = document.usage
}
