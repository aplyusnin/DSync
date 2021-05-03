package main

import (
	"bytes"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"os"
)

var uri = "http://localhost:8090/UPLOAD"

func SendFile(filename string, remoteDirectory string) {
	file, err := os.Open(filename)
	if err != nil {
		fmt.Println(err)
	}

	//contents, err := ioutil.ReadAll(file)
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

	io.Copy(part, file) // new

	//part.Write(contents)

	err = writer.Close()
	if err != nil {
		fmt.Println(err)
	}

	request, err := http.NewRequest("POST", uri, body)
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
		fmt.Println(err)
	} else {
		print("STATUS: ")
		println(resp.StatusCode)
		println(resp.Header)
		var respbody []byte
		resp.Body.Read(respbody)
		resp.Body.Close()
		fmt.Println(respbody)
	}

	println("sent " + filename)
}

func SendDirectory(folder Folder) {
	for _, file := range folder.Files {
		SendFile(folder.Path+file.Name, folder.RemotePath)
	}

	println("sent folder: " + folder.Path)
}
