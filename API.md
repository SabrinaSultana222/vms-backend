# VMS Vehicle REST API Documentation

Base URL: `/api/vehicles`

---

## Overview

The Vehicle Management System (VMS) provides REST endpoints for managing vehicle inventory. All requests and responses use JSON (`application/json`).

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
- **Request Headers**: `Content-Type: application/json`
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
  - `status`: Required (non-blank, max 50 chars)

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
  - `400 Bad Request`: Validation failure (missing or blank fields, exceeded field lengths)
  - `409 Conflict`: Vehicle number already exists in the system

---

### 2. List All Vehicles
Retrieves a list of all vehicle records.

- **Method**: `GET`
- **Path**: `/api/vehicles`
- **Request Body**: None
- **Response**: `200 OK`
  ```json
  [
    {
      "id": 1,
      "vehicleNumber": "DHK-11-2233",
      "company": "Acme Corp",
      "type": "Truck",
      "brand": "Volvo",
      "model": "FH16",
      "status": "ACTIVE"
    }
  ]
  ```
  *(Returns an empty array `[]` if no vehicles exist.)*
- **Common Errors**:
  - `500 Internal Server Error`: Unexpected server or database failure

---

### 3. Get Vehicle by ID
Retrieves details of a single vehicle by its primary key ID.

- **Method**: `GET`
- **Path**: `/api/vehicles/{id}`
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
  - `404 Not Found`: No vehicle exists with the specified ID

---

### 4. Update Vehicle
Updates an existing vehicle record by its ID.

- **Method**: `PUT`
- **Path**: `/api/vehicles/{id}`
- **Path Parameters**:
  - `id` (Long, required): The unique identifier of the vehicle to update
- **Request Headers**: `Content-Type: application/json`
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
  - `400 Bad Request`: Validation failure in payload fields
  - `404 Not Found`: Vehicle with specified ID does not exist
  - `409 Conflict`: Specified `vehicleNumber` is already assigned to another vehicle

---

### 5. Delete Vehicle
Deletes a vehicle record from the system by its ID.

- **Method**: `DELETE`
- **Path**: `/api/vehicles/{id}`
- **Path Parameters**:
  - `id` (Long, required): The unique identifier of the vehicle to delete
- **Request Body**: None
- **Response**: `204 No Content` (Empty response body)
- **Common Errors**:
  - `404 Not Found`: Vehicle with specified ID does not exist
