package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.{GraphQLEmitterContext, RootType}
import amf.graphql.internal.spec.emitter.domain.GraphQLEmitterHelper.emitArgumentsWithDescriptions

case class GraphQLRootTypeEmitter(rootType: RootType, ctx: GraphQLEmitterContext, b: StringDocBuilder)
    extends GraphQLEmitter {

  def emit(): Unit = {
    b.fixed { f =>
      f += s"type ${rootType.name} {"
      emitRootFields(f)
      f += "}"
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
    emiRootFieldDescription(ep, l)

    val isMultiLine = arguments.exists(a => a.documentation.nonEmpty)
    if (isMultiLine) l.fixed { f =>
      f += (s"$name(", pos(ep.annotations))
      emitArgumentsWithDescriptions(arguments, f, ctx)
      f += s"): $returnedType"
    }
    else {
      if (arguments.nonEmpty) {
        val args = arguments.map(_.value).mkString(",")
        l += (s"$name($args): $returnedType", pos(ep.annotations))
      } else {
        l += (s"$name: $returnedType", pos(ep.annotations))
      }
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
          GraphQLArgumentGenerator(param, ctx).generate()
        }
      case None => Nil
    }
  }
}
