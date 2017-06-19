package amf.client

import amf.remote.JvmPlatform

class JvmClient extends BaseClient {
  override protected val remote = JvmPlatform()
}
