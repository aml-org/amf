package amf.emit

import amf.client.environment.AMFConfiguration
import amf.client.remod.amfcore.config.RenderOptions
import amf.compiler.CompilerTestBuilder
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.io.FunSuiteCycleTests
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
    "Simple library raml"                         -> ("libraries.raml", Raml10YamlHint)                -> ("lib/lib.raml", Raml10),
    "Simple library oas"                          -> ("libraries.json", Oas20JsonHint)                 -> ("lib/lib.json", Oas20),
    "Library raml to oas"                         -> ("libraries.raml", Raml10YamlHint)                -> ("lib/lib.raml.json", Oas20),
    "Library oas to raml"                         -> ("libraries.json", Oas20JsonHint)                 -> ("lib/lib.json.raml", Raml10),
    "Library raml to amf"                         -> ("libraries.raml", Raml10YamlHint)                -> ("lib/lib.raml.%s", Amf),
    "Library oas to amf"                          -> ("libraries.json", Oas20JsonHint)                 -> ("lib/lib.json.%s", Amf),
    "Library amf to oas from include"             -> ("libraries.json.%s", AmfJsonHint)                -> ("lib/lib.json", Oas20),
    "Library amf to raml from include"            -> ("libraries.raml.%s", AmfJsonHint)                -> ("lib/lib.raml", Raml10),
    "Data type fragment raml to raml"             -> ("data-type-fragment.raml", Raml10YamlHint)       -> ("fragments/person.raml", Raml10),
    "Data type fragment oas to oas"               -> ("data-type-fragment.json", Oas20JsonHint)        -> ("fragments/person.json", Oas20),
    "Data type fragment raml to oas"              -> ("data-type-fragment.raml", Raml10YamlHint)       -> ("fragments/person.json", Oas20),
    "Data type fragment oas to raml"              -> ("data-type-fragment.json", Oas20JsonHint)        -> ("fragments/person.json.raml", Raml10),
    "Data type fragment amf to raml from include" -> ("data-type-fragment.raml.%s", AmfJsonHint)       -> ("fragments/person.raml", Raml10),
    "Data type fragment amf to oas from include"  -> ("data-type-fragment.json.%s", AmfJsonHint)       -> ("fragments/person.json", Oas20),
    "Resource type fragment raml to raml"         -> ("resource-type-fragment.raml", Raml10YamlHint)   -> ("fragments/resource-type.raml", Raml10),
    "Trait fragment raml to raml"                 -> ("trait-fragment.raml", Raml10YamlHint)           -> ("fragments/trait.raml", Raml10),
    "Alias library reference raml test"           -> ("lib-alias-reference.raml", Raml10YamlHint)      -> ("lib/lib-declaration.raml", Raml10),
    "Security schemes fragment raml to raml"      -> ("security-scheme-fragment.raml", Raml10YamlHint) -> ("fragments/security-scheme.raml", Raml10),
    "Security schemes fragment oas to oas"        -> ("security-scheme-fragment.json", Oas20JsonHint)  -> ("fragments/security-scheme.json", Oas20),
    //TODO this should be emitted without the '-'? All external fragments (now NamedExample is parsed as an external fragment) are emitted as a String in YAML
    "Named Example fragment raml to raml" -> ("named-example.raml", Raml10YamlHint)     -> ("fragments/named-example.raml.raml", Raml10),
    "Named Example fragment oas to oas"   -> ("named-example.json", Oas20JsonHint)      -> ("fragments/named-example.json", Oas20),
    "External fragment raml to raml"      -> ("external-fragment.raml", Raml10YamlHint) -> ("fragments/external-fragment.raml.raml", Raml10)
  )

  fixture.foreach {
    case ((title, (document, hint)), (reference, Amf)) =>
      multiGoldenTest(title, reference) { config =>
        val amfConfig = buildConfig(None, None)
        build(s"file://$basePath$document", hint, amfConfig, None)
          .flatMap(renderReference(config.golden, Amf, _, amfConfig.withRenderOptions(config.renderOptions)))
          .flatMap(checkDiff(_, fs.asyncFile(s"$basePath${config.golden}")))
      }
    case ((title, (document, AmfJsonHint)), (reference, vendor)) =>
      multiSourceTest(title, document) { config =>
        val amfConfig = buildConfig(None, None)

        build(s"file://$basePath${config.source}", AmfJsonHint, amfConfig, None)
          .flatMap(renderReference(reference, vendor, _, amfConfig.withRenderOptions(defaultRenderOptions)))
          .flatMap(checkDiff(_, fs.asyncFile(s"$basePath$reference")))
      }
    case ((title, (document, hint)), (reference, vendor)) =>
      test(title) {
        val amfConfig = buildConfig(None, None)
        build(s"file://$basePath$document", hint, amfConfig, None)
          .flatMap(renderReference(reference, vendor, _, amfConfig.withRenderOptions(defaultRenderOptions)))
          .flatMap(checkDiff(_, fs.asyncFile(basePath + reference)))
      }
  }

  private def renderReference(reference: String,
                              vendor: Vendor,
                              unit: BaseUnit,
                              amfConfig: AMFConfiguration): Future[AsyncFile] = {
    val ref      = unit.references.head
    val actual   = fs.asyncFile(tmp(reference.replace("/", "--")))
    val rendered = amfConfig.createClient().render(ref, vendor.mediaType)
    actual.write(rendered).map(_ => actual)
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps

  case class ModuleContent(url: String, content: String)

}
