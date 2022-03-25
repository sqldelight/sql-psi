package com.alecstrong.sql.psi.core.psi

import com.intellij.psi.PsiElement

interface FromQuery : PsiElement {

  fun fromQuery(): Collection<QueryElement.QueryResult>
}
