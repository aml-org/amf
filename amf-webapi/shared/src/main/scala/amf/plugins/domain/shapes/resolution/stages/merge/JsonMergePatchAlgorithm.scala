package amf.plugins.domain.shapes.resolution.stages.merge

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{DataNodeModel, ShapeModel}
import amf.core.model.domain.{AmfArray, AmfElement, AmfObject, AmfScalar}
import amf.core.parser.FieldEntry
import amf.plugins.domain.webapi.models.{Key, Payload}

case class JsonMergePatch(isNull: AmfElement => Boolean, keyCriteria: KeyCriteria, ignoredFields: Seq[Field] = Seq()) {

  def merge[T <: AmfElement](target: T, patch: T): AmfElement = {
    (target, patch) match {
      case (targetObject: AmfObject, patchObject: AmfObject) => mergeObjects(targetObject, patchObject)
      case (targetArray: AmfArray, patchArray: AmfArray)     => mergeObjectLikeArrays(targetArray, patchArray)
      case (_, _)                                            => patch
    }
  }

  private def mergeObjectLikeArrays(target: AmfArray, patch: AmfArray) = {
    if (keyCriteria.hasKeysForAll(target) && keyCriteria.hasKeysForAll(patch)) {
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
    patch.fields
      .fields()
      .filter(entry => !ignoredFields.contains(entry.field))
      .foreach {
        case FieldEntry(field, fieldValue) =>
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
}

trait KeyCriteria {
  def getKeyFor(element: AmfElement): Option[String]
  def hasKey(element: AmfElement): Boolean    = getKeyFor(element).isDefined
  def hasKeysForAll(array: AmfArray): Boolean = array.values.forall(hasKey)
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
