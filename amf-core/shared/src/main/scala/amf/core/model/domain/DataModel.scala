package amf.core.model.domain

import amf.core.annotations.ScalarType
import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.DataNodeModel
import amf.core.metamodel.domain.DataNodeModel._
import amf.core.model.domain.templates.Variable
import amf.core.parser.{Annotations, Fields}
import amf.core.resolution.VariableReplacer
import amf.core.utils._
import amf.core.vocabulary.{Namespace, ValueType}
import org.yaml.model.{YPart, YSequence}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Base class for all dynamic DataNodes
  */
abstract class DataNode(annotations: Annotations) extends DynamicDomainElement {

  /** Replace all raml variables (any name inside double chevrons -> '<<>>') with the provided values. */
  def replaceVariables(values: Set[Variable]): Unit

  def name: String = fields(Name)

  def withName(name: String): this.type = set(Name, name)

  override def adopted(parent: String): this.type =
    if (Option(this.id).isEmpty) withId(parent + "/" + name.urlEncoded) else this

  def forceAdopted(parent: String): this.type = withId(parent + "/" + name.urlEncoded)

  override val fields: Fields = Fields()

  def cloneNode(): this.type

  override def meta = DataNodeModel
}

/**
  * Data records, with a list of properties
  */
class ObjectNode(override val fields: Fields, val annotations: Annotations) extends DataNode(annotations) {

  val properties: mutable.Map[String, DataNode]             = mutable.HashMap()
  val propertyAnnotations: mutable.Map[String, Annotations] = mutable.HashMap()

  def addProperty(propertyOrUri: String, objectValue: DataNode, annotations: Annotations = Annotations()): this.type = {
    val property = ensurePlainProperty(propertyOrUri)
    objectValue.adopted(this.id)

    properties += property -> objectValue
    propertyAnnotations.update(property, annotations)
    this
  }

  protected def ensurePlainProperty(propertyOrUri: String): String =
    if (propertyOrUri.indexOf(Namespace.Data.base) == 0) {
      propertyOrUri.replace(Namespace.Data.base, "")
    } else {
      propertyOrUri
    }

  override def dynamicFields: List[Field] =
    this.properties.keys
      .map({ p =>
        Field(DataNodeModel, Namespace.Data + p)
      })
      .toList ++ DataNodeModel.fields

  override def dynamicType = List(ObjectNode.builderType)

  override def valueForField(f: Field): Option[AmfElement] = f.value.ns match {
    case Namespace.Data => properties.get(f.value.name)
    case _              => None //this or fields.get(f)
  }

  override def replaceVariables(values: Set[Variable]): Unit = {
    properties.keys.foreach { key =>
      val value = properties(key)
      properties.remove(key)
      value.replaceVariables(values)
      properties += VariableReplacer.replaceVariables(key, values) -> value
    }

    propertyAnnotations.keys.foreach { key =>
      val value = propertyAnnotations(key)
      propertyAnnotations.remove(key)
      propertyAnnotations += VariableReplacer.replaceVariables(key, values) -> value
    }
  }

  override def cloneNode(): this.type = {
    val cloned = ObjectNode(annotations)

    properties.foreach {
      case (property: String, l: DataNode) =>
        cloned.properties += property          -> l.cloneNode()
        cloned.propertyAnnotations += property -> propertyAnnotations(property)
    }

    cloned.asInstanceOf[this.type]
  }

}

object ObjectNode {

  val builderType: ValueType = Namespace.Data + "Object"

  def apply(): ObjectNode = apply(Annotations())

  def apply(ast: YPart): ObjectNode = apply(Annotations(ast))

  def apply(annotations: Annotations): ObjectNode = new ObjectNode(Fields(), annotations)

}

/**
  * Scalar values with associated data type
  */
class ScalarNode(var value: String,
                 var dataType: Option[String],
                 override val fields: Fields,
                 val annotations: Annotations)
    extends DataNode(annotations) {

  val Value: Field = Field(Str, Namespace.Data + "value")

  override def dynamicFields: List[Field] = List(Value) ++ DataNodeModel.fields

  override def dynamicType = List(ScalarNode.builderType)

  override def valueForField(f: Field): Option[AmfElement] = f match {
    case Value =>
      val annotations = dataType match {
        case Some(dt) => Annotations() += ScalarType(dt)
        case None     => Annotations()
      }
      Some(AmfScalar(value, annotations))
    case _ => None
  }

  override def replaceVariables(values: Set[Variable]): Unit =
    value = VariableReplacer.replaceVariables(value, values)

  override def cloneNode(): this.type = {
    val cloned = ScalarNode(annotations)

    cloned.value = value
    cloned.dataType = dataType

    cloned.asInstanceOf[this.type]
  }
}

object ScalarNode {

  val builderType: ValueType = Namespace.Data + "Scalar"

  def apply(): ScalarNode = apply("", None)

  def apply(annotations: Annotations): ScalarNode = apply("", None, annotations)

  def apply(value: String, dataType: Option[String]): ScalarNode = apply(value, dataType, Annotations())

  def apply(value: String, dataType: Option[String], ast: YPart): ScalarNode =
    apply(value, dataType, Annotations(ast))

  def apply(value: String, dataType: Option[String], annotations: Annotations): ScalarNode =
    new ScalarNode(value: String, dataType: Option[String], Fields(), annotations)
}

/**
  * Arrays of values
  */
class ArrayNode(override val fields: Fields, val annotations: Annotations) extends DataNode(annotations) {

  val Member: Field = Field(Array(DataNodeModel), Namespace.Rdf + "member")

  var members: ListBuffer[DataNode] = ListBuffer()

  def addMember(member: DataNode): ListBuffer[DataNode] = members += member.adopted(this.id)

  override def dynamicFields: List[Field] = List(Member) ++ DataNodeModel.fields

  override def dynamicType = List(ArrayNode.builderType, Namespace.Rdf + "Seq")

  override def valueForField(f: Field): Option[AmfElement] = f match {
    case Member => Some(AmfArray(members))
    case _      => None
  }

  override def replaceVariables(values: Set[Variable]): Unit = members.foreach(_.replaceVariables(values))

  override def cloneNode(): this.type = {
    val cloned = ArrayNode(annotations)

    cloned.members = members.map(_.cloneNode())

    cloned.asInstanceOf[this.type]
  }
}

object ArrayNode {

  val builderType: ValueType = Namespace.Data + "Array"

  def apply(): ArrayNode = apply(Annotations())

  def apply(ast: YSequence): ArrayNode = apply(Annotations(ast))

  def apply(annotations: Annotations): ArrayNode = new ArrayNode(Fields(), annotations)
}
