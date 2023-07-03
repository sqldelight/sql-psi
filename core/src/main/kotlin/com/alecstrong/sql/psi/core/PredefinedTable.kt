package com.alecstrong.sql.psi.core

data class PredefinedTable(
  val name: String,
  val content: String,
) {
  val fileName = "___predefined/$name"
}
