package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.parser.syntax.NullableShape
import amf.graphql.plugins.parse.GraphQLParsePlugin.unpackNilUnion
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.client.scala.model.domain.operations.{AbstractOperation, AbstractParameter}

case class GraphQLOperationFieldEmitter(operation: AbstractOperation, ctx: GraphQLEmitterContext, b: StringDocBuilder)
    extends GraphQLEmitter {

  def emit(): Unit = {
    val name = operation.name.value()
    val arguments = operation.request.queryParameters.map { arg =>
      GraphQLArgumentGenerator(toApiContractParameter(arg), ctx).generate()
    }
    val isMultiLine  = arguments.exists(_.documentation.nonEmpty)
    val range        = operation.response.payload.schema
    val returnedType = typeTarget(range)

    b.fixed { f =>
      operation.description.option() match {
        case Some(desc) =>
          f.fixed { f =>
            documentationEmitter(desc, f, Some(pos(operation.annotations)))
          }
        case _ => // ignore
      }
      if (isMultiLine) {
        f.+=(s"$name(", pos(operation.annotations))
        f.obj { o =>
          arguments.zipWithIndex.foreach {
            case (GeneratedGraphQLArgument(desc, data), i) =>
              o.fixed { f =>
                desc.foreach { doc =>
                  documentationEmitter(doc, f)
                }

                if (i < arguments.length - 1) {
                  f.+=(s"$data,")
                } else {
                  f.+=(data)
                }
              }
          }
        }
        f.+=(s"): $returnedType")
      } else {
        val args = arguments.map(_.value).mkString(",")
        f.+=(s"$name($args): $returnedType", pos(operation.annotations))
      }
    }
  }

  def toApiContractParameter(arg: AbstractParameter): Parameter = {
    val param = Parameter()
      .withName(arg.name.value())
      .withRequired(arg.required.option().getOrElse(false))
      .withSchema(arg.schema)
    arg.description.option().foreach { desc =>
      param.withDescription(desc)
    }
    param
  }
}
