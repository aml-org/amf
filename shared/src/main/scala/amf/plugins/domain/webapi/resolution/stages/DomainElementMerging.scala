package amf.plugins.domain.webapi.resolution.stages

import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.domain.DomainElementModel._
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.metamodel.{Field, Type}
import amf.core.model.domain._
import amf.core.parser.{FieldEntry, Value}

/**
  * Merge 'other' element into 'main' element:
  * 1) 'main' node properties are inspected and those that are undefined in 'other' node remain unchanged.
  * 2) 'main' node receives all properties of 'other' node (excluding optional ones), which are undefined in the 'main' node.
  * 3) Properties defined in both 'main' node and 'other' node (including optional ones) are treated as follows:
  *   a) Scalar properties remain unchanged.
  *   b) Collection properties are merged by value.
  *   c) Values of object properties are subjected to steps 1-3 of this procedure.
  */
object DomainElementMerging {

  def merge[T <: DomainElement](main: T, other: T): T = {
    other.fields.fields().filter(ignored).foreach {
      case entry@FieldEntry(field, value) =>
        main.fields.entry(field) match {
          case None => // Case (2)
            field.`type` match {
              case t: OptionalField if isOptional(t, value.value.asInstanceOf[DomainElement]) => // Do nothing (2)
              case Type.ArrayLike(element) => setNonOptional(main, field, element, value)
              case _ => main.set(field, adoptInner(main.id, value.value))
            }
          case Some(existing) => // Case (3)
            field.`type` match {
              case _: Type.Scalar => // Do nothing (3.a)
              case Type.ArrayLike(element) => mergeByValue(main, field, element, existing.value, value)
              case _: DomainElementModel => merge(existing.domainElement, entry.domainElement)
              case _ => throw new Exception(s"Cannot merge '${field.`type`}':not a (Scalar|Array|Object)")
            }
        }
    }
    main
  }

  def adoptInner(id: String, target: AmfElement): AmfElement = target match {
    case array: AmfArray =>
      AmfArray(array.values.map(adoptInner(id, _)), array.annotations)
    case element: DomainElement =>
      element.adopted(id)

      element.fields.foreach {
        case (_, value) => adoptInner(element.id, value.value)
      }

      element
    case _ => target
  }

  private def setNonOptional(target: DomainElement, field: Field, element: Type, other: Value): Unit = {
    element match {
      case t: OptionalField =>
        val nonOptional =
          other.value.asInstanceOf[AmfArray].values.filter(v => !isOptional(t, v.asInstanceOf[DomainElement]))
        target.set(field, adoptInner(target.id, AmfArray(nonOptional)))
      case _ => target.set(field, adoptInner(target.id, other.value))
    }
  }

  private def mergeByValue(target: DomainElement, field: Field, element: Type, main: Value, other: Value): Unit = {
    val m = main.value.asInstanceOf[AmfArray]
    val o = other.value.asInstanceOf[AmfArray]

    element match {
      case _: Type.Scalar => mergeByValue(target, field, m, o)
      case key: KeyField  => mergeByKeyValue(target, field, element, key, m, o)
      case _              => throw new Exception(s"Cannot merge '$element': not a KeyField nor a Scalar")
    }
  }

  private def mergeByValue(target: DomainElement, field: Field, main: AmfArray, other: AmfArray): Unit = {
    val existing = main.values.map(_.asInstanceOf[AmfScalar].value).toSet
    other.values.foreach { value =>
      val scalar = value.asInstanceOf[AmfScalar].value
      if (!existing.contains(scalar)) {
        target.add(field, AmfScalar(scalar)) // Remove annotations so it is added last in the list.
      }
    }
  }

  private def mergeByKeyValue(target: DomainElement,
                              field: Field,
                              element: Type,
                              key: KeyField,
                              main: AmfArray,
                              other: AmfArray): Unit = {

    val existing = main.values.flatMap { m =>
      val obj = m.asInstanceOf[DomainElement]
      obj.fields.entry(key.key).map(_.scalar.value -> obj)
    }.toMap // TODO value without key?

    other.values.foreach { o =>
      val obj = o.asInstanceOf[DomainElement]
      obj.fields.entry(key.key) match {
        case Some(value) =>
          if (existing.contains(value.scalar.value)) {
            merge(existing(value.scalar.value), obj.adopted(target.id))
          } else if (!isOptional(element, obj)) { // Case (2) -> If node is undefined in 'main' but is optional in 'other'.
            target.add(field, adoptInner(target.id, o))
          }
        case _ =>
      }
    }
  }

  private def isOptional(`type`: Type, obj: DomainElement) =
    `type`.isInstanceOf[OptionalField] && obj.fields
      .entry(`type`.asInstanceOf[OptionalField].optional)
      .exists(_.scalar.toBool)

  private def ignored(entry: FieldEntry) = entry.field match {
    case Extends | Includes | Sources => false
    case _                            => true
  }
}

/** Merge two data nodes of the same type. This merging applies the 'other' side as an overlay to the 'main' side. */
object DataNodeMerging {

  def merge(existing: DataNode, overlay: DataNode): Unit = {
    (existing, overlay) match {
      case (left: ScalarNode, right: ScalarNode) =>
        left.value = right.value
        left.dataType = right.dataType
      case (left: ObjectNode, right: ObjectNode) =>
        mergeObjectNode(left, right)
      case (left: ArrayNode, right: ArrayNode) =>
        // Add members that are not in the left array.
        mergeArrayNode(left, right)
      case _ =>
    }
  }

  def mergeObjectNode(left: ObjectNode, right: ObjectNode): Unit =
    for { (key, value) <- right.properties } {
      left.properties.get(key) match {
        case Some(property) => merge(property, value)
        case None           => left.addProperty(key, adoptInner(left.id, value), right.propertyAnnotations(key))
      }
    }

  /** Merge array data nodes by value: If scalar, check it's not there and add. If object or array, just add but adoptInner ids. */
  private def mergeArrayNode(main: ArrayNode, other: ArrayNode): Unit = {
    val existing = main.members.collect { case s: ScalarNode => s.value }

    other.members.foreach {
      case scalar: ScalarNode =>
        if (!existing.contains(scalar.value)) main.addMember(scalar)
      case node =>
        main.addMember(adoptInner(main.id, node))
    }
  }

  def adoptInner(id: String, target: DataNode): DataNode = {
    target.forceAdopted(id)
    target match {
      case array: ArrayNode =>
        array.members.foreach(adoptInner(array.id, _))
      case obj: ObjectNode =>
        obj.properties.values.foreach(adoptInner(obj.id, _))
      case _ =>
    }
    target
  }
}
