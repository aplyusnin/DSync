package main

import (
	"bytes"
	"fmt"
	"io"
	"log"
	"mime/multipart"
	"net/http"
	"os"
)

var url = "http://localhost:8090"
var uploadUri = "/UPLOAD"
var downloadUri = "/DOWNLOAD"

func UploadFile(filename string, remoteDirectory string) {
	file, err := os.Open(filename)
	if err != nil {
		fmt.Println(err)
	}

	fi, err := file.Stat()
	if err != nil {
		fmt.Println(err)
	}

	defer file.Close()

	body := new(bytes.Buffer)
	writer := multipart.NewWriter(body)
	part, err := writer.CreateFormFile("filename", fi.Name())
	if err != nil {
		fmt.Println(err)
	}

	io.Copy(part, file)

	err = writer.Close()
	if err != nil {
		fmt.Println(err)
	}

	request, err := http.NewRequest("POST", url+uploadUri, body)
	if err != nil {
		fmt.Println(err)
	}

	q := request.URL.Query()
	q.Add("login", "1")
	q.Add("password", "12345")
	q.Add("repo", remoteDirectory)
	q.Add("filename", fi.Name())
	request.URL.RawQuery = q.Encode()

	request.Header.Add("Content-Type", writer.FormDataContentType())

	client := &http.Client{}

	resp, err := client.Do(request)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
	} else {
		var responseBody []byte
		resp.Body.Read(responseBody)
		resp.Body.Close()
		fmt.Println("File uploaded: " + filename)
	}
}

func UploadDirectory(folder Folder) {
	for _, file := range folder.Files {
		UploadFile(folder.Path+file.Name, folder.RemotePath)
	}
}

func DownloadFile(filename string, remoteDirectory string, version string, localDirectory string) {
	req, err := http.NewRequest("GET", url+downloadUri, nil)
	if err != nil {
		log.Println(err)
	}

	client := &http.Client{}

	q := req.URL.Query()
	q.Add("login", "1")
	q.Add("password", "12345")
	q.Add("repo", remoteDirectory)
	q.Add("filename", filename)
	q.Add("version", version)
	req.URL.RawQuery = q.Encode()

	res, err := client.Do(req)
	if err != nil {
		log.Println(err)
	}
	defer res.Body.Close()

	file, err := os.Create(localDirectory + filename)
	if err != nil {
		log.Println(err)
	}
	defer file.Close()

	io.Copy(file, res.Body)

}
