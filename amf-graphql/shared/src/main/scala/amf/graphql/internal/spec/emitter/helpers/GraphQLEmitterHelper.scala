package amf.graphql.internal.spec.emitter.helpers
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.domain.{GeneratedGraphQLArgument, GraphQLDescriptionEmitter}

object GraphQLEmitterHelper {
  def emitArgumentsWithDescriptions(
      arguments: Seq[GeneratedGraphQLArgument],
      f: StringDocBuilder,
      ctx: GraphQLEmitterContext
  ) = {
    f.obj { o =>
      arguments.zipWithIndex.foreach { case (GeneratedGraphQLArgument(description, arg), pos) =>
        o.fixed { of =>
          GraphQLDescriptionEmitter(description, ctx, of).emit()
          val argString = if (isLastArgument(arguments, pos)) arg else s"$arg,"
          LineEmitter(of, argString).emit()
        }
      }
    }
  }
  private def isLastArgument(arguments: Seq[GeneratedGraphQLArgument], pos: Int) = pos == arguments.length - 1
}
