package amf.plugins.domain.shapes.resolution.stages.merge

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{DataNodeModel, ShapeModel}
import amf.core.model.domain.{AmfArray, AmfElement, AmfObject, AmfScalar}
import amf.plugins.domain.webapi.models.{Key, Payload}

object JsonMergePatch {

  def apply(isNull: AmfElement => Boolean, keyCriteria: KeyCriteria): JsonMergePatch =
    new JsonMergePatch(isNull, keyCriteria)
}

class JsonMergePatch(val isNull: AmfElement => Boolean, val keyCriteria: KeyCriteria) {

  def merge[T <: AmfElement](target: T, patch: T): AmfElement = {
    (target, patch) match {
      case (targetObject: AmfObject, patchObject: AmfObject) => mergeObjects(targetObject, patchObject)
      case (targetArray: AmfArray, patchArray: AmfArray)     => mergeObjectLikeArrays(targetArray, patchArray)
      case (_, _)                                            => patch
    }
  }

  private def mergeObjectLikeArrays(target: AmfArray, patch: AmfArray) = {
    if (isObjectLikeArray(target) && isObjectLikeArray(patch)) {
      val mergedArray = mergeArrays(patch, target)
      AmfArray(mergedArray)
    } else patch
  }

  private def mergeArrays(patch: AmfArray, target: AmfArray) = {
    val targetMap = getTargetMap(target)
    patch.values
      .foldLeft(targetMap) { (acc, p) =>
        val targetElement = acc.getOrElse(keyCriteria.getKeyFor(p).get, bogusTarget)
        acc + (keyCriteria.getKeyFor(p).get -> merge(targetElement, p))
      }
      .values
      .toSeq
  }

  private def getTargetMap(target: AmfArray): Map[String, AmfElement] =
    target.values
      .map(t => (keyCriteria.getKeyFor(t).get, t))
      .toMap[String, AmfElement]

  private def bogusTarget = AmfScalar(0)

  private def mergeObjects(target: AmfObject, patch: AmfObject) = {
    patch.fields.foreach {
      case (field, fieldValue) =>
        val element = fieldValue.value
        if (isNull(element)) target.fields.removeField(field)
        else if (skipRecursiveMerge(field)) target.set(field, fieldValue.value)
        else {
          val nextValue = merge(target.fields.get(field), element)
          target.set(field, nextValue)
        }
    }
    target
  }

  private def skipRecursiveMerge(field: Field) =
    field.`type`.isInstanceOf[ShapeModel] || field.`type`.equals(DataNodeModel)

  def isObjectLikeArray(array: AmfArray): Boolean = array.values.forall(keyCriteria.getKeyFor(_).isDefined)
}

trait KeyCriteria {
  def getKeyFor(element: AmfElement): Option[String]
}

case class DefaultKeyCriteria() extends KeyCriteria {

  override def getKeyFor(element: AmfElement): Option[String] = element match {
    case withKey: Key => withKey.key.option()
    case _            => None
  }
}

case class AsyncKeyCriteria() extends KeyCriteria {

  override def getKeyFor(element: AmfElement): Option[String] = element match {
    case _: Payload   => Option("sameKey")
    case withKey: Key => withKey.key.option()
    case _            => None
  }
}
