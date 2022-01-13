package amf.graphql.internal.spec.emitter.context

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.Shape
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.shapes.client.scala.model.domain.NodeShape

import scala.collection.mutable

case class RootType(name: String, rootType: GraphQLWebApiContext.RootTypes.Value, fields: mutable.Map[String, EndPoint] = mutable.Map())

class GraphQLEmitterContext(document: BaseUnit) {

  var queryType: Option[RootType] = None
  var mutationType: Option[RootType] = None
  var subscriptionType: Option[RootType] = None

  val inputTypeNames: mutable.Set[String] = mutable.Set()

  val webApi: WebApi = document.asInstanceOf[Document].encodes.asInstanceOf[WebApi]

  private def processPath(path: String) : Option[(GraphQLWebApiContext.RootTypes.Value, String)] = {
    if (path.startsWith("/query/")) {
      Some((GraphQLWebApiContext.RootTypes.Query, path.split("\\/query\\/").last))
    } else if (path.startsWith("/subscription/")) {
      Some((GraphQLWebApiContext.RootTypes.Subscription, path.split("\\/subscription\\/").last))
    } else if (path.startsWith("/mutation/")) {
      Some((GraphQLWebApiContext.RootTypes.Mutation, path.split("\\/mutation\\/").last))
    } else {
      None
    }
  }

  private def topLevelTypeFor(rootType: GraphQLWebApiContext.RootTypes.Value, rootTypeName: String, field: String, ep: EndPoint): Unit = {
    val rootTypeDefinition: RootType = rootType match {
      case GraphQLWebApiContext.RootTypes.Query =>
        queryType match {
          case Some(qt) => qt
          case _        =>
            queryType = Some(RootType(rootTypeName, GraphQLWebApiContext.RootTypes.Query))
            queryType.get
        }
      case GraphQLWebApiContext.RootTypes.Subscription =>
        subscriptionType match {
          case Some(qt) => qt
          case _        =>
            subscriptionType = Some(RootType(rootTypeName, GraphQLWebApiContext.RootTypes.Subscription))
            subscriptionType.get
        }
      case GraphQLWebApiContext.RootTypes.Mutation =>
        mutationType match {
          case Some(qt) => qt
          case _        =>
            mutationType = Some(RootType(rootTypeName, GraphQLWebApiContext.RootTypes.Mutation))
            mutationType.get
        }
    }
    rootTypeDefinition.fields += (field -> ep)
  }

  def mustEmitSchema: Boolean = {
    val hasDescription = webApi.description.option().nonEmpty
    val namedQueryType = queryType.exists(_.name != "Query")
    val namedSubscriptionType = subscriptionType.exists(_.name != "Subscription")
    val namedMutationType = mutationType.exists(_.name != "Mutation")
    hasDescription || namedQueryType || namedSubscriptionType || namedMutationType
  }

  def classifyEndpoints(): GraphQLEmitterContext = {
    val endpoints = webApi.endPoints
    endpoints.foreach { ep =>
      val path = ep.path.value()
      val rootTypeName = ep.name.option().getOrElse("UnknownType").split("\\.").head
      processPath(path) match {
        case Some((rootType, field))        =>
          topLevelTypeFor(rootType, rootTypeName, field, ep)
        case _                                                          => // ignore
      }
    }
    this
  }

  def indexInputTypes: GraphQLEmitterContext = {
    val shapes:Seq[Option[NodeShape]] = webApi.endPoints.flatMap { ep =>
      ep.operations flatMap  { op =>
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
          case _         => None
        }
      }
    }

    val nodeShapes = shapes collect { case Some(s) => s}

    nodeShapes.foreach { s =>
      val name = s.effectiveLinkTarget().asInstanceOf[Shape].name.value()
      inputTypeNames += name
    }
    this
  }
}
