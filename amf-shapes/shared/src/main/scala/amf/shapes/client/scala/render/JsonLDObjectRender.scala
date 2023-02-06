package amf.shapes.client.scala.render

import amf.core.client.platform.model.DataTypes
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfScalar}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.{Obj, Type}
import amf.core.internal.metamodel.Type.{ArrayLike, Scalar}
import amf.core.internal.parser.domain.Value
import amf.core.internal.render.emitters.PartEmitter
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDObject
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.{YDocument, YNode}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

class JsonLDObjectRender(obj: JsonLDObject, syntaxProvider: SyntaxProvider) extends PartEmitter {
  override def emit(b: YDocument.PartBuilder): Unit = {
    emitObject(obj, b)
  }

  private def emitElement(element: AmfElement, pb: PartBuilder): Unit = element match {
    case scalar: AmfScalar => emitScalar(scalar, pb)
    case arr: AmfArray     => emitArray(arr, pb)
    case obj: JsonLDObject => emitObject(obj, pb)
    case _                 => ???
  }

  private def emitObject(innerObj: JsonLDObject, builder: YDocument.PartBuilder): Unit = {
    builder.obj(eb => {
      innerObj.meta.fields.filter(f => innerObj.fields.exists(f)).foreach { field =>
        val element: AmfElement = innerObj.fields.get(field)
        val pbFunc: PartBuilder => Unit = (pb: PartBuilder) => {
          emitElement(element, pb)
        }
        eb.entry(syntaxProvider.keyFor(obj.meta.typeIris.head, field.value.iri()), pbFunc)
      }
    })
  }

  private def emitArray(arr: AmfArray, pb: PartBuilder) = {
    pb.list(f => arr.values.foreach(emitElement(_, f)))
  }

  private def emitScalar(scalar: AmfScalar, pb: PartBuilder): Unit = {
    scalar.value match {
      case s: String  => pb += s
      case i: Int     => pb += i
      case f: Float   => pb += f
      case b: Boolean => pb += b
      case _          => pb += YNode.Null
    }
  }

  override def position(): Position = Position.ZERO
}

trait SyntaxProvider {

  def keyFor(clazz: String, property: String): String
}

object TermNameSyntaxProvider extends SyntaxProvider {
  override def keyFor(clazz: String, property: String): String = ValueType(property).name
}
