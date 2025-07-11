package claude

import (
	"github.com/p10r/pedro/internal"
)

type Fake struct {
}

func NewFake() *Fake {
	return &Fake{}
}

func (f *Fake) ParseCommand(text string) (internal.ParsingResult, error) {
	//TODO implement me
	panic("implement me")
}
