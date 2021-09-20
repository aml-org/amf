package amf.graphql.internal.spec.emitter.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.render.BaseEmitters.pos
import amf.graphql.internal.spec.emitter.context.{GraphQLEmitterContext, RootType}

case class GraphQLRootTypeEmitter(rootType: RootType, ctx: GraphQLEmitterContext, b: StringDocBuilder) extends GraphQLEmitter {

  def emit(): Unit = {
    b.fixed { f =>
      f.+=(s"type ${rootType.name} {")
      f.obj { o =>
        rootType.fields.foreach { case (_, ep: EndPoint) =>
          emitRootField(ep, o)
        }
      }
      f.+=("}")
    }
  }

  private def emitRootField(ep: EndPoint, l: StringDocBuilder): Unit = {
    val operation = ep.operations.head
    val name = ep.name.value().split("\\.").last
    val arguments: Seq[GeneratedGraphQLArgument] = Option(operation.request) match {
      case Some(request) =>
        request.queryParameters.map { param =>
          GraphQLArgumentGenerator(param, ctx).generate()
        }
      case None          => Nil
    }
    val returnedType = typeTarget(operation.responses.head.payloads.head.schema)
    val isMultiLine = arguments.exists(a => a.documentation.nonEmpty)
    l.fixed { f =>
      ep.description.option().foreach { doc =>
        documentationEmitter(doc, f)
      }
    }
    if (isMultiLine) l.fixed { f =>
      f.+=(s"${name}(", pos(ep.annotations))
      f.obj { o =>
        arguments.zipWithIndex.foreach { case (GeneratedGraphQLArgument(description, arg), pos) =>
          o.fixed { of =>
            description.foreach { desc =>
              documentationEmitter(desc, of)
            }
            if (pos < arguments.length - 1) {
              of.+=(s"${arg},")
            } else {
              of.+=(arg)
            }
          }
        }
      }
      f.+=(s"): $returnedType")
    } else {
      if (arguments.nonEmpty) {
        val args = arguments.map(_.value).mkString(",")
        l.+=(s"$name($args): $returnedType", pos(ep.annotations))
      } else {
        l.+=(s"$name: $returnedType", pos(ep.annotations))
      }
    }
  }
}
