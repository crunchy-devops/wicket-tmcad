# Point3D Implementation

A memory-efficient, immutable 3D point implementation in Java with comprehensive test coverage.

## Features

- Immutable 3D point representation using float coordinates (x, y, z)
- Memory-efficient implementation using primitive float types
- Thread-safe due to immutability
- Comprehensive unit tests with 80% code coverage requirement
- Proper implementation of `equals()`, `hashCode()`, and `toString()`

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Project Structure

```
src/
├── main/java/com/crunchydevops/
│   └── Point3D.java           # Main implementation
└── test/java/com/crunchydevops/
    └── Point3DTest.java       # Test suite
```

## Building and Testing

To build the project and run tests:

```bash
mvn clean test
```

This will:
1. Compile the source code
2. Run all unit tests
3. Generate a JaCoCo code coverage report
4. Verify that code coverage is at least 80%

The coverage report can be found at: `target/site/jacoco/index.html`

## Usage Example

```java
// Create a new point
Point3D point = new Point3D(1.0f, 2.0f, 3.0f);

// Access coordinates
float x = point.getX(); // 1.0f
float y = point.getY(); // 2.0f
float z = point.getZ(); // 3.0f

// Compare points
Point3D another = new Point3D(1.0f, 2.0f, 3.0f);
boolean areEqual = point.equals(another); // true
```

## Code Coverage Requirements

The project is configured to enforce a minimum of 80% line coverage using JaCoCo. The build will fail if coverage falls below this threshold. Current test suite covers:

- Constructor and getters
- All equality comparisons
- Hash code generation
- String representation
- Edge cases (zero values, negative values)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
