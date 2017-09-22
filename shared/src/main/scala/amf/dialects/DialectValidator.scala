package amf.dialects

import amf.document.{BaseUnit, Document}
import amf.metadata.{Field, Type}
import amf.model.{AmfArray, AmfElement, AmfScalar}

import scala.collection.mutable


object ValidationIssue{
  val ERROR=2
  val WARNING=1

}

case class ValidationIssue(message:String,entity:DomainEntity,property:Option[DialectPropertyMapping]=None,severity:Int=ValidationIssue.ERROR){

}

trait ErrorReporter{
  def accept(e:ValidationIssue)
}
/**
  * Created by Pavel Petrochenko 22/09/17.
  */
class DialectValidator(val reporter:ErrorReporter) {


    def validate(e:DomainEntity): Unit ={
       e.definition.props.values.foreach(prop=>{
         val isDefined = e.fields.raw(prop.field()).isDefined
          if (prop.required && !isDefined){
              reporter.accept(ValidationIssue(s"missing required property:${prop.name}",e))
          }
          if (isDefined) {
            val value = e.fields.get(prop.field());
            if (value.isInstanceOf[AmfArray]) {
              value.asInstanceOf[AmfArray].values.foreach(v => {
                vaidateValue(e, prop, v);
              })
            }
            else {
              vaidateValue(e, prop, value)
            }
          }
       })
    }

  private def vaidateValue(e:DomainEntity,prop: DialectPropertyMapping, value: AmfElement) = {
    value match {
      case scalar:AmfScalar=>{
        validateScalar(scalar,e, prop)
      }
      case d:DomainEntity=>{
          if (prop.isScalar && !prop.allowInplace){
            reporter.accept(ValidationIssue(s"property ${prop.name} should be scalar",e))
          }
          validate(d);
      }
    }

  }

  private def validateScalar(scalar:AmfScalar,e: DomainEntity, prop: DialectPropertyMapping) = {
    validatePropType(scalar, e, prop)
    validatePattern(scalar,e, prop)
    validateEnum(scalar,e, prop)
    validateMinMax(scalar,e, prop)
  }

  private def validateMinMax(scalar:AmfScalar,e: DomainEntity, prop: DialectPropertyMapping) = {
    prop.minimum.foreach(m => {
      try {
        val d = scalar.toString.toDouble;
        if (d < m.toDouble) {
          reporter.accept(ValidationIssue(s"property ${prop.name} should be not less then ${m}", e))
        }
      }
      catch {
        case _: Throwable => {}
      }
    })
    prop.maximum.foreach(m => {
      try {
        val d = scalar.toString.toDouble;
        if (d > m.toDouble) {
          reporter.accept(ValidationIssue(s"property ${prop.name} should be not more then ${m}", e))
        }
      }
      catch {
        case _: Throwable => {}
      }
    })
  }

  private def validateEnum(scalar:AmfScalar,e: DomainEntity, prop: DialectPropertyMapping) = {
    prop.enum.foreach(ev => {
      if (ev.indexOf(scalar.toString) == -1) {
        reporter.accept(ValidationIssue(s"property ${prop.name} should be one of  ${ev}", e))
      }
    })
  }

  private def validatePattern(scalar:AmfScalar,e: DomainEntity, prop: DialectPropertyMapping) = {
    prop.pattern.foreach(pattern => {
      if (!scalar.toString.matches(pattern)) {
        reporter.accept(ValidationIssue(s"property ${prop.name} should match to ${pattern}", e))
      }
    })
  }

  private def validatePropType(scalar: AmfScalar, e: DomainEntity, prop: DialectPropertyMapping) = {
    if (!prop.isScalar) {
      reporter.accept(ValidationIssue(s"property ${prop.name} should be object", e))
    }
    if (prop.range == Type.Bool) {
      if (scalar.toString != "true" && scalar.toString != "false") {
        reporter.accept(ValidationIssue(s"property ${prop.name} should be boolean", e))
      }
    }
    if (prop.range == Type.Int) {
      try {
        val d = scalar.toString.toInt;

      }
      catch {
        case _: Throwable => {
          reporter.accept(ValidationIssue(s"property ${prop.name} should be integer", e))

        }
      }
    }
  }
}

object DialectValidator{
  private def retrieveDomainEntity(unit: BaseUnit) = unit match {
    case document: Document => document.encodes match {
      case unit: DomainEntity => unit
      case other              => throw new Exception(s"Encoded domain element is not a dialect domain entity $other")
    }
    case _                  => throw new Exception(s"Cannot extract domain entity from unit that is not a document: $unit")
  }
  def validate(unit:BaseUnit): List[ValidationIssue] ={
    try {
      val errors = mutable.ListBuffer[ValidationIssue]();
      new DialectValidator(e => errors.+=(e)).validate(retrieveDomainEntity(unit));
      errors.toList
    } catch {
      case e:Exception=>e.printStackTrace();List[ValidationIssue]()
    }
  }
  def validate(e:DomainEntity): List[ValidationIssue] ={
    val errors=mutable.ListBuffer[ValidationIssue]();
    new DialectValidator(e=>errors.+=(e)).validate(e);
    errors.toList
  }
}