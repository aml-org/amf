package amf.client.convert

import amf.client.model.{AnyField, DoubleField, StrField}
import amf.client.model.document.{BaseUnit => ClientBaseUnit}
import amf.client.model.domain.{AbstractDeclaration => ClientAbstractDeclaration, ArrayNode => ClientArrayNode, CustomDomainProperty => ClientCustomDomainProperty, DataNode => ClientDataNode, DomainElement => ClientDomainElement, DomainExtension => ClientDomainExtension, ObjectNode => ClientObjectNode, ParametrizedDeclaration => ClientParameterizedDeclaration, PropertyShape => ClientPropertyShape, ScalarNode => ClientScalarNode, Shape => ClientShape, VariableValue => ClientVariableValue}
import amf.core.model.document.BaseUnit
import amf.core.model.domain._
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.model.domain.templates.{AbstractDeclaration, ParametrizedDeclaration, VariableValue}
import amf.core.unsafe.PlatformSecrets

import scala.collection.mutable

trait CoreBaseConverter
    extends PlatformSecrets
    with CustomDomainPropertyConverter
    with ShapeConverter
    with PropertyShapeConverter
    with DataNodeConverter
    with DomainExtensionConverter
    with DeclarationsConverter
    with VariableValueConverter
    with DomainElementConverter
    with BaseUnitConverter {

  type ClientList[E]
  type ClientMap[V]

  implicit class InternalSeqOps[Internal, Client](from: Seq[Internal])(
      implicit m: InternalClientMatcher[Internal, Client]) {
    def asClient: ClientList[Client] = asClientList(from, m)
  }

  implicit class ClientListOps[Internal, Client](from: ClientList[Client])(
      implicit m: ClientInternalMatcher[Client, Internal]) {
    def asInternal: Seq[Internal] = asInternalSeq(from, m)
  }

  implicit class InternalMapOps[Internal, Client](from: mutable.Map[String, Internal])(
      implicit m: InternalClientMatcher[Internal, Client]) {
    def asClient: ClientMap[Client] = asClientMap(from, m)
  }

  implicit class InternalOptionOps[Internal, Client](from: Option[Internal])(
      implicit m: InternalClientMatcher[Internal, Client]) {
    def asClient: Option[Client] = from.map(m.asClient)
  }

  implicit def asClient[Internal, Client](from: Internal)(
      implicit m: InternalClientMatcher[Internal, Client]): Client = m.asClient(from)

  implicit def asInternal[Internal, Client](from: Client)(
      implicit m: ClientInternalMatcher[Client, Internal]): Internal = m.asInternal(from)

  private[convert] def asClientList[Internal, Client](from: Seq[Internal],
                                                      m: InternalClientMatcher[Internal, Client]): ClientList[Client]

  private[convert] def asClientMap[Internal, Client](from: mutable.Map[String, Internal],
                                                     m: InternalClientMatcher[Internal, Client]): ClientMap[Client]

  private[convert] def asInternalSeq[Client, Internal](from: ClientList[Client],
                                                       m: ClientInternalMatcher[Client, Internal]): Seq[Internal]

  implicit object StrFieldMatcher extends IdentityMatcher[StrField]

  implicit object DoubleFieldMatcher extends IdentityMatcher[DoubleField]

  implicit object AnyFieldMatcher extends IdentityMatcher[AnyField]

  implicit object StringMatcher extends IdentityMatcher[String]

  implicit object AnyMatcher extends IdentityMatcher[Any]

  trait IdentityMatcher[T] extends InternalClientMatcher[T, T] with ClientInternalMatcher[T, T] {
    override def asClient(from: T): T = from

    override def asInternal(from: T): T = from
  }

}

/** Return internal instance for given client representation. */
trait ClientInternalMatcher[Client, Internal] {
  def asInternal(from: Client): Internal
}

/** Return client instance for given internal representation. */
trait InternalClientMatcher[Internal, Client] {
  def asClient(from: Internal): Client
}

/** Matcher functioning in two directions. */
trait BidirectionalMatcher[Internal, Client]
    extends ClientInternalMatcher[Client, Internal]
    with InternalClientMatcher[Internal, Client]

trait DomainExtensionConverter {

  implicit object DomainExtensionMatcher extends BidirectionalMatcher[DomainExtension, ClientDomainExtension] {
    override def asClient(from: DomainExtension): ClientDomainExtension = ClientDomainExtension(from)

    override def asInternal(from: ClientDomainExtension): DomainExtension = from._internal
  }

}

trait DataNodeConverter {

  implicit object ObjectNodeMatcher extends BidirectionalMatcher[ObjectNode, ClientObjectNode] {
    override def asClient(from: ObjectNode): ClientObjectNode   = ClientObjectNode(from)
    override def asInternal(from: ClientObjectNode): ObjectNode = from._internal
  }

  implicit object ScalarNodeMatcher extends BidirectionalMatcher[ScalarNode, ClientScalarNode] {
    override def asClient(from: ScalarNode): ClientScalarNode   = ClientScalarNode(from)
    override def asInternal(from: ClientScalarNode): ScalarNode = from._internal
  }

  implicit object ArrayNodeMatcher extends BidirectionalMatcher[ArrayNode, ClientArrayNode] {
    override def asClient(from: ArrayNode): ClientArrayNode   = ClientArrayNode(from)
    override def asInternal(from: ClientArrayNode): ArrayNode = from._internal
  }

  implicit object DataNodeMatcher extends BidirectionalMatcher[DataNode, ClientDataNode] {
    override def asClient(from: DataNode): ClientDataNode = from match {
      case o: ObjectNode => ObjectNodeMatcher.asClient(o)
      case s: ScalarNode => ScalarNodeMatcher.asClient(s)
      case a: ArrayNode  => ArrayNodeMatcher.asClient(a)
    }
    override def asInternal(from: ClientDataNode): DataNode = from._internal
  }
}

trait DeclarationsConverter extends PlatformSecrets {

  implicit object AbstractDeclarationMatcher
      extends BidirectionalMatcher[AbstractDeclaration, ClientAbstractDeclaration] {
    override def asClient(from: AbstractDeclaration): ClientAbstractDeclaration =
      platform.wrap[ClientAbstractDeclaration](from)

    override def asInternal(from: ClientAbstractDeclaration): AbstractDeclaration = from._internal
  }

  implicit object ParameterizedDeclarationMatcher
      extends BidirectionalMatcher[ParametrizedDeclaration, ClientParameterizedDeclaration] {
    override def asClient(from: ParametrizedDeclaration): ClientParameterizedDeclaration =
      platform.wrap[ClientParameterizedDeclaration](from)

    override def asInternal(from: ClientParameterizedDeclaration): ParametrizedDeclaration = from._internal
  }

}

trait ShapeConverter extends PlatformSecrets {

  implicit object ShapeMatcher extends BidirectionalMatcher[Shape, ClientShape] {
    override def asClient(from: Shape): ClientShape = platform.wrap[ClientShape](from)

    override def asInternal(from: ClientShape): Shape = from._internal
  }

}

trait PropertyShapeConverter extends PlatformSecrets {

  implicit object PropertyShapeMatcher extends BidirectionalMatcher[PropertyShape, ClientPropertyShape] {
    override def asClient(from: PropertyShape): ClientPropertyShape = platform.wrap[ClientPropertyShape](from)

    override def asInternal(from: ClientPropertyShape): PropertyShape = from._internal
  }

}

trait VariableValueConverter {

  implicit object VariableValueMatcher extends BidirectionalMatcher[VariableValue, ClientVariableValue] {
    override def asInternal(from: ClientVariableValue): VariableValue = from._internal

    override def asClient(from: VariableValue): ClientVariableValue = ClientVariableValue(from)
  }

}

trait DomainElementConverter extends PlatformSecrets {

  implicit object DomainElementMatcher extends BidirectionalMatcher[DomainElement, ClientDomainElement] {
    override def asInternal(from: ClientDomainElement): DomainElement = from._internal

    override def asClient(from: DomainElement): ClientDomainElement = platform.wrap[ClientDomainElement](from)
  }

}

trait BaseUnitConverter extends PlatformSecrets {

  implicit object BaseUnitMatcher extends BidirectionalMatcher[BaseUnit, ClientBaseUnit] {
    override def asInternal(from: ClientBaseUnit): BaseUnit = from._internal

    override def asClient(from: BaseUnit): ClientBaseUnit = platform.wrap[ClientBaseUnit](from)
  }

}

trait CustomDomainPropertyConverter {

  implicit object CustomDomainPropertyMatcher
      extends BidirectionalMatcher[CustomDomainProperty, ClientCustomDomainProperty] {
    override def asInternal(from: ClientCustomDomainProperty): CustomDomainProperty = from._internal

    override def asClient(from: CustomDomainProperty): ClientCustomDomainProperty = ClientCustomDomainProperty(from)
  }

}
