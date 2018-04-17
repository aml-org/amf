package amf.client.resource

import amf.core.unsafe.PlatformBuilder

/*object HttpResourceLoader {
  def apply(): JsHttpResourceLoader = PlatformBuilder.platform.http()
}*/

trait JsHttpResourceLoader extends BaseHttpResourceLoader

trait JsFileResourceLoader extends BaseFileResourceLoader
