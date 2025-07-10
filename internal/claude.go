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
