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
	/*	heh := []Folder{
			{
				Path: "path",
				Files: []File{
					{
						Name: "file1",
						Hash: "hash1",
					},
					{
						Name: "file2",
						Hash: "hash2",
					},
				},
			},
			{
				Path: "path2",
				Files: []File{
					{
						Name: "file3",
						Hash: "hash3",
					},
				},
			},
		}
	*/
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
