package main

import (
	"errors"
	"encoding/json"
	"bufio"
	"encoding/hex"
	"flag"
	"fmt"
	"github.com/fsnotify/fsnotify"
	"github.com/gorilla/websocket"
	"io/ioutil"
	"log"
	"os"
	"os/signal"
	"strings"
	"sync"
	"syscall"
	"time"
	"golang.org/x/crypto/ssh/terminal"
)

var websocketUri = "ws://localhost:8090/events/"

var configPath = os.Getenv("HOME") + "/.dsync"

var mu sync.Mutex

type eventMsg struct {
	Event   string `json:"event"`
	Owner   string `json:"owner"`
	Repo    string `json:"repo"`
	File    string `json:"file"`
	Version string `json:"version"`
}

type latestMsg struct {
	Op    string `json:"op"`
	Owner string `json:"owner"`
	Repo  string `json:"repo"`
	File  string `json:"file"`
}

type subscriptionMsg struct {
	Op    string `json:"op"`
	Owner string `json:"owner"`
	Repo  string `json:"repo"`
}

type loginMsg struct {
	Op       string `json:"op"`
	Token    string `json:"token"`
}

type loginResponse struct {
	Status string `json:"status"`
	Error  string `json:"error"`
}

type latestResponse struct {
	Version string `json:"version"`
}

type responseStruct struct {
	Event   string `json:"event"`
	Owner   string `json:"owner"`
	Repo    string `json:"repo"`
	File    string `json:"file"`
	Version string `json:"version"`
}

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

func mergeFile(folder Folder, filename string, remoteHash string, remoteFolder string, token string, username string) {

	if _, err := os.Stat(folder.Path + filename); os.IsNotExist(err) {
		DownloadFile(filename, remoteFolder, remoteHash, folder.Path, token, username)
	} else {
		localHash := hex.EncodeToString(Hash(folder.Path + filename))
		//fmt.Println(localHash)
		if localHash != remoteHash {
			DownloadFile(filename, remoteFolder, remoteHash, folder.Path, token, username)
			//	fmt.Println("Downloaded file: " + folder.Path + message.File)
		}
	}
}

func mergeUploadFile(folder Folder, filename string, remoteHash string, remoteFolder string, token string, username string) {
	localHash := hex.EncodeToString(Hash(filename))
	if localHash != remoteHash {
		UploadFile(filename, remoteFolder, token, username)
	}
}

func credentials() (string, string) {
	reader := bufio.NewReader(os.Stdin)
	fmt.Print("Login: ")
	login, _ := reader.ReadString('\n')
	fmt.Print("Password: ")
	bytePassword, _ := terminal.ReadPassword(0)
	fmt.Println("")
	return strings.TrimSuffix(login, "\n"), string(bytePassword)
}

func main() {
	upload := flag.Bool("u", false, "Upload local version upon start")
	download := flag.Bool("d", false, "Download remote version upon start")
	login := flag.Bool("l", false, "Log in")
	register := flag.Bool("r", false, "Register a new user")
	create := flag.Bool("c", false, "Create a new repo")
	flag.Parse()

	if *login {
		l, p := credentials()
		token := GetToken(l, p)
		if token == "" {
			fmt.Println("Incorrect credentials")
		} else {
			saveToken(token, l)
			fmt.Println("Logged in as " + l)
		}
		//println(token)
		os.Exit(0)
	}
	if *register {
		l, p := credentials()
		err := CreateUser(l, p)
		if err == nil {
			fmt.Println("User created successfully")
		} else {
			fmt.Println("Error: ", err)
		}
		os.Exit(0)
	}
	if *upload && *download {
		fmt.Println("Both upload and download flags are specified")
		os.Exit(1)
	}

	if !*upload && !*download && !*create {
		fmt.Println("No upload or download flag specified")
		os.Exit(1)
	}

	token, username, err := getToken()
	if err != nil {
		fmt.Println("You are not logged in")
		os.Exit(1)
	}

	fmt.Println("Logged in as " + username)

	if *create {
		err := CreateRepo(flag.Args()[0], token)
		if err == nil {
			fmt.Println("Remote directory created successfully")
		} else {
			fmt.Println("Error: ", err)
		}
		os.Exit(0)
	}

	remoteLocalMap := make(map[string][]Folder)

	//println(upload)
	//println(download)

	m := getFoldersMap(flag.Args())


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

	for _, folder := range structure {
		remoteLocalMap[folder.RemotePath] = append(remoteLocalMap[folder.RemotePath], folder)
	}

	//fmt.Println(remoteLocalMap)

	done := make(chan bool)
	sigs := make(chan os.Signal, 1)

	receivedLatestResponses := make(chan responseStruct)
	receivedEventsMap := make(chan responseStruct)

	signal.Notify(sigs, syscall.SIGINT, syscall.SIGTERM)

	c, _, err := websocket.DefaultDialer.Dial(websocketUri, nil)
	if err != nil {
		log.Fatal("Unable to connect: ", err)
	}
	defer c.Close()


	err = c.WriteJSON(loginMsg{
		Op:       "login",
		Token:    token,
	})
	if err != nil {
		log.Fatal("Error during login: ", err)
	}

	response := loginResponse{}
	err = c.ReadJSON(&response)
	if err != nil {
		log.Fatal("Error during login: ", err)
	}
	if response.Status != "success" {
		log.Fatal("Error: ", response.Error)
	}

	go func() { // download
		for {
			message := <-receivedLatestResponses
			if message.Event == "fileUpdate" {
				for _, folder := range remoteLocalMap[message.Repo] {
					mergeFile(folder, message.File, message.Version, message.Repo, token, username)
				}
			}
		}

	}()

	go func() {
		for {
			r0 := responseStruct{}
			c.ReadJSON(&r0)
			if r0.Event == "fileUpdate" {
				receivedLatestResponses <- r0
			} else {
				receivedEventsMap <- r0
			}
		}
	}()

	c.SetPongHandler(func(string) error {
		c.SetReadDeadline(time.Now().Add(50 * time.Second))
		return nil
	})

	for _, folder := range structure {
		folder := folder

		subscription := subscriptionMsg{
			Op:    "subscribe",
			Owner: username,
			Repo:  folder.RemotePath,
		}

		err := c.WriteJSON(subscription)
		if err != nil {
			fmt.Print("ERROR!!! ")
			fmt.Println(err)
			return
		}

		if *upload {
			for _, file := range folder.Files {
				query := latestMsg{Op: "latest", Owner: username, Repo: folder.RemotePath, File: file.Name}
				c.WriteJSON(query)
				resp := <-receivedEventsMap
				mergeUploadFile(folder, folder.Path+file.Name, resp.Version, folder.RemotePath, token, username)
			}

			remoteRepoInfo := GetRepoInfo(folder.RemotePath, token, "1")

			for _, fileInfo := range remoteRepoInfo.Files {
				mergeFile(folder, fileInfo.Filename, fileInfo.Version, folder.RemotePath, token, username)
			}
		} else {
			remoteRepoInfo := GetRepoInfo(folder.RemotePath, token, "1")

			for _, fileInfo := range remoteRepoInfo.Files {
				mergeFile(folder, fileInfo.Filename, fileInfo.Version, folder.RemotePath, token, username)
			}

			for _, file := range folder.Files {
				query := latestMsg{Op: "latest", Owner: username, Repo: folder.RemotePath, File: file.Name}
				c.WriteJSON(query)
				resp := <-receivedEventsMap
				mergeUploadFile(folder, folder.Path+file.Name, resp.Version, folder.RemotePath, token, username)
			}
		}

		go func() { // upload
			ticker := time.NewTicker(15 * time.Second)
			defer ticker.Stop()

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
					//	fmt.Printf("EVENT: %#v\n", event)
					query := latestMsg{Op: "latest", Owner: username, Repo: folder.RemotePath, File: strings.Replace(event.Name, folder.Path, "", 1)}
					err := c.WriteJSON(query)
					if err != nil {
						fmt.Println("New error")
						fmt.Println(err)
					}

					resp := <-receivedEventsMap
					mergeUploadFile(folder, event.Name, resp.Version, folder.RemotePath, token, username)

					watcher.Add(event.Name)

				case err := <-watcher.Errors:
					fmt.Println("ERROR", err)

				case <-ticker.C:
					//	fmt.Println("TICK")
				}
			}
		}()
	}

	fmt.Println("Running")

	go func() {
		sig := <-sigs
		fmt.Println(sig.String() + " received")
		fmt.Println("Terminating")
		done <- true
	}()

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
	_, err := os.Stat(configPath + "/.config.json")
	if os.IsNotExist(err) {
		file, err := os.Create(configPath + "/.config.json")
		if err != nil {
			log.Fatal(err)
		}


		var emptyStruct = TokenFile{
			Token: "",
		}
		out, _ := json.Marshal(emptyStruct)
		file.Write(out)
		file.Close()
	}

}


/*func touch() {
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
*/
func getToken() (string, string, error) {
	touch()

	b, err := ioutil.ReadFile(configPath + "/.config.json")
	if err != nil {
		log.Fatal(err)
	}
	var res = TokenFile{}
	err = json.Unmarshal(b, &res)
	if err != nil {
		print(err)
	}
	if res.Token == "" {
		return "", "", errors.New("No saved token")
	}
	return res.Token, res.Username, nil
}

type TokenFile struct {
	Token string `json:"token"`
	Username string `json:"username"`
}

func saveToken(token string, username string) {
	touch()
	f, err := os.Create(configPath + "/.config.json")
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()

	var st = TokenFile{
		Token: token,
		Username: username,
	}

	out, _ := json.Marshal(st)
	f.Write(out)

	f.Close()
}


