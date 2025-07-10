package claude

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/p10r/pedro/httputil"
	"github.com/p10r/pedro/internal"
	"net/http"
)

const (
	ApiUrl = "https://api.anthropic.com/v1/messages"
	model  = "claude-3-5-sonnet-20241022"
)

type Client struct {
	ApiUrl string
	ApiKey string
	client *http.Client
}

func NewClient(apiUrl string, apiKey string) *Client {
	return &Client{
		ApiUrl: apiUrl,
		ApiKey: apiKey,
		client: httputil.NewDefaultClient(),
	}
}

type request struct {
	Model     string    `json:"model"`
	MaxTokens int       `json:"max_tokens"`
	System    string    `json:"system"`
	Messages  []message `json:"messages"`
}

type message struct {
	Role    string `json:"role"`
	Content string `json:"content"`
}

type response struct {
	Content []struct {
		Text string `json:"text"`
		Type string `json:"type"`
	} `json:"content"`
	Model string `json:"model"`
	Role  string `json:"role"`
}

func (c Client) ParseCommand(text string) (internal.ParsingResult, error) {
	input := request{
		Model:     model,
		MaxTokens: 500,
		System:    prompt,
		Messages: []message{
			{
				Role:    "user",
				Content: fmt.Sprintf("Parse this text: %s", text),
			},
		},
	}

	jsonData, err := json.Marshal(input)
	if err != nil {
		return internal.ParsingResult{}, fmt.Errorf("claude.Client: failed to marshal claude request: %w", err)
	}

	req, err := http.NewRequest("POST", ApiUrl, bytes.NewBuffer(jsonData))
	if err != nil {
		return internal.ParsingResult{}, fmt.Errorf("claude.Client: failed to create claude request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("x-api-key", c.ApiKey)
	req.Header.Set("anthropic-version", "2023-06-01")

	res, err := c.client.Do(req)
	if err != nil {
		return internal.ParsingResult{}, fmt.Errorf("claude.Client: could not send reques %w", err)
	}
	//nolint:errcheck
	defer res.Body.Close()

	if res.StatusCode != 200 {
		return internal.ParsingResult{}, fmt.Errorf("claude.Client: unexpected response %s", res.Status)
	}

	var claudeRes response
	err = json.NewDecoder(res.Body).Decode(&claudeRes)
	if err != nil {
		return internal.ParsingResult{}, fmt.Errorf("claude.Client: error when parsing response json: %w", err)
	}

	var r result
	err = json.Unmarshal([]byte(claudeRes.Content[0].Text), &r)
	if err != nil {
		return internal.ParsingResult{}, fmt.Errorf("claude.client: cannot unmarshal %s. err: %w", claudeRes.Content[0].Text, err)
	}

	domainResult, err := r.toParsingResult()
	if err != nil {
		return internal.ParsingResult{}, err
	}
	return domainResult, nil
}
