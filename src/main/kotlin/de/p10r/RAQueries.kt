package de.p10r

import java.time.LocalDate

fun RASlug.toGetArtistQuery() =
  """{"query":"{\n    artist(slug:\"$value\") {\n id\n name\n }\n}\n","variables":{}}"""


fun RAArtist.toGetEventsQuery(date: LocalDate): String =
  """
        {
            "operationName": "GET_DEFAULT_EVENTS_LISTING",
            "variables": {
                "indices": [
                    "EVENT"
                ],
                "pageSize": 20,
                "page": 1,
                "aggregations": [],
                "filters": [
                    {
                        "type": "ARTIST",
                        "value": "$id"
                    },
                    {
                        "type": "DATERANGE",
                        "value": "{\"gte\":\"${date}T00:00:00.000Z\"}"
                    }
                ],
                "sortOrder": "ASCENDING",
                "sortField": "DATE",
                "baseFilters": [
                    {
                        "type": "ARTIST",
                        "value": "111504"
                    },
                    {
                        "type": "DATERANGE",
                        "value": "{\"gte\":\"${date}T00:00:00.000Z\"}"
                    }
                ]
            },
            "query": "query GET_DEFAULT_EVENTS_LISTING(${'$'}indices: [IndexType!], ${'$'}aggregations: [ListingAggregationType!], ${'$'}filters: [FilterInput], ${'$'}pageSize: Int, ${'$'}page: Int, ${'$'}sortField: FilterSortFieldType, ${'$'}sortOrder: FilterSortOrderType, ${'$'}baseFilters: [FilterInput]) {\n listing(indices: ${'$'}indices, aggregations: [], filters: ${'$'}filters, pageSize: ${'$'}pageSize, page: ${'$'}page, sortField: ${'$'}sortField, sortOrder: ${'$'}sortOrder) {\n data {\n ...eventFragment\n }\n totalResults\n }\n aggregations: listing(indices: ${'$'}indices, aggregations: ${'$'}aggregations, filters: ${'$'}baseFilters, pageSize: 0, sortField: ${'$'}sortField, sortOrder: ${'$'}sortOrder) {\n aggregations {\n type\n values {\n value\n name\n }\n }\n }\n}\n\nfragment eventFragment on IListingItem {\n ... on Event {\n id\n title\n date\n startTime\n contentUrl\n images {\n id\n filename\n alt\n type\n crop\n }\n venue {\n id\n name\n contentUrl\n live\n area {\n id\n name\n urlName\n country {\n id\n name\n urlCode\n }\n }\n }\n pick {\n id\n blurb\n }\n }\n}\n"
        }
      """.trimIndent()
