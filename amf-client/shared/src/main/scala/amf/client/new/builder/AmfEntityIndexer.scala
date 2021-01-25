package amf.client.`new`.builder

import amf.client.`new`.EntitiesRegistry

trait AmfEntityIndexer {
  // register core?
  //register web api?
  def build: EntitiesRegistry
}

//which options?
object AmfCompleteEntityIndexer extends AmfEntityIndexer {
  def withAll() = {
    // registrer alll
  }

  override def build: EntitiesRegistry = new EntitiesRegistry()
}
