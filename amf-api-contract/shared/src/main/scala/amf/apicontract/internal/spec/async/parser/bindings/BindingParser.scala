package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.BindingVersion
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.core.client.scala.model.domain.{AmfElement, AmfObject, AmfScalar, DomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec._
import amf.shapes.internal.spec.common.JSONSchemaDraft7SchemaVersion
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.RequiredField
import org.yaml.model.{YMap, YMapEntry, YNode, YNodePlain, YPart, YScalar, YSequence}

trait BindingParser[+Binding <: DomainElement] extends SpecParserOps {

  def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding

  protected def parseBindingVersion(binding: BindingVersion, field: Field, map: YMap)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    map.key("bindingVersion", field in binding)

    if (bindingVersionIsEmpty(binding)) setDefaultBindingVersionValue(binding, field)
  }

  protected def getBindingVersion(map: YMap, binding: String, spec: Spec): String = {
    extractValidBindingVersion(map) match {
      case Some(version) => version
      case None          => getDefaultBindingVersion(binding, spec)
    }
  }

  protected def extractValidBindingVersion(map: YMap): Option[String] = map.key("bindingVersion") match {
    case Some(value) =>
      value.value match {
        case plain: YNodePlain =>
          plain.value match {
            case scalar: YScalar if isSemVer(scalar.text) => Some(scalar.text)
            case _                                        => None
          }
        case _ => None
      }
    case None => None
  }

  protected def isSemVer(str: String): Boolean = {
    val regex = """^([0-9]+)\.([0-9]+)\.([0-9]+)$""".r
    regex.findFirstIn(str).isDefined
  }

  protected def getDefaultBindingVersion(binding: String, spec: Spec): String = {
    (binding, spec) match {
      case ("Amqp091ChannelBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)   => "0.1.0"
      case ("Amqp091OperationBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.1.0"
      case ("Amqp091MessageBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)   => "0.1.0"
      case("AnypointMQMessageBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.0.1"
    }
  }

  protected def setDefaultValue(obj: AmfObject, field: Field, element: AmfElement): obj.type = {
    // TODO: if field not explicit in spec we should check for synthesized and not emit it
    obj.setWithoutId(field, element, Annotations.synthesized())
  }

  private def setDefaultBindingVersionValue(binding: BindingVersion, field: Field) = {
    binding.setWithoutId(field, AmfScalar("latest"), Annotations.synthesized())
  }

  private def bindingVersionIsEmpty(binding: BindingVersion) = {
    binding.bindingVersion.isNullOrEmpty
  }

  protected def parseSchema(field: Field, binding: DomainElement, entry: YPart)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    val entryLike = entry match {
      case map: YMapEntry => YMapEntryLike(map.value)
      case node: YNode => YMapEntryLike(node)
    }
    OasTypeParser(
      entryLike,
      "schema",
      shape => shape.withName("schema"),
      JSONSchemaDraft7SchemaVersion
    )
      .parse()
      .foreach { shape =>
        binding.setWithoutId(field, shape, Annotations(entry))
        shape
      }
  }

  protected def missingRequiredFieldViolation(
      ctx: AsyncWebApiContext,
      node: AmfObject,
      missingField: String,
      schema: String
  ): Unit = {
    ctx.violation(RequiredField, node, s"field '$missingField' is required in a $schema")
  }

}
