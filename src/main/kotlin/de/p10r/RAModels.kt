package de.p10r

data class RASlug(val value: String)

/**
 * Wrapper to represent:
 * {
 *     "data": {
 *         "artist": {
 *             "id": "943",
 *             "name": "Boys Noize"
 *         }
 *     }
 * }
 */
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

