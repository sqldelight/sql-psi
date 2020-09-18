package com.alecstrong.sql.psi.core

import com.intellij.psi.PsiFile
import java.util.concurrent.atomic.AtomicReference

internal class ModifiableFileLazy<out T>(
  private val initializer: () -> T
) {
  private var modifiedStamp = -1L
  private var state = AtomicReference<T>()

  fun forFile(file: PsiFile): T {
    if (file.modificationStamp == modifiedStamp) {
      return state.get()
    }

    synchronized(this) {
      if (file.modificationStamp == modifiedStamp) {
        return state.get()
      }

      state.set(initializer())
      modifiedStamp = file.modificationStamp
      return state.get()
    }
  }
}
