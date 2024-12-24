package domain

type UntrackedMatch struct {
	HomeName         string
	AwayName         string
	StartTime        int64
	FlashscoreName   string // Country + League
	Country          string
	League           string
	Stage            string
	HomeScoreCurrent int
	AwayScoreCurrent int
}

type UntrackedMatches []UntrackedMatch

type Match struct {
	ID        int64
	HomeName  string
	AwayName  string
	StartTime int64
	Country   string
	League    string
	// Set through scraper package
	StatsUrl string
}

type Matches []Match

func NewMatch(id int64, match UntrackedMatch) Match {
	return Match{
		ID:        id,
		HomeName:  match.HomeName,
		AwayName:  match.AwayName,
		StartTime: match.StartTime,
		Country:   match.Country,
		League:    match.League,
	}
}

type FinishedMatch struct {
	UntrackedMatch
	HomeSetScore int
	AwaySetScore int
}

type FinishedMatches []FinishedMatch
