# Shared Ride Pooling Implementation Guide

## Overview

The Shared Ride Pooling feature enables passengers to join existing rides from drivers, reducing costs through ride-sharing and improving ride utilization efficiency. The system integrates with an ML model to predict driver acceptance rates for optimal ride matching.

## Architecture

### Components

1. **ShareRideDetail Entity** - Database model representing a passenger's participation in a shared ride
2. **ShareRideDetailService** - Business logic for pooling operations
3. **ShareRideDetailController** - REST API endpoints for pooling operations
4. **MLService** - Integration with ML model for driver acceptance prediction
5. **Repository** - Data access layer with advanced queries

### Data Flow

```
Passenger Request
       ↓
Search Available Rides
       ↓
ML Model Predicts Driver Acceptance
       ↓
Join Shared Ride (Create ShareRideDetail)
       ↓
Calculate Pooled Cost
       ↓
Complete Ride
```

## API Endpoints

### 1. Join Shared Ride
**Endpoint**: `POST /shared-ride/join`

**Description**: Passenger joins an existing shared ride

**Request Body**:
```json
{
  "rideDetailId": 123,
  "userId": 456,
  "rideRequestId": null,
  "passengerStartLat": 6.927079,
  "passengerStartLng": 80.771957,
  "passengerEndLat": 6.915160,
  "passengerEndLng": 80.875701,
  "passengerRideDistance": 12.5,
  "startCity": "Colombo",
  "endCity": "Kandy"
}
```

**Response**:
```json
{
  "id": 789,
  "messages": "Shared ride created successfully"
}
```

**Status Codes**:
- `201 CREATED` - Successfully joined ride
- `400 BAD REQUEST` - Invalid request data
- `404 NOT FOUND` - Ride or user not found
- `409 CONFLICT` - No available seats

---

### 2. Search Nearby Rides
**Endpoint**: `GET /shared-ride/search`

**Description**: Find available shared rides near passenger's location (default 15km radius)

**Query Parameters**:
- `startLat` (required): Passenger start latitude
- `startLng` (required): Passenger start longitude
- `endLat` (required): Passenger end latitude
- `endLng` (required): Passenger end longitude

**Example**:
```
GET /shared-ride/search?startLat=6.927079&startLng=80.771957&endLat=6.915160&endLng=80.875701
```

**Response**:
```json
[
  {
    "rideDetailId": 123,
    "driverProfileId": 1,
    "startCity": "Colombo",
    "endCity": "Kandy",
    "startLocationLatitude": 6.927000,
    "startLocationLongitude": 80.771000,
    "endLocationLatitude": 6.910000,
    "endLocationLongitude": 80.870000,
    "startTime": "2026-03-20T14:30:00",
    "currentPassengers": 2,
    "availableSeats": 3,
    "totalRideDistance": 120.5,
    "totalRideCost": 2500.00,
    "perKmRate": 20.83,
    "estimatedCostPerPassenger": 833.33,
    "driverRating": 4.8,
    "totalRidesAsDriver": 150
  }
]
```

---

### 3. Get Available Rides with Custom Radius
**Endpoint**: `GET /shared-ride/available`

**Description**: Find available shared rides with custom search radius

**Query Parameters**:
- `startLat` (required): Passenger start latitude
- `startLng` (required): Passenger start longitude
- `endLat` (required): Passenger end latitude
- `endLng` (required): Passenger end longitude
- `radius` (optional): Search radius in km (default: 15)

**Example**:
```
GET /shared-ride/available?startLat=6.927079&startLng=80.771957&endLat=6.915160&endLng=80.875701&radius=25
```

---

### 4. Get Ride Passengers
**Endpoint**: `GET /shared-ride/passengers/{rideDetailId}`

**Description**: Get all passengers currently in a shared ride

**Path Parameters**:
- `rideDetailId`: The ride detail ID

**Response**:
```json
[
  {
    "id": 1001,
    "rideDetailId": 123,
    "userId": 456,
    "userEmail": "passenger@example.com",
    "passengerName": "John Doe",
    "passengerStartLat": 6.927079,
    "passengerStartLng": 80.771957,
    "passengerEndLat": 6.915160,
    "passengerEndLng": 80.875701,
    "startCity": "Colombo",
    "endCity": "Kandy",
    "passengerRideDistance": 12.5,
    "passengerCost": 833.33,
    "status": "CONFIRMED",
    "createdDate": "2026-03-20T14:00:00",
    "modifiedDate": null
  }
]
```

---

### 5. Get Passenger Ride History
**Endpoint**: `GET /shared-ride/history/{userId}`

**Description**: Get completed shared rides for a passenger

**Path Parameters**:
- `userId`: The user ID

**Response**: Array of completed shared ride details

---

### 6. Get Shared Ride Details
**Endpoint**: `GET /shared-ride/details/{shareRideDetailId}`

**Description**: Get detailed information about a specific shared ride

**Path Parameters**:
- `shareRideDetailId`: The shared ride detail ID

---

### 7. Confirm Shared Ride
**Endpoint**: `PUT /shared-ride/{shareRideDetailId}/confirm`

**Description**: Confirm/accept a shared ride

**Response**:
```json
{
  "id": 789,
  "messages": "Shared ride confirmed successfully"
}
```

---

### 8. Cancel Shared Ride
**Endpoint**: `PUT /shared-ride/{shareRideDetailId}/cancel`

**Description**: Cancel a shared ride

**Response**:
```json
{
  "id": 789,
  "messages": "Shared ride cancelled successfully"
}
```

---

### 9. Calculate Pooled Cost
**Endpoint**: `GET /shared-ride/cost/{rideDetailId}`

**Description**: Calculate cost per passenger after pooling

**Response**:
```json
{
  "costPerPassenger": 833.33
}
```

---

### 10. Update Ride Status
**Endpoint**: `PUT /shared-ride/{shareRideDetailId}/status`

**Description**: Update shared ride status

**Request Body**:
```json
{
  "status": "COMPLETED"
}
```

**Valid Statuses**:
- `PENDING` - Awaiting confirmation
- `CONFIRMED` - Rider confirmed
- `COMPLETED` - Ride completed
- `CANCELLED` - Ride cancelled

---

### 11. Request Shared Ride with ML-Based Matching
**Endpoint**: `POST /shared-ride/request-with-matching`

**Description**: Request shared ride with ML model-based driver acceptance prediction

**Request Body**:
```json
{
  "rideDetailId": 123,
  "userId": 456,
  "passengerStartLat": 6.927079,
  "passengerStartLng": 80.771957,
  "passengerEndLat": 6.915160,
  "passengerEndLng": 80.875701,
  "passengerRideDistance": 12.5,
  "startCity": "Colombo",
  "endCity": "Kandy"
}
```

**Response**:
```json
{
  "id": 789,
  "messages": "Shared ride created successfully"
}
```

---

## Pooling Logic

### Cost Calculation

The shared ride pooling uses intelligent cost calculation:

1. **Distance-Based Split**: If per-km rate is available:
   ```
   Passenger Cost = Passenger Distance × Per-km Rate
   ```

2. **Equal Split**: If no per-km rate:
   ```
   Passenger Cost = Total Ride Cost ÷ Number of Passengers
   ```

### Geographic Filtering

Rides are filtered using:
- **Haversine Formula**: Calculates great-circle distance between coordinates
- **Radius Search**: Default 15km radius, customizable up to 50km
- **Route Overlap**: Ensures passenger route overlaps with ride route

### Passenger Matching

Passengers are matched with rides based on:
- Location proximity
- Destination alignment
- Available seats
- Ride status (ACTIVE rides only)

---

## ML Service Integration

### Configuration

Add these properties to `application.properties`:

```properties
# ML Service Configuration
ml.service.base-url=http://localhost:8000
ml.service.enabled=true
ml.service.timeout=5000
```

### ML Model Features

The ML model accepts the following driver features for acceptance rate prediction:

1. **route_deviation_pct**: Percentage deviation from optimal route (0-100%)
2. **zone_density**: Passenger density in the pickup zone (0-100)
3. **trip_distance_km**: Estimated trip distance in kilometers
4. **heading_angle_deg**: Vehicle heading angle in degrees (0-360)

### ML Service Endpoints

#### Health Check
```
GET /health
Response:
{
  "status": "ok",
  "model_loaded": true,
  "model_version": "gradient_boosting_v2"
}
```

#### Prediction
```
POST /predict
Request:
{
  "passenger_id": "P123",
  "drivers": [
    {
      "driver_id": "D456",
      "route_deviation_pct": 5.5,
      "zone_density": 75,
      "trip_distance_km": 12.5,
      "heading_angle_deg": 45.0
    }
  ]
}

Response:
{
  "passenger_id": "P123",
  "ranked_drivers": [
    {
      "rank": 1,
      "driver_id": "D456",
      "predicted_acceptance_rate": 0.92,
      "route_deviation_pct": 5.5,
      "zone_density": 75,
      "trip_distance_km": 12.5,
      "heading_angle_deg": 45.0
    }
  ],
  "top_driver_id": "D456",
  "model_version": "gradient_boosting_v2"
}
```

---

## Statuses

### Shared Ride Statuses

| Status | Description |
|--------|-------------|
| `PENDING` | Passenger joined, awaiting confirmation |
| `CONFIRMED` | Passenger confirmed, ride in progress |
| `COMPLETED` | Ride completed successfully |
| `CANCELLED` | Ride cancelled by passenger or driver |

### Ride Detail Statuses

| Status | Description |
|--------|-------------|
| `ACTIVE` | Ride available for new passengers |
| `FULL` | All seats taken |
| `IN_PROGRESS` | Ride started |
| `COMPLETED` | Ride completed |
| `CANCELLED` | Ride cancelled |

---

## Database Schema

### ShareRideDetail Table

```sql
CREATE TABLE shared_ride_detail (
  id BIGINT PRIMARY KEY,
  ride_detail_id BIGINT NOT NULL UNIQUE,
  request_id BIGINT,
  user_id BIGINT NOT NULL,
  passenger_start_location_longitude DECIMAL(10,7),
  passenger_start_location_latitude DECIMAL(10,7),
  passenger_end_location_longitude DECIMAL(10,7),
  passenger_end_location_latitude DECIMAL(10,7),
  start_city VARCHAR(200),
  end_city VARCHAR(200),
  passenger_cost DECIMAL(25,2),
  passenger_ride_distance DECIMAL(25,2),
  status VARCHAR(20),
  created_date TIMESTAMP,
  created_user VARCHAR(100),
  modified_date TIMESTAMP,
  modified_user VARCHAR(100),
  sync_ts TIMESTAMP,
  version BIGINT,
  FOREIGN KEY (ride_detail_id) REFERENCES driver_ride_detail(id),
  FOREIGN KEY (user_id) REFERENCES user(id)
);
```

---

## Error Handling

### Common Error Codes

| Error | Status Code | Description |
|-------|------------|-------------|
| `RIDE_NOT_FOUND` | 404 | Ride detail not found |
| `USER_NOT_FOUND` | 404 | User not found |
| `SHARED_RIDE_NOT_FOUND` | 404 | Shared ride detail not found |
| `RIDE_NOT_ACTIVE` | 409 | Ride is not in active status |
| `NO_AVAILABLE_SEATS` | 409 | No seats available in the ride |
| `Invalid value` | 400 | Required field missing or invalid |
| `Cannot be blank` | 400 | Required field is blank |

### Error Response Format

```json
{
  "errorMessage": "No available seats for this ride",
  "errorCode": "NO_AVAILABLE_SEATS"
}
```

---

## Example Use Cases

### Use Case 1: Search and Join a Shared Ride

```bash
# Step 1: Search for nearby rides
curl -X GET "http://localhost:8080/ride-mate/shared-ride/search?startLat=6.927079&startLng=80.771957&endLat=6.915160&endLng=80.875701"

# Step 2: Select a ride and join
curl -X POST "http://localhost:8080/ride-mate/shared-ride/join" \
  -H "Content-Type: application/json" \
  -d '{
    "rideDetailId": 123,
    "userId": 456,
    "passengerStartLat": 6.927079,
    "passengerStartLng": 80.771957,
    "passengerEndLat": 6.915160,
    "passengerEndLng": 80.875701,
    "passengerRideDistance": 12.5,
    "startCity": "Colombo",
    "endCity": "Kandy"
  }'

# Step 3: View passengers in the ride
curl -X GET "http://localhost:8080/ride-mate/shared-ride/passengers/123"

# Step 4: Calculate pooled cost
curl -X GET "http://localhost:8080/ride-mate/shared-ride/cost/123"

# Step 5: Confirm the ride
curl -X PUT "http://localhost:8080/ride-mate/shared-ride/789/confirm"
```

### Use Case 2: View Ride History

```bash
curl -X GET "http://localhost:8080/ride-mate/shared-ride/history/456"
```

### Use Case 3: ML-Based Driver Matching via Backend

```bash
curl -X POST "http://localhost:8080/ride-mate/shared-ride/request-with-matching" \
  -H "Content-Type: application/json" \
  -d '{
    "rideDetailId": 123,
    "userId": 456,
    "passengerStartLat": 6.927079,
    "passengerStartLng": 80.771957,
    "passengerEndLat": 6.915160,
    "passengerEndLng": 80.875701,
    "passengerRideDistance": 12.5,
    "startCity": "Colombo",
    "endCity": "Kandy"
  }'
```

### Use Case 4: Direct ML Model API Calls

**Check ML Service Health:**
```bash
curl -X GET "http://localhost:8000/health"
```

**Response:**
```json
{
  "status": "ok",
  "model_loaded": true,
  "model_version": "gradient_boosting_v2"
}
```

**Get Driver Acceptance Predictions:**
```bash
curl -X POST "http://localhost:8000/predict" \
  -H "Content-Type: application/json" \
  -d '{
    "passenger_id": "P123",
    "drivers": [
      {
        "driver_id": "D456",
        "route_deviation_pct": 5.5,
        "zone_density": 75,
        "trip_distance_km": 12.5,
        "heading_angle_deg": 45.0
      },
      {
        "driver_id": "D789",
        "route_deviation_pct": 12.3,
        "zone_density": 65,
        "trip_distance_km": 11.8,
        "heading_angle_deg": 52.5
      },
      {
        "driver_id": "D321",
        "route_deviation_pct": 8.7,
        "zone_density": 82,
        "trip_distance_km": 13.2,
        "heading_angle_deg": 38.0
      }
    ]
  }'
```

**Response:**
```json
{
  "passenger_id": "P123",
  "ranked_drivers": [
    {
      "rank": 1,
      "driver_id": "D456",
      "predicted_acceptance_rate": 0.92,
      "route_deviation_pct": 5.5,
      "zone_density": 75,
      "trip_distance_km": 12.5,
      "heading_angle_deg": 45.0
    },
    {
      "rank": 2,
      "driver_id": "D321",
      "predicted_acceptance_rate": 0.87,
      "route_deviation_pct": 8.7,
      "zone_density": 82,
      "trip_distance_km": 13.2,
      "heading_angle_deg": 38.0
    },
    {
      "rank": 3,
      "driver_id": "D789",
      "predicted_acceptance_rate": 0.78,
      "route_deviation_pct": 12.3,
      "zone_density": 65,
      "trip_distance_km": 11.8,
      "heading_angle_deg": 52.5
    }
  ],
  "top_driver_id": "D456",
  "model_version": "gradient_boosting_v2"
}
```

**ML Feature Explanations:**

| Feature | Range | Description | Example |
|---------|-------|-------------|---------|
| `route_deviation_pct` | 0-100 | How much the route deviates from optimal (lower is better) | 5.5 = 5.5% deviation |
| `zone_density` | 0-100 | Passenger density in pickup zone (higher = busier area) | 75 = 75% density |
| `trip_distance_km` | 0-500 | Estimated trip distance in kilometers | 12.5 km trip |
| `heading_angle_deg` | 0-360 | Vehicle's current heading in degrees | 45° = Northeast |

**Complete ML Integration Flow:**

```
1. Passenger requests shared ride
   ↓
2. Backend gets list of available drivers
   ↓
3. Backend sends driver features to ML model:
   POST /predict with drivers array
   ↓
4. ML returns ranked drivers by acceptance probability
   ↓
5. Backend matches passenger with top-ranked driver
   ↓
6. Shared ride created with highest-probability driver
```

---

## Performance Optimization

### Database Indexes

Recommended indexes for efficient pooling:

```sql
CREATE INDEX idx_shared_ride_ride_detail_status 
ON shared_ride_detail(ride_detail_id, status);

CREATE INDEX idx_shared_ride_user_status 
ON shared_ride_detail(user_id, status);

CREATE INDEX idx_shared_ride_coordinates 
ON shared_ride_detail(
  passenger_start_location_latitude,
  passenger_start_location_longitude,
  passenger_end_location_latitude,
  passenger_end_location_longitude
);

CREATE INDEX idx_shared_ride_created_date 
ON shared_ride_detail(created_date DESC);
```

### Caching Recommendations

- Cache available ride pools for 30 seconds
- Cache driver availability for 1 minute
- Cache ML model health status for 5 minutes

---

## Configuration

### Application Properties

```properties
# Shared Ride Pool Configuration
ride.pooling.max-search-radius=50
ride.pooling.default-search-radius=15
ride.pooling.seat-availability-threshold=1
ride.pooling.cost-calculation-method=distance_based

# ML Service Configuration
ml.service.base-url=http://localhost:8000
ml.service.enabled=true
ml.service.timeout=5000
ml.service.retry-attempts=2
```

---

## Security Considerations

1. **Authentication**: All endpoints require valid JWT token
2. **Authorization**: Users can only access their own ride history
3. **Data Validation**: All inputs are validated against SQL injection and XSS
4. **Rate Limiting**: Implement rate limiting on search endpoint (100 req/min per user)
5. **Data Encryption**: Sensitive coordinates stored encrypted in database

---

## Future Enhancements

1. **Real-time Updates**: WebSocket support for real-time ride availability
2. **Advanced Matching**: ML-based passenger-driver compatibility matching
3. **Dynamic Pricing**: Surge pricing based on demand and time
4. **Group Rides**: Support for pre-planned group rides
5. **Recurring Rides**: Support for recurring shared rides (daily commutes)
6. **Environmental Impact**: Track CO2 savings from ride pooling

---

## Support & Troubleshooting

### Common Issues

**Issue**: ML service unavailable
- Solution: Check ML service is running on configured URL
- Fallback: System will continue without ML predictions

**Issue**: No available rides found
- Solution: Increase search radius or adjust time filters
- Check: Ensure end city matches driver's destination

**Issue**: Seat capacity exceeded
- Solution: Driver needs to create new ride or passengers need to cancel
- Prevention: Real-time seat availability updates

---

## Author Information

- **Created by**: Iruni
- **Date**: March 20, 2026
- **Version**: 1.0.0

---

**Last Updated**: March 20, 2026
