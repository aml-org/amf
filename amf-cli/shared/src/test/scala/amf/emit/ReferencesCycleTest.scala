package amf.emit

import amf.apicontract.client.scala.AMFConfiguration
import amf.compiler.CompilerTestBuilder
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote._
import amf.io.FunSuiteCycleTests
import amf.testing.ConfigProvider.configFor
import amf.testing.{AmfJsonLd, Oas20Json, Raml10Yaml, Target}
import org.mulesoft.common.io.AsyncFile
import org.mulesoft.common.test.ListAssertions
import org.mulesoft.common.test.Tests.checkDiff

import scala.concurrent.Future

/**
  * Created by hernan.najles on 9/19/17.
  */
class ReferencesCycleTest extends FunSuiteCycleTests with ListAssertions with CompilerTestBuilder {

  override val basePath = "amf-cli/shared/src/test/resources/references/"

  private val fixture = Seq(
    "Simple library raml"                         -> ("libraries.raml", Raml10YamlHint)                -> ("lib/lib.raml", Raml10Yaml),
    "Simple library oas"                          -> ("libraries.json", Oas20JsonHint)                 -> ("lib/lib.json", Oas20Json),
    "Library raml to oas"                         -> ("libraries.raml", Raml10YamlHint)                -> ("lib/lib.raml.json", Oas20Json),
    "Library oas to raml"                         -> ("libraries.json", Oas20JsonHint)                 -> ("lib/lib.json.raml", Raml10Yaml),
    "Library raml to amf"                         -> ("libraries.raml", Raml10YamlHint)                -> ("lib/lib.raml.%s", AmfJsonLd),
    "Library oas to amf"                          -> ("libraries.json", Oas20JsonHint)                 -> ("lib/lib.json.%s", AmfJsonLd),
    "Library amf to oas from include"             -> ("libraries.json.%s", AmfJsonHint)                -> ("lib/lib.json", Oas20Json),
    "Library amf to raml from include"            -> ("libraries.raml.%s", AmfJsonHint)                -> ("lib/lib.raml", Raml10Yaml),
    "Data type fragment raml to raml"             -> ("data-type-fragment.raml", Raml10YamlHint)       -> ("fragments/person.raml", Raml10Yaml),
    "Data type fragment oas to oas"               -> ("data-type-fragment.json", Oas20JsonHint)        -> ("fragments/person.json", Oas20Json),
    "Data type fragment raml to oas"              -> ("data-type-fragment.raml", Raml10YamlHint)       -> ("fragments/person.json", Oas20Json),
    "Data type fragment oas to raml"              -> ("data-type-fragment.json", Oas20JsonHint)        -> ("fragments/person.json.raml", Raml10Yaml),
    "Data type fragment amf to raml from include" -> ("data-type-fragment.raml.%s", AmfJsonHint)       -> ("fragments/person.raml", Raml10Yaml),
    "Data type fragment amf to oas from include"  -> ("data-type-fragment.json.%s", AmfJsonHint)       -> ("fragments/person.json", Oas20Json),
    "Resource type fragment raml to raml"         -> ("resource-type-fragment.raml", Raml10YamlHint)   -> ("fragments/resource-type.raml", Raml10Yaml),
    "Trait fragment raml to raml"                 -> ("trait-fragment.raml", Raml10YamlHint)           -> ("fragments/trait.raml", Raml10Yaml),
    "Alias library reference raml test"           -> ("lib-alias-reference.raml", Raml10YamlHint)      -> ("lib/lib-declaration.raml", Raml10Yaml),
    "Security schemes fragment raml to raml"      -> ("security-scheme-fragment.raml", Raml10YamlHint) -> ("fragments/security-scheme.raml", Raml10Yaml),
    "Security schemes fragment oas to oas"        -> ("security-scheme-fragment.json", Oas20JsonHint)  -> ("fragments/security-scheme.json", Oas20Json),
    //TODO this should be emitted without the '-'? All external fragments (now NamedExample is parsed as an external fragment) are emitted as a String in YAML
    "Named Example fragment raml to raml" -> ("named-example.raml", Raml10YamlHint)     -> ("fragments/named-example.raml.raml", Raml10Yaml),
    "Named Example fragment oas to oas"   -> ("named-example.json", Oas20JsonHint)      -> ("fragments/named-example.json", Oas20Json),
    "External fragment raml to raml"      -> ("external-fragment.raml", Raml10YamlHint) -> ("fragments/external-fragment.raml.raml", Raml10Yaml)
  )

  fixture.foreach {
    case ((title, (document, hint)), (reference, AmfJsonLd)) =>
      multiGoldenTest(title, reference) { config =>
        val amfConfig = buildConfig(configFor(hint.vendor), None, None)
        build(s"file://$basePath$document", hint, amfConfig, None)
          .flatMap(renderReference(config.golden, AmfJsonLd, _, amfConfig.withRenderOptions(config.renderOptions)))
          .flatMap(checkDiff(_, fs.asyncFile(s"$basePath${config.golden}")))
      }
    case ((title, (document, AmfJsonHint)), (reference, vendor)) =>
      multiSourceTest(title, document) { config =>
        val amfConfig = buildConfig(configFor(vendor.spec), None, None)
        build(s"file://$basePath${config.source}", AmfJsonHint, amfConfig, None)
          .flatMap(renderReference(reference, vendor, _, amfConfig.withRenderOptions(defaultRenderOptions)))
          .flatMap(checkDiff(_, fs.asyncFile(s"$basePath$reference")))
      }
    case ((title, (document, hint)), (reference, vendor)) =>
      test(title) {
        val parseConfig  = buildConfig(configFor(hint.vendor), None, None)
        val renderConfig = buildConfig(configFor(vendor.spec), None, None)
        build(s"file://$basePath$document", hint, parseConfig, None)
          .flatMap(renderReference(reference, vendor, _, renderConfig.withRenderOptions(defaultRenderOptions)))
          .flatMap(checkDiff(_, fs.asyncFile(basePath + reference)))
      }
  }

  private def renderReference(reference: String,
                              vendor: Target,
                              unit: BaseUnit,
                              amfConfig: AMFConfiguration): Future[AsyncFile] = {
    val ref      = unit.references.head
    val actual   = fs.asyncFile(tmp(reference.replace("/", "--")))
    val rendered = amfConfig.baseUnitClient().render(ref, vendor.mediaType)
    actual.write(rendered).map(_ => actual)
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps

  case class ModuleContent(url: String, content: String)

}
