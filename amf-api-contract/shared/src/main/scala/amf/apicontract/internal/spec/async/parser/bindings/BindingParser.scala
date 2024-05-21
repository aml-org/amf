package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.BindingVersion
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.apicontract.internal.spec.spec.OasDefinitions
import amf.apicontract.internal.validation.definitions.ParserSideValidations.{
  UnsupportedBindingVersion,
  UnsupportedBindingVersionWarning
}
import amf.core.client.scala.model.domain.{AmfElement, AmfObject, AmfScalar, DomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.{Annotations, SearchScope}
import amf.core.internal.remote.Spec
import amf.core.internal.remote.Spec._
import amf.core.internal.validation.CoreValidations
import amf.shapes.internal.spec.common.JSONSchemaDraft7SchemaVersion
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.RequiredField
import org.yaml.model._

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
    regex.findFirstIn(str).isDefined || str == "latest"
  }

  protected def getDefaultBindingVersion(binding: String, spec: Spec): String = {
    (binding, spec) match {
      case ("Amqp091ChannelBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)    => "0.2.0"
      case ("Amqp091OperationBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)  => "0.2.0"
      case ("Amqp091MessageBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)    => "0.2.0"
      case ("AnypointMQMessageBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.0.1"
      case ("AnypointMQChannelBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.0.1"
      case ("KafkaOperationBinding", ASYNC20)                                                                => "0.1.0"
      case ("KafkaOperationBinding", ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)              => "0.3.0"
      case ("KafkaMessageBinding", ASYNC20)                                                                  => "0.1.0"
      case ("KafkaMessageBinding", ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)                => "0.3.0"

      // defined in 0.3.0 onwards
      case ("KafkaServerBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)  => "0.3.0"
      case ("KafkaChannelBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.3.0"

      case ("HttpOperationBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.1.0"
      case ("HttpMessageBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)   => "0.1.0"
      case ("GooglePubSubChannelBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) =>
        "0.1.0"
      case ("GooglePubSubMessageBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) =>
        "0.1.0"
      case ("MqttServerBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)    => "0.1.0"
      case ("MqttOperationBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.1.0"
      case ("MqttMessageBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26)   => "0.1.0"
      case("SolaceServerBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.3.0"
      case("SolaceOperationBinding", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.3.0"
      case("SolaceOperationDestination", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.3.0"
      case("SolaceOperationQueue", ASYNC20 | ASYNC21 | ASYNC22 | ASYNC23 | ASYNC24 | ASYNC25 | ASYNC26) => "0.3.0"
    }
  }

  protected def invalidBindingVersion(obj: AmfObject, version: String, binding: String, warning: Boolean = false)(
      implicit ctx: AsyncWebApiContext
  ): Unit = {
    if (warning) {
      ctx.eh.warning(
        UnsupportedBindingVersionWarning,
        obj,
        Some("bindingVersion"),
        s"Version $version is not supported in a $binding",
        obj.annotations.sourceLocation
      )
    } else {
      ctx.eh.violation(
        UnsupportedBindingVersion,
        obj,
        s"Version $version is not supported in a $binding",
        obj.annotations.sourceLocation
      )
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
      case node: YNode    => YMapEntryLike(node)
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

  protected def handleRef(fullRef: String, searchLabel: String, entry: YMapEntry, field: Field, binding: DomainElement)(
      implicit ctx: AsyncWebApiContext
  ): Unit = {
    val label = OasDefinitions.stripOas3ComponentsPrefix(fullRef, searchLabel)
    ctx.declarations
      .findType(label, SearchScope.Named)
      .map(shape => binding.setWithoutId(field, shape, Annotations(entry)))
      .getOrElse {
        remote(fullRef, entry, field, binding)
      }
  }

  private def remote(fullRef: String, entry: YMapEntry, field: Field, binding: DomainElement)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    ctx.navigateToRemoteYNode(fullRef) match {
      case Some(remoteResult) =>
        parseSchema(field, binding, remoteResult.remoteNode)
      case None =>
        ctx.eh.violation(
          CoreValidations.UnresolvedReference,
          binding,
          s"Cannot find link reference $fullRef",
          entry.location
        )
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

  def parseScalarOrRefOrSchema(binding: DomainElement, entry: YMapEntry, intField: Field, schemaField: Field)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    entry.value.tagType match {
      case YType.Int | YType.Str =>
        Some(entry).foreach(intField in binding)
      case YType.Map =>
        ctx.link(entry.value) match {
          case Left(fullRef) =>
            handleRef(fullRef, "schemas", entry, schemaField, binding)
          case Right(_) =>
            parseSchema(schemaField, binding, entry)
        }
    }
  }
}
