package amf.graphql.internal.spec.emitter.document

import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.domain.{GraphQLEmitter, GraphQLRootTypeEmitter, GraphQLTypeEmitter}
import amf.shapes.client.scala.model.domain.AnyShape

class GraphQLDocumentEmitter(document: BaseUnit, builder: StringDocBuilder) extends GraphQLEmitter {

  val ctx: GraphQLEmitterContext = new GraphQLEmitterContext(document).classifyEndpoints().indexInputTypes

  def emit(): Unit = {
    builder.doc { doc =>
      if (ctx.mustEmitSchema) {
        emitSchema(doc)
      }
      emitTopLevelTypes(doc)
      emitTypes(doc)
    }
  }

  // TODO: schema is not being rendered
  private def emitSchema(doc: StringDocBuilder) = {
    doc.fixed { b =>
      ctx.webApi.description.option().foreach { description =>
        documentationEmitter(description, b)
      }
      b.+=("schema {")
      b.obj { obj =>
        ctx.queryType.foreach { queryType =>
          obj.+=(s"query: ${queryType.name}")
        }
        ctx.mutationType.foreach { mutationType =>
          obj.+=(s"mutation: ${mutationType.name}")
        }
        ctx.subscriptionType.foreach { subscriptionType =>
          obj.+=(s"subscription: ${subscriptionType.name}")
        }
      }
      b.+=("}")
    }
  }

  def emitTopLevelTypes(b: StringDocBuilder): Unit = {
    val rootLevelTypes = ctx.queryType ++ ctx.mutationType ++ ctx.subscriptionType
    rootLevelTypes.foreach { queryType =>
      GraphQLRootTypeEmitter(queryType, ctx, b).emit()
    }
  }

  def emitTypes(doc: StringDocBuilder): Unit = {
    document.asInstanceOf[Document].declares.foreach {
      case shape: AnyShape =>
        GraphQLTypeEmitter(shape, ctx, doc).emit()
      case directive: CustomDomainProperty =>
      // TODO: emit directive declarations, something like: GraphQLDirectiveDeclarationEmitter(directive, ctx, doc).emit()
    }
  }

}
