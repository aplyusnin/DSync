# DSync

## Usage

Compile client:
```
cd client
make
```

Launch server: 
```
cd Server
gradle run
```

Log in as an existing user:
```
./client/dsync -l
```
After that, enter login and password. If the credentials are correct, the authentication token will be stored in `${HOME}/.dsync` directory.

Create a new user:
```
./client/dsync -c
```
After that, enter login and password of the new user.

All following operations require the client to be authenticated.

Synchronize a directory with remote directory `repo1`, overwriting the changes on server:
```
./client/dsync -u /local/directory/name/:repo1
```
Expected behavior: any files in the local directory is uploaded to server if it is not stored on server or has differences with the corresponding file on server. After that, all files on server which are not found in the local directory are downloaded.

Synchronize a directory with remote directory `repo1`, overwriting local changes:
```
./client/dsync -d /local/directory/name/:repo1
```
Expected behavior: any file in the remote directory is downloaded unless it is already stored in local directory and has no differences between local and remote versions.

In both cases, once a file in a local directory is changed when client is running, it is uploaded to the server and downloaded to all other local directories which are synchronized with the same remote directory. For example, here two local directories are synchronized with the same remote directory:

```
./client/dsync -u /local/directory1/:repo1
./client/dsync -u /local/directory2/:repo1
```
If a file in directory `directory1` is changed, its new version is uploaded by the first instance of the client and then downloaded by the second instance of the client to directory `directory2`. Similarly, if a file in `directory2` is changed, it will uploaded to the server and downloaded to `directory1`.

In contrast, suppose that two local directories are synchronized to different remote directories:
```
./client/dsync -u /local/directory1/:repo1
./client/dsync -u /local/directory2/:repo2
```
In this case, if a file `directory1` is changed, its new version is uploaded by the first instance of the client as well, although it is not downloaded to `directory2` by the second instance of the client.

## Usecases

### User logs-in into sysem

1. User enters their login and password.
2. Client sends authorization data to server.
3. Server validates received pair
4. Server sends reply:
    1. Session key on validation success
    2. Error otherwise

## User start syncronization of file

1. User selects file(-s) via CLI
2. Client creates light-weight snapshot of selected files
3. Client sends snapshot to server using session id
4. Server creates sub-directory based on given snapshot and associates it with user, version dependency tree and assigns hash to files.
5. Server notifies client on creation 
    1. Server returns hash-key of file(-s) on server on seccess
    2. Error otherwise
6. Client sends file(-s) data to server.

## User shares access with given file
1. User selects file(-s) to share
2. User selects logins to share with
3. Client sends sharing information
4. Server updates access lists
5. Server notifies client about errors

## User requests version tree
1. User selects file which version tree they wants to get
2. Client sends hash-key of file to server
3. Server response
    1. Dependency lists on success
    2. Error otherwise

## User commits new version
1. User selects file(-s) 
2. Client sends hash-key and version of file(-s) to server
3. Server duplicates version, assigns to it new version identifier and updates dependencies
4. Server response with result
5. If creation succed client uploads new version

## User requests for version
1. User selects file and it's version
2. Client sends hash-key of file and version hash
3. Server restores information about version and sends file to client

# Features
- Server stores files as is 
- Dependency-lists are stored for each file independently
- Client and server communicates via http
- For each user server stores accessibility list and symlinks to editable files
