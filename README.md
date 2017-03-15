# Random-forest-file-caching

## Overview
The files are saved at a specific root in the server forest determined by the hash value returned by the hash function with root parameters. Using this hash function, every file generates a random tree structure from the server forest. The clients request for files from the leaf nodes of the random tree, made known using the hash function. The request propogates from the leaf towards the node till the file is found. Every time a node services a request, it keeps a track of the popularity of the requested file. If the file request is services 5 times at a particular node, the file is deemed popular on that node and the file is cached(replicated) at the children nodes of this node. Any future requests for the file at this path are serviced by either of the child nodes of the current node in consideration. 

## Compiling the code
Store all 4 .java files in a single directory.
The files can be compiled by running the command 'javac *.java'

## Running the code

### To run the Server side:
go to the appropriate directory and run the command 'java Server', the registry and the storage directory for the server will be automatically created upon running it.

### To run the Client side:
go to the appropriate directory and run the command 'java Client [file name]', the registry and the storage directory for the client will be automatically created upon running it.

[file name] = name of the file to be fetched.

## Expected output
The client side will display the trace route of the servers visited to find the file, and mention where the file search began and where the file was found, if it exists in the system. If the file is not present in the system, the client will display the trace route of all the servers that were visited in order to find the file.

## Under the hood
The replication of the file at the child node takes place once a parent with the file receives 5 requests(becomes popular) for the same. The nodes where the file is replicated will be mentioned at the parent server terminal at the time of transmission and the child nodes will acknowledge the same. However, the Leaf nodes will not replicate the file anywhere even if the file is regarded popular.   
