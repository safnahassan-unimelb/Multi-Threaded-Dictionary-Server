# Multi-Threaded-Dictionary-Server
This project is aimed at designing and implementing a multi-threaded server that provides the functionality of a dictionary, and clients, wherein the clients can concurrently perform four main functions through the server:

• Search the meaning(s) of a word.

• Add a new word.

• Remove an existing word.

• Update the meaning(s) of a word.

This work makes explicit use of sockets and threads to achieve the desired architecture.

Commands:

```java –jar DictionaryServer.jar <port> <dictionary-file>```

```java –jar DictionaryClient.jar <server-address> <server-port>```
