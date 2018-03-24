package com.alecstrong.sqlite.psi.core

import com.intellij.psi.PsiFile
import kotlin.reflect.KProperty

internal class ModifiableFileLazy<out T>(
  private val file: PsiFile,
  private val initializer: () -> T
) {
  private var modifiedStamp = file.modificationStamp
  private var state: T? = null

  operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
    if (file.modificationStamp == modifiedStamp) {
      state?.let { return it }
    }

    synchronized(this) {
      if (file.modificationStamp == modifiedStamp) {
        state?.let { return it }
      }

      val _state = initializer()
      state = _state
      modifiedStamp = file.modificationStamp
      return _state
    }
  }
}