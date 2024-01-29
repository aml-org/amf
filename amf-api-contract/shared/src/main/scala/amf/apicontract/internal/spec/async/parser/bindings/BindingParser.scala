package amf.apicontract.internal.spec.async.parser.bindings

import amf.apicontract.client.scala.model.domain.bindings.BindingVersion
import amf.apicontract.internal.spec.async.parser.context.AsyncWebApiContext
import amf.apicontract.internal.spec.common.parser.SpecParserOps
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.metamodel.Field
import amf.core.internal.parser.YMapOps
import amf.core.internal.parser.domain.Annotations
import amf.shapes.internal.spec.common.JSONSchemaDraft7SchemaVersion
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import amf.shapes.internal.spec.oas.parser.OasTypeParser
import org.yaml.model.{YMap, YMapEntry}

trait BindingParser[Binding <: DomainElement] extends SpecParserOps {

  def parse(entry: YMapEntry, parent: String)(implicit ctx: AsyncWebApiContext): Binding

  protected def parseBindingVersion(binding: BindingVersion, field: Field, map: YMap)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    map.key("bindingVersion", field in binding)

    if (bindingVersionIsEmpty(binding)) setDefaultBindingVersionValue(binding, field)
  }

  private def setDefaultBindingVersionValue(binding: BindingVersion, field: Field) = {
    binding.setWithoutId(field, AmfScalar("latest"), Annotations.synthesized())
  }

  private def bindingVersionIsEmpty(binding: BindingVersion) = {
    binding.bindingVersion.isNullOrEmpty
  }

  protected def parseSchema(field: Field, binding: DomainElement, entry: YMapEntry)(implicit
      ctx: AsyncWebApiContext
  ): Unit = {
    OasTypeParser(
      YMapEntryLike(entry.value),
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
}
