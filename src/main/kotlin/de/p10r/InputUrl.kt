package de.p10r

data class InputUrl private constructor(val value: String) {
  companion object {
    fun ofOrNull(value: String) =
      if (value.startsWith("http") || value.startsWith("ra.co"))
        InputUrl(value)
      else null
  }
}

fun InputUrl.toRASlug() = RASlug(
  value.replaceBeforeLast(delimiter = '/', replacement = "")
    .removePrefix("/")
)
