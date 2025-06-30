package soundcloud

import "fmt"

// oAuthConfig provides all the necessary parameters for clientcredentials.Config
type oAuthConfig struct {
	clientId     string
	clientSecret string
	tokenUrl     string
}

func newOAuthConfig(clientId, clientSecret, tokenUrl string) (oAuthConfig, error) {
	if clientId == "" {
		return oAuthConfig{}, fmt.Errorf("clientId can't be empty")
	}
	if clientSecret == "" {
		return oAuthConfig{}, fmt.Errorf("clientSecret can't be empty")
	}
	if tokenUrl == "" {
		return oAuthConfig{}, fmt.Errorf("tokenUrl can't be empty")
	}

	return oAuthConfig{
		clientId:     clientId,
		clientSecret: clientSecret,
		tokenUrl:     tokenUrl,
	}, nil
}
