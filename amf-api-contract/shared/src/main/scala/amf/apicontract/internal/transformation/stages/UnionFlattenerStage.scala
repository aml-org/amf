package amf.apicontract.internal.transformation.stages

import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, FieldsFilter}
import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.transform.TransformationStep
import amf.shapes.client.scala.model.domain.UnionShape

class UnionFlattenerStage extends TransformationStep() {
  override def transform(
      model: BaseUnit,
      errorHandler: AMFErrorHandler,
      configuration: AMFGraphConfiguration
  ): BaseUnit = {
    model.iterator(fieldsFilter = FieldsFilter.All).foreach {
      case u: UnionShape =>
        val newMembers = flattenedMembers(members = u.anyOf, Seq(u))
        u.withAnyOf(newMembers)
      case _ => // ignore
    }
    model
  }

  private def flattenedMembers(members: Seq[Shape], visited: Seq[UnionShape]): Seq[Shape] = {
    members.flatMap {
      case u: UnionShape if visited.contains(u) =>
        Nil
      case u: UnionShape =>
        flattenedMembers(u.anyOf, visited :+ u)
      case other =>
        Seq(other)
    }.distinct
  }
}
