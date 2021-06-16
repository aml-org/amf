package amf.apicontract.internal.transformation.compatibility.raml

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject, RecursiveShape, Shape}
import amf.core.client.scala.transform.stages.TransformationStep

import scala.collection.mutable.ListBuffer

class RecursionDetection() extends TransformationStep {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    model match {
      case doc: Document => doc.fields.fields().foreach(f => advance(f.element, ListBuffer[String](), Set.empty))
      case _             => // Nothing
    }
    model
  }

  private def advance(element: AmfElement, general: ListBuffer[String], branch: Set[String]): AmfElement =
    element match {
      case rec: RecursiveShape => rec
      case obj: AmfObject =>
        val id = obj.id
        if (branch.contains(id))
          RecursiveShape(obj.asInstanceOf[Shape]) // If the ID already exists in the branch, then is a recursion
        else if (general.contains(id)) obj // If the ID already was visited, then don't need to do it again
        else {
          val branchIds = if (isValidShape(obj)) {
            general += id
            branch + id
          } else branch
          obj.fields
            .fields()
            .foreach(f => {
              advance(f.element, general, branchIds) match {
                case r: RecursiveShape => obj.fields.setWithoutId(f.field, r)
                case a: AmfArray       => obj.fields.setWithoutId(f.field, a)
                case _                 => // Nothing to do, the field remains the same
              }
            })
          obj
        }
      case array: AmfArray =>
        val elements = array.values.map(advance(_, general, branch))
        if (elements.exists(_.isInstanceOf[RecursiveShape])) AmfArray(elements, array.annotations)
        else array
      case other => other
    }

  // The recursion detection should be on meaning shapes (not in PropertyShape, but in its range; not in ArrayShape like, but in its items)
  private def isValidShape(obj: AmfObject): Boolean =
    obj.isInstanceOf[Shape] && !obj.isInstanceOf[PropertyShape] && !obj.isInstanceOf[DataArrangementShape]

}
