package amf.client.`new`.builder

import amf.ProfileName
import amf.client.`new`.{AmfRegistry, PluginsRegistry}
import amf.client.`new`.amfcore.{AmfParsePlugin, AmfResolvePlugin, AmfValidatePlugin}
import amf.client.`new`.amfcore.plugins.GuessingParsePlugin

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class AmfRegistryBuilder {
  protected val parsePlugins: ListBuffer[AmfParsePlugin]
  protected val resolvePlugins: ListBuffer[AmfResolvePlugin]
  protected val validatePlugins: ListBuffer[AmfValidatePlugin]
  protected val resolutionPipelines: mutable.Map[ProfileName, AmfResolutionPipelineFactory] = mutable.Map.empty

  protected val defaultPlugin: AmfParsePlugin   = GuessingParsePlugin // ?? default handling?
  protected val entityBuilder: AmfEntityIndexer = AmfCompleteEntityIndexer

  def newResolutionPipeline(profile: ProfileName): AmfResolutionPipelineFactory = {
    resolutionPipelines.getOrElse(profile, new AmfResolutionPipelineFactory(profile))
  }

  def build(): AmfRegistry = {

    AmfRegistry(
      PluginsRegistry(parsePlugins.toMap, resolvePlugins.toMap, validatePlugins.toMap, defaultPlugin),
      entityBuilder.build,
      resolutionPipelines.map(k => k._1 -> k._2.build()).toMap
    )
  }
}
