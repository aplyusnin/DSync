package main

import (
	"crypto/sha256"
	"io"
	"log"
	"os"
)

func Hash(filename string) []byte {
	f, err := os.Open(filename)
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()
	hasher := sha256.New()
	if _, err := io.Copy(hasher, f); err != nil {
		log.Fatal(err)
	}

	return hasher.Sum(nil)
}
