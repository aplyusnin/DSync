package main

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"mime/multipart"
	"net/http"
	"os"
)

var uri = "http://localhost:5150/"

func SendFile(filename string, remoteDirectory string) {
	file, err := os.Open(filename)
	if err != nil {
		fmt.Println(err)
	}

	contents, err := ioutil.ReadAll(file)
	if err != nil {
		fmt.Println(err)
	}
	fi, err := file.Stat()
	if err != nil {
		fmt.Println(err)
	}

	file.Close()

	body := new(bytes.Buffer)
	writer := multipart.NewWriter(body)
	part, err := writer.CreateFormFile("param", fi.Name())
	if err != nil {
		fmt.Println(err)
	}

	part.Write(contents)

	err = writer.Close()
	if err != nil {
		fmt.Println(err)
	}

	request, err := http.NewRequest("POST", uri, body)
	if err != nil {
		fmt.Println(err)
	}

	client := &http.Client{}

	resp, err := client.Do(request)
	if err != nil {
		fmt.Println(err)
	} else {
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
	println("sent folder: " + folder.Path)
}
