package ra

import (
	"errors"
	"fmt"
	"net"
	"net/http"
	"pedro-go/domain"
	"time"
)

var ErrSlugNotFound = errors.New("slug not found on ra.co")

type Client struct {
	http    *http.Client
	baseUri string
}

func NewClient(baseUri string) *Client {
	c := &http.Client{
		Timeout: 10 * time.Second,
		Transport: &http.Transport{
			DialContext: (&net.Dialer{
				Timeout:   10 * time.Second,
				KeepAlive: 10 * time.Second,
			}).DialContext,
			TLSHandshakeTimeout:   5 * time.Second,
			ResponseHeaderTimeout: 5 * time.Second,
			ExpectContinueTimeout: 1 * time.Second,
		},
	}
	return &Client{http: c, baseUri: baseUri}
}

func (c *Client) GetArtistBySlug(slug domain.RASlug) (domain.ArtistInfo, error) {
	req, err := newGetArtistReq(slug, c.baseUri)
	if err != nil {
		return domain.ArtistInfo{}, fmt.Errorf("could not create request: %w", err)
	}

	res, err := c.http.Do(req)
	if err != nil {
		return domain.ArtistInfo{}, fmt.Errorf("could not get response: %w", err)
	}

	a, err := NewArtist(res)
	return a.ToArtistInfo(), err
}

func (c *Client) GetEventsByArtistId(
	raId string,
	start time.Time,
	end time.Time,
) (domain.Events, error) {
	req, err := newGetEventsReq(raId, start, end, c.baseUri)
	if err != nil {
		return domain.Events{}, fmt.Errorf("could not create request: %w", err)
	}

	res, err := c.http.Do(req)
	e, err := NewEvent(res, err)
	return e.ToDomainEvents(), err
}
