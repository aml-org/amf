package amf.jsonldschema.internal.scala

object JsonLDSchemaPayloadWrapper {

  def wrap(data: String, dialectName: String): String = {
    wrapExp.replace(DIALECT_NAME_EXP, dialectName).replace(DATA_EXP, data)
  }

  private val DIALECT_NAME_EXP = "<jsonld_replacing_name>"
  private val DATA_EXP         = "<jsonld_replacing_data>"

  private val wrapExp = "\n {\n  \"$dialect\": \"" + DIALECT_NAME_EXP + "\",\n  \"$data\": " + DATA_EXP + "\n}"

}
