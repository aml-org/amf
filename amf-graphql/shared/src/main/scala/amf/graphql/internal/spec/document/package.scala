package amf.graphql.internal.spec

import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject, AmfScalar}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.Annotations
import org.mulesoft.antlrast.ast.Node

package object document {

  implicit class GraphQLFieldSetter[T <: AmfObject](obj: T) {

    class GraphQLModelSetter(element: AmfElement, fieldAnnotations: Annotations) {
      def as(field: Field): T = {
        if (setWithoutId) obj.setWithoutId(field, element, fieldAnnotations)
        else obj.set(field, element, fieldAnnotations)
      }

    }

    private var synthesized                           = false
    private var setWithoutId                          = false
    private var parsingFn: Option[Node => AmfElement] = None

    def synthetically(): GraphQLFieldSetter[T] = {
      this.synthesized = true
      this
    }

    def withoutId(): GraphQLFieldSetter[T] = {
      this.setWithoutId = true
      this
    }

    def using(parsingFn: Node => AmfElement): GraphQLFieldSetter[T] = {
      this.parsingFn = Some(parsingFn)
      this
    }

    private def getAnnotation: Annotations = if (synthesized) Annotations.synthesized() else Annotations.inferred()

    def set(element: AmfElement) = new GraphQLModelSetter(element, getAnnotation)

    def set(node: Node): GraphQLModelSetter = parsingFn match {
      case Some(parsingFunction) => new GraphQLModelSetter(parsingFunction(node), getAnnotation)
      case None                  =>
        // TODO: report error and set a default like ctx.eh.violation()
        new GraphQLModelSetter(AmfScalar(node.toString()), getAnnotation)
    }

    def set(value: Boolean) = new GraphQLModelSetter(AmfScalar(value), Annotations.synthesized())

    def set(value: String) = new GraphQLModelSetter(AmfScalar(value), Annotations.synthesized())

    def set(value: Int) = new GraphQLModelSetter(AmfScalar(value), Annotations.synthesized())

    def set(values: Seq[AmfElement]): GraphQLModelSetter = {
      if (synthesized)
        new GraphQLModelSetter(AmfArray(values), Annotations.synthesized())
      else
        new GraphQLModelSetter(AmfArray(values, Annotations.virtual()), Annotations.inferred())
    }
  }
}
