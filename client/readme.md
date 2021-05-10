# DSync client

Compile: `make`

Run: `dsync [-u|-d] /local/folder/:remoteRepo ...`

`-u` specifies that the current state of local folder(s) will be uploaded to server first, overwriting the remote state.

`-d` specifies that the remote state will be downloaded from the server, overwriting the state of local folder(s).
