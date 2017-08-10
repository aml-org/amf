package amf.model

import java.util

import amf.domain

import scala.collection.JavaConverters._

/**
  * Document jvm class
  */
case class Document(private[amf] val document: amf.document.BaseUnit)
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
  val location: String = document.location

  /**
    * The parsing Unit that it's encoded for this [[Document]]
    */
  val encodes: WebApi =
    document match {
      case d: amf.document.Document => WebApi(d.encodes)
      case _                        => null
    }

  /**
    *
    */
  val declares: java.util.List[amf.domain.DomainElement] =
    document match {
      case d: amf.document.Document => d.declares.asJava
      case _                        => new util.ArrayList[domain.DomainElement]()
    }

  /** Returns the list document URIs referenced from the document that has been parsed to generate this model */
  override def unit: amf.document.BaseUnit = document
}
