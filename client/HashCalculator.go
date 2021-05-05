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
	hash := sha256.New()
	if _, err := io.Copy(hash, f); err != nil {
		log.Fatal(err)
	}

	return hash.Sum(nil)
}
