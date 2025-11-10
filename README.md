# MULTITHREADED-CHAT-APPLICATION

Company Name: CODETECH IT SOLUTIONS

Name: Rohith Jairaam.P.C

Intern ID: CT04DR656

Domain name: JAVA programming

Duration : 4 weeks

Mentor Name : NEELA SANTOSH

What you get

ChatServer.java — multithreaded server that accepts many clients and broadcasts messages.

ChatClient.java — client program that connects, sends messages, and shows incoming messages.

README + usage instructions and a polished deliverable description for GitHub.

Design notes (short)

Server uses a ServerSocket on a configurable port and spawns a ClientHandler thread for each connected client.

Server maintains a thread-safe list of connected clients' PrintWriters so it can broadcast messages.

Client uses a socket to connect, one thread to read messages from the server and the main thread to read user input and send messages.

Supports a /quit command for clients to disconnect cleanly.
