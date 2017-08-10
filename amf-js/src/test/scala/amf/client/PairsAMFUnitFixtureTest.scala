package amf.client

import amf.emit.AMFUnitFixtureTest
import amf.model.{Document, WebApi}

/**
  *
  */
trait PairsAMFUnitFixtureTest extends AMFUnitFixtureTest {

  def webApiBare: WebApi = {
    WebApi(`document/api/bare`.encodes)
  }

  def webApiAdvanced: WebApi = {
    WebApi(`document/api/advanced`.encodes)
  }

  def unitBare: Document = {
    Document(super.`document/api/bare`)
  }

  def unitAdvanced: Document = {
    Document(super.`document/api/advanced`)
  }
}
