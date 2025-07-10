## Local Development

Install [.direnv](https://github.com/direnv/direnv), [Lefthook](https://github.com/evilmartians/lefthook) & [nilaway](https://github.com/uber-go/nilaway?tab=readme-ov-file#standalone-checker)

```bash
lefthook install

cp .envrc-example .envrc

direnv allow .

go run cmd/main.go
```

## TODOs

### Unfollow artists

### Notify when they upload new tracks

### Follow festival sets

- allow search parameters
    - by stage
    - by artist

### Follow artists on resident advisor

Notify when they're playing a new event near to you

### Grab songs from sets

Imagine you're at a festival, you really like the song, and you can submit a request to Pedro as follows:

> "Artist XYZ, Festival 123, current time, set is running for 1 hour"

Pedro will then:

- Scrape soundcloud until the set is uploaded (for a given time)
- Post a comment, tagging you around the time you made the request
- Try to find out the name of the song/remix

### Block User Messages longer than X

- No need that users eat up tokens

### Provide Claude with the existing artists

- This would help to let the AI fix up any grammar errors

### Handle 529 (Server Overload)