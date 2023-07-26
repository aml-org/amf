package amf.graphqlfederation.internal.spec.transformation

import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.adoption.{DefaultIdMaker, IdAdopter}
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{AmfObject, DomainElement}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.metamodel.document.DocumentModel
import amf.graphqlfederation.internal.spec.transformation.introspection.IntrospectionTypes._
import amf.graphqlfederation.internal.spec.transformation.introspection.IntrospectionDirectives._
import amf.shapes.client.scala.model.domain._

import scala.collection.mutable

object IntrospectionElementsAdditionStep extends TransformationStep {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = model match {
    case doc: Document => transform(doc, configuration)
    case other         => other
  }

  private def transform(doc: Document, configuration: AMFGraphConfiguration): Document = {
    val existing = doc.declares.toList
    val nextDoc  = addIntrospectionElements(doc)
    adopt(nextDoc, existing, configuration)
  }

  private def addIntrospectionElements(doc: Document): Document = {
    val fieldSet = FieldSet()
    val _any     = _Any()
    val _service = _Service()
    val _entity = {
      retrieveEntities(doc) match {
        case entities if entities.nonEmpty => Some(_Entity(entities))
        case _                             => None
      }

    }
    addTypesToDocument(doc, fieldSet, _any, _service, _entity)
    addExtensionEndpoints(doc, _any, _service, _entity)
  }

  private def addTypesToDocument(
      doc: Document,
      fieldSet: ScalarShape,
      _any: ScalarShape,
      _service: NodeShape,
      _entity: Option[UnionShape]
  ) = {
    val types = {
      _entity match {
        case Some(e) => List(_any, fieldSet, _service, e)
        case _       => List(fieldSet, _service)
      }
    }
    val directives = List(
      `@external`,
      `@requires`(fieldSet),
      `@provides`(fieldSet),
      `@key`(fieldSet),
      `@shareable`,
      `@inaccessible`,
      `@override`
    )
    doc.setArrayWithoutId(DocumentModel.Declares, doc.declares ++ types ++ directives)
  }

  private def addExtensionEndpoints(
      doc: Document,
      _any: ScalarShape,
      _service: NodeShape,
      _entity: Option[UnionShape]
  ) = {
    val endpoints    = _Query(_any, _entity, _service)
    val apiEndpoints = doc.encodes.asInstanceOf[Api].endPoints
    doc.encodes.asInstanceOf[Api].withEndPoints(apiEndpoints ++ endpoints)
    doc
  }

  private def adopt(doc: Document, existing: List[DomainElement], configuration: AMFGraphConfiguration): Document = {
    val entries: List[(String, DomainElement)]  = existing.map(x => x.id -> x)
    val skipped: mutable.Map[String, AmfObject] = mutable.Map(entries: _*)
    // there are hacks to avoid or force adoption on declared introspected custom domain properties.
    // Is ok to ignore indexed adopter? is this a different internal case to generate ids for out of the box declarations?
    new IdAdopter(doc.id, new DefaultIdMaker(), skipped).adoptFromRelative(doc)
    doc
  }

  private def retrieveEntities(doc: Document): Seq[NodeShape] = doc.declares.collect {
    case n: NodeShape if isEntity(n) => n
  }

  private def isEntity(n: NodeShape): Boolean =
    n.keys.nonEmpty && n.keys.exists(_.isResolvable.value()) && !n.isAbstract.value()
}
