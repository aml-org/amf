package amf.model

import java.net.URL

import scala.collection.JavaConverters._

/**
  * Document jvm class
  */
case class Document(private[amf] val document: amf.document.Document)
    extends BaseUnit
    with DeclaresModel
    with EncodesModel {

  /**
    * list of references to other [[DomainElement]]s
    */
  override val references: java.util.List[BaseUnit] =
    document.references
      .map(d => amf.model.Document(d.asInstanceOf[amf.document.Document]).asInstanceOf[BaseUnit])
      .asJava

  /**
    * Uri that identifies the document
    */
  val location: URL = new URL(document.location)

  /**
    * The parsing Unit that it's encoded for this [[Document]]
    */
  val encodes: DomainElement = WebApi(document.encodes)

  /**
    *
    */
  val declares: java.util.List[amf.domain.DomainElement] = document.declares.asJava

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override def unit: amf.document.BaseUnit = document
}
