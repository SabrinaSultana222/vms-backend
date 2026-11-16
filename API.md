# VMS Vehicle REST API Documentation

Base URL: `/api/vehicles`

---

## Overview

The Vehicle Management System (VMS) provides REST endpoints for managing vehicle inventory. All requests and responses use JSON (`application/json`).

### Vehicle Status Enum
Vehicle status is managed using the `VehicleStatus` enum with exactly three permitted values:
- `ACTIVE`
- `INACTIVE`
- `MAINTENANCE`

Status values are stored as strings (`EnumType.STRING`) in the database. Any invalid status value in request bodies or query parameters will result in a `400 Bad Request` containing the standard `ErrorResponse`.

### Standard Error Response Format
All error responses adhere to the following schema:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "vehicleNumber: Vehicle number is required",
  "path": "/api/vehicles",
  "timestamp": "2026-11-11T12:00:00Z"
}
```

---

## Endpoints

### 1. Create Vehicle
Creates a new vehicle record in the system.

- **Method**: `POST`
- **Path**: `/api/vehicles`
- **Request Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <jwt-token>`
- **Request Body**:
  ```json
  {
    "vehicleNumber": "DHK-11-2233",
    "company": "Acme Corp",
    "type": "Truck",
    "brand": "Volvo",
    "model": "FH16",
    "status": "ACTIVE"
  }
  ```
  *Validation Rules*:
  - `vehicleNumber`: Required (non-blank, max 50 chars, unique)
  - `company`: Required (non-blank, max 100 chars)
  - `type`: Required (non-blank, max 50 chars)
  - `brand`: Required (non-blank, max 100 chars)
  - `model`: Required (non-blank, max 100 chars)
  - `status`: Required (`VehicleStatus` enum: `ACTIVE`, `INACTIVE`, `MAINTENANCE`)

- **Response**: `201 Created`
  ```json
  {
    "id": 1,
    "vehicleNumber": "DHK-11-2233",
    "company": "Acme Corp",
    "type": "Truck",
    "brand": "Volvo",
    "model": "FH16",
    "status": "ACTIVE"
  }
  ```
- **Common Errors**:
  - `400 Bad Request`: Validation failure (missing or blank fields, invalid `status` enum value, exceeded field lengths)
  - `401 Unauthorized`: Missing or invalid JWT authentication token
  - `409 Conflict`: Vehicle number already exists in the system

---

### 2. List All Vehicles (Paged & Filtered)
Retrieves a paginated and filterable list of vehicle records.

- **Method**: `GET`
- **Path**: `/api/vehicles`
- **Request Headers**:
  - `Authorization: Bearer <jwt-token>`
- **Query Parameters**:
  | Parameter | Type | Required | Default | Description |
  |---|---|---|---|---|
  | `q` | String | No | - | Case-insensitive partial search on `vehicleNumber`, `company`, `brand`, and `model` (with `vehicleNumber` as the primary match). |
  | `status` | String (enum) | No | - | Filter by vehicle status (`ACTIVE`, `INACTIVE`, `MAINTENANCE`). Invalid status returns `400 Bad Request`. |
  | `type` | String | No | - | Exact type match (case-insensitive). |
  | `sortBy` | String | No | `id` | Whitelisted field to sort by: `vehicleNumber`, `company`, `brand`, `model`, `status`, `id`. Any other value returns `400 Bad Request`. |
  | `sortDir` | String | No | `asc` | Sort direction: `asc` or `desc` (case-insensitive). |
  | `page` | Integer | No | `0` | Zero-based page index. Must be `>= 0`. |
  | `size` | Integer | No | `10` | Page size. Must be between `1` and `100`. |

- **Response**: `200 OK`
  ```json
  {
    "content": [
      {
        "id": 1,
        "vehicleNumber": "DHK-11-2233",
        "company": "Acme Corp",
        "type": "Truck",
        "brand": "Volvo",
        "model": "FH16",
        "status": "ACTIVE"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
  ```
  *(Returns `"content": []`, `"totalElements": 0`, `"totalPages": 0` when no matching vehicles exist.)*

- **Common Errors**:
  - `400 Bad Request`: Invalid parameter value (e.g., non-whitelisted `sortBy`, invalid `status`, invalid `sortDir`, negative `page`, or `size` exceeding 100)
  - `401 Unauthorized`: Missing or invalid JWT authentication token
  - `500 Internal Server Error`: Unexpected server or database failure

---

### 3. Get Vehicle by ID
Retrieves details of a single vehicle by its primary key ID.

- **Method**: `GET`
- **Path**: `/api/vehicles/{id}`
- **Request Headers**:
  - `Authorization: Bearer <jwt-token>`
- **Path Parameters**:
  - `id` (Long, required): The unique identifier of the vehicle
- **Request Body**: None
- **Response**: `200 OK`
  ```json
  {
    "id": 1,
    "vehicleNumber": "DHK-11-2233",
    "company": "Acme Corp",
    "type": "Truck",
    "brand": "Volvo",
    "model": "FH16",
    "status": "ACTIVE"
  }
  ```
- **Common Errors**:
  - `401 Unauthorized`: Missing or invalid JWT authentication token
  - `404 Not Found`: No vehicle exists with the specified ID

---

### 4. Update Vehicle
Updates an existing vehicle record by its ID.

- **Method**: `PUT`
- **Path**: `/api/vehicles/{id}`
- **Request Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <jwt-token>`
- **Path Parameters**:
  - `id` (Long, required): The unique identifier of the vehicle to update
- **Request Body**:
  ```json
  {
    "vehicleNumber": "DHK-11-2233",
    "company": "Acme Logistics",
    "type": "Truck",
    "brand": "Volvo",
    "model": "FH16 2026",
    "status": "MAINTENANCE"
  }
  ```
  *Validation Rules*:
  - `vehicleNumber`: Required (non-blank, max 50 chars, unique)
  - `company`: Required (non-blank, max 100 chars)
  - `type`: Required (non-blank, max 50 chars)
  - `brand`: Required (non-blank, max 100 chars)
  - `model`: Required (non-blank, max 100 chars)
  - `status`: Required (`VehicleStatus` enum: `ACTIVE`, `INACTIVE`, `MAINTENANCE`)

- **Response**: `200 OK`
  ```json
  {
    "id": 1,
    "vehicleNumber": "DHK-11-2233",
    "company": "Acme Logistics",
    "type": "Truck",
    "brand": "Volvo",
    "model": "FH16 2026",
    "status": "MAINTENANCE"
  }
  ```
- **Common Errors**:
  - `400 Bad Request`: Validation failure in payload fields or invalid `status` enum value
  - `401 Unauthorized`: Missing or invalid JWT authentication token
  - `404 Not Found`: Vehicle with specified ID does not exist
  - `409 Conflict`: Specified `vehicleNumber` is already assigned to another vehicle

---

### 5. Delete Vehicle
Deletes a vehicle record from the system by its ID.

- **Method**: `DELETE`
- **Path**: `/api/vehicles/{id}`
- **Request Headers**:
  - `Authorization: Bearer <jwt-token>`
- **Path Parameters**:
  - `id` (Long, required): The unique identifier of the vehicle to delete
- **Request Body**: None
- **Response**: `204 No Content` (Empty response body)
- **Common Errors**:
  - `401 Unauthorized`: Missing or invalid JWT authentication token
  - `404 Not Found`: Vehicle with specified ID does not exist
