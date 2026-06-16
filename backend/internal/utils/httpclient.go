package utils

import (
	"log"
	"net/http"
	"time"
)

var Client = &http.Client{
	Timeout:   30 * time.Second,
	Transport: &loggingTransport{wrapped: http.DefaultTransport},
}

type loggingTransport struct {
	wrapped http.RoundTripper
}

func (t *loggingTransport) RoundTrip(req *http.Request) (*http.Response, error) {
	resp, err := t.wrapped.RoundTrip(req)
	if err != nil {
		log.Printf("%s %s: %v", req.Method, req.URL, err)
		return nil, err
	}

	log.Printf("%s %d %s", req.Method, resp.StatusCode, req.URL)
	return resp, nil
}
