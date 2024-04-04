# Multi-player Trivia Game

## Introduction
This project is a network-based, multi-player trivia game that allows players to compete by answering questions over the network. Utilizing Java for both the client and server applications, this game features a server managing the game logic and client communication, alongside a graphical user interface for players to interact with the game seamlessly.

## Features
- **Concurrency Control:** Leveraging multi-threading to handle multiple clients.
- **Socket Programming:** Utilizing TCP for reliable communication and UDP for polling.
- **GUI and Event Handling:** A user-friendly interface for game interaction.
- **Game Logic:** Comprehensive logic for question delivery, answer validation, and score tallying.
- **Network Synchronization:** Ensures timely and accurate updates across clients.

## Prerequisites
- Java Development Kit (JDK) installed on your system.

## Installation

### Server Setup
1. **Compile the Server Application**
    ```sh
    javac Server.java
    ```
2. **Run the Server**
    ```sh
    java Server
    ```

### Client Setup
1. **Update the `serverIP` variable** with the IP address of the server in `ClientWindow.java`.
2. **Compile the Client Application**
    ```sh
    javac ClientWindow.java
    ```
3. **Run the Client**
    ```sh
    java ClientWindow
    ```

## Project Design

### Server
- Implemented as a multi-threaded application in Java.
- Manages game logic and client communications via TCP and UDP.
- Utilizes a shared queue for handling UDP messages and reads trivia questions from a text file.

### Client
- Uses Java Swing for the graphical user interface.
- Connects to the server via TCP and uses UDP for sending polling messages.
- Displays trivia questions, multiple-choice answers, and tracks scores and time limits.

## Contributions
This project was developed by Brian, Klaus, and Riley, with each contributing to various aspects of the design, implementation, and testing phases.

