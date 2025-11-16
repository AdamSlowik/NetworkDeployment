# 💻 Network Topology PoC

A Proof of Concept (PoC) project for managing and querying network device topology.

---

## Getting Started

### Prerequisites

* **Java 17 or higher** (The project was developed using a Java 17). 
In original Prerequisites it was 'Java 8 or higher'.
I wasn't sure how to interpret that. It could be that this project should be compatible with Java8+
or I can pick any Java newer than 8. I decided that it's the latter hence I used Java 17
* **Gradle**

### WARNING!!!
**Tests are passing on LTS versions (17, 21, 25).
Some non LTS versions might not be supported.
For example application do not start on Java18.
To run it on non LTS versions some neo4j starting parameters need to be tweaked. 
In My opinion it's ok for POC. For production version neo4j should run on separate JVM.
Such approach would eliminate this problem.**

### Running the Application

1.  Clone the repository:
    ```bash
    git clone https://github.com/AdamSlowik/NetworkDeployment.git
    cd [Your Project Directory]
    ```
2.  Run the application:
    ```bash
    ./gradlew bootRun
    ```

3. import postman collection from:
    ```code
    networkDeployment.postman_collection.json
    ```

4. When some nodes are created visit:
    ```link
    http://localhost:7474/browser/
    ```
   login without credentials and execute following query:
    ```code
    match(n)
    return n
    ```
   
---

## 💡 Key Assumptions and Scope

This project is a Proof of Concept, and the following assumptions and limitations apply:

1.  **Java Version:** The task description stated "Java 8 or higher." I interpreted this as a requirement to **use** a Java 8+ environment for development and runtime, which has been done.
2.  **Entity Scope:** The initial description mentioned two entities: `NetworkDeployment` and `Device`. Since the API specification did not explicitly require a separate `NetworkDeployment` entity, I made the assumption that it might not be strictly necessary to model it explicitly as a separate persistent entity. **The Network Deployment is implicitly defined by the disconnected subgraphs of the overall network topology.**
3.  **PoC Limitations:** As a PoC, the focus was on the core domain structure. Thus, certain aspects are simplified:
    * **Security:** Security has not been configured.
    * **Database:** An **embedded, in-memory Neo4j** database is used for simplicity, and its data is cleared upon application startup.
    * **Query Optimization:** Repository queries are functional but may not be optimally tuned for production performance.
    * **Observability:** it's not configured

---

## 🧠 Project Design and Architectural Decisions

The most critical decision for this project was selecting the appropriate data structure for storing network information. The primary focus was on creating a structure that is **flexible and extensible** to accommodate future domain evolution.

### Graph Database Rationale (Neo4j)

While the current network structure typically assumes a **tree topology** (which is often sufficient), real-world scenarios, such as **ring topologies** or **redundant uplink connections**, require more flexibility. Furthermore, different types of network devices and connections might possess unique properties. To be prepared for this complexity, a **Graph Database (Neo4j)** was chosen.

#### ✅ Advantages of Using a Graph Database

* **Flexibility:** Easily add multiple uplink edges (parents) to a node for **redundancy/backup paths**.
* **Complex Topologies:** Simple implementation of **ring topologies** and other complex graph structures.
* **Custom Edges:** Allows for the creation of different edge types (e.g., `VPN_TUNNEL`, `BACKUP`, `UPLINK`), each with its own properties.
* **Implicit Network Deployment:** The concept of a `NetworkDeployment` naturally emerges from the graph structure. Any **disconnected component** (a distinct graph or tree) in the database represents a separate deployment. Merging two deployments simply involves creating an edge between two nodes belonging to different subgraphs.
* **Pathfinding:** Graph databases excel at **pathfinding algorithms** (e.g., finding the shortest path between any two nodes), with the ability to exclude certain nodes (like Access Points) or edges (like `BACKUP` links) from the search.
* **Better Modeling:** A graph inherently models a network structure more accurately than a set of relational tables.

#### ❌ Disadvantages of Using a Graph Database

* **Higher Entry Barrier:** Graph databases are less common than relational databases, potentially requiring a higher initial learning curve.

### Implementation Summary

The flexibility offered by Neo4j allows the project to start with a simple structure that meets the initial requirements and then gradually evolve **without significant modifications to the underlying database structure**. In contrast, a standard relational database would likely require complex initial schemas or disruptive schema migrations as the network domain expands.

Aside from the use of Neo4j, the rest of the project utilizes a **standard Spring Boot architecture**.

---
