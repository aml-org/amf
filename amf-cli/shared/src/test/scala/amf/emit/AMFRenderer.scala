package amf.emit

import amf.aml.internal.utils.VocabulariesRegister
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.Syntax.Syntax
import amf.core.internal.remote.{Hint, Platform, Vendor}
import amf.core.internal.render.AMFSerializer
import amf.core.internal.unsafe.PlatformSecrets
import amf.testing.ConfigProvider.configFor
import amf.testing.Target

import scala.concurrent.{ExecutionContext, Future}

// TODO: this is only here for compatibility with the test suite
class AMFRenderer(unit: BaseUnit, target: Hint, options: RenderOptions) extends PlatformSecrets {

  // Remod registering
  VocabulariesRegister.register(platform)

  /** Print ast to string. */
  def renderToString(implicit executionContext: ExecutionContext): String = render()

  /** Print ast to file. */
  def renderToFile(remote: Platform, path: String)(implicit executionContext: ExecutionContext): Future[Unit] = {
    val result = render()
    remote.write(path, result)
  }

  private def render()(implicit executionContext: ExecutionContext): String = {
    val config = configFor(target.vendor).withRenderOptions(options)
    new AMFSerializer(unit, target.syntax.mediaType, config.renderConfiguration).renderToString
  }
}

object AMFRenderer {
  def apply(unit: BaseUnit, target: Hint, options: RenderOptions, syntax: Option[Syntax] = None): AMFRenderer =
    new AMFRenderer(unit, target, options)
}
