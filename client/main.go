package main

import (
	"encoding/hex"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"os/signal"
	"strings"
	"syscall"

	"github.com/fsnotify/fsnotify"
)

var configPath = os.Getenv("HOME") + "/.dsync"

func getFoldersMap(args []string) map[string]string {
	m := make(map[string]string)
	m1 := make(map[string]string)
	for _, pair := range args {
		p := strings.Split(pair, ":")
		if len(p) > 2 || len(p) == 0 {
			panic("Illegal input: \"" + pair + "\"")
		}
		if _, ok := m[p[0]]; ok {
			panic("Repeated virtual folder root: \"" + p[0] + "\"")
		}
		if _, ok := m1[p[1]]; ok {
			panic("Repeated local folder: \"" + p[1] + "\"")
		}
		m[p[0]] = p[1]
		m1[p[1]] = p[0]
	}
	return m
}

func main() {
	m := getFoldersMap(os.Args[1:])

	var structure []Folder

	for k, v := range m {

		flag := false

		for _, str := range structure {
			if str.Path == k {
				flag = true
				if str.RemotePath != v {
					str.RemotePath = v
				}

				files, err := ioutil.ReadDir(k)
				if err != nil {
					panic("Cannot open directory: " + k)
				}
				for _, file := range files {
					if file.IsDir() {
						// nested directory, todo make a function for that
					} else {
						flag1 := false
						for _, fileDescription := range str.Files {
							if fileDescription.Name == file.Name() {
								flag1 = true
							}
						}

						if !flag1 {
							str.Files = append(str.Files, File{Name: file.Name(), Hash: hex.EncodeToString(Hash(file.Name()))})
						}
					}
				}
			}
		}

		if !flag {
			newfolder := Folder{Path: k, RemotePath: v, Files: []File{}}
			files, err := ioutil.ReadDir(k)
			if err != nil {
				panic("Cannot open directory: " + k)
			}
			for _, file := range files {
				if file.IsDir() {
					// nested directory, todo make a function for that
				} else {
					newfolder.Files = append(newfolder.Files, File{Name: file.Name(), Hash: hex.EncodeToString(Hash(k + file.Name()))})
				}
			}
			structure = append(structure, newfolder)
		}
	}

	done := make(chan bool)
	sigs := make(chan os.Signal, 1)

	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)

	go func() {
		sig := <-sigs
		fmt.Println(sig.String() + " received")
		fmt.Println("Terminating")
		done <- true
	}()

	for _, folder := range structure {
		folder := folder
		go func() {
			UploadDirectory(folder)
			watcher, err := fsnotify.NewWatcher()
			if err != nil {
				fmt.Println(err)
			}
			defer watcher.Close()
			for _, f := range folder.Files {
				if error := watcher.Add(folder.Path + f.Name); err != nil {
					fmt.Println("ERROR", error)
				}
			}
			for {
				select {
				case event := <-watcher.Events:
					//fmt.Printf("EVENT: %#v\n", event)
					UploadFile(event.Name, folder.RemotePath)

				case err := <-watcher.Errors:
					fmt.Println("ERROR", err)
				}
			}
		}()
	}

	fmt.Println("Running")

	if len(structure) > 0 {
		<-done
	}
}

func touch() {
	if _, err := os.Stat(configPath); os.IsNotExist(err) {
		err := os.MkdirAll(configPath, os.ModePerm)
		if err != nil {
			log.Fatal(err)
		}
	}

	_, err := os.Stat(os.Getenv("HOME") + "/.dsync/config.json")
	if os.IsNotExist(err) {
		file, err := os.Create(os.Getenv("HOME") + "/.dsync/config.json")

		if err != nil {
			log.Fatal(err)
		}

		out := Marshal([]Folder{})

		file.Write(out)
		file.Close()
	}
}

func getStructure() []Folder {
	touch()

	b, err := ioutil.ReadFile(configPath + "/config.json")
	if err != nil {
		log.Fatal(err)
	}
	return Unmarshal(b)
}
