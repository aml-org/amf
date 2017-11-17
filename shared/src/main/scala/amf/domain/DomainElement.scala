package amf.domain

import amf.domain.Annotation.LexicalInformation
import amf.domain.`abstract`.{ParametrizedDeclaration, ParametrizedResourceType, ParametrizedTrait}
import amf.domain.extensions.DomainExtension
import amf.metadata.Field
import amf.metadata.domain.DomainElementModel._
import amf.metadata.domain.LinkableElementModel
import amf.model.{AmfArray, AmfElement, AmfObject, AmfScalar}
import amf.spec.{DeclarationPromise, Declarations, ParserContext}
import amf.vocabulary.{Namespace, ValueType}
import org.yaml.model.YPart

trait Linkable extends AmfObject { this: DomainElement with Linkable =>
  var linkTarget: Option[DomainElement]    = None
  var linkAnnotations: Option[Annotations] = None

  def isLink: Boolean           = linkTarget.isDefined
  def linkLabel: Option[String] = Option(fields(LinkableElementModel.Label))

  def linkCopy(): Linkable

  def withLinkTarget(target: DomainElement): this.type = {
    linkTarget = Some(target)
    set(LinkableElementModel.TargetId, target.id)
  }

  def withLinkLabel(label: String): this.type = set(LinkableElementModel.Label, label)

  def link[T](label: String, annotations: Annotations = Annotations()): T = {
    linkCopy()
      .withLinkTarget(this)
      .withLinkLabel(label)
      .add(annotations)
      .asInstanceOf[T]
  }

  // Unresolved references to things that can be linked
  // TODO: another trait?
  var isUnresolved: Boolean = false
  var refName = ""
  var refAst: Option[YPart] = None
  var refCtx: Option[ParserContext] = None

  def unresolved(refName: String, refAst: YPart)(implicit ctx: ParserContext) = {
    isUnresolved = true
    this.refName = refName
    this.refAst = Some(refAst)
    refCtx = Some(ctx)
    this
  }

  def toFutureRef(resolve:(Linkable) => Unit) = {
    refCtx match {
      case Some(ctx) => ctx.declarations.futureRef(refName, DeclarationPromise(
        resolve,
        () => ctx.violation(id, s"Unresolved reference $refName from root context ${ctx.rootContextDocument}", refAst.get)
      ))
      case none => throw new Exception("Cannot create unresolved reference with missing parsing context")
    }
  }
}

/**
  * Internal model for any domain element
  */
trait DomainElement extends AmfObject {
  def customDomainProperties: Seq[DomainExtension] = fields(CustomDomainProperties)
  def extend: Seq[DomainElement]                   = fields(Extends)

  def withCustomDomainProperties(customProperties: Seq[DomainExtension]): this.type =
    setArray(CustomDomainProperties, customProperties)

  def withExtends(extend: Seq[DomainElement]): this.type = setArray(Extends, extend)

  def withResourceType(name: String): ParametrizedResourceType = {
    val result = ParametrizedResourceType().withName(name)
    add(Extends, result)
    result
  }

  def withTrait(name: String): ParametrizedTrait = {
    val result = ParametrizedTrait().withName(name)
    add(Extends, result)
    result
  }

  def getTypeIds(): List[String] = dynamicTypes().toList ++ `type`.map(_.iri())

  def getPropertyIds(): List[String] = fields.fields().map(f => f.field.value.iri()).toList

  def getScalarByPropertyId(propertyId: String): List[Any] = {
    fields.fields().find { f: FieldEntry =>
      f.field.value.iri() == Namespace.uri(propertyId).iri()
    } match {
      case Some(fieldEntry) =>
        fieldEntry.element match {
          case scalar: AmfScalar                    => List(scalar.value)
          case arr: AmfArray if arr.values.nonEmpty => arr.values.toList
          case _                                    => List()
        }
      case None => List()
    }
  }

  def getObjectByPropertyId(propertyId: String): Seq[DomainElement] = {
    fields.fields().find { f: FieldEntry =>
      f.field.value.iri() == Namespace.uri(propertyId).iri()
    } match {
      case Some(fieldEntry) =>
        fieldEntry.element match {
          case entity: DomainElement => List(entity)
          case arr: AmfArray if arr.values.nonEmpty && arr.values.head.isInstanceOf[DomainElement] =>
            arr.values.map(_.asInstanceOf[DomainElement]).toList
          case _ => List()
        }
      case None => List()
    }
  }

  def position(): Option[amf.parser.Range] = annotations.find(classOf[LexicalInformation]) match {
    case Some(info) => Some(info.range)
    case _          => None
  }
}

trait DynamicDomainElement extends DomainElement {
  def dynamicFields: List[Field]
  def dynamicType: List[ValueType]

  // this is used to generate the graph
  def valueForField(f: Field): Option[AmfElement]
}
