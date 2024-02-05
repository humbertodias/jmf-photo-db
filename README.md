### JMF Photo DB

JMF application that establishes a connection with your webcam, captures an instant photo, converts it into a blob, and then persists it into a database.

```mermaid
sequenceDiagram
    participant App as Java Application
    participant Webcam as Webcam
    participant Database as Database

    App->>Webcam: Start capturing image
    Webcam-->>App: Captured Image
    App->>App: Convert image to Blob
    App->>Database: Save Blob image
    Database-->>App: Image Saved
```

### Usage

```shell
mvn package
java -jar target/photo-db-1.0-jar-with-dependencies.jar <cod_inst> <rgm_alun>
```