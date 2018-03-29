package amf.core.remote

import amf.core.remote.Syntax.{Json, PlainText, Syntax, Yaml}

/**
  * Created by pedro.colunga on 10/9/17.
  */
object Vendor {
  def unapply(name: String): Option[Vendor] = {
    name match {
      case Raml10.name  => Some(Raml10)
      case Raml08.name  => Some(Raml08)
      case Raml.name    => Some(Raml) // todo remove later
      case Oas2.name    => Some(Oas2)
      case Oas3.name    => Some(Oas3)
      case Oas.name     => Some(Oas)
      case Amf.name     => Some(Amf)
      case Payload.name => Some(Payload)
      case _            => None
    }
  }
}

sealed trait Vendor {
  val name: String
  val defaultSyntax: Syntax

  def isSameWithoutVersion(vendor: Vendor): Boolean = vendor == this
}

trait Raml extends Vendor {
  def version: String

  override val name: String          = ("raml " + version).trim
  override val defaultSyntax: Syntax = Yaml

  override def toString: String = name.trim

  override def isSameWithoutVersion(vendor: Vendor): Boolean = {
    vendor match {
      case _: Raml => true
      case _       => false
    }
  }
}

trait Oas extends Vendor {
  def version: String

  override val name: String          = ("oas " + version).trim
  override val defaultSyntax: Syntax = Json

  override def toString: String = name.trim

  override def isSameWithoutVersion(vendor: Vendor): Boolean = {
    vendor match {
      case _: Oas => true
      case _      => false
    }
  }
}

trait RamlVocabulary extends Vendor {

  override val name: String          = "RAML Vocabularies"
  override val defaultSyntax: Syntax = Yaml

  override def toString: String = name.trim

  override def isSameWithoutVersion(vendor: Vendor): Boolean = {
    vendor match {
      case _: Oas => true
      case _      => false
    }
  }
}

object Oas extends Oas {
  override def version: String = ""
}

object Oas2 extends Oas {
  override def version: String = "2.0"
}

object Oas3 extends Oas {
  override def version: String = "3.0.0"
}

object Raml extends Raml {
  override def version: String = ""
}

object RamlVocabulary extends RamlVocabulary

object Raml10 extends Raml {
  override def version: String = "1.0"
}

object Raml08 extends Raml {
  override def version: String = "0.8"
}

object Amf extends Vendor {
  override val name: String          = "amf"
  override val defaultSyntax: Syntax = Json
}

object Unknown extends Vendor {
  override val name: String          = "external"
  override val defaultSyntax: Syntax = PlainText
}

object Payload extends Vendor {
  override val name: String          = "payload"
  override val defaultSyntax: Syntax = Json
}

object Extension extends Vendor {
  override val name: String          = "extension"
  override val defaultSyntax: Syntax = Yaml
}
