package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.helpers.GraphQLEmitterHelper.emitArgumentsWithDescriptions
import amf.graphql.internal.spec.emitter.helpers.LineEmitter
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapeParameter}

case class GraphQLOperationFieldEmitter(operation: ShapeOperation, ctx: GraphQLEmitterContext, b: StringDocBuilder)
    extends GraphQLEmitter {

  def emit(): Unit = {
    val name         = operation.name.value()
    val arguments    = collectArguments
    val returnedType = extractGraphQLType
    val directives   = GraphQLDirectiveApplicationsRenderer(operation)

    // arguments side by side or each in a new line
    val isMultiLine = arguments.exists(_.documentation.nonEmpty)

    b.fixed { f =>
      emitFieldDescription(f)
      if (isMultiLine) {
        LineEmitter(f, s"$name(").emit()
        emitArgumentsWithDescriptions(arguments, f, ctx)
        LineEmitter(f, "):", returnedType, directives).emit()
      } else {
        val inputValueDefinitions = arguments.map(_.value).mkString(", ")
        val argumentsDefinition   = s"($inputValueDefinitions)"
        LineEmitter(f, s"$name$argumentsDefinition:", returnedType, directives).emit()
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
        GraphQLOperationArgumentGenerator(toApiContractParameter(arg), ctx).generate()
    }
  }
  private def toApiContractParameter(arg: ShapeParameter): Parameter = {
    val param = Parameter()
      .withName(arg.name.value())
      .withRequired(arg.required.option().getOrElse(false))
      .withSchema(arg.schema)
      .withCustomDomainProperties(arg.customDomainProperties)

    Option(arg.defaultValue).map { default =>
      param.withDefaultValue(default)
    }
    arg.description.option().foreach { desc =>
      param.withDescription(desc)
    }
    param
  }
}
