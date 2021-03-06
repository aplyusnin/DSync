package main

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log"
	"mime/multipart"
	"net/http"
	"os"
)

var url = "http://localhost:8090"
var uploadUri = "/DATA/ACCESS/UPLOAD"
var downloadUri = "/DATA/ACCESS/DOWNLOAD"
var repoInfoUri = "/DATA/ACCESS/REPOINFO"
var newRepoUrl = "/DATA/NEWREPO"
var loginUrl = "/LOGIN"
var newUserUrl = "/NEWUSER"
var tokenHeaderName = "X-Access-Token"

type TokenInfo struct {
	Error string `json:"error"`
	Token string `json:"token"`
}

type RemoteRepoInfo struct {
	Files []RemoteFileInfo `json:"files"`
}

type RemoteFileInfo struct {
	Filename string `json:"filename"`
	Version  string `json:"version"`
}

type CreateUserInfo struct {
	Status string `json:"status"`
	Error  string `json:"error"`
}

type CreateRepoInfo struct {
	Status string `json:"status"`
	Error  string `json:"error"`
}

func UploadFile(filename string, remoteDirectory string, token string, username string) {
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

	request.Header.Set(tokenHeaderName, token)

	q := request.URL.Query()
	q.Add("repo", remoteDirectory)
	q.Add("filename", fi.Name())
	q.Add("owner", username)
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

func UploadDirectory(folder Folder, token string, username string) {
	for _, file := range folder.Files {
		UploadFile(folder.Path+file.Name, folder.RemotePath, token, username)
	}
}

func DownloadFile(filename string, remoteDirectory string, version string, localDirectory string, token string, username string) {
	req, err := http.NewRequest("GET", url+downloadUri, nil)
	if err != nil {
		log.Println(err)
	}

	client := &http.Client{}

	req.Header.Set(tokenHeaderName, token)

	q := req.URL.Query()
	q.Add("repo", remoteDirectory)
	q.Add("filename", filename)
	q.Add("version", version)
	q.Add("owner", username)
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

func GetRepoInfo(repoName string, token string, username string) RemoteRepoInfo {
	req, err := http.NewRequest("GET", url+repoInfoUri, nil)
	if err != nil {
		log.Println(err)
	}
	req.Header.Set(tokenHeaderName, token)

	client := &http.Client{}

	q := req.URL.Query()
	q.Add("repo", repoName)
	q.Add("owner", username)
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

func GetToken(login string, password string) string {
	req, err := http.NewRequest("GET", url+loginUrl, nil)
	if err != nil {
		log.Println(err)
	}

	client := &http.Client{}

	q := req.URL.Query()
	q.Add("login", login)
	q.Add("password", password)
	req.URL.RawQuery = q.Encode()

	res, err := client.Do(req)
	if err != nil {
		log.Println(err)
	}
	defer res.Body.Close()

	info := TokenInfo{}

	json.NewDecoder(res.Body).Decode(&info)
	return info.Token
}

func CreateUser(login string, password string) error {
	req, err := http.NewRequest("GET", url+newUserUrl, nil)
	if err != nil {
		log.Println(err)
	}

	client := &http.Client{}

	q := req.URL.Query()
	q.Add("login", login)
	q.Add("password", password)
	req.URL.RawQuery = q.Encode()

	res, err := client.Do(req)
	if err != nil {
		log.Println(err)
	}
	defer res.Body.Close()

	info := CreateUserInfo{}

	json.NewDecoder(res.Body).Decode(&info)
	if info.Error != "" {
		return errors.New(info.Error)
	}
	return nil
}

func CreateRepo(repo string, token string) error {
	req, err := http.NewRequest("GET", url+newRepoUrl, nil)
	if err != nil {
		return err
	}
	req.Header.Set(tokenHeaderName, token)

	client := &http.Client{}

	q := req.URL.Query()
	q.Add("repo", repo)
	req.URL.RawQuery = q.Encode()

	res, err := client.Do(req)
	if err != nil {
		return err
	}
	defer res.Body.Close()

	info := CreateRepoInfo{}
	if info.Error != "" {
		return errors.New(info.Error)
	}

	return nil
}
