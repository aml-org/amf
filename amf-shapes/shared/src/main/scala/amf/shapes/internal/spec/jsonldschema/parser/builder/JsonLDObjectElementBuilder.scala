package amf.shapes.internal.spec.jsonldschema.parser.builder

import amf.core.client.scala.model.domain.context.EntityContextBuilder
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.jsonldinstance.{JsonLDElement, JsonLDObject}
import amf.shapes.internal.spec.jsonldschema.instance.model.meta.JsonLDEntityModel
import amf.shapes.internal.spec.jsonldschema.parser.JsonLDParserContext
import amf.shapes.internal.spec.jsonldschema.validation.JsonLDSchemaValidations.IncompatibleNodes
import org.mulesoft.common.client.lexical.SourceLocation

import scala.collection.mutable

class JsonLDObjectElementBuilder(location: SourceLocation, key: String) extends JsonLDElementBuilder(location) {
  override type THIS = JsonLDObjectElementBuilder
  type KEY           = String
  type TERM          = String
  val properties: mutable.Map[KEY, JsonLDPropertyBuilder] = mutable.Map()
  val termIndex: mutable.Map[TERM, KEY]                   = mutable.Map()
  def +(property: JsonLDPropertyBuilder): JsonLDObjectElementBuilder = {

    // TODO: handle terms colitions. Check termIndex registry
    properties += property.key -> property
    termIndex += property.term -> property.key
    this
  }

  override def merge(
      other: JsonLDObjectElementBuilder
  )(implicit ctx: JsonLDParserContext): JsonLDObjectElementBuilder = {
    super.merge(other)

    other.properties.foreach { t =>
      if (properties.contains(t._2.key)) mergeAndReplaceProperty(properties(t._2.key), t._2)
      else this + t._2
    }
    this
  }

  def mergeAndReplaceProperty(current: JsonLDPropertyBuilder, other: JsonLDPropertyBuilder)(implicit
      ctx: JsonLDParserContext
  ): Any = {
    val merged: JsonLDElementBuilder = current.element match {
      case obj: JsonLDObjectElementBuilder if other.element.isInstanceOf[JsonLDObjectElementBuilder] =>
        obj.merge(other.element.asInstanceOf[JsonLDObjectElementBuilder])
      case array: JsonLDArrayElementBuilder if other.element.isInstanceOf[JsonLDArrayElementBuilder] =>
        array.merge(other.element.asInstanceOf[JsonLDArrayElementBuilder])
      case scalar: JsonLDScalarElementBuilder if other.element.isInstanceOf[JsonLDScalarElementBuilder] =>
        scalar.merge(other.element.asInstanceOf[JsonLDScalarElementBuilder])
      case other =>
        ctx.violation(IncompatibleNodes, "", IncompatibleNodes.message, current.location)
        other
    }

    properties.remove(current.key)
    this + other.copy(element = merged)
    this
  }

  override def build(ctxBuilder: EntityContextBuilder): JsonLDElement = {

    val fields = termIndex.map { case (term, key) =>
      val currentBuilder: JsonLDPropertyBuilder = properties(key)
      val element                               = currentBuilder.element.build(ctxBuilder)
      (Field(element.meta, ValueType(term)) -> element)
    }
    // TODO native-jsonld: handle hierarchy for same term at two properties.
    val entityModel = new JsonLDEntityModel(classTerms.map(ValueType.apply).toList, fields.keys.toList)
    ctxBuilder + entityModel
    val dObject = new JsonLDObject(Fields(), Annotations(), entityModel)
    fields.foreach { f => dObject.set(f._1, f._2) }
    dObject
  }

  override def canEquals(other: Any): Boolean = other.isInstanceOf[JsonLDObjectElementBuilder]
}

object JsonLDObjectElementBuilder {
  def empty(key: String) = new JsonLDObjectElementBuilder(SourceLocation.Unknown, key)
}

case class JsonLDPropertyBuilder(
    term: String,
    key: String,
    father: Option[String],
    element: JsonLDElementBuilder,
    location: SourceLocation
)
