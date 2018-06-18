package amf.core.validation.core

import scala.collection.mutable

case class ValidationProfile(name: String,
                             baseProfileName: Option[String],
                             violationLevel: Seq[String] = Seq.empty,
                             infoLevel: Seq[String] = Seq.empty,
                             warningLevel: Seq[String] = Seq.empty,
                             disabled: Seq[String] = Seq.empty,
                             validations: Seq[ValidationSpecification] = Seq.empty,
                             prefixes: mutable.Map[String,String] = mutable.Map.empty){}