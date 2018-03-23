package amf.plugins.domain.webapi.resolution

import amf.ProfileNames
import amf.core.annotations.{Aliases, LexicalInformation}
import amf.core.emitter.SpecOrdering
import amf.core.model.document.{BaseUnit, DeclaresModel, Fragment, Module}
import amf.core.model.domain.{AmfArray, DataNode, DomainElement, NamedDomainElement}
import amf.core.parser.ParserContext
import amf.core.resolution.stages.ResolvedNamedEntity
import amf.core.services.RuntimeValidator
import amf.plugins.document.webapi.annotations.ExtendedField
import amf.plugins.document.webapi.contexts.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.plugins.document.webapi.parser.spec.declaration.DataNodeEmitter
import amf.plugins.domain.webapi.models.{EndPoint, Operation}
import org.yaml.model._

import scala.collection.mutable.ListBuffer

object ExtendsHelper {
  def custom(profile: String): RamlWebApiContext = profile match {
    case ProfileNames.RAML08 => new CustomRaml08WebApiContext()
    case _                   => new CustomRaml10WebApiContext()
  }

  def asOperation[T <: BaseUnit](profile: String,
                                 node: DataNode,
                                 unit: T,
                                 extensionId: String,
                                 keepEditingInfo: Boolean,
                                 context: Option[RamlWebApiContext] = None): Operation = {
    val ctx = context.getOrElse(custom(profile))

    val document = YDocument {
      _.obj {
        _.entry(
          "extends",
          DataNodeEmitter(node, SpecOrdering.Default).emit(_)
        )
      }
    }

    val entry = document.as[YMap].entries.head
    declarations(ctx, unit)
    val operation = ctx.factory.operationParser(entry, _ => Operation(), true).parse()
    if (keepEditingInfo) annotateExtensionId(operation, extensionId)
    operation
  }

  def asEndpoint[T <: BaseUnit](unit: T,
                                profile: String,
                                dataNode: DataNode,
                                name: String,
                                extensionId: String,
                                keepEditingInfo: Boolean,
                                context: Option[RamlWebApiContext] = None): EndPoint = {
    val ctx = context.getOrElse(custom(profile))

    val document = YDocument {
      _.obj {
        _.entry(
          "/endpoint",
          DataNodeEmitter(dataNode, SpecOrdering.Default).emit(_)
        )
      }
    }
    val endPointEntry = document.as[YMap].entries.head
    val collector     = ListBuffer[EndPoint]()

    declarations(ctx, unit)

    val validator = RuntimeValidator.validator
    ctx.factory.endPointParser(endPointEntry, _ => EndPoint(), None, collector, true).parse()
    validator.foreach(RuntimeValidator.register)

    collector.toList match {
      case e :: Nil =>
        if (keepEditingInfo) annotateExtensionId(e, extensionId)
        e
      case Nil      => throw new Exception(s"Couldn't parse an endpoint from resourceType '$name'.")
      case _        => throw new Exception(s"Nested endpoints found in resourceType '$name'.")
    }
  }

  private def annotateExtensionId(point: DomainElement, extensionId: String): Unit = {
    val extendedFieldAnnotation = ExtendedField(extensionId)
    point.fields.fields().foreach { field =>
      field.value.annotations += extendedFieldAnnotation
      field.value.value match {
        case elem: DomainElement => annotateExtensionId(elem, extensionId)
        case arr: AmfArray => arr.values.foreach {
          case elem: DomainElement =>
            elem.annotations += extendedFieldAnnotation
            annotateExtensionId(point, extensionId)
          case other                   =>
            other.annotations += extendedFieldAnnotation
        }
        case  scalar => scalar.annotations += extendedFieldAnnotation
      }
    }
  }

  private def declarations(ctx: RamlWebApiContext, model: BaseUnit): Unit = {
    model match {
      case d: DeclaresModel =>
        d.declares.foreach { declaration =>
          ctx.declarations += declaration
          processDeclaration(declaration, ctx, model)
        }
      case _ =>
    }
    nestedDeclarations(ctx, model)
  }

  private def nestedDeclarations(ctx: RamlWebApiContext, model: BaseUnit): Unit = {
    model.references.foreach {
      case f: Fragment =>
        ctx.declarations += (f.location, f)
        nestedDeclarations(ctx, f)
      case m: Module if m.annotations.find(classOf[Aliases]).isDefined =>
        val nestedCtx = new Raml10WebApiContext(ParserContext())
        m.declares.foreach { declaration =>
          processDeclaration(declaration, nestedCtx, m)
        }
        m.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set())).aliases.map(_._1).foreach { alias =>
          ctx.declarations.libraries += (alias -> nestedCtx.declarations)
        }
        nestedDeclarations(ctx, m)
    }
  }

  private def processDeclaration(declaration: DomainElement, nestedCtx: RamlWebApiContext, model: BaseUnit): Unit = {
    declaration.annotations.find(classOf[ResolvedNamedEntity]) match {
      case Some(resolvedNamedEntity) =>
        resolvedNamedEntity.vals.foreach {
          case (_, namedEntities) =>
            val inContext = namedEntities.find(entity =>
              entity.isInstanceOf[DomainElement] && entity.asInstanceOf[DomainElement].id.contains(model.location))
            declaration match {
              // we recover the local alias we removed when resolving
              case element: NamedDomainElement if inContext.isDefined =>
                val localName = inContext.get.name.value()
                val realName  = element.name.value()
                element.withName(localName)
                nestedCtx.declarations += declaration
                element.withName(realName)
              case _ =>
                nestedCtx.declarations += declaration
            }
        }
      case _ => nestedCtx.declarations += declaration
    }
  }
}

class CustomRaml08WebApiContext extends Raml08WebApiContext(ParserContext()) {
  override def handle[T](error: YError, defaultValue: T): T = defaultValue
  override def violation(id: String,
                         node: String,
                         property: Option[String],
                         message: String,
                         lexical: Option[LexicalInformation]): Unit =
    super.violation(id, node, property, message, lexical)
  override def warning(id: String,
                       node: String,
                       property: Option[String],
                       message: String,
                       lexical: Option[LexicalInformation]): Unit = {}
  override def handle(node: YPart, e: SyamlException): Unit       = {}
}

class CustomRaml10WebApiContext extends Raml10WebApiContext(ParserContext()) {
  override def handle[T](error: YError, defaultValue: T): T = defaultValue
  override def violation(id: String,
                         node: String,
                         property: Option[String],
                         message: String,
                         lexical: Option[LexicalInformation]): Unit =
    super.violation(id, node, property, message, lexical)
  override def warning(id: String,
                       node: String,
                       property: Option[String],
                       message: String,
                       lexical: Option[LexicalInformation]): Unit = {}
  override def handle(node: YPart, e: SyamlException): Unit       = {}
}
