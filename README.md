# NDB - Car Rental Application

This project is a **Car Rental Application** that uses **PostgreSQL** as the primary database. The application is built using **Java** and managed with **Gradle**.

---

## Features

- Manage car rentals efficiently.
- Store data in **PostgreSQL** for persistence.
- Scalable and maintainable project structure.
- Use **Hibernate** for efficiently mapping model classes into database objects with ORM.

---

## Prerequisites

Make sure you have the following installed:

1. **Java 17** or higher.
2. **Gradle** 7 or higher.
3. **PostgreSQL** server running locally or use docker-compose file.

---

## Cloning the Repository

To clone the project repository `ORM` branch:

```bash
git clone https://github.com/Pietras567/NDB.git --branch ORM
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
   - Update the **JPA** configuration details in `src/main/resources/META-INF/persistence.xml`.
   - Update the docker-compose.

---

## Running the Application

To run the application run the Main.java file in the src catalog.

---

## Testing

To execute tests, run:

```bash
./gradlew test
```

---

## Deployment

The application can be containerized using Docker. A docker-compose is arleady present in the root catalog of the project to handle deployment.

---
