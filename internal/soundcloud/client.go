package soundcloud

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/p10r/pedro/internal"
	"github.com/p10r/pedro/internal/foundation"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/clientcredentials"
	"net/http"
	url2 "net/url"
	"strings"
	"testing"
)

const (
	TokenUrl = "https://secure.soundcloud.com/oauth/token"
	ApiUrl   = "https://api.soundcloud.com/"
)

type Client struct {
	apiUrl string
	client *http.Client
}

func NewClient(conf OAuthConfig, apiUrl string) (*Client, error) {
	ctx := context.Background()
	ctx = context.WithValue(ctx, oauth2.HTTPClient, foundation.NewDefaultClient())

	config := &clientcredentials.Config{
		ClientID:       conf.clientId,
		ClientSecret:   conf.clientSecret,
		TokenURL:       conf.tokenUrl,
		Scopes:         nil,
		EndpointParams: nil,
		AuthStyle:      oauth2.AuthStyleAutoDetect,
	}

	authedClient := config.Client(ctx)

	baseUrl := strings.TrimSuffix(apiUrl, "/")

	return &Client{
		apiUrl: baseUrl,
		client: authedClient,
	}, nil
}

func (c *Client) ArtistByUrl(url string) (internal.SoundcloudArtist, error) {
	params := url2.Values{}
	params.Add("url", url)

	apiUrl, err := url2.Parse(c.apiUrl + "/resolve")
	if err != nil {
		return internal.SoundcloudArtist{}, fmt.Errorf("soundcloud.Client: Cannot parse: %w", err)
	}
	apiUrl.RawQuery = params.Encode()

	req, err := http.NewRequest("GET", apiUrl.String(), nil)
	if err != nil {
		return internal.SoundcloudArtist{}, fmt.Errorf("soundcloud.Client: unexpected error: %w", err)
	}

	req.Header.Set("accept", "application/json; charset=utf-8")

	res, err := c.client.Do(req)
	if err != nil {
		return internal.SoundcloudArtist{}, fmt.Errorf("soundcloud.Client: error executing req: %w", err)
	}
	//nolint:errcheck
	defer res.Body.Close()

	if res.StatusCode == 404 {
		return internal.SoundcloudArtist{}, fmt.Errorf("cannot find artist %s", url)
	}

	if res.StatusCode != 200 {
		return internal.SoundcloudArtist{}, fmt.Errorf("status code is %v", res.Status)
	}

	var artist internal.SoundcloudArtist
	err = json.NewDecoder(res.Body).Decode(&artist)
	if err != nil {
		return internal.SoundcloudArtist{}, fmt.Errorf("soundcloud.Client: error when parsing json: %w", err)
	}

	return artist, nil
}

func (c *Client) ArtistByQuery(query string) ([]internal.SoundcloudArtist, error) {
	params := url2.Values{}
	params.Add("q", query)

	apiUrl, err := url2.Parse(c.apiUrl + "/users")
	if err != nil {
		return []internal.SoundcloudArtist{}, fmt.Errorf("soundcloud.Client: Cannot parse: %w", err)
	}
	apiUrl.RawQuery = params.Encode()

	req, err := http.NewRequest("GET", apiUrl.String(), nil)
	if err != nil {
		return []internal.SoundcloudArtist{}, fmt.Errorf("soundcloud.Client: unexpected error: %w", err)
	}

	req.Header.Set("accept", "application/json; charset=utf-8")

	res, err := c.client.Do(req)
	if err != nil {
		return []internal.SoundcloudArtist{}, fmt.Errorf("soundcloud.Client: error executing req: %w", err)
	}
	//nolint:errcheck
	defer res.Body.Close()

	if res.StatusCode != 200 {
		return []internal.SoundcloudArtist{}, fmt.Errorf("status code is %v", res.Status)
	}

	var artists []internal.SoundcloudArtist
	err = json.NewDecoder(res.Body).Decode(&artists)
	if err != nil {
		return []internal.SoundcloudArtist{}, fmt.Errorf("soundcloud.Client: error when parsing json: %w", err)
	}
	if artists == nil {
		return []internal.SoundcloudArtist{}, nil
	}
	return artists, nil
}

func MustNewClient(t *testing.T, tokenUrl, apiUrl, clientId, clientSecret string) *Client {
	conf, err := NewOAuthConfig(clientId, clientSecret, tokenUrl)
	if err != nil {
		t.Fatal("could not create oauth conf %w", err)
	}

	c, err := NewClient(conf, apiUrl)
	if err != nil {
		t.Fatal("could not create Client %w", err)
	}
	return c
}
