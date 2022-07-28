package amf.graphqlfederation.internal.spec.transformation

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{AmfObject, DomainElement}
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import amf.core.client.scala.transform.TransformationStep
import amf.core.internal.adoption.IdAdopter
import amf.core.internal.annotations.Declares
import amf.core.internal.metamodel.document.DocumentModel
import amf.graphql.internal.spec.parser.syntax.Locations
import amf.graphql.internal.spec.parser.syntax.Locations.domainFor
import amf.graphqlfederation.internal.spec.transformation.IntrospectionElements._
import amf.shapes.client.scala.model.domain.{AnyShape, NilShape, NodeShape, ScalarShape, UnionShape}

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
    val entitiesToAdd = getIntrospectionElements(doc)
    val existing      = Option(doc.encodes).toList ::: doc.declares.toList
    doc.withDeclares(doc.declares.toList ::: entitiesToAdd)
    adopt(doc, existing)
  }

  private def getIntrospectionElements(doc: Document): List[DomainElement] = {
    val fieldSet = _FieldSet()

    val types      = List(_Any(), fieldSet, _Service(), _Entity(assumeNodeShapes(doc)))
    val directives = List(`@external`(), `@requires`(fieldSet), `@provides`(fieldSet), `@key`(fieldSet))
    types ++ directives
  }

  private def adopt(doc: Document, existing: List[DomainElement]): Document = {
    val entries: List[(String, DomainElement)]  = existing.map(x => x.id -> x)
    val skipped: mutable.Map[String, AmfObject] = mutable.Map(entries: _*)
    new IdAdopter(doc, doc.id, skipped).adoptFromRelative()
    doc
  }

  private def assumeNodeShapes(doc: Document): Seq[NodeShape] = doc.declares.collect { case n: NodeShape => n }
}

object IntrospectionElements {

  private val FIELD_DEFINITION = "FIELD_DEFINITION"
  private val SCHEMA           = "SCHEMA"
  private val OBJECT           = "OBJECT"
  private val INTERFACE        = "INTERFACE"

  def _Any(): ScalarShape = {
    ScalarShape()
      .withName("_Any")
      .withFormat("_Any")
      .withDataType(DataTypes.String)
  }

  def _FieldSet(): ScalarShape = {
    ScalarShape()
      .withName("_FieldSet")
      .withFormat("_FieldSet")
      .withDataType(DataTypes.String)
  }

  def _Service(): NodeShape = {
    NodeShape()
      .withName("_Service")
      .withProperties(
        List(
          PropertyShape()
            .withName("sdl")
            .withRange(
              ScalarShape()
                .withDataType(DataTypes.String)
            )
        )
      )
  }

  def _Entity(types: Seq[NodeShape]): UnionShape = {
    val typesWithKey = types.filter(_.keys.nonEmpty)
    UnionShape()
      .withName("_Entity")
      .withAnyOf(typesWithKey)
  }

  def `@external`(): CustomDomainProperty = {
    CustomDomainProperty()
      .withName("external")
      .withSchema(NodeShape())
      .withDomain(domainFor(SCHEMA))
  }

  def `@requires`(fieldSet: ScalarShape): CustomDomainProperty = {
    CustomDomainProperty()
      .withName("requires")
      .withSchema(nullable(fieldSetArgument(fieldSet)))
      .withDomain(domainFor(FIELD_DEFINITION))
  }

  def `@provides`(fieldSet: ScalarShape): CustomDomainProperty = {
    CustomDomainProperty()
      .withName("provides")
      .withSchema(nullable(fieldSetArgument(fieldSet)))
      .withDomain(domainFor(FIELD_DEFINITION))
  }

  def `@key`(fieldSet: ScalarShape): CustomDomainProperty = {
    // TODO: 'repeatable is not modeled'
    CustomDomainProperty()
      .withName("key")
      .withSchema(nullable(fieldSetArgument(fieldSet)))
      .withDomain(domainFor(OBJECT, INTERFACE))
  }

  private def fieldSetArgument(fieldSet: ScalarShape): NodeShape = {
    NodeShape()
      .withProperties(
        List(
          PropertyShape()
            .withName("fieldSet")
            .withRange(fieldSet)
        )
      )
  }

  private def nullable(shape: NodeShape): UnionShape = {
    UnionShape()
      .withAnyOf(
        List(
          shape,
          NilShape()
        )
      )
  }
}
