package amf.agentnetwork.internal.convert

import amf.core.internal.convert.CoreClientConverters

object AgentNetworkClientConverters extends AgentNetworkBaseConverter with AgentNetworkBaseClientConverter {
  // Overriding to match type
  override type ClientOption[E] = CoreClientConverters.ClientOption[E]
  override type ClientList[E]   = CoreClientConverters.ClientList[E]
  override type ClientFuture[T] = CoreClientConverters.ClientFuture[T]
  override type ClientLoader    = CoreClientConverters.ClientLoader
  override type ClientReference = CoreClientConverters.ClientReference
}
