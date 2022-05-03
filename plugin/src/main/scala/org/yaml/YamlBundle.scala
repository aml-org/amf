package org.yaml

import java.util.ResourceBundle

import com.intellij.CommonBundle
import com.intellij.reference.SoftReference
import org.jetbrains.annotations.PropertyKey

import scala.annotation.varargs

/** Created by emilio.gabeiras on 6/4/17.
  */
object YamlBundle {

  private var ourBundle: SoftReference[ResourceBundle] = _
  private final val BUNDLE                             = "messages.YAMLBundle"

  @varargs def message(@PropertyKey(resourceBundle = BUNDLE) key: String, params: Any*): String =
    CommonBundle.message(getBundle, key, params)
  def message(@PropertyKey(resourceBundle = BUNDLE) key: String): String =
    CommonBundle.message(getBundle, key)

  private def getBundle = {
    var bundle: ResourceBundle = SoftReference.dereference(ourBundle)
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE)
      ourBundle = new SoftReference(bundle)
    }
    bundle
  }
}
