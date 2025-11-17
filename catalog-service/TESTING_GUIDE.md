# Testing Guide for Spring Boot 4.0 with JDK 25

## Issue Summary

When running integration tests with **REST Assured** in a Spring Boot 4.0.0-SNAPSHOT project using **JDK 25**, you may encounter a `NullPointerException` in Groovy's metaclass handling:

```
java.lang.NullPointerException
    at java.base/java.lang.Class.isAssignableFrom(Native Method)
    at org.codehaus.groovy.runtime.metaclass.ClosureMetaClass.invokeOnDelegationObject
```

### Root Cause

This issue occurs due to **incompatibility between**:
1. **Groovy 5.x** (bundled with REST Assured 5.5.0) and **JDK 21+/25**
2. **REST Assured's MockMvc module** and **Spring Framework 7.0** (used in Spring Boot 4.0)

## Solution

Use **Spring's MockMvc directly** instead of REST Assured for integration testing. This approach:
- ✅ Works perfectly with JDK 25
- ✅ Compatible with Spring Framework 7.0
- ✅ No Groovy dependency issues
- ✅ Native Spring Boot testing support

## Implementation

### Before (REST Assured - NOT WORKING)

```java
import static io.restassured.RestAssured.given;

@Sql("/test-data.sql")
class ProductControllerTest extends AbstractIT {
    @Test
    void shouldReturnProducts() {
        given().contentType(ContentType.JSON)
                .when()
                .get("/api/products")
                .then()
                .statusCode(200)
                .body("data", hasSize(10))
                .body("totalElements", is(15));
    }
}
```

### After (Spring MockMvc - WORKING ✅)

```java
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Sql("/test-data.sql")
class ProductControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldReturnProducts() throws Exception {
        mockMvc.perform(get("/api/products").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(10)))
                .andExpect(jsonPath("$.totalElements", is(15)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(false)))
                .andExpect(jsonPath("$.hasNext", is(true)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }
}
```

## Key Changes

1. **Import MockMvc classes** instead of REST Assured
2. **Inject `WebApplicationContext`** and set up MockMvc in `@BeforeEach`
3. **Use `mockMvc.perform()`** instead of `given().when().then()`
4. **Use `jsonPath()` and `status()`** matchers for assertions
5. **Add `throws Exception`** to test methods

## Benefits of MockMvc

- **Native Integration**: Part of Spring Test framework
- **Better Performance**: No HTTP overhead, tests run faster
- **More Control**: Direct access to Spring MVC infrastructure
- **Better Error Messages**: More detailed failure information
- **Future-Proof**: Always compatible with latest Spring versions

## JVM Configuration (Already Applied)

The `pom.xml` has been configured with necessary JVM arguments for JDK 25 compatibility:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>
            --add-opens=java.base/java.lang=ALL-UNNAMED
            --add-opens=java.base/java.util=ALL-UNNAMED
            --add-opens=java.base/java.lang.invoke=ALL-UNNAMED
            --add-opens=java.base/java.lang.reflect=ALL-UNNAMED
            <!-- ... more JVM arguments ... -->
        </argLine>
    </configuration>
</plugin>
```

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProductControllerTest

# Run with IntelliJ IDEA
# Right-click on test class → Run 'ProductControllerTest'
```

## References

- [Spring MockMvc Documentation](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html)
- [Spring Boot Testing Guide](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [MockMvc vs REST Assured](https://www.baeldung.com/spring-boot-testing)

## Additional Notes

- REST Assured is still included in dependencies for potential future use when compatibility improves
- For REST Assured to work properly with JDK 25 and Spring Framework 7.0, we would need:
  - REST Assured version that supports both Groovy 5+ with JDK 25 fixes AND Spring Framework 7.0 API changes
  - As of now (November 2025), such version doesn't exist

