package ra

import (
	"fmt"
	"net/http"
)

type Client struct {
	http    *http.Client
	baseUri string //todo
}

func New(baseUri string) *Client {
	return &Client{http: &http.Client{}, baseUri: baseUri}
}

func (c Client) GetArtistBySlug(slug string) (ArtistRes, error) {
	req, err := getArtistBySlugReq(slug, c.baseUri)

	res, err := c.http.Do(req)
	if err != nil {
		return ArtistRes{}, err
	}
	defer res.Body.Close()

	if res.StatusCode != http.StatusOK {
		return ArtistRes{}, fmt.Errorf("request failed with status code: %v", res.StatusCode)
	}

	return NewArtistFrom(res.Body)
}