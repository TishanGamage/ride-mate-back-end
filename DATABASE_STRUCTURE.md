# Database Structure - RideMate Backend

## Overview
This document provides a complete overview of all database tables based on the Liquibase YAML migration files.

---

## Tables Summary

| # | Table Name | Purpose | Type |
|---|------------|---------|------|
| 1 | user | Core authentication and account management | Core |
| 2 | user_profile | Extended user profile information | Core |
| 3 | emergency_contacts | User emergency contact information | Core |
| 4 | identification_type | Lookup table for ID types | Reference |
| 5 | user_identification_details | User identification documents | Core |
| 6 | vehicle_type | Lookup table for vehicle types with pricing | Reference |
| 7 | vehicle_make | Lookup table for vehicle manufacturers | Reference |
| 8 | driver_profile | Driver-specific information | Core |
| 9 | driver_vehicle_details | Driver vehicle information | Core |
| 10 | document_details | User uploaded documents with URLs | Core |
| 11 | verification_codes | Email verification codes | Transient |
| 12 | common_seq | Sequence generator | System |

---

## Table Details

### 1. **user**
**Purpose**: Core authentication and account management for all users

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key |
| email | VARCHAR(255) | NO | UNIQUE | User email address |
| phone_number | VARCHAR(20) | NO | UNIQUE | User phone number |
| password_hash | VARCHAR(255) | NO | | Encrypted password |
| role | VARCHAR(20) | NO | | User role (PASSENGER/DRIVER/ADMIN) |
| status | VARCHAR(20) | NO | Default: "ACTIVE" | Account status (ACTIVE/SUSPENDED) |
| email_verified | VARCHAR(5) | NO | Default: "NO" | Email verification status (YES/NO) |
| last_login_date | TIMESTAMP | YES | | Last login timestamp |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |

**Note**: Table name is `user` (singular), not `users`

---

### 2. **user_profile**
**Purpose**: Extended profile information for all users

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key |
| user_id | BIGINT | NO | FK → user(id), UNIQUE, ON DELETE CASCADE | Reference to user |
| first_name | VARCHAR(100) | YES | | User's first name |
| last_name | VARCHAR(100) | YES | | User's last name |
| profile_image_url | VARCHAR(500) | YES | | Profile picture URL |
| nic_passport | VARCHAR(100) | YES | | National ID or Passport number |
| date_of_birth | DATE | YES | | Date of birth |
| gender | VARCHAR(10) | YES | | Gender |
| bio | TEXT | YES | | User biography/description |
| address_line_1 | VARCHAR(255) | YES | | Address line 1 |
| address_line_2 | VARCHAR(255) | YES | | Address line 2 |
| address_line_3 | VARCHAR(255) | YES | | Address line 3 |
| address_line_4 | VARCHAR(255) | YES | | Address line 4 |
| city | VARCHAR(100) | YES | | City |
| state | VARCHAR(100) | YES | | State/Province |
| postal_code | VARCHAR(20) | YES | | Postal/ZIP code |
| country | VARCHAR(100) | YES | | Country |
| preferred_language | VARCHAR(10) | NO | Default: "EN" | Preferred language code |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |

**Relationships:**
- One-to-One with `user` (required)
- One-to-Many with `emergency_contacts`

**Note**: Table name is `user_profile` (singular), not `user_profiles`

---

### 3. **emergency_contacts**
**Purpose**: Stores emergency contact information for users (supports multiple contacts)

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key |
| user_id | BIGINT | NO | FK → user(id), ON DELETE CASCADE | Reference to user |
| contact_name | VARCHAR(200) | NO | | Emergency contact name |
| contact_phone | VARCHAR(20) | NO | | Emergency contact phone number |
| relationship | VARCHAR(50) | YES | | Relationship to user |
| is_default | VARCHAR(3) | NO | Default: "NO" | Default contact flag (YES/NO) |
| email | VARCHAR(255) | YES | | Emergency contact email |
| address | VARCHAR(500) | YES | | Emergency contact address |
| notes | TEXT | YES | | Additional notes |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |

**Indexes:**
- `idx_emergency_contacts_user_id` on `user_id`
- `idx_emergency_contacts_is_default` on `(user_id, is_default)`

**Relationships:**
- Many-to-One with `user`

**Note**: Each user can have multiple emergency contacts, with one marked as default

---

### 4. **identification_type**
**Purpose**: Lookup table for types of identification documents

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key |
| type_code | VARCHAR(20) | NO | UNIQUE | Identification type code |
| type_name | VARCHAR(100) | NO | | Identification type name |
| description | VARCHAR(255) | YES | | Type description |
| status | VARCHAR(10) | NO | | Active/Inactive status |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |

---

### 5. **user_identification_details**
**Purpose**: Stores user identification documents (National ID, Passport, etc.)

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key |
| user_id | BIGINT | NO | FK → user(id), ON DELETE CASCADE | Reference to user |
| identification_type_id | BIGINT | NO | FK → identification_type(id) | Type of identification |
| identification_number | VARCHAR(100) | NO | | ID document number |
| issue_date | DATE | YES | | Document issue date |
| expiry_date | DATE | YES | | Document expiry date |
| issuing_country | VARCHAR(100) | YES | | Issuing country |
| issuing_authority | VARCHAR(255) | YES | | Issuing authority |
| front_image_url | VARCHAR(500) | YES | | Front image URL |
| back_image_url | VARCHAR(500) | YES | | Back image URL |
| is_verified | VARCHAR(10) | NO | | Verification status |
| verified_date | TIMESTAMP | YES | | Verification date |
| verified_by | VARCHAR(100) | YES | | Who verified the document |
| verification_notes | TEXT | YES | | Verification notes |
| status | VARCHAR(20) | NO | | Document status |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |


---

### 6. **vehicle_type**
**Purpose**: Lookup table for vehicle types with pricing information

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key |
| type_code | VARCHAR(20) | NO | UNIQUE | Vehicle type code |
| type_name | VARCHAR(100) | NO | | Vehicle type name |
| description | VARCHAR(255) | YES | | Type description |
| base_fare | DECIMAL(10,2) | YES | | Base fare for this type |
| per_km_rate | DECIMAL(10,2) | YES | | Rate per kilometer |
| min_seats | INT | YES | | Minimum seats |
| max_seats | INT | YES | | Maximum seats |
| is_active | VARCHAR(3) | NO | Default: "YES" | Active status (YES/NO) |
| display_order | INT | NO | Default: 0 | Display order in UI |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |

**Indexes:**
- `idx_vehicle_type_code` on `type_code`

**Pre-populated Data:**
- CAR: Base 50.00, Per km 25.00, Seats 2-5
- VAN: Base 100.00, Per km 40.00, Seats 6-15
- BIKE: Base 30.00, Per km 15.00, Seats 1-2
- SUV: Base 75.00, Per km 35.00, Seats 4-7

---

### 7. **vehicle_make**
**Purpose**: Lookup table for vehicle manufacturers

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key |
| make_code | VARCHAR(50) | NO | UNIQUE | Make code |
| make_name | VARCHAR(100) | NO | | Make name |
| country_of_origin | VARCHAR(100) | YES | | Country of origin |
| is_active | VARCHAR(3) | NO | Default: "YES" | Active status (YES/NO) |
| display_order | INT | NO | Default: 0 | Display order in UI |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |

**Indexes:**
- `idx_vehicle_make_code` on `make_code`

**Pre-populated Data:**
- TOYOTA (Japan)
- MERCEDES_BENZ (Germany)
- BMW (Germany)
- HONDA (Japan)

---

### 8. **driver_profile**
**Purpose**: Driver-specific information for users who register as drivers

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key |
| user_profile_id | BIGINT | NO | FK → user(id), UNIQUE, ON DELETE CASCADE | Reference to user |
| driver_license_number | VARCHAR(50) | NO | UNIQUE | Driver's license number |
| driver_license_expiry | DATE | NO | | License expiration date |
| driver_license_verified | VARCHAR(3) | NO | | License verification status (YES/NO) |
| driver_license_front_url | VARCHAR(500) | YES | | Front image of license |
| driver_license_back_url | VARCHAR(500) | YES | | Back image of license |
| rating_as_driver | DECIMAL(3,2) | NO | Default: 0.0 | Driver rating (0.00-5.00) |
| total_rides_as_driver | BIGINT | NO | Default: 0 | Total completed rides |
| total_earnings | DECIMAL(10,2) | NO | Default: 0.0 | Total earnings |
| account_status | VARCHAR(20) | NO | | Driver account status |
| approved_date | TIMESTAMP | YES | | Date driver was approved |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |

**Relationships:**
- One-to-One with `user`
- One-to-Many with `driver_vehicle_details`

**Note**: Column named `user_profile_id` but references `user(id)` table

---

### 9. **driver_vehicle_details**
**Purpose**: Stores information about vehicles owned/operated by drivers

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key |
| driver_profile_id | BIGINT | NO | FK → driver_profile(id), ON DELETE CASCADE | Reference to driver |
| vehicle_type_id | BIGINT | NO | FK → vehicle_type(id) | Type of vehicle |
| vehicle_make_id | BIGINT | NO | FK → vehicle_make(id) | Vehicle manufacturer |
| registration_number | VARCHAR(50) | NO | UNIQUE | Vehicle registration number |
| model | VARCHAR(100) | NO | | Vehicle model |
| year | INT | NO | | Manufacturing year |
| color | VARCHAR(50) | NO | | Vehicle color |
| seats | INT | NO | | Number of seats |
| vehicle_image_url | VARCHAR(500) | YES | | Vehicle image URL |
| registration_certificate_url | VARCHAR(500) | YES | | Registration certificate URL |
| insurance_number | VARCHAR(100) | YES | | Insurance policy number |
| insurance_provider | VARCHAR(100) | YES | | Insurance company name |
| insurance_expiry | DATE | YES | | Insurance expiry date |
| insurance_document_url | VARCHAR(500) | YES | | Insurance document URL |
| is_verified | VARCHAR(3) | NO | Default: "NO" | Verification status (YES/NO) |
| verified_date | TIMESTAMP | YES | | Verification date |
| verified_by | VARCHAR(100) | YES | | Who verified the vehicle |
| is_primary | VARCHAR(3) | NO | Default: "NO" | Primary vehicle flag (YES/NO) |
| is_active | VARCHAR(3) | NO | Default: "YES" | Active status (YES/NO) |
| status | VARCHAR(20) | NO | Default: "PENDING_VERIFICATION" | Vehicle status |
| rejection_reason | TEXT | YES | | Reason for rejection |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |

**Indexes:**
- `idx_driver_vehicle_driver_id` on `driver_profile_id`
- `idx_driver_vehicle_type_id` on `vehicle_type_id`
- `idx_driver_vehicle_make_id` on `vehicle_make_id`
- `idx_driver_vehicle_registration` on `registration_number`
- `idx_driver_vehicle_status` on `status`

---

### 10. **document_details**
**Purpose**: Standalone document storage table for uploaded files. Other tables reference documents by storing the document_details.id (e.g., user_profile.nic_front_image_document_id)

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key (used as document reference) |
| document_name | VARCHAR(255) | NO | | Document name |
| document_url | VARCHAR(500) | NO | | Document storage URL |
| file_size | BIGINT | YES | | File size in bytes |
| file_type | VARCHAR(50) | YES | | File MIME type (e.g., image/png) |
| upload_date | TIMESTAMP | NO | | Document upload timestamp |
| status | VARCHAR(20) | NO | Default: "ACTIVE" | Document status (ACTIVE/DELETED) |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |

**Indexes:**
- `idx_document_details_status` on `status`

**Usage Pattern:**
- Other tables store the `document_details.id` as a foreign key reference
- Example: `user_profile.nic_front_image_document_id` → `document_details.id`
- This allows multiple tables to reference documents without duplicating storage

**Note:** This table does NOT have a user_id field. The relationship is established by other tables storing the document ID.

---

### 11. **verification_codes**
**Purpose**: Stores email verification codes for user registration/authentication

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| id | BIGINT | NO | PK, Auto Increment | Primary key |
| email | VARCHAR(255) | NO | UNIQUE | Email address |
| code | VARCHAR(6) | NO | | Verification code (6 characters) |
| expiry_time | TIMESTAMP | NO | | Code expiration time |
| verified | VARCHAR(6) | NO | | Verification status |
| attempt_count | BIGINT | NO | Default: 0 | Number of verification attempts |
| created_date | TIMESTAMP | NO | | Record creation timestamp |
| created_user | VARCHAR(100) | NO | | User who created the record |
| modified_date | TIMESTAMP | YES | | Last modification timestamp |
| modified_user | VARCHAR(100) | YES | | User who modified the record |
| sync_ts | TIMESTAMP | NO | | Synchronization timestamp |
| version | BIGINT | NO | Default: 0 | Optimistic locking version |

**Indexes:**
- `idx_verification_codes_email` on `email`
- `idx_verification_codes_expiry_time` on `expiry_time`

---

### 12. **common_seq**
**Purpose**: Sequence generator table for generating unique IDs

| Column | Type | Nullable | Constraints | Description |
|--------|------|----------|-------------|-------------|
| next_val | BIGINT | - | | Next sequence value |

**Initial Value:** 1

---

## Entity Relationships

### Relationship Diagram
```
user (1) ──── (1) user_profile
  │
  ├──── (0..*) emergency_contacts
  │
  ├──── (0..1) driver_profile
  │              │
  │              └──── (0..*) driver_vehicle_details
  │                              │              │
  │                              │              │
  │                     vehicle_type (*)    vehicle_make (*)
  │
  └──── (0..*) user_identification_details ──── (*) identification_type
```

### Key Relationships

1. **user → user_profile**: One-to-One (Required)
   - Every user must have a profile
   - Foreign Key: `user_profile.user_id` → `user.id`
   - Cascade delete enabled

2. **user → emergency_contacts**: One-to-Many
   - A user can have multiple emergency contacts
   - Foreign Key: `emergency_contacts.user_id` → `user.id`
   - Cascade delete enabled
   - One contact can be marked as default

3. **user → driver_profile**: One-to-One (Optional)
   - A user can optionally become a driver
   - Foreign Key: `driver_profile.user_profile_id` → `user.id`
   - Cascade delete enabled

4. **user → user_identification_details**: One-to-Many
   - A user can have multiple identification documents
   - Foreign Key: `user_identification_details.user_id` → `user.id`
   - Cascade delete enabled

5. **driver_profile → driver_vehicle_details**: One-to-Many
   - A driver can have multiple vehicles
   - Foreign Key: `driver_vehicle_details.driver_profile_id` → `driver_profile.id`
   - Cascade delete enabled

6. **identification_type → user_identification_details**: One-to-Many
   - Reference data for ID types

7. **vehicle_type → driver_vehicle_details**: One-to-Many
   - Reference data for vehicle categories

8. **vehicle_make → driver_vehicle_details**: One-to-Many
   - Reference data for vehicle manufacturers

---

## Common Patterns

### Audit Fields
All tables include these standard audit fields:
- `created_date` / `created_user`: When and who created the record
- `modified_date` / `modified_user`: When and who last modified the record
- `sync_ts`: Synchronization timestamp for data sync operations
- `version`: Optimistic locking version number

### Data Types Convention
- **IDs**: BIGINT with auto-increment
- **Status/Flag Fields**: VARCHAR with YES/NO or status codes
- **Timestamps**: TIMESTAMP
- **Monetary Values**: DECIMAL(10,2)
- **Ratings**: DECIMAL(3,2)
- **URLs**: VARCHAR(500)
- **Long Text**: TEXT

---

## Status Values & Enumerations

### user.role
- `PASSENGER` - Regular passenger user
- `DRIVER` - Driver user
- `ADMIN` - Administrator user

### user.status
- `ACTIVE` - Account is active
- `SUSPENDED` - Account is suspended

### YES/NO Fields
- `email_verified`
- `driver_license_verified`
- `is_verified`
- `is_active`
- `is_primary`

### driver_vehicle_details.status
- `PENDING_VERIFICATION` - Waiting for verification
- `VERIFIED` - Vehicle verified
- `REJECTED` - Vehicle rejected
- `ACTIVE` - Vehicle active
- `INACTIVE` - Vehicle inactive

---

## Important Notes

⚠️ **Table Naming Inconsistencies:**
1. The main user table is named `user` (singular), not `users`
2. The profile table is named `user_profile` (singular), not `user_profiles`
3. Some foreign keys have misleading column names:
   - `driver_profile.user_profile_id` actually references `user(id)` (not user_profile)

⚠️ **Foreign Key References:**
- `user_profile.user_id` → `user(id)` ✓ Correct
- `emergency_contacts.user_id` → `user(id)` ✓ Correct
- `user_identification_details.user_id` → `user(id)` ✓ Correct
- `driver_profile.user_profile_id` → `user(id)` ⚠️ Misleading name (should be user_id)

---

## Migration Order

Based on `db/changelog.yml`, tables are created in this order:

1. `common_seq` - Sequence table
2. `identification_type` - Reference data
3. `vehicle_type` - Reference data with sample data
4. `vehicle_make` - Reference data with sample data
5. `user` - Core authentication table
6. `user_profile` - User profile data
7. `emergency_contacts` - User emergency contacts
8. `user_identification_details` - User ID documents
9. `document_details` - Uploaded documents
10. `verification_codes` - Email verification
11. `driver_profile` - Driver information
12. `driver_vehicle_details` - Driver vehicles

---

**Document Last Updated:** March 2, 2026  
**Database Schema Version:** 1.1  
**Total Tables:** 12  
**Reference Tables:** 3 (identification_type, vehicle_type, vehicle_make)  
**Core Tables:** 8 (user, user_profile, emergency_contacts, user_identification_details, document_details, driver_profile, driver_vehicle_details, verification_codes)  
**System Tables:** 1 (common_seq)

