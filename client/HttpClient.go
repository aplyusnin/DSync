package main

import (
	"bytes"
	"encoding/json"
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
var repoInfoUri = "/REPOINFO"
var websocketAddress = ""

type RemoteRepoInfo struct {
	Files []RemoteFileInfo `json:"files"`
}

type RemoteFileInfo struct {
	Filename string `json:"filename"`
	Version  string `json:"version"`
}

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
	q.Add("login", Username)
	q.Add("password", Password)
	q.Add("repo", remoteDirectory)
	q.Add("filename", fi.Name())
	q.Add("owner", Username)
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
	q.Add("owner", "1")
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

	_, err = io.Copy(file, res.Body)
	if err != nil {
		log.Println(err)
	}

	fmt.Println("File downloaded: " + localDirectory + filename)
}

func GetRepoInfo(repoName string) RemoteRepoInfo {
	req, err := http.NewRequest("GET", url+repoInfoUri, nil)
	if err != nil {
		log.Println(err)
	}

	client := &http.Client{}

	q := req.URL.Query()
	q.Add("login", "1")
	q.Add("password", "12345")
	q.Add("repo", repoName)
	q.Add("owner", "1")
	req.URL.RawQuery = q.Encode()

	res, err := client.Do(req)
	if err != nil {
		log.Println(err)
	}
	defer res.Body.Close()

	info := RemoteRepoInfo{}

	json.NewDecoder(res.Body).Decode(&info)

	return info
}
