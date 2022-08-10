package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.domain.GraphQLEmitterHelper.emitArgumentsWithDescriptions
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapeParameter}

case class GraphQLOperationFieldEmitter(operation: ShapeOperation, ctx: GraphQLEmitterContext, b: StringDocBuilder)
    extends GraphQLEmitter {

  def emit(): Unit = {
    val name         = operation.name.value()
    val arguments    = collectArguments
    val returnedType = extractGraphQLType

    // arguments side by side or each in a new line
    val isMultiLine = arguments.exists(_.documentation.nonEmpty)

    b.fixed { f =>
      emitFieldDescription(f)
      if (isMultiLine) {
        f += (s"$name(", pos(operation.annotations))
        emitArgumentsWithDescriptions(arguments, f, ctx)
        f += s"): $returnedType"
      } else {
        val args = arguments.map(_.value).mkString(",")
        f += (s"$name($args): $returnedType", pos(operation.annotations))
      }
    }
  }

  private def emitFieldDescription(f: StringDocBuilder): Unit = {
    val desc = operation.description.option()
    GraphQLDescriptionEmitter(desc, ctx, f, Some(pos(operation.annotations))).emit()
  }
  private def extractGraphQLType = {
    val range = operation.responses.head.payload.schema
    typeTarget(range)
  }
  private def collectArguments = {
    operation.request.queryParameters.map { arg =>
      GraphQLArgumentGenerator(toApiContractParameter(arg), ctx).generate()
    }
  }
  private def toApiContractParameter(arg: ShapeParameter): Parameter = {
    val param = Parameter()
      .withName(arg.name.value())
      .withRequired(arg.required.option().getOrElse(false))
      .withSchema(arg.schema)

    Option(arg.defaultValue).map { default =>
      param.withDefaultValue(default)
    }
    arg.description.option().foreach { desc =>
      param.withDescription(desc)
    }
    param
  }
}
