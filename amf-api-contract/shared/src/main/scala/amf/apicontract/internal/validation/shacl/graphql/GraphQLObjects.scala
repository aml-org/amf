package amf.apicontract.internal.validation.shacl.graphql

import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain.{EndPoint, Parameter, Response}
import amf.apicontract.internal.validation.shacl.graphql.GraphQLUtils.{isValidInputType, isValidOutputType}
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.model.domain.operations._
import amf.shapes.internal.annotations.InputTypeField

import scala.annotation.tailrec

trait GraphQLElement {
  def name: String
  def annotations: Annotations
}

trait GraphQLField extends GraphQLElement {
  def datatype: Option[String]
  def schema: Option[Shape]
}

trait GraphQLArgument extends GraphQLElement {
  def datatype: Option[String]
  def default: DataNode
}

case class GraphQLObject(node: NodeShape) extends GraphQLElement {
  def name: String                      = node.name.value()
  def annotations: Annotations          = node.annotations
  def isInterface: Boolean              = node.isAbstract.value()
  def isInput: Boolean                  = node.isInputOnly.value()
  def isSchema: Boolean                 = node.name.isNullOrEmpty
  def properties: Seq[GraphQLProperty]  = node.properties.map(GraphQLProperty)
  def operations: Seq[GraphQLOperation] = node.operations.map(GraphQLOperation)

  def fields(): GraphQLFields = GraphQLFields(properties, operations)

  def allFields(): Seq[GraphQLField] = fields().fields()

  def inherits: Seq[GraphQLObject] = node.inherits.map(_.asInstanceOf[NodeShape]).map(GraphQLObject)
}

case class GraphQLFields(properties: Seq[GraphQLProperty], operations: Seq[GraphQLOperation]) {
  def names: Set[String] = {
    (properties.map(_.name) ++ operations.map(_.name)).toSet
  }

  def fields(): Seq[GraphQLField] = properties ++ operations
}

case class GraphQLEndpoint(endpoint: EndPoint) extends GraphQLElement {
  def name: String                      = endpoint.name.value()
  def annotations: Annotations          = endpoint.annotations
  def path: String                      = endpoint.path.value()
  def operations: Seq[GraphQLOperation] = endpoint.operations.map(GraphQLOperation)
  def parameters: Seq[GraphQLParameter] = operations.flatMap(_.parameters)
  def isValidInputType: Boolean         = parameters.forall(_.isValidInputType)
  def isValidOutputType: Boolean        = parameters.forall(_.isValidOutputType)
}

case class GraphQLProperty(property: PropertyShape) extends GraphQLField {
  def name: String             = property.name.value()
  def annotations: Annotations = property.annotations
  def datatype: Option[String] = GraphQLUtils.datatype(property.range)
  def isAny: Boolean = property.range match {
    case sc: ScalarShape => sc.dataType.value() == DataType.Any
    case _               => false
  }
  def isNullable: Boolean = {
    property.range match {
      case u: UnionShape => GraphQLNullable(u).isNullable
      case _             => false
    }
  }
  def default: Option[DataNode]  = Option(property.default)
  def range: Shape               = property.range
  def isValidInputType: Boolean  = GraphQLUtils.isValidInputType(range)
  def isValidOutputType: Boolean = GraphQLUtils.isValidOutputType(range)
  def schema: Option[Shape]      = Some(range)
  def minCount: Int              = property.minCount.value()
  def maxCount: Int              = property.maxCount.value()

}

case class GraphQLOperation(operation: AbstractOperation) extends GraphQLField {
  def name: String                       = operation.name.value()
  def annotations: Annotations           = operation.annotations
  def parameters: Seq[GraphQLParameter]  = operation.request.queryParameters.map(GraphQLParameter)
  def response: Option[AbstractResponse] = operation.responses.headOption

  def datatype: Option[String] = payload flatMap (payload => GraphQLUtils.datatype(payload.schema))
  def isAny: Boolean = payload.map(_.schema) match {
    case Some(sc: ScalarShape) => sc.dataType.value() == DataType.Any
    case _                     => false
  }

  // TODO: why is it store in different fields? payload vs payloads
  def payload: Option[AbstractPayload] = response.flatMap {
    case r: Response       => r.payloads.headOption
    case sr: ShapeResponse => Some(sr.payload)
    case _                 => None
  }

  def isValidOutputType: Boolean = payload match {
    case Some(payload) => GraphQLUtils.isValidOutputType(payload.schema)
    case None          => false
  }

  def schema: Option[Shape] = payload.map(_.schema)
}

case class GraphQLParameter(parameter: AbstractParameter) extends GraphQLArgument {
  def name: String               = parameter.name.value()
  def annotations: Annotations   = parameter.annotations
  def datatype: Option[String]   = GraphQLUtils.datatype(parameter.schema)
  def default: DataNode          = parameter.schema.default
  def required: Boolean          = parameter.required.value()
  def schema: Shape              = parameter.schema
  def isValidInputType: Boolean  = GraphQLUtils.isValidInputType(schema)
  def isValidOutputType: Boolean = GraphQLUtils.isValidOutputType(schema)
}

case class GraphQLDirective(directive: CustomDomainProperty) extends GraphQLElement {
  def name: String             = directive.name.value()
  def annotations: Annotations = directive.annotations
  def fields: GraphQLFields    = GraphQLObject(directive.schema.asInstanceOf[NodeShape]).fields()
}

case class GraphQLAppliedDirective(directive: DomainExtension) extends GraphQLElement {
  def name: String             = directive.name.value()
  def annotations: Annotations = directive.annotations
  def definedProps(): Seq[GraphQLProperty] = directive.definedBy match {
    case c: CustomDomainProperty =>
      c.schema match {
        case n: NodeShape => n.properties.map(GraphQLProperty)
        case _            => Seq()
      }
    case _ => Seq()
  }

  def propertyValues(): Seq[DataNode] = directive.extension match {
    case o: ObjectNode => o.allProperties().toList
    case _             => Seq()
  }
}

case class GraphQLNullable(union: UnionShape) {
  def name: String = {
    val name = union.name
    if (name.isNullOrEmpty) {
      union.anyOf match {
        case List(_: NilShape, n: NodeShape)   => n.name.value()
        case List(_: NilShape, u: UnionShape)  => GraphQLNullable(u).name
        case List(_: NilShape, s: ScalarShape) => s.name.option().getOrElse(GraphQLDataTypes.from(s))
        case List(_: NilShape, a: ArrayShape) =>
          val items = a.items match {
            case u: UnionShape => GraphQLNullable(u).name
            case s: Shape      => s.name.value()
          }
          s"[$items]"
        case _ => "union"
      }
    } else name.value()
  }

  def isNullable: Boolean = {
    union.anyOf.size == 2 && union.anyOf.head.isInstanceOf[NilShape]
  }

  def wrappedShape: Shape = union.anyOf.filter(!_.isInstanceOf[NilShape]).head

  def isValidInput: Boolean = {
    isNullable && isValidInputType(wrappedShape)
  }

  def isValidOutput: Boolean = {
    if (isNullable) {
      isValidOutputType(wrappedShape)
    } else {
      true
    }
  }
}
