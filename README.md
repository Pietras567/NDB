# NDB - Car Rental Application

This project is a **Car Rental Application** that uses **MongoDB** as the primary database, **Redis** as a caching
mechanism and **Kafka** as the message broker. The application is built using **Java** and managed with **Gradle**.

---

## Features

- Manage car rentals efficiently.
- Store data in **MongoDB** for persistence.
- Leverage **Redis** for caching to improve performance.
- **Kafka** as a message broker for rent events.
- Scalable and maintainable project structure.

---

## Prerequisites

Make sure you have the following installed:

1. **Java 23** or higher.
2. **Gradle** 7 or higher.
3. **MongoDB**, **Redis** and **Kafka** servers running locally or use docker-compose file.

---

## Cloning the Repository

To clone the project repository `Kafka` branch:

```bash
git clone https://github.com/Pietras567/NDB.git --branch Kafka
cd NDB
```

---

## Setting up the Project

Follow these steps to set up the project:

1. Run the following Gradle command to build the project:
   ```bash
   ./gradlew build
   ```

2. Configure the database and cache manager (if needed):
   - Update the **Redis** configuration details in `src/main/resources/app.config` and **MongoDB** configuration in `mongod.conf`.
   - Update the docker-compose or Dockerfile.

---

## Running the Application

To run the application run the Main.java file in the src catalog of the producer project.
To receive messages from broker run Main.java file from the src catalog of the consumer project.

---

## Testing

To execute tests, run:

```bash
./gradlew test
```

---

## Deployment

The application can be containerized using Docker. A docker-compose and Dockerfile are arleady present in the root catalog of the project to handle
deployment.

---
