package amf.domain.extensions

import amf.common.AMFAST
import amf.domain.{Annotations, DynamicDomainElement, Fields}
import amf.metadata.domain.extensions.DataNodeModel.Name
import amf.common.core._
import amf.metadata.Field
import amf.vocabulary.Namespace
import amf.metadata.Type.{Iri, Str, Array}
import amf.metadata.domain.extensions.DataNodeModel
import amf.model.{AmfArray, AmfElement, AmfScalar}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * We need to generate unique IDs for all data nodes if the name is not set
  */
object idCounter {
  private var c = 0

  def genId(id: String): String = {
    c += 1
    s"${id}_$c"
  }

  // TODO:
  // Ideally this should be resetted every single time we parse
  def reset(): Unit = c = 0
}

/**
  * Base class for all dynamic DataNodes
  */
abstract class DataNode(annotations: Annotations) extends DynamicDomainElement {

  def name: String = Option(fields(Name)).getOrElse(defaultName)

  protected def defaultName: String     = idCounter.genId("dataNode")
  def withName(name: String): this.type = set(Name, name)

  override val fields: Fields = Fields()
}

/**
  * Data records, with a list of properties
  */
case class ObjectNode(override val fields: Fields, annotations: Annotations) extends DataNode(annotations) {

  val properties: mutable.HashMap[String,ListBuffer[DataNode]] = mutable.HashMap()
  val propertyAnnotations: mutable.HashMap[String,Annotations] = mutable.HashMap()

  override def defaultName: String = idCounter.genId("object")

  def addProperty(property: String, objectValue: DataNode, annotations: Annotations = Annotations()): this.type = {
    objectValue.adopted(this.id)
    val propertyList = properties.getOrElse(property, ListBuffer())
    objectValue match {
      case obj: ObjectNode =>
        propertyList.find(_.id == objectValue.id) match {
          case Some(_) => // ignore, duplicated value
          case None    => propertyList += obj
        }
      case _ => propertyList += objectValue // scalar values can be duplicated
    }
    properties.update(property, propertyList)
    propertyAnnotations.update(property, annotations)
    this
  }

  override def dynamicFields: List[Field] =
    this.properties.keys
      .map({ p =>
        Field(DataNodeModel, Namespace.WihtoutNamespace + p)
      })
      .toList ++ DataNodeModel.fields

  override def dynamicType = List(Namespace.Data + "Object")

  override def adopted(parent: String): this.type = withId(parent + "/" + name.urlEncoded)

  override def valueForField(f: Field): Option[AmfElement] = properties.get(f.value.iri()) match {
    case Some(els) if els.nonEmpty => Some(els.head)
    case _                         => None
  }
}

object ObjectNode {

  def apply(): ObjectNode = apply(Annotations())

  def apply(ast: AMFAST): ObjectNode = apply(Annotations(ast))

  def apply(annotations: Annotations): ObjectNode = ObjectNode(Fields(), annotations)
}

/**
  * Scalar values with associated data type
  */
case class ScalarNode(value: String, dataType: Option[String], override val fields: Fields, annotations: Annotations)
    extends DataNode(annotations) {
  override def defaultName: String = idCounter.genId("scalar")

  val Range: Field = Field(Iri, Namespace.Rdfs + "range")
  val Value: Field = Field(Str, Namespace.Data + "value")

  override def dynamicFields: List[Field] = List(Range, Value) ++ DataNodeModel.fields

  override def dynamicType = List(Namespace.Data + "Scalar")

  override def adopted(parent: String): this.type = withId(parent + "/" + name.urlEncoded)

  override def valueForField(f: Field): Option[AmfElement] = f match {
    case Range =>
      dataType match {
        case Some(dt) => Some(AmfScalar(dt))
        case None     => None
      }
    case Value =>
      Some(AmfScalar(value))
    case _ => None
  }
}

object ScalarNode {

  def apply(): ScalarNode = apply("", None)

  def apply(value: String, dataType: Option[String]): ScalarNode = apply(value, dataType, Annotations())

  def apply(value: String, dataType: Option[String], ast: AMFAST): ScalarNode =
    apply(value, dataType, Annotations(ast))

  def apply(value: String, dataType: Option[String], annotations: Annotations): ScalarNode =
    ScalarNode(value: String, dataType: Option[String], Fields(), annotations)
}

/**
  * Arrays of values
  */
case class ArrayNode(override val fields: Fields, annotations: Annotations) extends DataNode(annotations) {
  override def defaultName: String = idCounter.genId("array")

  val Member: Field = Field(Array(DataNodeModel), Namespace.Rdf + "member")

  var members: ListBuffer[DataNode] = ListBuffer()

  def addMember(member: DataNode): ListBuffer[DataNode] = members += member.adopted(this.id)

  override def dynamicFields: List[Field] = List(Member) ++ DataNodeModel.fields

  override def dynamicType = List(Namespace.Rdf + "Seq", Namespace.Data + "Array")

  override def adopted(parent: String): this.type = withId(parent + "/" + name.urlEncoded)

  override def valueForField(f: Field): Option[AmfElement] = f match {
    case Member => Some(AmfArray(members))
    case _      => None
  }
}

object ArrayNode {

  def apply(): ArrayNode = apply(Annotations())

  def apply(ast: AMFAST): ArrayNode = apply(Annotations(ast))

  def apply(annotations: Annotations): ArrayNode = ArrayNode(Fields(), annotations)
}
