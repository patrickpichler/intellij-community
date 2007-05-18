/*
 * Copyright 2000-2007 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.groovy.lang.completion.filters.types;

import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinitionBody;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrOpenBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrTypeElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GrReferenceElement;
import org.jetbrains.plugins.groovy.lang.completion.GroovyCompletionUtil;

/**
 * @author ilyas
 */
public class BuiltInTypeFilter implements ElementFilter {
  public boolean isAcceptable(Object element, PsiElement context) {
    if (asSimpleVariable(context) || asTypedMethod(context) ||
        asVariableInBlock(context)) {
      return true;
    }
    return context.getParent() instanceof GrExpression &&
        context.getParent().getParent() instanceof GroovyFile &&
        GroovyCompletionUtil.isNewStatement(context, false);
  }

  public boolean isClassAcceptable(Class hintClass) {
    return true;
  }

  private static boolean asSimpleVariable(PsiElement context) {
    return context.getParent() instanceof GrTypeDefinitionBody;
  }

  private static boolean asVariableInBlock(PsiElement context) {
    if (context.getParent() instanceof GrReferenceExpression &&
        (context.getParent().getParent() instanceof GrOpenBlock ||
        context.getParent().getParent() instanceof GrClosableBlock) &&
        GroovyCompletionUtil.isNewStatement(context, true)) {
      return true;
    }

    if (context.getParent() instanceof  GrReferenceExpression &&
        context.getParent().getParent() instanceof GrApplicationExpression &&
        (context.getParent().getParent().getParent() instanceof GrOpenBlock ||
        context.getParent().getParent().getParent() instanceof GrClosableBlock) &&
        GroovyCompletionUtil.isNewStatement(context, true)) {
      return true;
    }

    return context.getParent() instanceof GrTypeDefinitionBody;
  }

  private static boolean asTypedMethod(PsiElement context) {
    return context.getParent() instanceof GrReferenceElement &&
        context.getParent().getParent() instanceof GrTypeElement &&
        context.getParent().getParent().getParent() instanceof GrMethod &&
        context.getParent().getParent().getParent().getParent() instanceof GrTypeDefinitionBody &&
        context.getTextOffset() == context.getParent().getParent().getParent().getParent().getTextOffset();

  }


  @NonNls
  public String toString() {
    return "built-in-types keywords filter";
  }

}
