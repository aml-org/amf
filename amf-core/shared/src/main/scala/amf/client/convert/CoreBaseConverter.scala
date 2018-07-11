package amf.client.convert

import amf.ProfileName
import amf.client.model.document.{BaseUnit => ClientBaseUnit}
import amf.client.model.domain.{
  AbstractDeclaration => ClientAbstractDeclaration,
  ArrayNode => ClientArrayNode,
  CustomDomainProperty => ClientCustomDomainProperty,
  DataNode => ClientDataNode,
  DomainElement => ClientDomainElement,
  DomainExtension => ClientDomainExtension,
  Graph => ClientGraph,
  ObjectNode => ClientObjectNode,
  ParametrizedDeclaration => ClientParameterizedDeclaration,
  PropertyShape => ClientPropertyShape,
  ScalarNode => ClientScalarNode,
  Shape => ClientShape,
  VariableValue => ClientVariableValue
}
import amf.client.model.{
  Annotations => ClientAnnotations,
  AnyField => ClientAnyField,
  BoolField => ClientBoolField,
  DoubleField => ClientDoubleField,
  FloatField => ClientFloatField,
  IntField => ClientIntField,
  StrField => ClientStrField
}
import amf.client.remote.Content
import amf.client.resource.{ResourceLoader => ClientResourceLoader}
import amf.client.validate.{ValidationReport => ClientValidatorReport, ValidationResult => ClientValidationResult}
import amf.core.model._
import amf.core.model.document.BaseUnit
import amf.core.model.domain._
import amf.core.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.model.domain.templates.{AbstractDeclaration, ParametrizedDeclaration, VariableValue}
import amf.core.parser.Annotations
import amf.core.unsafe.PlatformSecrets
import amf.core.validation.{AMFValidationReport, AMFValidationResult, ValidationCandidate, ValidationShapeSet}
import amf.internal.resource.{ResourceLoader, ResourceLoaderAdapter}
import amf.client.validate.{
  ValidationCandidate => ClientValidationCandidate,
  ValidationShapeSet => ClientValidationShapeSet
}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CoreBaseConverter
    extends PlatformSecrets
    with CollectionConverter
    with FutureConverter
    with FieldConverter
    with CustomDomainPropertyConverter
    with ShapeConverter
    with PropertyShapeConverter
    with DataNodeConverter
    with DomainExtensionConverter
    with DeclarationsConverter
    with VariableValueConverter
    with ValidationConverter
    with DomainElementConverter
    with BaseUnitConverter
    with ResourceLoaderConverter
    with GraphDomainConverter
    with ValidationCandidateConverter
    with ValidationShapeSetConverter {

  implicit def asClient[Internal, Client](from: Internal)(
      implicit m: InternalClientMatcher[Internal, Client]): Client = m.asClient(from)

  implicit def asInternal[Internal, Client](from: Client)(
      implicit m: ClientInternalMatcher[Client, Internal]): Internal = m.asInternal(from)

  implicit object StringMatcher      extends IdentityMatcher[String]
  implicit object BooleanMatcher     extends IdentityMatcher[Boolean]
  implicit object IntMatcher         extends IdentityMatcher[Int]
  implicit object DoubleMatcher      extends IdentityMatcher[Double]
  implicit object FloatMatcher       extends IdentityMatcher[Float]
  implicit object AnyMatcher         extends IdentityMatcher[Any]
  implicit object UnitMatcher        extends IdentityMatcher[Unit]
  implicit object ProfileNameMatcher extends IdentityMatcher[ProfileName]

  implicit object ContentMatcher extends IdentityMatcher[Content]

  trait IdentityMatcher[T] extends InternalClientMatcher[T, T] with ClientInternalMatcher[T, T] {
    override def asClient(from: T): T   = from
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

trait FutureConverter {

  type ClientFuture[T]

  implicit class InternalFutureOps[Internal, Client](from: Future[Internal])(
      implicit m: InternalClientMatcher[Internal, Client]) {
    def asClient: ClientFuture[Client] = asClientFuture(from.map(m.asClient))
  }

  implicit class ClientFutureOps[Internal, Client](from: ClientFuture[Client])(
      implicit m: ClientInternalMatcher[Client, Internal]) {
    def asInternal: Future[Internal] = asInternalFuture(from, m)
  }

  protected def asClientFuture[T](from: Future[T]): ClientFuture[T]

  protected def asInternalFuture[Client, Internal](from: ClientFuture[Client],
                                                   matcher: ClientInternalMatcher[Client, Internal]): Future[Internal]
}

trait CollectionConverter {

  type ClientOption[E]
  type ClientList[E]
  type ClientMap[V]

  trait ClientOptionLike[E] {
    def isEmpty: Boolean
    def folded[B](ifEmpty: => B)(f: E => B): B
    def mapped[B](f: E => B): ClientOption[B]
    def getOrNull: E
    def getOrElse[B >: E](default: => B): B
  }

  implicit class InternalOptionOps[Internal, Client](from: Option[Internal])(
      implicit m: InternalClientMatcher[Internal, Client]) {
    def asClient: ClientOption[Client] = asClientOption(from, m)
  }

  implicit class ClientListOps[Internal, Client](from: ClientList[Client])(
      implicit m: ClientInternalMatcher[Client, Internal]) {
    def asInternal: Seq[Internal] = asInternalSeq(from, m)
  }

  implicit class ClientOptionOps[Client](from: ClientOption[Client]) {
    def toScala: Option[Client] = toScalaOption(from)
  }

  implicit class InternalMapOps[Internal, Client](from: mutable.Map[String, Internal])(
      implicit m: InternalClientMatcher[Internal, Client]) {
    def asClient: ClientMap[Client] = asClientMap(from, m)
  }

  implicit class InternalSeqOps[Internal, Client](from: Seq[Internal])(
      implicit m: InternalClientMatcher[Internal, Client]) {
    def asClient: ClientList[Client] = asClientList(from, m)
  }

  protected def asClientOption[Internal, Client](from: Option[Internal],
                                                 m: InternalClientMatcher[Internal, Client]): ClientOption[Client]

  protected def toScalaOption[E](from: ClientOption[E]): Option[E]

  protected def toClientOption[E](from: Option[E]): ClientOption[E]

  private[convert] def asClientList[Internal, Client](from: Seq[Internal],
                                                      m: InternalClientMatcher[Internal, Client]): ClientList[Client]

  protected def asClientMap[Internal, Client](from: mutable.Map[String, Internal],
                                              m: InternalClientMatcher[Internal, Client]): ClientMap[Client]

  protected def asInternalSeq[Client, Internal](from: ClientList[Client],
                                                m: ClientInternalMatcher[Client, Internal]): Seq[Internal]
}

trait FieldConverter extends CollectionConverter {

  implicit object StrFieldMatcher extends InternalClientMatcher[StrField, ClientStrField] {
    override def asClient(from: StrField): ClientStrField = ClientStrField(from)
  }

  implicit object IntFieldMatcher extends InternalClientMatcher[IntField, ClientIntField] {
    override def asClient(from: IntField): ClientIntField = ClientIntField(from)
  }

  implicit object BoolFieldMatcher extends InternalClientMatcher[BoolField, ClientBoolField] {
    override def asClient(from: BoolField): ClientBoolField = ClientBoolField(from)
  }

  implicit object DoubleFieldMatcher extends InternalClientMatcher[DoubleField, ClientDoubleField] {
    override def asClient(from: DoubleField): ClientDoubleField = ClientDoubleField(from)
  }

  implicit object FloatFieldMatcher extends InternalClientMatcher[FloatField, ClientFloatField] {
    override def asClient(from: FloatField): ClientFloatField = ClientFloatField(from)
  }

  implicit object AnyFieldMatcher extends InternalClientMatcher[AnyField, ClientAnyField] {
    override def asClient(from: AnyField): ClientAnyField = ClientAnyField(from)
  }

  implicit object AnnotationsFieldMatcher extends BidirectionalMatcher[Annotations, ClientAnnotations] {
    override def asClient(from: Annotations): ClientAnnotations = ClientAnnotations(from)

    override def asInternal(from: ClientAnnotations): Annotations = from._internal
  }
}

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
      case _ => // noinspection ScalaStyle
        null
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

trait ValidationConverter {
  implicit object ValidationReportMatcher extends BidirectionalMatcher[AMFValidationReport, ClientValidatorReport] {
    override def asClient(from: AMFValidationReport): ClientValidatorReport = new ClientValidatorReport(from)

    override def asInternal(from: ClientValidatorReport): AMFValidationReport = from._internal
  }

  implicit object ValidationResultMatcher extends BidirectionalMatcher[AMFValidationResult, ClientValidationResult] {
    override def asClient(from: AMFValidationResult): ClientValidationResult = new ClientValidationResult(from)

    override def asInternal(from: ClientValidationResult): AMFValidationResult = from._internal
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

trait GraphDomainConverter {

  implicit object GraphDomainConverter extends BidirectionalMatcher[Graph, ClientGraph] {
    override def asInternal(from: ClientGraph): Graph = from._internal

    override def asClient(from: Graph): ClientGraph = ClientGraph(from)
  }

}

trait ResourceLoaderConverter {
  type ClientLoader <: ClientResourceLoader
  type Loader

  implicit object ResourceLoaderMatcher extends BidirectionalMatcher[ResourceLoader, ClientResourceLoader] {
    override def asClient(from: ResourceLoader): ClientResourceLoader = from match {
      case ResourceLoaderAdapter(adaptee) => adaptee
    }

    override def asInternal(from: ClientResourceLoader): ResourceLoader = ResourceLoaderAdapter(from)
  }

//  implicit class ClientLoaderOps[Internal, Client](from: ClientLoader)(
//    implicit m: ClientInternalMatcher[Client, Internal]) {
//    def asInternal: ResourceLoader = asInternalLoader(from, m)
//  }
//
//  protected def asInternalLoader[Client, Internal](from: ClientLoader,
//                                                   matcher: ClientInternalMatcher[Client, Internal]): ResourceLoader
}

trait ValidationCandidateConverter {

  implicit object ValidationCandidateMatcher
      extends BidirectionalMatcher[ValidationCandidate, ClientValidationCandidate] {
    override def asClient(from: ValidationCandidate): ClientValidationCandidate = ClientValidationCandidate(from)

    override def asInternal(from: ClientValidationCandidate): ValidationCandidate = from._internal
  }
}

trait ValidationShapeSetConverter {

  implicit object ValidationShapeSetMatcher
      extends BidirectionalMatcher[ValidationShapeSet, ClientValidationShapeSet] {
    override def asClient(from: ValidationShapeSet): ClientValidationShapeSet = ClientValidationShapeSet(from)

    override def asInternal(from: ClientValidationShapeSet): ValidationShapeSet = from._internal
  }
}
