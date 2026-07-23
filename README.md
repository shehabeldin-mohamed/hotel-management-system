# Hotel Management System

An object-oriented Hotel Management System built with **Java** and **Hibernate ORM**. This project was developed as the final Project for the Design and Analysis of Information Systems (MAS) course, transitioning conceptual user requirements into a concrete implementation.

> **Note on Scope:** As an academic project, the primary focus was on rigorous system architecture, UML modeling, and object-oriented design principles. To meet course constraints and ensure easy portability, the application utilizes Java Swing for the frontend and an embedded H2 database rather than a full web-based production stack.

## Tech Stack
* **Language:** Java
* **Database / Persistence:** Hibernate ORM, H2 Database (Embedded for easy setup)
* **GUI:** Java Swing
* **Design & Modeling:** UML (Class, Activity, State Diagrams)

## Key Features
* **Comprehensive System Architecture:** Designed using detailed UML modeling based on concrete user stories and dynamic analysis.
* **Database Abstraction:** Utilizes Hibernate ORM to map complex relational data models to Java objects.
* **Interactive GUI:** Features a Java Swing interface demonstrating seamless data flow between underlying business entities.

## System Documentation (UML)
A major focus of this project was software analysis and design[cite: 1]. You can view the complete system architecture, user stories, and dynamic analysis in the master documentation file:
* [InnPoint System Documentation (PDF)](./docs/hotel-management-system-documentation.pdf)

## How to Run
Because this project utilizes an embedded H2 database and includes a data-seeding script, it is completely portable and requires no external database server setup.

1. Clone the repository: `git clone https://github.com/shehabeldin-mohamed/hotel-management-system.git`
2. Open the project in your preferred IDE (IntelliJ IDEA, Eclipse, etc.).
3. Run the `Main.java` file. The H2 database will initialize and populate with sample data and users automatically.
