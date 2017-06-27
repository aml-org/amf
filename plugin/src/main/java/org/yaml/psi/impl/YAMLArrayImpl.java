package org.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.yaml.psi.YAMLSequence;
import org.yaml.psi.YAMLSequence;

/**
 * @author oleg
 */
public class YAMLArrayImpl extends YAMLSequenceImpl implements YAMLSequence {
  public YAMLArrayImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "YAML array";
  }
}