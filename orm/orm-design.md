1. Design Pattern: The Data Access Object (DAO) pattern is an effective way to create an ORM library that supports different database implementations. This pattern involves creating a DAO interface which defines a set of methods that can be used to access data. This interface can then be implemented by concrete classes that provide the actual implementation for different databases.

2. Interfaces: The following interfaces should be created to support an ORM library: 
- Data Access Object (DAO) - This is the main interface responsible for defining the methods used to access data from the database.
- Entity Manager - This interface defines methods for managing entities in the database.
- Query Builder - This interface defines methods for creating and executing database queries.
- Result Set Mapper - This interface defines methods for mapping database query results to Java objects.

3. Popular Databases: The top 5 most popular databases that should be supported by an ORM library are: 
- MySQL
- PostgreSQL
- Microsoft SQL Server
- Oracle Database
- SQLite

4. Code Example: Here is an example of a DAO interface that can be used to access data from a database:

public interface EmployeeDAO {

    public List<Employee> getEmployees();

    public Employee getEmployeeById(int id);

    public void saveEmployee(Employee employee);

    public void deleteEmployee(int id);

}

###

1. A design pattern that is applicable for designing an ORM library to support different database implementations is the Data Access Object (DAO) pattern. This pattern allows for the abstraction of data access logic from the underlying data sources, and provides a consistent interface for other components in the application to access data.

2. Interfaces that should be created to support different database implementations include a DataAccessObject interface, which provides methods for establishing connections, executing queries, and managing transactions. Additionally, a DatabaseManager interface should be created, which provides methods for obtaining database connections and executing queries or updates against the database.

3. The top 5 pure Java databases that should be supported are Apache Derby, H2, PostgreSQL, Oracle, and HSQLDB.

4. An example of code for a DataAccessObject interface is provided below:

public interface DataAccessObject {

    public Connection getConnection();

    public void executeQuery(String query) throws SQLException;

    public ResultSet executeQueryWithResult(String query) throws SQLException;

    public void executeUpdate(String query) throws SQLException;

    public boolean beginTransaction() throws SQLException;

    public void commitTransaction() throws SQLException;

    public void rollbackTransaction() throws SQLException;

}

###

## Java Library Design for an ORM Library 

An object-relational mapping (ORM) library is used to map objects in a Java application to a data source. The library should be designed to support different database implementations, while still allowing the application to be portable across multiple databases. The following design patterns are applicable: 

* **Data Access Object (DAO) Pattern** - This pattern uses a DAO class that abstracts and encapsulates all access to the data source. The DAO class provides a consistent API for database access that can be used across multiple databases.

* **Factory Pattern** - This pattern creates objects based on the data source implementation. A Factory class can be used to create objects that represent different tables or views in the database.

* **Adapter Pattern** - This pattern allows a class to adapt an existing database to a new interface. This is useful for making a single application compatible with multiple databases.

When designing the library, the following interfaces should be created:

* **DataSource** - This interface is used to represent a data source. It should define methods for creating connections, executing queries, and other operations.

* **DatabaseAdapter** - This interface is used to adapt an existing database to a new interface. It should define methods for mapping objects to the database and executing queries.

* **Query** - This interface is used to represent a query. It should define methods for setting parameters and executing queries.

The following are the top pure Java embedded databases that should be supported:

* **Apache Derby** - Apache Derby is a pure Java database that is lightweight, fast, and easy to use.

* **HSQLDB** - HSQLDB is a pure Java database that supports SQL and JDBC.

* **SQLite** - SQLite is a lightweight, zero-configuration, and open source database.

* **H2 Database** - H2 Database is a pure Java database that is fast, open source, and supports SQL.

Below is an example of a DataSource interface that could be used for an ORM library:

```java
public interface DataSource {
    Connection getConnection() throws SQLException;
    void closeConnection(Connection connection);
    void executeQuery(String query, List<Object> params);
    ResultSet executeQuery(String query);
}
```


# ORM Library Design

## Design Pattern

The most applicable design pattern for an ORM library is the **Data Access Object (DAO)** pattern. This pattern enables a clean separation between the object model and the database implementation. It provides an interface for accessing and manipulating the data, and a concrete implementation for each supported database.

## Interfaces

The following interfaces should be created:

* **Data Access Object (DAO)**: This interface provides methods for accessing and manipulating data in a database.
* **Database Connection**: This interface provides methods for connecting to a database and executing queries.
* **Object Model**: This interface defines the object model that the ORM library should support.

## Supported Databases

The top embedded NOSQL databases that should be supported are MongoDB, Couchbase, Cassandra, and Redis.

The top embedded SQL databases that should be supported are SQLite, MySQL, and PostgreSQL.

The databases that are purely Java are H2 and Derby.

## Java Library Design: ORM Library to Support Different Database Implementations 

### Design Patterns
1. Strategy Pattern: The strategy pattern is an object-oriented design pattern that allows a program to dynamically select an algorithm from a family of algorithms while making sure that the client remains unaware of the specific implementation used. This is useful for ORM libraries, as it allows for the implementation to be swapped out without the client having to make any changes.

2. Factory Pattern: The factory pattern is an object-oriented design pattern for creating objects without the need for the client to specify the exact class of object that is to be created. This is useful for ORM libraries, as it allows for the library to be extended easily without the client having to worry about implementation details.

3. Adapter Pattern: The adapter pattern is an object-oriented design pattern that allows the interface of an existing class to be used as the interface for a new class. This can be used to make different database implementations compatible with the same ORM library.

### Interfaces
1. Database Interface: This interface would define the methods necessary to interact with a database, such as connecting, running queries, and retrieving results. This would be implemented by each database implementation, and the ORM library would use this interface to interact with the database.

2. ORM Interface: This interface would define the methods necessary to manage objects in the database. This would be implemented by the ORM library, and the client would use this interface to interact with the ORM library.

3. Model Interface: This interface would define the methods necessary to map an object to the database. This would be implemented by the client, and the ORM library would use this interface to map objects to the database.

####

# ORM (Object-Relational-Mapping) in Combination with IOC (Inversion Of Control), DI (Dependency Injection).

One of the most important stages of designing a Java library is to decouple the schema definition from the database implementation. This way, the user can run the library on different database implementations without any problems.

In order to achieve this goal, the following interfaces and classes are needed:

* A **Database Driver Interface** to handle different types of databases and define a set of generic methods to provide easy access to the database(s).
* An **ORM Framework** which defines a layout for mapping the object relations from the database to a Java class, and vice versa. This allows for advanced querying and searching within the database.
* An **Object Model** which contains the same information as the database, but in an object-oriented format, allowing for easier manipulation and better performance.
* A **Dependency Injection Framework** which allows dependencies to be injected into classes so that the code is more maintainable.

The different design patterns that are applicable in this scenario include: 

* **DataMapper** pattern, which separates the database access layer and the object model layer, allowing each layer to be modified independently.
*  **Factory** pattern, which is used to create abstract factories, like a DatabaseDriverFactory for creating database driver objects depending on the type of database used.
* **Dependency Injection** pattern, which allows dependencies between the different classes to be injected into the objects so that the code is more maintainable.
* **Proxy** pattern, which allows the database driver to proxy requests to different databases.

These approaches enable the schema definition to be decoupled from the database implementation. This means that the library is more flexible and easier to maintain, and can be used with multiple databases without requiring the user to do any extra work.

######

# The Repository Pattern 

The repository pattern is an architectural design pattern used to abstract data access and store data in persistence layers such as databases or web services. It helps decouple the application logic, which utilizes the data, from the data itself. It serves as an abstraction layer between the data and the business logic of the application.

The main pros of using the repository pattern are: 

- It makes data source agnostic: The repository pattern makes the data source agnostic. This makes it easier to switch between different data sources, should the need arise. 

- It simplifies data access: By abstracting the data, the repository pattern simplifies and hides the details of data access. Therefore, the application logic can concentrate on retrieving the exact needed data, without having to worry about querying and storing them.

- It allows for better unit testing: data access code can be tested in isolation, as part of a unit test, by using mocking frameworks like Mockito.

Some cons of using the repository pattern are: 

- It introduces an additional layer of software: Having an additional layer of software adds more complexity to the system, and increased complexity can lead to buggy, unstable applications.

- It can cause hard to diagnose errors: If the repository pattern is improperly implemented, it can cause hard to diagnose errors. For example, implementing an incorrect SQL query can be hard to track down.

## Example Code 

Here is an example of a repository pattern implementation in Java for a database data store. First, we create an `ItemRepository` interface, which will serve as an abstraction layer:

```java
public interface ItemRepository {

  public List<Item> findAllItems();
  public Item findItemById(int id);
  public void saveItem(Item item);
  public void deleteItem(int id);

} 
```

Then, we implement this interface against a database in a class called `DatabaseItemRepository`:

```java
public class DatabaseItemRepository implements ItemRepository {

  private Database database;

  public DatabaseItemRepository (Database database) {
    this.database = database;
  }

  public List<Item> findAllItems() {
    String query = "SELECT * FROM items";
    List<Item> items = new ArrayList<>();
    // execute query and store items in list
    return items;
  }

  public Item findItemById(int id) {
    String query = "SELECT * FROM items WHERE id=?";
    Item item = new Item();
    // execute query and store item
    return item;
  }
 
  public void saveItem(Item item) {
    String query = "INSERT INTO items (...) VALUES (...)";
    // execute query
  }

  public void deleteItem(int id) {
    String query = "DELETE FROM items WHERE id=?";
    // execute query
  }

} 
```

In this way, the `ItemRepository` interface is the only dependency that an application needs to access the data. It can be swapped out easily with a different implementation, such as a web service repository.

####

The DataMapper pattern, also known as the Object-Relational Mapping (ORM) pattern, is a design pattern used to abstract the database layer from the business logic. It provides a way to map the data stored in a relational database to classes and objects in an object-oriented programming language.

Pros:

-DataMapper eliminates the need for a large amount of code as it maps objects to their relational database counterparts.
-Allows for easier maintenance as SQL queries can be applied to any object instead of writing custom code for a specific set of objects.
-Reduces the amount of dependencies of a system since manual querying is eliminated and objects are independent of the database.

Cons:
-The logic of the database can be isolated or forgotten when using the DataMapper pattern.
-Sometimes the abstractions in the pattern can introduce complexity.
-Increased processing time since multiple calls may be required for complicated queries.

Java Code Examples: 

// Sets up the DataMapper
Mapper<Person> mapper = new Mapper<>(Person.class);

// Selects a Person object from the database
Person person = mapper.select(1);

// Updates the Person object in the database
person.setName("John Doe");
mapper.update(person);

// Deletes a Person object from the database
mapper.delete(person);

#####

The Strategy Pattern is a behavioral design pattern that defines a family of algorithms, encapsulates each algorithm, and makes them interchangeable. This allows the algorithms to be selected and changed at runtime depending on the specific requirements of the application.

Pros:
* Makes code more extensible - Allows algorithms to be selected and changed at runtime. 
* Easily reusable code - Algorithms can be plugged-in and reused without having to write extra code.
* Increases code readability - Algorithm logic is encapsulated within their own classes which are easy to read and understand.

Cons:
* Over-engineering - If the application doesn’t need to be dynamically changed or if all algorithms are used in the same context, the Strategy Pattern can be over-engineering.
* Difficulty testing - Because algorithms are encapsulated in different classes, it can be difficult to test all algorithms in a single unit test. 

Java Code Example:

```java
public interface Strategy {
   public int execute(int a, int b);
}

public class AddStrategy implements Strategy {
   @Override
   public int execute(int a, int b) {
      return a + b;
   }
}

public class SubtractStrategy implements Strategy {
   @Override
   public int execute(int a, int b) {
      return a - b;
   }
}

public class Context {
   private Strategy strategy;

   public Context(Strategy strategy){
      this.strategy = strategy;
   }

   public int executeStrategy(int a, int b){
      return strategy.execute(a, b);
   }
}

public class StrategyExample {
   public static void main(String[] args) {
      Context context = new Context(new AddStrategy());
      System.out.println("10 + 5 = " + context.executeStrategy(10, 5));

      context = new Context(new SubtractStrategy());
      System.out.println("10 - 5 = " + context.executeStrategy(10, 5));
   }
}
```


## What components does an ORM library need to decouple the different SQL implementations from the user application?

1. Database Connection Factory: This component is responsible for establishing and managing the connection between the user application and the database. It provides a standard way for the user application to connect to the database, regardless of the underlying SQL implementation. For example, in Java this could be implemented using a JDBC DataSource object.

Example code:

DataSource dataSource = DataSourceFactory.getDataSource("jdbc:mysql://localhost:3306/my_db");

2. SQL Dialect: This component is responsible for providing a way for the user application to use a generic SQL query language, regardless of the underlying SQL implementation. It provides a way to translate the generic SQL query language into the specific SQL dialect used by the target database. For example, in Java this could be implemented using the Hibernate Dialect class.

Example code:

Dialect dialect = DialectFactory.getDialect("MySQL");

3. Query Executor: This component is responsible for executing the SQL queries and returning the results to the user application. For example, in Java this could be implemented using the Hibernate Session class.

Example code:

Session session = sessionFactory.openSession();
List results = session.createQuery("SELECT * FROM my_table").list();