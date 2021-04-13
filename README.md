# DSync
#Usecases

## User logs-in into sysem

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
2. Client sends hash-key of file(-s), hash of version and hash of file(-s) to server
3. Server duplicates version with given version and updates dependencies
4. Server response with result
5. If creation succed client uploads new version

## User requests for version
1. User selects file and it's version
2. Client sends hash-key of file and version hash
3. Server restores information about version and sends file to client

# Features
- Server stores files as is 
- Dependency-lists are stored for each file independently
- Client and server communicates via web-sockets
- For each user server stores accessibility list and symlinks to editable files
