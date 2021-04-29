package main

func SendFile(file File) {
	println("sent " + file.Name)
}

func SendDirectory(folder Folder) {
	println("sent folder: " + folder.Path)
}
