package de.p10r

data class RASlug(val value: String)

data class RAArtistResponse(val data: RAData) {
  data class RAData(val artist: RAArtist?)
}

data class RAArtist(val id: String, val name: String)
