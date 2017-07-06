package org.yaml.lexer

/**
  * Yaml Context: c
  * This parameter allows productions to tweak their behavior according to their surrounding.
  * YAML supports two groups of contexts, distinguishing between block styles and flow styles.
  *
  * In block styles, indentation is used to delineate structure.
  * To capture human perception of indentation the rules require special treatment of the “-” character, used in block sequences.
  * Hence in some cases productions need to behave differently:
  * <ul>
  * <li>inside block sequences (block-in context)</li>
  * <li>and outside them (block-out context).</li>
  * </ul>
  * inside block sequences (block-in context) and outside them (block-out context).
  *
  * In flow styles, explicit indicators are used to delineate structure.
  * These styles can be viewed as the natural extension of JSON to cover tagged, single-quoted and plain scalars.
  * Since the latter have no delineating indicators, they are subject to some restrictions to avoid ambiguities.
  * These restrictions depend on where they appear:
  * <ul>
  *    <li>as implicit keys directly inside a block mapping (block-key)</li>
  *    <li>as implicit keys inside a flow mapping (flow-key)</li>
  *    <li>as values inside a flow collection (flow-in)</li>
  *    <li>or as values outside one (flow-out)</li>
  *</ul>
  */
sealed class YamlContext {
    def isFlow: Boolean = false

}

case object BlockKey extends YamlContext
case object FlowKey extends YamlContext
case object FlowIn extends YamlContext
case object FlowOut extends YamlContext
case object BlockIn extends YamlContext
case object BlockOut extends YamlContext
