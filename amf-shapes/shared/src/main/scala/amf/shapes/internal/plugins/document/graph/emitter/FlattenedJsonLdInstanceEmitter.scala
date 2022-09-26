package amf.shapes.internal.plugins.document.graph.emitter

import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.vocabulary.{Namespace, NamespaceAliases}
import amf.core.internal.plugins.document.graph.emitter.{
  ApplicableMetaFieldRenderProvider,
  FlattenedGraphEmitterContext,
  FlattenedJsonLdEmitter,
  GraphEmitterContext
}
import org.yaml.builder.DocBuilder
import amf.core.client.scala.config
import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject, AmfScalar}
import amf.core.internal.metamodel.Type
import amf.core.internal.parser.domain.{Annotations, Value}
import amf.shapes.client.scala.model.domain.jsonldinstance.JsonLDElementModel
import org.yaml.builder.DocBuilder.Part

object FlattenedJsonLdInstanceEmitter {

  def emit[T](
      unit: BaseUnit,
      builder: DocBuilder[T],
      renderOptions: RenderOptions = config.RenderOptions(),
      namespaceAliases: NamespaceAliases = Namespace.defaultAliases,
      fieldProvision: ApplicableMetaFieldRenderProvider
  ): Boolean = {
    implicit val ctx: FlattenedGraphEmitterContext =
      FlattenedGraphEmitterContext(unit, renderOptions, namespaceAliases = namespaceAliases)
    new FlattenedJsonLdInstanceEmitter(builder, renderOptions, fieldProvision).root(unit)
    true
  }
}

class FlattenedJsonLdInstanceEmitter[T](
    override val builder: DocBuilder[T],
    override val options: RenderOptions,
    override val fieldProvision: ApplicableMetaFieldRenderProvider
)(implicit ctx: FlattenedGraphEmitterContext)
    extends FlattenedJsonLdEmitter[T](builder, options, fieldProvision)(ctx) {
  override def createArrayValues(a: Type.Array, seq: AmfArray, b: Part[T], v: Value): Unit = {
    seq.values.foreach { v => emitArrayMember(v, b) }
  }

  override def emitArray(a: Type.Array, v: Value, b: Part[T], sources: Value => Unit): Option[T] = {
    def emitList: Part[T] => Unit = (b: Part[T]) => {
      val seq = v.value.asInstanceOf[AmfArray]
      sources(v)
      createArrayValues(a, seq, b, v)
    }

    b.obj(_.entry("@list", _.list(emitList)))
  }

  def emitArrayMember(element: AmfElement, b: Part[T]): Unit = {
    element match {
      case obj: AmfObject    => emitObjMember(obj, b, inArray = true)
      case scalar: AmfScalar => emitScalarMember(scalar, b)
      case arr: AmfArray =>
        b.list { b =>
          createArrayValues(Type.Array(Type.Any), arr, b, Value(arr, Annotations()))
        }
    }
  }
}
