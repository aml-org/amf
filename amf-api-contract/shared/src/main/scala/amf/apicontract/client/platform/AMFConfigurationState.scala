package amf.apicontract.client.platform

import amf.aml.client.platform.AMLConfigurationState
import amf.apicontract.client.scala.{AMFConfigurationState => InternalAMFConfigurationState}

/* Contains methods to get information about the current state of the configuration */
class AMFConfigurationState private[amf] (private val _internal: InternalAMFConfigurationState)
    extends AMLConfigurationState(_internal) {

  private[amf] def this(configuration: AMFConfiguration) = {
    this(new InternalAMFConfigurationState(configuration._internal))
  }

}
