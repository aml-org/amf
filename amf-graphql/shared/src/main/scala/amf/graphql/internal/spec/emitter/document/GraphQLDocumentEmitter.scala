package amf.graphql.internal.spec.emitter.document

import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.graphql.internal.spec.emitter.context.GraphQLEmitterContext
import amf.graphql.internal.spec.emitter.domain.{
  GraphQLDescriptionEmitter,
  GraphQLDirectiveApplicationsRenderer,
  GraphQLDirectiveDeclarationEmitter,
  GraphQLEmitter,
  GraphQLRootTypeEmitter,
  GraphQLTypeEmitter
}
import amf.graphql.internal.spec.emitter.helpers.LineEmitter
import amf.shapes.client.scala.model.domain.AnyShape

class GraphQLDocumentEmitter(document: BaseUnit, builder: StringDocBuilder) extends GraphQLEmitter {

  val ctx: GraphQLEmitterContext = new GraphQLEmitterContext(document).classifyEndpoints()

  def emit(): Unit = {
    builder.doc { doc =>
      if (ctx.mustEmitSchema) {
        emitSchema(doc)
      }
      emitTopLevelTypes(doc)
      emitDeclarations(doc)
    }
  }

  // TODO: schema is not being rendered
  private def emitSchema(doc: StringDocBuilder) = {
    val directives = GraphQLDirectiveApplicationsRenderer(ctx.webApi)

    doc.fixed { b =>
      GraphQLDescriptionEmitter(ctx.webApi.description.option(), ctx, b).emit()
      LineEmitter(b, "schema", directives, "{").emit()
      emitRootOperationTypeDefinitions(b)
      LineEmitter(b).closeBlock()
    }
  }

  private def emitRootOperationTypeDefinitions(b: StringDocBuilder) = {
    b.obj { obj =>
      ctx.queryType.foreach { queryType =>
        LineEmitter(obj, "query:", queryType.name).emit()
      }
      ctx.mutationType.foreach { mutationType =>
        LineEmitter(obj, "mutation:", mutationType.name).emit()
      }
      ctx.subscriptionType.foreach { subscriptionType =>
        LineEmitter(obj, "subscription:", subscriptionType.name).emit()
      }
    }
  }

  private def emitTopLevelTypes(b: StringDocBuilder): Unit = {
    val rootLevelTypes = ctx.queryType ++ ctx.mutationType ++ ctx.subscriptionType
    rootLevelTypes.foreach { queryType =>
      GraphQLRootTypeEmitter(queryType, ctx, b).emit()
    }
  }

  private def emitDeclarations(doc: StringDocBuilder): Unit = {
    document.asInstanceOf[Document].declares.foreach {
      case shape: AnyShape =>
        GraphQLTypeEmitter(shape, ctx, doc).emit()
      case directive: CustomDomainProperty if !isStandardDirective(directive) =>
        GraphQLDirectiveDeclarationEmitter(directive, ctx, doc).emit()
      case _ => // ignore
    }
  }

  // TODO: Change this filter to an annotation to make it scalable
  private def isStandardDirective(directive: CustomDomainProperty): Boolean = directive.name.value() == "deprecated"
}
