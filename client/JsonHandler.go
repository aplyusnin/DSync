package main

import "encoding/json"

type File struct {
	Name string
	Hash string
}
type Folder struct {
	Path       string
	Files      []File
	RemotePath string
}

func Marshal(a []Folder) []byte {
	b, err := json.Marshal(a)
	if err != nil {
		println(err)
	}
	return b
}

func Unmarshal(jsonBlob []byte) []Folder {
	var res []Folder
	err := json.Unmarshal(jsonBlob, &res)
	if err != nil {
		print(err)
	}
	return res
}
