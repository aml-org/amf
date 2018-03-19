package amf.client.model

trait BaseAnyValField extends BaseField with ValueField {

  override type ValueType <: AnyVal
}
