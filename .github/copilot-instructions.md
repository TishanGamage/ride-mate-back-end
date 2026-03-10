# GitHub Copilot Instructions for RideMate Backend

## Project Overview
RideMate is a ride-sharing backend application built with Spring Boot 3.2.0, Java 21, MySQL database, and Spring Security. The application follows a layered architecture with clear separation of concerns.

## Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Java Version**: 21
- **Database**: MySQL with Liquibase migrations
- **Security**: Spring Security with password encoding
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven
- **Logging**: SLF4J with Lombok @Slf4j
- **Validation**: Jakarta Bean Validation

## Architecture & Project Structure

### Package Structure
```
com.ride.mate/
├── config/          # Configuration classes (Security, Web, etc.)
├── controller/      # REST API endpoints
├── core/            # Base classes, utilities, exception handlers
├── domain/          # JPA entities (database models)
├── enums/           # Application enumerations
├── exception/       # Custom exception classes
├── repository/      # Spring Data JPA repositories
├── resources/       # DTOs (Request/Response objects)
├── service/         # Business logic interfaces
│   └── impl/        # Service implementations
└── util/            # Utility classes
```

### Layer Responsibilities
1. **Controller Layer**: Handle HTTP requests/responses, delegate to services
2. **Service Layer**: Implement business logic, validations, transactions
3. **Repository Layer**: Database access using Spring Data JPA
4. **Domain Layer**: JPA entities representing database tables
5. **Resource Layer**: DTOs for API requests and responses

## Coding Standards & Conventions

### 1. Class Naming Conventions
- **Controllers**: `[Feature]Controller.java` (e.g., `UserRegistrationController`)
- **Services**: `[Feature]Service.java` (interface) + `[Feature]ServiceImpl.java` (implementation)
- **Repositories**: `[Entity]Repository.java` (e.g., `UserRepository`)
- **Entities**: `[EntityName].java` (e.g., `User`, `DriverProfile`)
- **DTOs/Resources**: `[Feature][Add/Update/Response]Resource.java`
- **Enums**: `[Name].java` (e.g., `UserRole`, `UserStatus`, `YesNo`)

### 2. File Header Documentation
**ALWAYS include this JavaDoc header in every class:**

```java
/**
 * [Class Name]
 * [Brief description of purpose]
 *
 * @author [Author Name]
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 [DD-MM-YYYY]    N/A          N/A          [Author]        Initial Development
 */
```

### 3. Entity Classes (Domain Layer)

#### Base Entity Pattern
- All entities MUST extend `BaseEntity` which provides:
  - `Long id` with sequence generator
  - `Long version` for optimistic locking
  - `Timestamp syncTs`

#### Entity Standards
```java
@Getter
@Setter
@Entity
@Table(name = "table_name")  // Use singular, snake_case
public class EntityName extends BaseEntity implements Serializable {
    
    // Use JPA annotations with explicit column names
    @Column(name = "column_name", nullable = false, length = 255)
    private String fieldName;
    
    // Use enums with @Enumerated
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEnum status;
    
    // Always include audit fields (NOT in BaseEntity)
    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;
    
    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;
    
    @Column(name = "modified_date")
    private Timestamp modifiedDate;
    
    @Column(name = "modified_user", length = 100)
    private String modifiedUser;
    
    // Relationships at the end
    @OneToOne(mappedBy = "fieldName", cascade = CascadeType.ALL, orphanRemoval = true)
    private RelatedEntity relatedEntity;
}
```

### 4. Repository Layer

```java
@Repository
public interface EntityRepository extends JpaRepository<Entity, Long> {
    
    // Use descriptive method names following Spring Data conventions
    Optional<Entity> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<Entity> findByStatusAndRole(Status status, Role role);
}
```

### 5. Service Layer

#### Service Interface
```java
public interface EntityService {
    Entity createEntity(EntityAddResource request);
    Entity updateEntity(EntityUpdateResource request);
    // Other business methods
}
```

#### Service Implementation Standards
```java
@Slf4j
@Service
@Transactional
public class EntityServiceImpl extends MessagePropertyBase implements EntityService {
    
    // Use constructor injection (NO @Autowired)
    private final EntityRepository entityRepository;
    private final Environment environment;
    
    public EntityServiceImpl(EntityRepository entityRepository, 
                            Environment environment) {
        this.entityRepository = entityRepository;
        this.environment = environment;
    }
    
    @Override
    public Entity createEntity(EntityAddResource request) {
        // Log entry point
        log.info("Processing entity creation for: {}", request.getIdentifier());
        
        // Validate business rules
        if (entityRepository.existsByEmail(request.getEmail())) {
            log.warn("Validation failed: Email already exists - {}", request.getEmail());
            throw new ValidateRecordException(
                environment.getProperty(EMAIL_ALREADY_EXISTS), "message");
        }
        
        // Create and populate entity
        Entity entity = new Entity();
        entity.setField(request.getField());
        entity.setCreatedDate(DateUtil.getDate());
        entity.setCreatedUser("SYSTEM");
        
        // Save to database
        Entity savedEntity = entityRepository.save(entity);
        log.info("Entity created successfully with ID: {}", savedEntity.getId());
        
        return savedEntity;
    }
}
```

#### Service Layer Rules
- ALWAYS extend `MessagePropertyBase` for message property access
- Use `@Slf4j` for logging
- Use `@Transactional` at class level
- Use constructor injection, NOT field injection
- Log at key points: entry, warnings, success, errors
- Throw `ValidateRecordException` with property messages for validation errors
- Set audit fields: `createdDate`, `createdUser`, `modifiedDate`, `modifiedUser`
- Use `DateUtil.getDate()` for timestamps
- Use `"SYSTEM"` as the default user for audit fields

### 6. Controller Layer

```java
@Slf4j
@RestController
@RequestMapping(value = "/endpoint")
@CrossOrigin(origins = "*")
public class FeatureController extends MessagePropertyBase {
    
    private final FeatureService featureService;
    private final Environment environment;
    
    public FeatureController(FeatureService featureService, 
                            Environment environment) {
        this.featureService = featureService;
        this.environment = environment;
    }
    
    /**
     * [Endpoint description]
     *
     * @param request [description]
     * @return ResponseEntity with response
     */
    @PostMapping
    public ResponseEntity<?> createEntity(@Valid @RequestBody EntityAddResource request) {
        log.info("Received request for: {}", request.getIdentifier());
        
        Entity entity = featureService.createEntity(request);
        
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(entity.getId());
        response.setMessages(environment.getProperty(RECORD_CREATED));
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
```

#### Controller Rules
- Extend `MessagePropertyBase`
- Use `@CrossOrigin(origins = "*")` for CORS
- Use `@Valid` for request validation
- Return `SuccessAndErrorDetailsResource` for success responses
- Use appropriate HTTP status codes (CREATED for POST, OK for GET/PUT)
- Retrieve messages from environment properties

### 7. Resource/DTO Classes

```java
@Getter
@Setter
public class EntityAddResource {
    
    @NotBlank(message = "{can.not.be.blank}")
    @Email(message = "{email.invalid}")
    private String email;
    
    @NotBlank(message = "{can.not.be.blank}")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "{invalid.value}")
    private String phoneNumber;
    
    @NotNull(message = "{invalid.value}")
    private EnumType enumField;
}
```

#### Validation Messages
**CRITICAL**: All validation messages in Resource/DTO classes MUST use property placeholders with curly braces `{}`.

**Rules:**
1. **Always use curly braces**: `message = "{property.key}"` format
2. **Never hardcode**: `message = "Hardcoded text"` is NOT allowed
3. **Define in notification.properties**: All validation message keys MUST exist in `notification.properties`

**Standard Validation Message Properties:**

| Validation Type | Property Key | Usage Example |
|----------------|--------------|---------------|
| Required fields | `{can.not.be.blank}` | `@NotBlank(message = "{can.not.be.blank}")` |
| Email validation | `{email.invalid}` | `@Email(message = "{email.invalid}")` |
| Email required | `{email.required}` | `@NotBlank(message = "{email.required}")` |
| General validation | `{invalid.value}` | `@NotNull(message = "{invalid.value}")` |
| Pattern validation | `{verification.code.pattern}` | `@Pattern(regexp = "^[0-9]{6}$", message = "{verification.code.pattern}")` |

**Example of CORRECT Resource Class:**
```java
@Getter
@Setter
public class UserRegistrationAddResource {
    
    @NotBlank(message = "{can.not.be.blank}")
    @Email(message = "{email.invalid}")
    private String email;
    
    @NotBlank(message = "{can.not.be.blank}")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "{invalid.value}")
    private String phoneNumber;
    
    @NotNull(message = "{invalid.value}")
    private UserRole role;
}
```

**Example of WRONG Resource Class:**
```java
// ❌ WRONG - Hardcoded messages
@NotBlank(message = "Email is required")  // Missing curly braces
@Email(message = "Invalid email format")  // Hardcoded text

// ❌ WRONG - Missing curly braces
@NotBlank(message = "can.not.be.blank")  // Should be {can.not.be.blank}
@Email(message = "email.invalid")         // Should be {email.invalid}
```

**Available Validation Message Keys in notification.properties:**
- `can.not.be.blank` = Cannot be blank.
- `invalid.value` = Invalid value.
- `email.required` = Email is required.
- `email.invalid` = Invalid email format.
- `verification.code.required` = Verification code is required.
- `verification.code.pattern` = Verification code must be 6 digits.

### 8. Exception Handling

```java
// Throw validation exceptions with property messages
throw new ValidateRecordException(
    environment.getProperty(MESSAGE_PROPERTY_KEY), 
    "message"
);
```

#### Exception Message Standards
**CRITICAL**: All exception messages MUST follow these standards:

1. **Constant Naming Convention**: Use UPPER_SNAKE_CASE format (e.g., `EMAIL_ALREADY_EXISTS`, `RECORD_NOT_FOUND`)
2. **MessagePropertyBase Registration**: ALL message constants MUST be defined in `MessagePropertyBase` class
3. **notification.properties Entries**: ALL message keys MUST have corresponding entries in `notification.properties` file
4. **NO Hardcoded Strings**: NEVER use hardcoded property key strings like `environment.getProperty("email.already.exists")`

**Example of CORRECT Exception Handling:**
```java
// ✅ CORRECT - Using constant
throw new ValidateRecordException(
    environment.getProperty(EMAIL_ALREADY_EXISTS), 
    "message"
);

// ❌ WRONG - Using hardcoded string
throw new ValidateRecordException(
    environment.getProperty("email.already.exists"), 
    "message"
);
```

**Steps to Add New Exception Message:**
1. Add constant to `MessagePropertyBase.java`:
   ```java
   protected static final String RECORD_NOT_FOUND = "record.not.found";
   ```
2. Add message to `notification.properties`:
   ```properties
   record.not.found = Record is not found!
   ```
3. Use constant in service:
   ```java
   throw new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
   ```

#### Common Message Property Keys (in MessagePropertyBase)
**Verification Messages:**
- `VERIFICATION_CODE_NOT_FOUND`
- `VERIFICATION_ALREADY_VERIFIED`
- `VERIFICATION_CODE_EXPIRED`
- `VERIFICATION_MAX_ATTEMPTS_EXCEEDED`
- `VERIFICATION_INVALID_CODE`
- `VERIFICATION_SUCCESS`

**Common Messages:**
- `RECORD_CREATED`
- `RECORD_UPDATED`
- `RECORD_NOT_FOUND`
- `RECORD_VERSION_MISMATCH`
- `EMAIL_ALREADY_EXISTS`
- `PHONE_NUMBER_ALREADY_EXISTS`
- `EXPIRY_DATE_MUST_BE_FUTURE`

**Identification Messages:**
- `IDENTIFICATION_TYPE_NOT_FOUND`

**Login Messages:**
- `LOGIN_USER_NOT_FOUND`
- `LOGIN_INVALID_CREDENTIALS`
- `LOGIN_ACCOUNT_SUSPENDED`
- `LOGIN_EMAIL_NOT_VERIFIED`
- `LOGIN_SUCCESS`

### 9. Database Conventions

#### Table Naming
- Use **singular** form: `user` (NOT `users`)
- Use **snake_case**: `user_profile`, `emergency_contacts`

#### Column Naming
- Use **snake_case**: `email_verified`, `phone_number`, `created_date`
- Timestamp fields: `created_date`, `modified_date`, `last_login_date`
- Audit fields: `created_user`, `modified_user`
- Version field: `version` (for optimistic locking)

#### Liquibase Migrations
- Store in `src/main/resources/mysql/`
- Master changelog: `src/main/resources/db/changelog.yml`
- Use YAML format for changesets

### 10. Enums

```java
public enum EnumName {
    VALUE_ONE,
    VALUE_TWO,
    VALUE_THREE
}
```

Common Enums:
- `UserRole`: PASSENGER, DRIVER, ADMIN
- `UserStatus`: ACTIVE, SUSPENDED, INACTIVE
- `YesNo`: YES, NO

### 11. Logging Standards

```java
// Entry point logging
log.info("Processing [operation] for: {}", identifier);

// Warning logging
log.warn("[Operation] failed: [Reason] - {}", value);

// Success logging
log.info("[Entity] [operation] successfully with ID: {}", id);

// Error logging (handled by exception handler)
```

### 12. Date/Time Handling

- Use `Timestamp` for database fields
- Use `LocalDate` for date-only fields
- Use `DateUtil.getDate()` to get current timestamp
- Use `DateUtil.stringToLocalDate()` for parsing
- Use `DateUtil.isFutureLocalDateTime()` for validation

### 13. Security

- Passwords MUST be encoded using `PasswordEncoder`
- Use constructor injection for `PasswordEncoder`
- Example: `user.setPasswordHash(passwordEncoder.encode(request.getPassword()))`

### 14. Testing

- Test classes in: `src/test/java/com/ride/mate/`
- Follow same package structure as main
- Name: `[ClassName]Tests.java`

## Common Patterns

### Pattern 1: Entity Creation
```java
Entity entity = new Entity();
entity.setField(request.getField());
entity.setCreatedDate(DateUtil.getDate());
entity.setCreatedUser("SYSTEM");
Entity saved = repository.save(entity);
```

### Pattern 2: Entity Update
```java
Entity entity = repository.findById(id)
    .orElseThrow(() -> new ValidateRecordException(
        environment.getProperty(RECORD_NOT_FOUND), "message"));

entity.setField(request.getField());
entity.setModifiedDate(DateUtil.getDate());
entity.setModifiedUser("SYSTEM");

Entity updated = repository.save(entity);
```

### Pattern 3: Validation Before Save
```java
if (repository.existsByEmail(request.getEmail())) {
    log.warn("Validation failed: Email already exists - {}", request.getEmail());
    throw new ValidateRecordException(
        environment.getProperty(EMAIL_ALREADY_EXISTS), "message");
}
```

### Pattern 4: Optional Handling
```java
identificationTypeRepository.findById(id)
    .ifPresentOrElse(
        details::setIdentificationType,
        () -> {
            throw new ValidateRecordException(
                environment.getProperty(IDENTIFICATION_TYPE_NOT_FOUND), 
                "message");
        }
    );
```

## Do's and Don'ts

### ✅ DO:
- Use Lombok annotations (`@Getter`, `@Setter`, `@Slf4j`)
- Use constructor injection
- Extend `BaseEntity` for all entities
- Extend `MessagePropertyBase` for services and controllers
- Include complete JavaDoc headers
- Log all important operations
- Use transaction management with `@Transactional`
- Validate all inputs
- Use property files for messages
- Use singular table names
- Set audit fields on all operations
- Use `YesNo` enum for boolean-like flags
- Use `@CrossOrigin(origins = "*")` on controllers
- Use UPPER_SNAKE_CASE constants for all exception messages (e.g., `EMAIL_ALREADY_EXISTS`)
- Add all message constants to both `MessagePropertyBase` and `notification.properties`

### ❌ DON'T:
- Use `@Autowired` field injection
- Use hardcoded error messages
- Use hardcoded property key strings like `environment.getProperty("email.already.exists")`
- Use hardcoded validation messages like `@NotBlank(message = "Email is required")`
- Use validation messages without curly braces like `@Email(message = "email.invalid")`
- Use plural table names
- Create entities without audit fields
- Forget to log operations
- Skip validation
- Return entities directly from controllers
- Use primitive boolean for database flags (use YesNo enum)
- Forget `@Transactional` on service classes
- Miss the change history in JavaDoc headers
- Add exception messages without defining constants in `MessagePropertyBase`
- Add validation messages without defining them in `notification.properties`

## Property File Structure

### application.properties
- Database configuration
- Server context path: `/ride-mate`
- Mail server configuration

### notification.properties
- All user-facing messages
- Error messages
- Success messages

## API Response Pattern

### Success Response
```json
{
  "id": 123,
  "messages": "Record created successfully"
}
```

### Error Response
Handled by `BaseResponseEntityExceptionHandler`

## Additional Notes

1. **Email Verification**: System uses verification codes stored in `verification_codes` table
2. **Password Security**: All passwords stored as hashes using Spring Security's PasswordEncoder
3. **Optimistic Locking**: All entities include version field for concurrency control
4. **Multi-Role Support**: Users can be PASSENGER, DRIVER, or ADMIN
5. **Audit Trail**: All entities track who created/modified and when
6. **Sequence Generator**: Using custom sequence `common_seq` for all IDs

## When Creating New Features

1. **Define Domain Entity** → Extend BaseEntity, add audit fields
2. **Create Repository Interface** → Extend JpaRepository
3. **Create Resource DTOs** → Add/Update/Response resources with validation (use `{property.key}` format)
4. **Add Validation Messages** → Define all validation message keys in `notification.properties`
5. **Create Service Interface** → Define business methods
6. **Implement Service** → Extend MessagePropertyBase, add @Transactional
7. **Add Exception Messages** → Add constants to `MessagePropertyBase` and values to `notification.properties`
8. **Create Controller** → Extend MessagePropertyBase, add REST endpoints
9. **Create Liquibase Migration** → Define database schema in YAML

**Example Workflow for New Validation Message:**
1. Add to DTO: `@NotBlank(message = "{field.required}")`
2. Add to `notification.properties`: `field.required = Field is required.`
3. Use in code: Message automatically resolved from properties file

**Example Workflow for New Exception Message:**
1. Add constant to `MessagePropertyBase`: `protected static final String RESOURCE_NOT_FOUND = "resource.not.found";`
2. Add to `notification.properties`: `resource.not.found = Resource not found.`
3. Use in service: `throw new ValidateRecordException(environment.getProperty(RESOURCE_NOT_FOUND), "message");`

## Current Date Reference
Today's date is: **March 2, 2026**

Use this date when generating new file headers or documentation timestamps.

---

**Remember**: This is a production-grade application. Follow all conventions strictly to maintain code consistency and quality.

