package amf.metadata

import amf.vocabulary.Namespace

/**
  * Field
  */
case class Field(`type`: Type, namespace: Namespace, name: String)
