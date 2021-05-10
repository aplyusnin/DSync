# DSync client

Compile: `make`

Usage: `dsync (-u|-d) [/local/directory/:remoteRepo...]`

* `-u` specifies that the current state of local directory(s) will be uploaded to server first, overwriting the remote state. After that, all new files on server (i.e. those not stored in the local directory) will be downloaded.

* `-d` specifies that the remote state will be downloaded from the server, overwriting the state of local directory(s). After that, all new files in the local directory (i.e. those not stored on server) will be uploaded.
