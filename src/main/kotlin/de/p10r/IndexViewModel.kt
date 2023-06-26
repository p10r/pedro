package de.p10r

import org.http4k.template.ViewModel

data class IndexViewModel(val artists: List<Artist>) : ViewModel
