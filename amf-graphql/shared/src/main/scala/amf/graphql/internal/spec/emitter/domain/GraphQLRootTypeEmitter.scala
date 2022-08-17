package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.{GraphQLEmitterContext, RootType}
import amf.graphql.internal.spec.emitter.helpers.GraphQLEmitterHelper.emitArgumentsWithDescriptions
import amf.graphql.internal.spec.emitter.helpers.LineEmitter

case class GraphQLRootTypeEmitter(rootType: RootType, ctx: GraphQLEmitterContext, b: StringDocBuilder)
    extends GraphQLEmitter {

  def emit(): Unit = {
    b.fixed { f =>
      LineEmitter(f, "type", rootType.name, "{").emit()
      emitRootFields(f)
      LineEmitter(f, "}").emit()
    }
  }

  private def emitRootFields(f: StringDocBuilder) = {
    f.obj { o =>
      rootType.fields.foreach { case (_, ep: EndPoint) =>
        emitRootField(ep, o)
      }
    }
  }
  private def emitRootField(ep: EndPoint, l: StringDocBuilder): Unit = {
    val operation    = ep.operations.head
    val name         = extractRootFieldName(ep)
    val arguments    = collectArguments(operation)
    val returnedType = extractGraphQLType(operation)
    val directives   = GraphQLDirectiveApplicationsRenderer(ep)

    emiRootFieldDescription(ep, l)

    val isMultiLine  = arguments.exists(a => a.documentation.nonEmpty)
    val hasArguments = arguments.nonEmpty

    if (isMultiLine) l.fixed { f =>
      LineEmitter(f, s"$name(").emit()
      emitArgumentsWithDescriptions(arguments, f, ctx)
      LineEmitter(f, "):", returnedType, directives).emit()
    }
    else {
      val inputValuesDefinition = arguments.map(_.value).mkString(", ")
      val argumentsDefinition   = if (hasArguments) s"($inputValuesDefinition)" else ""
      LineEmitter(l, s"$name$argumentsDefinition:", returnedType, directives).emit()
    }
  }
  private def emiRootFieldDescription(ep: EndPoint, l: StringDocBuilder) = {
    val rootFieldDescription = ep.description.option()
    if (rootFieldDescription.isDefined) {
      l.fixed { f =>
        GraphQLDescriptionEmitter(rootFieldDescription, ctx, f).emit()
      }
    }
  }
  private def extractGraphQLType(operation: Operation) = typeTarget(operation.responses.head.payloads.head.schema)
  private def extractRootFieldName(ep: EndPoint)       = ep.name.value().split("\\.").last
  private def collectArguments(operation: Operation) = {
    Option(operation.request) match {
      case Some(request) =>
        request.queryParameters.map { param =>
            GraphQLOperationArgumentGenerator(param, ctx).generate()
        }
      case None => Nil
    }
  }
}
