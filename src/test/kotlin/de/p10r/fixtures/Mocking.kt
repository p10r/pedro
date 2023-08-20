package de.p10r.fixtures

import java.lang.reflect.Proxy

/**
 * creates a proxy class which can be delegated to
 * Takes care of overriding every method that has not been overridden with TODO
 */
inline fun <reified T> fake(className: String): T = Proxy.newProxyInstance(
  T::class.java.classLoader,
  arrayOf(T::class.java)
) { _, _, _ ->
  TODO("$className: Not implemented!")
} as T
