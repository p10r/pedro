package soundcloud

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/p10r/pedro/httputil"
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
	apiUrl *url2.URL
	client *http.Client
}

func NewClient(conf oAuthConfig, apiUrl string) (*Client, error) {
	ctx := context.Background()
	ctx = context.WithValue(ctx, oauth2.HTTPClient, httputil.NewDefaultClient())

	config := &clientcredentials.Config{
		ClientID:       conf.clientId,
		ClientSecret:   conf.clientSecret,
		TokenURL:       conf.tokenUrl,
		Scopes:         nil,
		EndpointParams: nil,
		AuthStyle:      oauth2.AuthStyleAutoDetect,
	}

	authedClient := config.Client(ctx)

	baseUrl, err := url2.Parse(strings.TrimSuffix(apiUrl, "/"))
	if err != nil {
		return nil, fmt.Errorf("soundcloud.Client: cannot parse apiUrl %v, err: %w", apiUrl, err)
	}

	return &Client{
		apiUrl: baseUrl,
		client: authedClient,
	}, nil
}

type Artist struct {
	Id       int    `json:"id"`
	Urn      string `json:"urn"`
	Url      string `json:"permalink_url"`
	Username string `json:"username"`
}

func (c *Client) ArtistByUrl(url string) (Artist, error) {
	params := url2.Values{}
	params.Add("url", url)

	apiUrl, err := url2.Parse("https://api.soundcloud.com/resolve")
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.Client: Cannot parse: %w", err)
	}
	apiUrl.RawQuery = params.Encode()

	req, err := http.NewRequest("GET", apiUrl.String(), nil)
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.Client: unexpected error: %w", err)
	}

	req.Header.Set("accept", "application/json; charset=utf-8")

	res, err := c.client.Do(req)
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.Client: error executing req: %w", err)
	}
	//nolint:errcheck
	defer res.Body.Close()

	if res.StatusCode == 404 {
		return Artist{}, fmt.Errorf("cannot find artist %s", url)
	}

	if res.StatusCode != 200 {
		return Artist{}, fmt.Errorf("status code is %v", res.Status)
	}

	var artist Artist
	err = json.NewDecoder(res.Body).Decode(&artist)
	if err != nil {
		return Artist{}, fmt.Errorf("soundcloud.Client: error when parsing json: %w", err)
	}

	return artist, nil
}

func MustNewClient(t *testing.T, tokenUrl, apiUrl, clientId, clientSecret string) *Client {
	conf, err := newOAuthConfig(clientId, clientSecret, tokenUrl)
	if err != nil {
		t.Fatal("could not create oauth conf %w", err)
	}

	c, err := NewClient(conf, apiUrl)
	if err != nil {
		t.Fatal("could not create Client %w", err)
	}
	return c
}
