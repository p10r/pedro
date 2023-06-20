package de.p10r

data class RASlug(val value: String)

data class RAArtistResponse(val data: RAData) {
  data class RAData(val artist: RAArtist?)
}

data class RAArtist(val id: String, val name: String)

data class RAEventsResponse(val data: RADataWrapper) {
  data class RADataWrapper(val listing: RAListing) {
    data class RAListing(val data: List<RAEvent>) {
      data class RAEvent(
        val id: String
      )
    }
  }
}

