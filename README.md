### JMF Photo DB

JMF application that take photos from your webcam and store in database as a BLOB file.

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
