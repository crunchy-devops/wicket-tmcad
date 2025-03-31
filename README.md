# Point3D and PointCloud Implementation

A memory-efficient, immutable 3D point implementation in Java with comprehensive test coverage, including a PointCloud class for geometric operations.

## Features

- Immutable 3D point representation using float coordinates (x, y, z)
- Memory-efficient implementation using primitive float types
- Thread-safe due to immutability
- Comprehensive unit tests with 80% code coverage requirement
- Proper implementation of `equals()`, `hashCode()`, and `toString()`
- PointCloud class with geometric operations:
  - Distance calculation between points
  - Slope calculation (angle from horizontal plane)
  - Bearing angle calculation (angle from true north)
  - Efficient point storage with unique IDs

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Project Structure

```
src/
├── main/java/com/crunchydevops/
│   ├── Point3D.java           # Main point implementation
│   └── PointCloud.java        # Point cloud with geometric operations
└── test/java/com/crunchydevops/
    ├── Point3DTest.java       # Point3D test suite
    └── PointCloudTest.java    # PointCloud test suite
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

// Create points
Point3D p1 = new Point3D(0.0f, 0.0f, 0.0f);
Point3D p2 = new Point3D(3.0f, 4.0f, 0.0f);

// Create a point cloud
PointCloud cloud = new PointCloud();
cloud.addPoint(1L, p1);
cloud.addPoint(2L, p2);

// Calculate geometric properties
double distance = cloud.distance(1L, 2L).orElse(0.0); // 5.0
double slope = cloud.slope(1L, 2L).orElse(0.0);       // 0.0 degrees (horizontal)
double bearing = cloud.bearing(1L, 2L).orElse(0.0);   // ~36.87 degrees
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
