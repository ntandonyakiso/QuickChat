# QuickChat

It is a simple command-line Java application that simulates user registration, login, composing/sending/storing SMS-style messages, and basic stored-message queries. Built as a learning / coursework project.

## Why I built it
I built QuickChat as a hands-on exercise to practice core Java programming, object-oriented design, input validation, file I/O (JSON), and unit testing. The project was created as part of coursework and to demonstrate how a small interactive CLI app can manage user registration, authentication, message lifecycle (send / store / disregard), and basic reporting.

This project was developed using Apache NetBeans (IDE) and packaged with Maven (Java 11).

## What it does
- Lets a user register with a username, password and cell number (validation enforced).
- Allows the user to log in (simple in-memory check against the registration data).
- Lets the user create messages with recipient validation and message-length checks.
- Supports actions per message: Send, Disregard, or Store for later.
- Persists stored messages to `stored-messages.json` and can:
  - List sender/recipient for stored messages
  - Show the longest stored message
  - Search stored messages by message ID or recipient
  - Delete stored messages by hash
  - Display a simple report and count of total messages sent
- Includes unit tests (JUnit 5) for message-related behavior.

## Tech / Stack
- Language: Java
- Runtime: Java 11
- Build: Maven (pom.xml)
- IDE used: Apache NetBeans
- Test framework: JUnit 5

## Project structure (important files)
- pom.xml — Maven configuration
- src/main/java/za/ac/iie/quickchat/
  - QuickChatApp.java — CLI entry point and menu logic
  - Login.java — registration/login validation (stubbed)
  - Message.java — message handling, storage, search, report
- src/test/java/za/ac/iie/quickchat/
  - MessageTest.java — unit tests
- stored-messages.json — example / persisted messages

## How to run

Prerequisites
- Java 11 (JDK)
- Maven (for build & tests)
- (Optional) Apache NetBeans to open and run the Maven project directly

From the command line:

1. Build and run tests:
```bash
mvn test
```

2. Compile and package (skip tests if you prefer):
```bash
mvn -DskipTests package
```

3. Run the CLI:
```bash
mvn -DskipTests compile
java -cp target/classes za.ac.iie.quickchat.QuickChatApp
```

Or compile manually and run:
```bash
javac -d out $(find src/main/java -name "*.java")
java -cp out za.ac.iie.quickchat.QuickChatApp
```

Opening in Apache NetBeans
- Open Apache NetBeans → File → Open Project → select the project folder (the Maven pom.xml will be detected).
- Use NetBeans Run (or right-click project → Run) to launch QuickChat or run tests via the IDE's Test action.

Notes
- The app reads/writes `stored-messages.json` in the project root for stored messages.
- No external configuration or secrets are required for the example CLI.

## Tests
Run unit tests with:
```bash
mvn test
```
Tests are written with JUnit 5 and exercise message-handling behaviour.

## Future improvements (ideas)
- Replace the Login stub with a persistent user store (file or DB).
- Add more robust input handling for interactive terminals.
- Add integration tests for file I/O and menu flows.
- Add more comprehensive validation and error handling.
- Replace console menu with a simple REST API or GUI.









want, I can:
- Create this README.md and add it to the repository for you.
- Expand any section (usage examples, Message API details, or NetBeans-specific tips).
