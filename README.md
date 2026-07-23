# Hotel Management System

An object-oriented Hotel Management System built with **Java** and **Hibernate ORM**. This project was developed from the ground up, starting from conceptual user requirements to a fully functional GUI application.

## 🚀 Tech Stack
* **Language:** Java
* **Database / Persistence:** Hibernate ORM, H2 Database (Embedded for easy setup)
* **GUI:** Java Swing
* **Design & Modeling:** UML (Class, Activity, State Diagrams)

## 📋 Key Features
* **Comprehensive System Architecture:** Designed using detailed UML modeling based on concrete user stories and dynamic analysis.
* **Database Abstraction:** Utilizes Hibernate ORM to map complex relational data models to Java objects.
* **Interactive GUI:** Features a Java Swing interface demonstrating seamless data flow between underlying business entities.

## 📂 System Documentation (UML)
A major focus of this project was software analysis and design. You can view the system architecture in the `/docs` folder:
* [Analytical & Design Class Diagrams](./docs/class-diagram.png)
* [Activity Diagrams](./docs/activity-diagram.png)
* [State Diagrams](./docs/state-diagram.png)

## 🛠️ How to Run Locally
Because this project utilizes an embedded H2 database, it is completely portable and requires no external database server setup.

1. Clone the repository: `git clone https://github.com/YourUsername/hotel-management-system.git`
2. Open the project in your preferred IDE (IntelliJ IDEA, Eclipse, etc.).
3. Run the `Main.java` file. The H2 database will initialize automatically.