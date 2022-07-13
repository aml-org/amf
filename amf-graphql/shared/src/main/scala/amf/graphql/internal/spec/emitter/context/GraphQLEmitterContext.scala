package amf.graphql.internal.spec.emitter.context

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.Shape
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.shapes.client.scala.model.domain.NodeShape

import scala.collection.mutable

case class RootType(
    name: String,
    rootType: GraphQLBaseWebApiContext.RootTypes.Value,
    fields: mutable.Map[String, EndPoint] = mutable.Map()
)

class GraphQLEmitterContext(document: BaseUnit) {

  var queryType: Option[RootType]        = None
  var mutationType: Option[RootType]     = None
  var subscriptionType: Option[RootType] = None

  val inputTypeNames: mutable.Set[String] = mutable.Set()

  val webApi: WebApi = document.asInstanceOf[Document].encodes.asInstanceOf[WebApi]

  private def processPath(path: String): Option[(GraphQLBaseWebApiContext.RootTypes.Value, String)] = {
    if (path.startsWith("/query/")) {
      Some((GraphQLBaseWebApiContext.RootTypes.Query, path.split("\\/query\\/").last))
    } else if (path.startsWith("/subscription/")) {
      Some((GraphQLBaseWebApiContext.RootTypes.Subscription, path.split("\\/subscription\\/").last))
    } else if (path.startsWith("/mutation/")) {
      Some((GraphQLBaseWebApiContext.RootTypes.Mutation, path.split("\\/mutation\\/").last))
    } else {
      None
    }
  }

  private def topLevelTypeFor(
      rootType: GraphQLBaseWebApiContext.RootTypes.Value,
      rootTypeName: String,
      field: String,
      ep: EndPoint
  ): Unit = {
    val rootTypeDefinition: RootType = rootType match {
      case GraphQLBaseWebApiContext.RootTypes.Query =>
        queryType match {
          case Some(qt) => qt
          case _ =>
            queryType = Some(RootType(rootTypeName, GraphQLBaseWebApiContext.RootTypes.Query))
            queryType.get
        }
      case GraphQLBaseWebApiContext.RootTypes.Subscription =>
        subscriptionType match {
          case Some(qt) => qt
          case _ =>
            subscriptionType = Some(RootType(rootTypeName, GraphQLBaseWebApiContext.RootTypes.Subscription))
            subscriptionType.get
        }
      case GraphQLBaseWebApiContext.RootTypes.Mutation =>
        mutationType match {
          case Some(qt) => qt
          case _ =>
            mutationType = Some(RootType(rootTypeName, GraphQLBaseWebApiContext.RootTypes.Mutation))
            mutationType.get
        }
    }
    rootTypeDefinition.fields += (field -> ep)
  }

  def mustEmitSchema: Boolean = {
    val hasDescription        = webApi.description.option().nonEmpty
    val namedQueryType        = queryType.exists(_.name != "Query")
    val namedSubscriptionType = subscriptionType.exists(_.name != "Subscription")
    val namedMutationType     = mutationType.exists(_.name != "Mutation")
    hasDescription || namedQueryType || namedSubscriptionType || namedMutationType
  }

  def classifyEndpoints(): GraphQLEmitterContext = {
    val endpoints = webApi.endPoints
    endpoints.foreach { ep =>
      val path         = ep.path.value()
      val rootTypeName = ep.name.option().getOrElse("UnknownType").split("\\.").head
      processPath(path) match {
        case Some((rootType, field)) =>
          topLevelTypeFor(rootType, rootTypeName, field, ep)
        case _ => // ignore
      }
    }
    this
  }

  def indexInputTypes: GraphQLEmitterContext = {
    val shapes: Seq[Option[NodeShape]] = webApi.endPoints.flatMap { ep =>
      ep.operations flatMap { op =>
        Option(op.request) match {
          case Some(req) =>
            req.queryParameters.map { param =>
              Option(param.schema) match {
                case Some(n: NodeShape) =>
                  Some(n)
                case _ =>
                  None
              }
            }
          case _ => None
        }
      }
    }

    val nodeShapes = shapes collect { case Some(s) => s }

    nodeShapes.foreach { s =>
      val name = s.effectiveLinkTarget().asInstanceOf[Shape].name.value()
      inputTypeNames += name
    }
    this
  }
}
