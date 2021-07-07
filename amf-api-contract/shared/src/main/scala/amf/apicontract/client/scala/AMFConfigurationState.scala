package amf.apicontract.client.scala

import amf.aml.client.scala.AMLConfigurationState

/* Contains methods to get information about the current state of the configuration */
class AMFConfigurationState private[amf] (override protected val configuration: AMFConfiguration)
    extends AMLConfigurationState(configuration) {}
