# YashTools ERP — Flow-Wise Integration Scenarios & Payloads Guide

This guide documents the complete business flows within **YashTools ERP**, detailing the HTTP method, endpoint URLs, headers, and JSON request bodies for every API involved in each scenario.

---

## Scenario 1: Quotation Negotiation & Admin Discount Approval Flow
* **Modules Covered:** `Customers`, `Quotations`, `Audit Logs`
* **Workflow:**
  1. Create customer.
  2. Create quotation and send to customer (Status: `SENT`).
  3. Customer requests discount -> Salesman applies 5% discount (Status: `REVISED`).
  4. Customer rejects again -> Salesman applies 15% discount.
  5. System triggers mandatory Admin Approval (due to discount exceeding 10% threshold).
  6. Admin approves -> Quotation becomes `APPROVED`.
  7. Alternatively, if customer rejects final terms, the quotation is marked as `CLOSED`/`CANCELLED`.

### Step 1.1: Onboard Customer (YT)
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/customers`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "companyName": "Apex Machining Co",
    "contactPerson": "Anish Mehta",
    "email": "anish@apexmachining.com",
    "mobile": "9812345678",
    "gstin": "27AAACA9988A1Z1",
    "billingAddress": "A-5, MIDC Bhosari, Pune",
    "deliveryAddress": "A-5, MIDC Bhosari, Pune",
    "paymentTermsDays": 30
}
```

### Step 1.2: Create Initial Quotation
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/quotations`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "customerId": "{{customerId}}",
    "remarks": "Standard quotation - first proposal",
    "items": [
        {
            "orderType": "NEW_TOOL",
            "toolName": "12mm Carbide Rougher",
            "itemName": "12mm Rougher Tool",
            "quantity": 10,
            "trial": false,
            "drawingReference": "DRW-12R-01",
            "materialType": "CARBIDE",
            "coatingRequired": true,
            "coatingType": "TiAlN",
            "diameter": 12.0,
            "fluteLength": 30.0,
            "shankDiameter": 12.0,
            "overallLength": 83.0,
            "specialGeometry": false,
            "specialProfile": false,
            "expressDelivery": false,
            "userMultiplier": 1.00,
            "technicalNotes": "Standard roughing profile"
        }
    ]
}
```

### Step 1.3: Update Status to SENT
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/quotations/{{quotationId}}/status`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "status": "SENT",
    "remarks": "Quotation proposal shared via email"
}
```

### Step 1.4: Apply 5% Discount (User Multiplier: 0.95)
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/quotations/{{quotationId}}`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "customerId": "{{customerId}}",
    "remarks": "Revised with 5% customer discount",
    "items": [
        {
            "id": "{{quotationItemId}}",
            "orderType": "NEW_TOOL",
            "toolName": "12mm Carbide Rougher",
            "itemName": "12mm Rougher Tool",
            "quantity": 10,
            "trial": false,
            "drawingReference": "DRW-12R-01",
            "materialType": "CARBIDE",
            "coatingRequired": true,
            "coatingType": "TiAlN",
            "diameter": 12.0,
            "fluteLength": 30.0,
            "shankDiameter": 12.0,
            "overallLength": 83.0,
            "specialGeometry": false,
            "specialProfile": false,
            "expressDelivery": false,
            "userMultiplier": 0.95,
            "technicalNotes": "Standard roughing profile"
        }
    ]
}
```

### Step 1.5: Resend Revised Quotation
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/quotations/{{quotationId}}/status`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "status": "REVISED",
    "remarks": "Shared revised pricing with 5% discount"
}
```

### Step 1.6: Apply 15% Discount (User Multiplier: 0.85 - Triggers Admin Approval)
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/quotations/{{quotationId}}`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "customerId": "{{customerId}}",
    "remarks": "Quotation discount set to 15% as final negotiation",
    "items": [
        {
            "id": "{{quotationItemId}}",
            "orderType": "NEW_TOOL",
            "toolName": "12mm Carbide Rougher",
            "itemName": "12mm Rougher Tool",
            "quantity": 10,
            "trial": false,
            "drawingReference": "DRW-12R-01",
            "materialType": "CARBIDE",
            "coatingRequired": true,
            "coatingType": "TiAlN",
            "diameter": 12.0,
            "fluteLength": 30.0,
            "shankDiameter": 12.0,
            "overallLength": 83.0,
            "specialGeometry": false,
            "specialProfile": false,
            "expressDelivery": false,
            "userMultiplier": 0.85,
            "technicalNotes": "Standard roughing profile"
        }
    ]
}
```

### Step 1.7: Lock Quotation (Required before approval)
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/quotations/{{quotationId}}/lock`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`

### Step 1.8: Option A - Admin Approves the Discount
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/quotations/{{quotationId}}/status`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}` (Must have `ADMIN` role)
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "status": "APPROVED",
    "remarks": "Admin overrides and approves the 15% volume discount request"
}
```

### Step 1.9: Option B - Close/Reject Quotation
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/quotations/{{quotationId}}/status`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "status": "CANCELLED",
    "remarks": "Quotation closed as customer rejected final discount pricing"
}
```

---

## Scenario 2: Standard Order-to-Cash (O2C) Flow with New Tool Trial
* **Modules Covered:** `Enquiries`, `Quotations`, `Work Orders`, `Finished Goods`, `Logistics`, `Sales Invoice`
* **Workflow:**
  1. Create enquiry with a trial item (quantity: 1, type: `NEW_TOOL`, `trial: true`).
  2. Convert enquiry to quotation.
  3. Lock and approve quotation.
  4. Convert to work order (with expected delivery date, no start/end dates yet).
  5. Planner schedules production on CNC Grinder machine.
  6. Operator processes tool.
  7. QA Inspects and Pack items.
  8. Deliver items via Delivery Challan.
  9. Customer gives feedback -> Mark trial as `SUCCESS`.
  10. Generate invoice.

### Step 2.1: Submit RFQ Enquiry with Trial Item
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/enquiries`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "customerId": "{{customerId}}",
    "remarks": "New custom drill design trial",
    "items": [
        {
            "orderType": "NEW_TOOL",
            "toolName": "10mm Custom Trial Drill",
            "itemName": "Trial drill profile",
            "quantity": 1,
            "trial": true,
            "drawingReference": "DRW-TR-990",
            "materialType": "CARBIDE",
            "coatingRequired": true,
            "coatingType": "TiAlN",
            "diameter": 10.0,
            "fluteLength": 40.0,
            "shankDiameter": 10.0,
            "overallLength": 95.0,
            "specialGeometry": true,
            "specialProfile": false,
            "expressDelivery": true,
            "technicalNotes": "Must pass drilling test to qualify for bulk order"
        }
    ]
}
```

### Step 2.2: Convert Enquiry to Quotation
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/quotations/convert-enquiry/{{enquiryId}}`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "remarks": "Quotation draft created from enquiry"
}
```

### Step 2.3: Lock & Approve Quotation
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/quotations/{{quotationId}}/status`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "status": "APPROVED",
    "remarks": "Proposal accepted"
}
```

### Step 2.4: Convert Quotation to Work Order (expectedDeliveryDate only)
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/work-orders`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "quotationId": "{{quotationId}}",
    "remarks": "Convert approved trial proposal",
    "expectedDeliveryDate": "2026-08-18",
    "poNumber": "PO-TRIAL-8877"
}
```

### Step 2.5: Plan Dates (Production Planner)
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/work-orders/{{workOrderId}}/planning`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "plannedStartDate": "2026-08-05",
    "plannedEndDate": "2026-08-12"
}
```

### Step 2.6: Generate Job Card
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/job-cards`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "workOrderId": "{{workOrderId}}",
    "workOrderItemId": "{{workOrderItemId}}",
    "totalQuantity": 1,
    "remarks": "High priority trial tool fabrication"
}
```

### Step 2.7: Assign Machine & Operator
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/production-schedules`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "jobCardId": "{{jobCardId}}",
    "machineId": "{{machineId}}",
    "operatorId": 1,
    "shift": "MORNING",
    "plannedStartDate": "2026-08-06",
    "plannedEndDate": "2026-08-08"
}
```

### Step 2.8: Start Execution
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/job-executions/start`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "jobCardId": "{{jobCardId}}",
    "machineId": "{{machineId}}",
    "operatorId": 1,
    "remarks": "Carbide rod setup verified"
}
```

### Step 2.9: Complete Execution
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/job-executions/stop`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "executionLogId": "{{executionLogId}}",
    "producedQty": 1,
    "rejectedQty": 0,
    "stopReason": "COMPLETED",
    "remarks": "Drill profile completed successfully"
}
```

### Step 2.10: Record Quality Inspection
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/quality-inspections`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
* **Note on Rejected/Failed Quantity:** If `rejectedQuantity` > 0 in this request, the system will:
  1. Auto-create a high-priority Reproduction/Replacement Job Card with prefix `-JC-RP-` for the rejected quantity.
  2. Set the parent Work Order status back to `IN_PROGRESS` to allow the reproduction run to execute.
```json
{
    "jobCardId": "{{jobCardId}}",
    "acceptedQuantity": 1,
    "rejectedQuantity": 0,
    "inspectionResult": "PASSED",
    "inspector": "QA-Lead-Vinod",
    "remarks": "Passed micro-accuracy check"
}
```

### Step 2.10.b: View E2E Work Order Progress Dashboard (Production Planner)
* **Method:** `GET`
* **URL:** `{{baseUrl}}/api/work-orders/{{workOrderId}}/progress`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`


### Step 2.11: Record Packing
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/packing-logs`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "workOrderId": "{{workOrderId}}",
    "packedQuantity": 1,
    "packedBy": "Packer-Omkar",
    "remarks": "Packed inside clear plastic tube"
}
```

### Step 2.12: Create Delivery Challan
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/delivery-challans`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "workOrderId": "{{workOrderId}}",
    "driverName": "Amit Shinde",
    "vehicleNumber": "MH-12-PQ-9988",
    "remarks": "Deliver for customer trial run",
    "items": [
        {
            "workOrderItemId": "{{workOrderItemId}}",
            "quantity": 1,
            "remarks": "Box 1"
        }
    ]
}
```

### Step 2.13: Dispatch Challan
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/delivery-challans/{{deliveryChallanId}}/dispatch`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`

### Step 2.14: Confirm Delivery
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/delivery-challans/{{deliveryChallanId}}/deliver`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "receivedBy": "Customer Store",
    "deliveryRemarks": "Box package received successfully"
}
```

### Step 2.15: Log Trial Result (Set to SUCCESS)
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/work-orders/items/{{workOrderItemId}}/trial-result`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "status": "SUCCESS",
    "feedback": "Drill profile successfully performed 5,000 cycles with high tolerance"
}
```

### Step 2.16: Generate Sales Invoice
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/sales-invoices`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "workOrderId": "{{workOrderId}}",
    "remarks": "Generate final tax invoice"
}
```

---

## Scenario 3: Logistics & Transit Damage Rejections Flow
* **Modules Covered:** `Logistics`, `Transit Damage`, `Job Cards`, `Work Orders`
* **Workflow:**
  1. Delivery Challan is dispatched.
  2. Tools are damaged in transit.
  3. Report transit damage.
  4. Approving damage reopens parent Work Order status to `IN_PROGRESS` and automatically spawns a priority replacement Job Card.

### Step 3.1: Report Damage
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/transit-damages`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "deliveryChallanId": "{{deliveryChallanId}}",
    "workOrderItemId": "{{workOrderItemId}}",
    "damagedQuantity": 2,
    "reportedBy": "Delivery Agent - Sunil",
    "description": "Courier carton dropped, chipping tip edges",
    "action": "REPLACE"
}
```

### Step 3.2: Approve Damage (Spawns replacement Job Card)
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/transit-damages/{{transitDamageReportId}}/approve`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "remarks": "Approved. Spawn priority replacement card immediately"
}
```

---

## Scenario 4: Procurement-to-Pay (P2P) Flow with Stock Audit Adjustments
* **Modules Covered:** `Vendors`, `Purchase Orders`, `Goods Receipts`, `Purchase Invoices`, `Vendor Payments`, `Stock Take`, `Stock Adjustments`
* **Workflow:**
  1. Create vendor.
  2. Create Purchase Order.
  3. Approve PO.
  4. Record GRN entry.
  5. Receive purchase invoice.
  6. Pay vendor.
  7. Run physical stock take -> Find quantity mismatch.
  8. Post stock adjustment to reconcile.

### Step 4.1: Onboard Supplier Vendor
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/vendors`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "code": "SUPP-CO-101",
    "name": "Carbide Raw Materials Inc",
    "contactPerson": "Ravi Shastri",
    "email": "ravi@carbideraws.com",
    "mobile": "9867543210",
    "gstin": "27AAACA9922A1Z3",
    "billingAddress": "Sector-4, MIDC Chakan, Pune",
    "deliveryAddress": "Sector-4, MIDC Chakan, Pune",
    "paymentTermsDays": 30
}
```

### Step 4.2: Create Purchase Order (PO)
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/purchase-orders`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "vendorId": "{{vendorId}}",
    "remarks": "Emergency rod replenishment PO",
    "items": [
        {
            "itemId": "{{itemId}}",
            "quantity": 50,
            "unitPrice": 850.00,
            "remarks": "10mm carbide rods"
        }
    ]
}
```

### Step 4.3: Approve PO
* **Method:** `PUT`
* **URL:** `{{baseUrl}}/api/purchase-orders/{{purchaseOrderId}}/approve`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`

### Step 4.4: Record Goods Receipt Note (GRN)
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/goods-receipts`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "purchaseOrderId": "{{purchaseOrderId}}",
    "gateEntryNumber": "GE-7766",
    "supplierChallanNumber": "CH-SR-88",
    "remarks": "Rods received in store, pass dimensions",
    "items": [
        {
            "poItemId": "{{poItemId}}",
            "receivedQuantity": 50,
            "acceptedQuantity": 50,
            "rejectedQuantity": 0,
            "rejectionReason": ""
        }
    ]
}
```

### Step 4.5: Record Supplier Purchase Invoice
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/purchase-invoices`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "purchaseOrderId": "{{purchaseOrderId}}",
    "invoiceNumber": "INV-CR-876",
    "invoiceDate": "2026-08-11",
    "remarks": "Invoiced by supplier"
}
```

### Step 4.6: Record Vendor Payment
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/vendor-payments`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "vendorId": "{{vendorId}}",
    "paymentDate": "2026-08-12",
    "paymentMode": "BANK_TRANSFER",
    "referenceNumber": "TXN-SBI-9988",
    "remarks": "PO settlement payment",
    "allocations": [
        {
            "purchaseInvoiceId": "{{purchaseInvoiceId}}",
            "amountToAllocate": 42500.00
        }
    ]
}
```

### Step 4.7: Record Stock Take Audit
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/stock-takes`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "remarks": "Found discrepancy in 12mm rods during warehouse check",
    "items": [
        {
            "itemId": "{{itemId}}",
            "physicalQuantity": 47.0
        }
    ]
}
```

### Step 4.8: Reconcile Stock Levels via Stock Adjustment
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/stock-adjustments`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "itemId": "{{itemId}}",
    "quantity": 3.0,
    "type": "SUBTRACT",
    "reason": "Scrapped due to rust damage",
    "remarks": "Reconciled audit mismatch"
}
```

---

## Scenario 5: Worker Daily Attendance & Payroll Settlement Flow
* **Modules Covered:** `Laborer`, `Attendance`, `Advances`, `Payouts`, `Labor Reports`
* **Workflow:**
  1. Register worker.
  2. Log worker attendance (Status: `PRESENT`).
  3. Worker requests advance.
  4. Process monthly payroll.
  5. Check labor reports.

### Step 5.1: Create Laborer Profile
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/labors`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "firstName": "Satish",
    "lastName": "Sawant",
    "mobileNumber": "9812233445",
    "role": "CNC_OPERATOR",
    "salaryType": "MONTHLY",
    "payoutAmount": 26000.00,
    "paymentDetails": "HDFC Bank A/C 123456789",
    "isActive": true
}
```

### Step 5.1.b: Get Operators List (For planning Machine Allocation)
* **Method:** `GET`
* **URL:** `{{baseUrl}}/api/labors/operators`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`


### Step 5.2: Check-In/Log Daily Attendance
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/attendance`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "laborerId": 1,
    "date": "2026-08-01",
    "status": "PRESENT",
    "checkInTime": "2026-08-01T08:00:00",
    "checkOutTime": "2026-08-01T18:00:00",
    "overtimeHours": 2.0,
    "remarks": "Standard morning shift with OT"
}
```

### Step 5.3: Disburse Salary Advance
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/advances`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "laborerId": 1,
    "requestDate": "2026-08-05",
    "amount": 2500.00,
    "repaymentStrategy": "DEDUCT_FROM_NEXT_PAYOUT",
    "remarks": "Personal advance"
}
```

### Step 5.4: Process Payout
* **Method:** `POST`
* **URL:** `{{baseUrl}}/api/payouts`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `Content-Type: application/json`
* **Request Body:**
```json
{
    "laborerId": 1,
    "startDate": "2026-08-01",
    "endDate": "2026-08-31",
    "remarks": "Process monthly payroll with deductions"
}
```

### Step 5.5: Fetch Labor Summary Report
* **Method:** `GET`
* **URL:** `{{baseUrl}}/api/labor-reports/summary?startDate=2026-08-01&endDate=2026-08-31`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`

---

## Scenario 6: Global Search APIs Reference
* **Modules Covered:** `Customers`, `Enquiries`, `Quotations`, `Work Orders`, `Sales Invoices`, `Purchase Orders`, `Vendors`
* **Workflow:**
  Use the dedicated `/search` endpoint to query resources by keywords, names, numbers, or related values.

### API 6.1: Search Customers (by name, email, contact person, mobile, gstin)
* **Method:** `GET`
* **URL:** `{{baseUrl}}/api/customers/search?query=Apex`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`

### API 6.2: Search Enquiries (by enquiry number, remarks, customer company name)
* **Method:** `GET`
* **URL:** `{{baseUrl}}/api/enquiries/search?query=ENQ`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`

### API 6.3: Search Quotations (by quotation number, remarks, customer company name)
* **Method:** `GET`
* **URL:** `{{baseUrl}}/api/quotations/search?query=QT`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`

### API 6.4: Search Work Orders (by work order number, PO number, remarks, customer company name)
* **Method:** `GET`
* **URL:** `{{baseUrl}}/api/work-orders/search?query=WO`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`

### API 6.5: Search Sales Invoices (by invoice number, remarks, work order number, customer company name)
* **Method:** `GET`
* **URL:** `{{baseUrl}}/api/sales-invoices/search?query=INV`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`
  * `X-Company-Code: YT`

### API 6.6: Search Purchase Orders (by PO number, remarks, vendor name)
* **Method:** `GET`
* **URL:** `{{baseUrl}}/api/purchase/purchase-orders/search?query=PO`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`

### API 6.7: Search Vendors (by vendor name, gstin, pan, contact details, address)
* **Method:** `GET`
* **URL:** `{{baseUrl}}/api/purchase/vendors/search?query=Carbide`
* **Headers:** 
  * `Authorization: Bearer {{accessToken}}`

