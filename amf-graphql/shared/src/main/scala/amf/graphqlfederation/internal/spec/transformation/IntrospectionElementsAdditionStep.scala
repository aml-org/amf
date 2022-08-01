package amf.graphqlfederation.internal.spec.transformation

import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{AmfObject, DomainElement}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.adoption.IdAdopter
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
    case doc: Document => transform(doc)
    case other         => other
  }

  private def transform(doc: Document): Document = {
    val existing = doc.declares.toList
    val nextDoc  = addIntrospectionElements(doc)
    adopt(nextDoc, existing)
  }

  private def addIntrospectionElements(doc: Document): Document = {
    val fieldSet = _FieldSet()
    val _any     = _Any()
    val _service = _Service()
    val _entity  = _Entity(assumeNodeShapes(doc))
    addTypesToDocument(doc, fieldSet, _any, _service, _entity)
    addExtensionEndpoints(doc, _any, _service, _entity)
  }

  private def addTypesToDocument(
      doc: Document,
      fieldSet: ScalarShape,
      _any: ScalarShape,
      _service: NodeShape,
      _entity: UnionShape
  ) = {
    val types      = List(_any, fieldSet, _service, _entity)
    val directives = List(`@external`(), `@requires`(fieldSet), `@provides`(fieldSet), `@key`(fieldSet))
    doc.setArrayWithoutId(DocumentModel.Declares, doc.declares ++ types ++ directives)
  }

  private def addExtensionEndpoints(doc: Document, _any: ScalarShape, _service: NodeShape, _entity: UnionShape) = {
    val endpoints    = _Query(_any, _entity, _service)
    val apiEndpoints = doc.encodes.asInstanceOf[Api].endPoints
    doc.encodes.asInstanceOf[Api].withEndPoints(apiEndpoints ++ endpoints)
    doc
  }

  private def adopt(doc: Document, existing: List[DomainElement]): Document = {
    val entries: List[(String, DomainElement)]  = existing.map(x => x.id -> x)
    val skipped: mutable.Map[String, AmfObject] = mutable.Map(entries: _*)
    new IdAdopter(doc, doc.id, skipped).adoptFromRelative()
    doc
  }

  private def assumeNodeShapes(doc: Document): Seq[NodeShape] = doc.declares.collect { case n: NodeShape => n }
}
