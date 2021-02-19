# JWS-GraphQL
JWS-GraphQL is an implemenation of GraphQL using [graphql-spqr](https://github.com/leangen/graphql-spqr) into the [JavaWebStack](https://github.com/JavaWebStack/Web-Framework) Eco-System.  
# Getting Started
## Repository
### Maven (pom.xml)
```xml
<repositories>
    <repository>
        <id>javawebstack</id>
        <url>https://repo.javawebstack.org</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.javawebstack</groupId>
        <artifactId>Web-Framework</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>

    <dependency>
        <groupId>de.x7airworker</groupId>
        <artifactId>JWS-GraphQL</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```
## Usage
```java
public class ExampleApplication extends WebApplication {
    // typical init already done
    protected void setupModules() {
        // the first parameter is optional and defines the route.
        addModule(new GraphQLModule("/graphql", UserService.class));
    }
}
```

```java
public class UserService {
    @GraphQLQuery(name = "users")
    public List<User> all () {
        return Repo.get(User.class).all();
    }
}
```

```java
public class User extends Model {
    @Column
    public int id;

    @Column
    public String name;

    public String getName() {
        return name;
    }
}
```

For more GraphQL specific examples refer to the [graphql-spqr repository](https://github.com/leangen/graphql-spqr)

### Make a query
```
GET http://localhost/graphql?query=query{users{name}}
```
or using the new `graphql` command:
```
graphql query{users{name}}
```