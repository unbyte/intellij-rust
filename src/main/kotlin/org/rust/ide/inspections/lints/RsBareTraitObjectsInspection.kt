/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.inspections.lints

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.rust.ide.inspections.RsProblemsHolder
import org.rust.lang.core.psi.*
import org.rust.lang.core.psi.ext.dyn
import org.rust.lang.core.psi.ext.isAtLeastEdition2018
import org.rust.lang.core.psi.ext.skipParens
import org.rust.lang.core.resolve.ref.deepResolve

class RsBareTraitObjectsInspection : RsLintInspection() {
    override fun getLint(element: PsiElement): RsLint = RsLint.BareTraitObjects

    override fun buildVisitor(holder: RsProblemsHolder, isOnTheFly: Boolean): RsVisitor =
        object : RsVisitor() {
            override fun visitTypeReference(typeReference: RsTypeReference) {
                if (!typeReference.isAtLeastEdition2018) return

                val traitType = typeReference.skipParens() as? RsTraitType
                val baseTypePath = (typeReference.skipParens() as? RsBaseType)?.path
                val isTraitType = traitType != null || baseTypePath?.reference?.deepResolve() is RsTraitItem
                val isSelf = baseTypePath?.cself != null
                val hasDyn = traitType?.dyn != null
                val hasImpl = traitType?.impl != null
                if (!isTraitType || isSelf || hasDyn || hasImpl) return

                holder.registerLintProblem(
                    typeReference,
                    "Trait objects without an explicit 'dyn' are deprecated",
                    object : LocalQuickFix {
                        override fun getFamilyName(): String = "Add 'dyn' keyword to trait object"

                        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
                            val target = descriptor.psiElement as RsTypeReference
                            val typeElement = target.skipParens()
                            val traitText = (typeElement as? RsBaseType)?.path?.text ?: (typeElement as RsTraitType).text
                            val new = RsPsiFactory(project).createDynTraitType(traitText)
                            target.replace(new)
                        }
                    }
                )
            }
        }
}
