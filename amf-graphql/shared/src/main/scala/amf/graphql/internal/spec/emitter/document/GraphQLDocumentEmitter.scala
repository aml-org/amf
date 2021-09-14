package amf.graphql.internal.spec.emitter.document

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.domain.{GraphQLEmitter, GraphQLRootTypeEmitter, GraphQLTypeEmitter}
import amf.shapes.client.scala.model.domain.AnyShape

class GraphQLDocumentEmitter(document: BaseUnit, builder: StringDocBuilder) extends GraphQLEmitter {

  val ctx = new GraphQLEmitterContext(document).classifyEndpoints().indexInputTypes

  def emit(): Unit = {
    builder.doc { doc =>
      if (ctx.mustEmitSchema) {
        emitSchema(doc)
      }
      emitTopLevelTypes(doc)
      emitTypes(doc)
    }
  }

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
    ctx.queryType.foreach { queryType =>
      GraphQLRootTypeEmitter(queryType, ctx,b).emit()
    }
    ctx.mutationType.foreach { queryType =>
      GraphQLRootTypeEmitter(queryType, ctx,b).emit()
    }
    ctx.subscriptionType.foreach { queryType =>
      GraphQLRootTypeEmitter(queryType, ctx,b).emit()
    }
  }

  def emitTypes(doc: StringDocBuilder): Unit = {
    document.asInstanceOf[Document].declares.foreach {
      case shape: AnyShape =>
        GraphQLTypeEmitter(shape, ctx, doc).emit()
    }
  }

}
