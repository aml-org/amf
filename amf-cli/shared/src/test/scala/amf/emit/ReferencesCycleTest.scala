package amf.emit

import amf.apicontract.client.scala.AMFConfiguration
import amf.compiler.CompilerTestBuilder
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote._
import amf.io.FunSuiteCycleTests
import amf.testing.ConfigProvider.configFor
import org.mulesoft.common.io.AsyncFile
import org.mulesoft.common.test.ListAssertions
import org.mulesoft.common.test.Tests.checkDiff

import scala.concurrent.Future

/**
  * Created by hernan.najles on 9/19/17.
  */
class ReferencesCycleTest extends FunSuiteCycleTests with ListAssertions with CompilerTestBuilder {

  override val basePath = "amf-cli/shared/src/test/resources/references/"

  private val fixture: Seq[((String, (String, Hint)), (String, Hint))] = Seq(
    "Simple library raml"                         -> ("libraries.raml", Raml10YamlHint)                -> ("lib/lib.raml", Raml10YamlHint),
    "Simple library oas"                          -> ("libraries.json", Oas20JsonHint)                 -> ("lib/lib.json", Oas20JsonHint),
    "Library raml to oas"                         -> ("libraries.raml", Raml10YamlHint)                -> ("lib/lib.raml.json", Oas20JsonHint),
    "Library oas to raml"                         -> ("libraries.json", Oas20JsonHint)                 -> ("lib/lib.json.raml", Raml10YamlHint),
    "Library raml to amf"                         -> ("libraries.raml", Raml10YamlHint)                -> ("lib/lib.raml.%s", AmfJsonHint),
    "Library oas to amf"                          -> ("libraries.json", Oas20JsonHint)                 -> ("lib/lib.json.%s", AmfJsonHint),
    "Library amf to oas from include"             -> ("libraries.json.%s", AmfJsonHint)                -> ("lib/lib.json", Oas20JsonHint),
    "Library amf to raml from include"            -> ("libraries.raml.%s", AmfJsonHint)                -> ("lib/lib.raml", Raml10YamlHint),
    "Data type fragment raml to raml"             -> ("data-type-fragment.raml", Raml10YamlHint)       -> ("fragments/person.raml", Raml10YamlHint),
    "Data type fragment oas to oas"               -> ("data-type-fragment.json", Oas20JsonHint)        -> ("fragments/person.json", Oas20JsonHint),
    "Data type fragment raml to oas"              -> ("data-type-fragment.raml", Raml10YamlHint)       -> ("fragments/person.json", Oas20JsonHint),
    "Data type fragment oas to raml"              -> ("data-type-fragment.json", Oas20JsonHint)        -> ("fragments/person.json.raml", Raml10YamlHint),
    "Data type fragment amf to raml from include" -> ("data-type-fragment.raml.%s", AmfJsonHint)       -> ("fragments/person.raml", Raml10YamlHint),
    "Data type fragment amf to oas from include"  -> ("data-type-fragment.json.%s", AmfJsonHint)       -> ("fragments/person.json", Oas20JsonHint),
    "Resource type fragment raml to raml"         -> ("resource-type-fragment.raml", Raml10YamlHint)   -> ("fragments/resource-type.raml", Raml10YamlHint),
    "Trait fragment raml to raml"                 -> ("trait-fragment.raml", Raml10YamlHint)           -> ("fragments/trait.raml", Raml10YamlHint),
    "Alias library reference raml test"           -> ("lib-alias-reference.raml", Raml10YamlHint)      -> ("lib/lib-declaration.raml", Raml10YamlHint),
    "Security schemes fragment raml to raml"      -> ("security-scheme-fragment.raml", Raml10YamlHint) -> ("fragments/security-scheme.raml", Raml10YamlHint),
    "Security schemes fragment oas to oas"        -> ("security-scheme-fragment.json", Oas20JsonHint)  -> ("fragments/security-scheme.json", Oas20JsonHint),
    //TODO this should be emitted without the '-'? All external fragments (now NamedExample is parsed as an external fragment) are emitted as a String in YAML
    "Named Example fragment raml to raml" -> ("named-example.raml", Raml10YamlHint)     -> ("fragments/named-example.raml.raml", Raml10YamlHint),
    "Named Example fragment oas to oas"   -> ("named-example.json", Oas20JsonHint)      -> ("fragments/named-example.json", Oas20JsonHint),
    "External fragment raml to raml"      -> ("external-fragment.raml", Raml10YamlHint) -> ("fragments/external-fragment.raml.raml", Raml10YamlHint)
  )

  fixture.foreach {
    case ((title, (document, hint)), (reference, AmfJsonHint)) =>
      multiGoldenTest(title, reference) { config =>
        val amfConfig = buildConfig(configFor(hint.spec), None, None)
        build(s"file://$basePath$document", hint, amfConfig, None)
          .flatMap(renderReference(config.golden, AmfJsonHint, _, amfConfig.withRenderOptions(config.renderOptions)))
          .flatMap(checkDiff(_, fs.asyncFile(s"$basePath${config.golden}")))
      }
    case ((title, (document, AmfJsonHint)), (reference, hint)) =>
      multiSourceTest(title, document) { config =>
        val amfConfig = buildConfig(configFor(hint.spec), None, None)
        build(s"file://$basePath${config.source}", AmfJsonHint, amfConfig, None)
          .flatMap(renderReference(reference, hint, _, amfConfig.withRenderOptions(defaultRenderOptions)))
          .flatMap(checkDiff(_, fs.asyncFile(s"$basePath$reference")))
      }
    case ((title, (document, hint)), (reference, targetHint)) =>
      test(title) {
        val parseConfig  = buildConfig(configFor(hint.spec), None, None)
        val renderConfig = buildConfig(configFor(targetHint.spec), None, None)
        build(s"file://$basePath$document", hint, parseConfig, None)
          .flatMap(renderReference(reference, targetHint, _, renderConfig.withRenderOptions(defaultRenderOptions)))
          .flatMap(checkDiff(_, fs.asyncFile(basePath + reference)))
      }
  }

  private def renderReference(reference: String,
                              targetHint: Hint,
                              unit: BaseUnit,
                              amfConfig: AMFConfiguration): Future[AsyncFile] = {
    val ref      = unit.references.head
    val actual   = fs.asyncFile(tmp(reference.replace("/", "--")))
    val rendered = amfConfig.baseUnitClient().render(ref, targetHint.syntax.mediaType)
    actual.write(rendered).map(_ => actual)
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps

  case class ModuleContent(url: String, content: String)

}
