package internal

const (
	ParsingCmdFollow   = "FOLLOW"
	ParsingCmdUnfollow = "UNFOLLOW"
	ParsingError       = "PARSING_ERROR"
)

type Claude interface {
	ParseCommand(text string) (ParsingResult, error)
}

type ParsingResult struct {
	Command       string
	SoundcloudUrl string
	ArtistName    string
}

type Soundcloud interface {
	ArtistByUrl(url string) (SoundcloudArtist, error)
}

type SoundcloudArtist struct {
	Id       int    `json:"id"`
	Urn      string `json:"urn"`
	Url      string `json:"permalink_url"`
	Username string `json:"username"`
}
