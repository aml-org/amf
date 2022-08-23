package amf.graphql.internal.spec.emitter.domain
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext

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
          if (isLastArgument(arguments, pos)) of += arg else of += s"$arg,"
        }
      }
    }
  }
  private def isLastArgument(arguments: Seq[GeneratedGraphQLArgument], pos: Int) = pos == arguments.length - 1
}
