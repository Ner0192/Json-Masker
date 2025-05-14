
# JSON Masking Agent

This Spring Boot agent module allows automatic **masking of sensitive fields** in HTTP JSON responses and **exports the masked response as a trace attribute** using OpenTelemetry.

## ✨ Features

- ✅ Automatically masks sensitive fields in JSON responses (e.g., `password`, `ssn`, `token`)
- ✅ Injects masked response into the active OpenTelemetry span
- ✅ Configurable fields to mask via application properties
- ✅ Easily pluggable into existing Spring Boot applications

---

## 📦 How to Use

### 1. Build the Agent JAR

From this project directory:

```bash
mvn clean install
```

The JAR will be generated at:

```
target/agent-0.0.1-SNAPSHOT.jar
```

---

### 2. Add to Your Application as a Local Dependency

To install agent jar in .m2 run the following command

```bash
mvn install:install-file \                          
  -Dfile=/absolute-path-to-jar/agent-0.0.1-SNAPSHOT.jar \
  -DgroupId=com.service \
  -DartifactId=agent \
  -Dversion=0.0.1-SNAPSHOT \
  -Dpackaging=jar
```

Then in your consuming app's `pom.xml`, add:

```xml
<dependency>
    <groupId>com.service</groupId>
    <artifactId>agent</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Add values to the scanBasePackages parameter in you application's main java file 

```java
@SpringBootApplication(scanBasePackages = {"{current-base-package}", "com.service.agent"})
```

---

### 3. Configure Fields to Mask

Set the fields to mask in your application's properties file:

#### ✅ In `application.properties`:
```properties
response.masked.fields=password,ssn,creditCard
```

#### ✅ Or in `application.yml`:
```yaml
response:
  masked:
    fields: "password,ssn,creditCard"
```

---

## 🧠 How It Works

- Uses `@ControllerAdvice` and Spring's `ResponseBodyAdvice` to intercept JSON responses.
- Uses regex-based masking to replace configured fields with `*****`.
- Attaches the masked response to the active OpenTelemetry span via:
  ```java
  Span.current().setAttribute("response.body", maskedJson);
  ```

---

## 🧪 Example

**Input JSON:**
```json
{
  "username": "john",
  "password": "secret123",
  "ssn": "123-45-6789"
}
```

**With `response.masked.fields=password,ssn`**, the response becomes:
```json
{
  "username": "john",
  "password": "*********",
  "ssn": "***********"
}
```

---

## 📋 Notes

- This module assumes the response is JSON-serializable.
- Currently supports flat JSON as wel as nested structures.
- This module is not a Java agent (`-javaagent`) — it's a Spring component library.
