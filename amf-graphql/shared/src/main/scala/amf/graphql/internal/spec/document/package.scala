package amf.graphql.internal.spec

import amf.core.client.scala.model.domain.{AmfArray, AmfElement, AmfObject, AmfScalar}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.domain.Annotations

package object document {

  implicit class GraphQLFieldSetter(obj: AmfObject) {

    class GraphQLModelSetter(element: AmfElement, annotations: Annotations) {
      def as(field: Field) = obj.set(field, element, annotations)

    }

    private var synthesized = false

    def synthetically(): GraphQLFieldSetter = {
      synthesized = true
      this
    }

    private def getAnnotation = if (synthesized) Annotations.synthesized() else Annotations.inferred()

    def set(element: AmfElement) = new GraphQLModelSetter(element, getAnnotation)

    def set(value: String) = new GraphQLModelSetter(AmfScalar(value), Annotations.synthesized())

    def set(values: Seq[AmfElement]) = {
      if (synthesized)
        new GraphQLModelSetter(AmfArray(values), Annotations.synthesized())
      else
        new GraphQLModelSetter(AmfArray(values, Annotations.virtual()), Annotations.inferred())
    }
  }
}
