package com.alecstrong.sql.psi.sample.core

interface Data {
  fun _id(): Int

  data class Impl(override val _id: Int): DataKt
}

interface DataKt : Data {
  val _id: Int

  override fun _id() = _id
}

