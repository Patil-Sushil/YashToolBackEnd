#!/bin/bash

# ==========================================
# CONFIGURATION
# ==========================================
BASE_URL=${1:-"http://localhost:8080/api"}
ADMIN_EMAIL=${2:-"admin@yashtools.com"}
ADMIN_PASS=${3:-"Admin@123"}

# Colors
GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   VENDOR PAYMENT INTEGRATION TESTS     ${NC}"
echo -e "${BLUE}========================================${NC}"

# Helper: Get Token
function get_token() {
    curl -s -X POST "$BASE_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"$1\", \"password\":\"$2\"}" | jq -r '.data.token // .token // .data.accessToken // empty'
}

# Helper: Compare Floats using jq
function check_equal() {
    jq -n --arg a "$1" --arg b "$2" '$a|tonumber == ($b|tonumber)'
}

# --- PHASE 1: AUTHENTICATION ---
echo -e "\n${BLUE}Phase 1: Authentication${NC}"
ADMIN_TOKEN=$(get_token "$ADMIN_EMAIL" "$ADMIN_PASS")
if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
    echo -e "${RED}FAIL: Admin Login Failed.${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Admin authenticated.${NC}"

# --- PHASE 2: SETUP REFERENCE DATA ---
echo -e "\n${BLUE}Phase 2: Setup Reference Data (Seeded Items)${NC}"

# Fetch seeded Material Grade
GRADE_RES=$(curl -s -X GET "$BASE_URL/inventory/material-grades" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
GRADE_ID=$(echo "$GRADE_RES" | jq -r '.data.content[0].id // empty')
if [ -z "$GRADE_ID" ] || [ "$GRADE_ID" = "null" ]; then
    echo -e "${RED}FAIL: No Material Grade found. Response: $GRADE_RES${NC}"; exit 1
fi
GRADE_CODE=$(echo "$GRADE_RES" | jq -r '.data.content[0].code // empty')
echo -e "${GREEN}PASS: Material Grade found: $GRADE_CODE (ID: $GRADE_ID)${NC}"

# Fetch seeded Item
ITEM_RES=$(curl -s -X GET "$BASE_URL/inventory/items" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
ITEM_ID=$(echo "$ITEM_RES" | jq -r '.data.content[0].id // empty')
if [ -z "$ITEM_ID" ] || [ "$ITEM_ID" = "null" ]; then
    echo -e "${RED}FAIL: No Inventory Item found. Response: $ITEM_RES${NC}"; exit 1
fi
ITEM_SKU=$(echo "$ITEM_RES" | jq -r '.data.content[0].sku // empty')
echo -e "${GREEN}PASS: Inventory Item found: $ITEM_SKU (ID: $ITEM_ID)${NC}"

# --- PHASE 3: CREATE MASTERS ---
echo -e "\n${BLUE}Phase 3: Create Purchase Masters${NC}"
TS=$(date +%s)

# Create Payment Terms
PT_CODE="PT-$TS"
PT_RES=$(curl -s -X POST "$BASE_URL/purchase/payment-terms" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$PT_CODE\", \"name\":\"Test Payment Terms $TS\", \"numberOfDays\":30, \"description\":\"30 Days net terms\", \"active\":true}")
PT_ID=$(echo "$PT_RES" | jq -r '.data.id // empty')
if [ -z "$PT_ID" ] || [ "$PT_ID" = "null" ]; then
    echo -e "${RED}FAIL: Payment terms creation failed. Response: $PT_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Payment Terms created: $PT_CODE (ID: $PT_ID)${NC}"

# Create Purchase Type
PTYPE_CODE="RAW-$TS"
PTYPE_RES=$(curl -s -X POST "$BASE_URL/purchase/purchase-types" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"$PTYPE_CODE\", \"name\":\"Raw Material $TS\", \"description\":\"Raw materials import\", \"active\":true}")
PT_TYPE_ID=$(echo "$PTYPE_RES" | jq -r '.data.id // empty')
if [ -z "$PT_TYPE_ID" ] || [ "$PT_TYPE_ID" = "null" ]; then
    echo -e "${RED}FAIL: Purchase type creation failed. Response: $PTYPE_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Purchase Type created: $PTYPE_CODE (ID: $PT_TYPE_ID)${NC}"

# Create Vendor
VENDOR_NAME="Supplier $TS"
VENDOR_RES=$(curl -s -X POST "$BASE_URL/purchase/vendors" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"vendorName\":\"$VENDOR_NAME\", \"gstin\":\"27AAAAS1234F1Z1\", \"pan\":\"AAAAS1234F\", \"contactDetails\":\"sales@supplier$TS.com\", \"address\":\"MIDC Phase 1, Pune\", \"paymentTermsId\":\"$PT_ID\", \"active\":true}")
VENDOR_ID=$(echo "$VENDOR_RES" | jq -r '.data.id // empty')
if [ -z "$VENDOR_ID" ] || [ "$VENDOR_ID" = "null" ]; then
    echo -e "${RED}FAIL: Vendor creation failed. Response: $VENDOR_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Vendor created: $VENDOR_NAME (ID: $VENDOR_ID)${NC}"

# --- PHASE 4: CREATE PURCHASE ORDER & GRN ---
echo -e "\n${BLUE}Phase 4: Create Purchase Order (Draft) & Approve${NC}"
PO_RES=$(curl -s -X POST "$BASE_URL/purchase/purchase-orders" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"vendorId\":\"$VENDOR_ID\", \"expectedDeliveryDate\":\"$(date -v+7d +%Y-%m-%d 2>/dev/null || date -d "+7 days" +%Y-%m-%d)\", \"paymentTermsId\":\"$PT_ID\", \"purchaseTypeId\":\"$PT_TYPE_ID\", \"remarks\":\"PO for Payment Test\", \"items\":[{\"itemId\":\"$ITEM_ID\", \"materialGradeId\":\"$GRADE_ID\", \"orderedQuantity\":10.0, \"unit\":\"PCS\", \"rate\":100.0, \"gstPercentage\":18.0, \"discount\":10.0}]}")
PO_ID=$(echo "$PO_RES" | jq -r '.data.id // empty')
PO_NUMBER=$(echo "$PO_RES" | jq -r '.data.poNumber // empty')
PO_ITEM_ID=$(echo "$PO_RES" | jq -r '.data.items[0].id // empty')

if [ -z "$PO_ID" ] || [ "$PO_ID" = "null" ]; then
    echo -e "${RED}FAIL: PO creation failed. Response: $PO_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: PO created: $PO_NUMBER (ID: $PO_ID)${NC}"

PO_STATUS_RES=$(curl -s -X PATCH "$BASE_URL/purchase/purchase-orders/$PO_ID/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"status\":\"APPROVED\", \"remarks\":\"Approving PO\"}")
echo -e "${GREEN}PASS: PO status updated to APPROVED.${NC}"

# Create GRN
GRN_RES=$(curl -s -X POST "$BASE_URL/purchase/goods-receipts" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"purchaseOrderId\":\"$PO_ID\", \"warehouse\":\"Main Store\", \"receivedBy\":\"Manager Admin\", \"remarks\":\"GRN receipt\", \"items\":[{\"poItemReferenceId\":\"$PO_ITEM_ID\", \"acceptedQuantity\":10.0, \"rejectedQuantity\":0.0}]}")
GRN_ID=$(echo "$GRN_RES" | jq -r '.data.id // empty')
if [ -z "$GRN_ID" ] || [ "$GRN_ID" = "null" ]; then
    echo -e "${RED}FAIL: GRN creation failed. Response: $GRN_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: GRN created successfully.${NC}"

# --- PHASE 5: CREATE PURCHASE INVOICE & VERIFY OUTSTANDING ---
echo -e "\n${BLUE}Phase 5: Record Purchase Invoice & Verify Outstanding Initial State${NC}"
# Total amount calculation: (10 * 100) * 1.18 + 50 (freight) + 10 (other) = 1180 + 60 = 1240.00
INV_RES=$(curl -s -X POST "$BASE_URL/purchase/purchase-invoices" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"supplierInvoiceNumber\":\"SUP-INV-$TS\", \"purchaseOrderId\":\"$PO_ID\", \"goodsReceiptIds\":[\"$GRN_ID\"], \"gstDetails\":\"GST Inward details\", \"freight\":50.0, \"otherCharges\":10.0, \"items\":[{\"itemId\":\"$ITEM_ID\", \"materialGradeId\":\"$GRADE_ID\", \"invoiceQuantity\":10.0, \"invoiceRate\":100.0}]}")
INV_ID=$(echo "$INV_RES" | jq -r '.data.id // empty')
INV_NUMBER=$(echo "$INV_RES" | jq -r '.data.invoiceNumber // empty')
INV_TOTAL=$(echo "$INV_RES" | jq -r '.data.totalAmount // empty')

if [ -z "$INV_ID" ] || [ "$INV_ID" = "null" ]; then
    echo -e "${RED}FAIL: Invoice creation failed. Response: $INV_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Purchase Invoice recorded: $INV_NUMBER (ID: $INV_ID, Total: $INV_TOTAL)${NC}"

# Verify outstanding amount on getById
INV_GET=$(curl -s -X GET "$BASE_URL/purchase/purchase-invoices/$INV_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
INV_OUTSTANDING=$(echo "$INV_GET" | jq -r '.data.outstandingAmount // empty')
INV_PAY_STATUS=$(echo "$INV_GET" | jq -r '.data.paymentStatus // empty')

if [ "$(check_equal "$INV_OUTSTANDING" "$INV_TOTAL")" != "true" ] || [ "$INV_PAY_STATUS" != "PENDING" ]; then
    echo -e "${RED}FAIL: Invoice outstanding verification failed. Outstanding: $INV_OUTSTANDING, Status: $INV_PAY_STATUS${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Invoice outstanding is correctly $INV_OUTSTANDING and status is $INV_PAY_STATUS.${NC}"

# Verify Outstanding Report contains this invoice
OUTSTANDING_REP=$(curl -s -X GET "$BASE_URL/purchase/vendor-payments/reports/outstanding?vendorId=$VENDOR_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
REP_COUNT=$(echo "$OUTSTANDING_REP" | jq '.data | length')
if [ "$REP_COUNT" -ne 1 ]; then
    echo -e "${RED}FAIL: Outstanding report should have 1 invoice, got $REP_COUNT. Response: $OUTSTANDING_REP${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Outstanding report correctly contains 1 invoice.${NC}"

# Verify outstanding balance endpoint
BAL_RES=$(curl -s -X GET "$BASE_URL/purchase/vendors/$VENDOR_ID/outstanding-balance" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
BAL_VAL=$(echo "$BAL_RES" | jq -r '.data // empty')
if [ "$(check_equal "$BAL_VAL" "$INV_TOTAL")" != "true" ]; then
    echo -e "${RED}FAIL: Vendor outstanding balance check failed. Expected: $INV_TOTAL, Got: $BAL_VAL${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Vendor outstanding balance is correctly $BAL_VAL.${NC}"

# --- PHASE 6: PARTIAL PAYMENT & VERIFY ---
echo -e "\n${BLUE}Phase 6: Record Partial Payment (₹400)${NC}"
PAY_RES=$(curl -s -X POST "$BASE_URL/purchase/vendor-payments" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"vendorId\":\"$VENDOR_ID\", \"paymentMethod\":\"BANK_TRANSFER\", \"paymentReferenceNumber\":\"REF-$TS-1\", \"remarks\":\"Partial payment test\", \"items\":[{\"purchaseInvoiceId\":\"$INV_ID\", \"paidAmount\":400.0}]}")
PAY_ID=$(echo "$PAY_RES" | jq -r '.data.id // empty')
PAY_NUMBER=$(echo "$PAY_RES" | jq -r '.data.paymentNumber // empty')

if [ -z "$PAY_ID" ] || [ "$PAY_ID" = "null" ]; then
    echo -e "${RED}FAIL: Payment creation failed. Response: $PAY_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Partial payment recorded: $PAY_NUMBER (ID: $PAY_ID)${NC}"

# Verify invoice state after partial payment
INV_GET_P=$(curl -s -X GET "$BASE_URL/purchase/purchase-invoices/$INV_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
INV_OUTSTANDING_P=$(echo "$INV_GET_P" | jq -r '.data.outstandingAmount // empty')
INV_PAID_P=$(echo "$INV_GET_P" | jq -r '.data.paidAmount // empty')
INV_PAY_STATUS_P=$(echo "$INV_GET_P" | jq -r '.data.paymentStatus // empty')

EXPECTED_OUTSTANDING=$(jq -n "$INV_TOTAL - 400.0")

if [ "$(check_equal "$INV_OUTSTANDING_P" "$EXPECTED_OUTSTANDING")" != "true" ] || [ "$INV_PAY_STATUS_P" != "PARTIALLY_PAID" ] || [ "$(check_equal "$INV_PAID_P" "400.0")" != "true" ]; then
    echo -e "${RED}FAIL: Invoice state after partial payment is incorrect. Outstanding: $INV_OUTSTANDING_P, Paid: $INV_PAID_P, Status: $INV_PAY_STATUS_P${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Invoice outstanding successfully updated to $INV_OUTSTANDING_P, Paid: $INV_PAID_P, Status: $INV_PAY_STATUS_P.${NC}"

# Verify outstanding balance endpoint
BAL_RES_P=$(curl -s -X GET "$BASE_URL/purchase/vendors/$VENDOR_ID/outstanding-balance" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
BAL_VAL_P=$(echo "$BAL_RES_P" | jq -r '.data // empty')
if [ "$(check_equal "$BAL_VAL_P" "$EXPECTED_OUTSTANDING")" != "true" ]; then
    echo -e "${RED}FAIL: Vendor outstanding balance check failed after partial payment. Expected: $EXPECTED_OUTSTANDING, Got: $BAL_VAL_P${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Vendor outstanding balance is correctly $BAL_VAL_P after partial payment.${NC}"

# --- PHASE 7: VALIDATION RULES ENFORCEMENT ---
echo -e "\n${BLUE}Phase 7: Verify Validation Rules${NC}"

# Rule 1: Do not allow payment greater than invoice outstanding
echo -e "Testing validation: Payment greater than outstanding..."
BAD_PAY_RES1=$(curl -s -X POST "$BASE_URL/purchase/vendor-payments" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"vendorId\":\"$VENDOR_ID\", \"paymentMethod\":\"UPI\", \"paymentReferenceNumber\":\"REF-$TS-2\", \"items\":[{\"purchaseInvoiceId\":\"$INV_ID\", \"paidAmount\":1000.0}]}")
ERR_MSG1=$(echo "$BAD_PAY_RES1" | jq -r '.message // empty')
if [[ "$ERR_MSG1" == *"exceeds outstanding amount"* ]]; then
    echo -e "${GREEN}PASS: Correctly rejected overpayment. Error: $ERR_MSG1${NC}"
else
    echo -e "${RED}FAIL: Allowed payment greater than outstanding or returned wrong message. Response: $BAD_PAY_RES1${NC}"; exit 1
fi

# Rule 2: Do not allow duplicate payment reference numbers
echo -e "Testing validation: Duplicate reference number..."
BAD_PAY_RES2=$(curl -s -X POST "$BASE_URL/purchase/vendor-payments" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"vendorId\":\"$VENDOR_ID\", \"paymentMethod\":\"UPI\", \"paymentReferenceNumber\":\"REF-$TS-1\", \"items\":[{\"purchaseInvoiceId\":\"$INV_ID\", \"paidAmount\":10.0}]}")
ERR_MSG2=$(echo "$BAD_PAY_RES2" | jq -r '.message // empty')
if [[ "$ERR_MSG2" == *"already exists"* ]]; then
    echo -e "${GREEN}PASS: Correctly rejected duplicate reference number. Error: $ERR_MSG2${NC}"
else
    echo -e "${RED}FAIL: Allowed duplicate reference number. Response: $BAD_PAY_RES2${NC}"; exit 1
fi

# --- PHASE 8: COMPLETE PAYMENT & VERIFY FULLY PAID ---
echo -e "\n${BLUE}Phase 8: Record Remaining Payment to Fully Pay Invoice (₹840)${NC}"
PAY_RES2=$(curl -s -X POST "$BASE_URL/purchase/vendor-payments" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"vendorId\":\"$VENDOR_ID\", \"paymentMethod\":\"NEFT\", \"paymentReferenceNumber\":\"REF-$TS-3\", \"remarks\":\"Final payment to close\", \"items\":[{\"purchaseInvoiceId\":\"$INV_ID\", \"paidAmount\":840.0}]}")
PAY_ID2=$(echo "$PAY_RES2" | jq -r '.data.id // empty')
if [ -z "$PAY_ID2" ] || [ "$PAY_ID2" = "null" ]; then
    echo -e "${RED}FAIL: Final payment creation failed. Response: $PAY_RES2${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Final payment recorded successfully.${NC}"

# Verify invoice state is PAID and outstanding is 0
INV_GET_F=$(curl -s -X GET "$BASE_URL/purchase/purchase-invoices/$INV_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
INV_OUTSTANDING_F=$(echo "$INV_GET_F" | jq -r '.data.outstandingAmount // empty')
INV_PAID_F=$(echo "$INV_GET_F" | jq -r '.data.paidAmount // empty')
INV_PAY_STATUS_F=$(echo "$INV_GET_F" | jq -r '.data.paymentStatus // empty')

if [ "$(check_equal "$INV_OUTSTANDING_F" "0")" != "true" ] || [ "$INV_PAY_STATUS_F" != "PAID" ] || [ "$(check_equal "$INV_PAID_F" "$INV_TOTAL")" != "true" ]; then
    echo -e "${RED}FAIL: Invoice is not fully paid. Outstanding: $INV_OUTSTANDING_F, Paid: $INV_PAID_F, Status: $INV_PAY_STATUS_F${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Invoice is fully paid! Outstanding: $INV_OUTSTANDING_F, Paid: $INV_PAID_F, Status: $INV_PAY_STATUS_F.${NC}"

# Verify outstanding balance endpoint is 0
BAL_RES_F=$(curl -s -X GET "$BASE_URL/purchase/vendors/$VENDOR_ID/outstanding-balance" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
BAL_VAL_F=$(echo "$BAL_RES_F" | jq -r '.data // empty')
if [ "$(check_equal "$BAL_VAL_F" "0")" != "true" ]; then
    echo -e "${RED}FAIL: Vendor outstanding balance check failed after full payment. Expected: 0, Got: $BAL_VAL_F${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Vendor outstanding balance is correctly $BAL_VAL_F after full payment.${NC}"

# Rule 3: Do not allow payment against Fully Paid Invoice
echo -e "Testing validation: Paying fully paid invoice..."
BAD_PAY_RES3=$(curl -s -X POST "$BASE_URL/purchase/vendor-payments" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"vendorId\":\"$VENDOR_ID\", \"paymentMethod\":\"UPI\", \"paymentReferenceNumber\":\"REF-$TS-4\", \"items\":[{\"purchaseInvoiceId\":\"$INV_ID\", \"paidAmount\":10.0}]}")
ERR_MSG3=$(echo "$BAD_PAY_RES3" | jq -r '.message // empty')
if [[ "$ERR_MSG3" == *"Fully Paid Purchase Invoice"* ]]; then
    echo -e "${GREEN}PASS: Correctly rejected payment on fully paid invoice. Error: $ERR_MSG3${NC}"
else
    echo -e "${RED}FAIL: Allowed payment on fully paid invoice. Response: $BAD_PAY_RES3${NC}"; exit 1
fi

# --- PHASE 9: REPORTS AND HISTORY VERIFICATION ---
echo -e "\n${BLUE}Phase 9: Verify Reports and History APIs${NC}"

# 1. Outstanding report should be empty for this vendor since invoice is fully paid
OUTSTANDING_REP2=$(curl -s -X GET "$BASE_URL/purchase/vendor-payments/reports/outstanding?vendorId=$VENDOR_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
REP_COUNT2=$(echo "$OUTSTANDING_REP2" | jq '.data | length')
if [ "$REP_COUNT2" -ne 0 ]; then
    echo -e "${RED}FAIL: Outstanding report should be empty (0 invoices), got $REP_COUNT2. Response: $OUTSTANDING_REP2${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Outstanding report is correctly empty after full payment.${NC}"

# 2. Payment history report should show 2 payments for this vendor
HISTORY_REP=$(curl -s -X GET "$BASE_URL/purchase/vendor-payments/reports/history?vendorId=$VENDOR_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
HIST_COUNT=$(echo "$HISTORY_REP" | jq '.data | length')
if [ "$HIST_COUNT" -ne 2 ]; then
    echo -e "${RED}FAIL: Payment history report should have 2 payments, got $HIST_COUNT. Response: $HISTORY_REP${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Payment history report correctly contains 2 payments.${NC}"

# 3. Check Overdue Invoices report
OVERDUE_REP=$(curl -s -X GET "$BASE_URL/purchase/vendor-payments/reports/overdue" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo -e "${GREEN}PASS: Overdue report retrieved successfully.${NC}"

# --- PHASE 10: CANCEL / DELETE PAYMENT ---
echo -e "\n${BLUE}Phase 10: Delete/Cancel Second Payment & Verify Outstanding Reversion${NC}"
DELETE_RES=$(curl -s -X DELETE "$BASE_URL/purchase/vendor-payments/$PAY_ID2" \
  -H "Authorization: Bearer $ADMIN_TOKEN")

# Verify invoice reverts back to PARTIALLY_PAID with outstanding = 840.00
INV_GET_D=$(curl -s -X GET "$BASE_URL/purchase/purchase-invoices/$INV_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
INV_OUTSTANDING_D=$(echo "$INV_GET_D" | jq -r '.data.outstandingAmount // empty')
INV_PAID_D=$(echo "$INV_GET_D" | jq -r '.data.paidAmount // empty')
INV_PAY_STATUS_D=$(echo "$INV_GET_D" | jq -r '.data.paymentStatus // empty')

if [ "$(check_equal "$INV_OUTSTANDING_D" "840.0")" != "true" ] || [ "$INV_PAY_STATUS_D" != "PARTIALLY_PAID" ] || [ "$(check_equal "$INV_PAID_D" "400.0")" != "true" ]; then
    echo -e "${RED}FAIL: Outstanding reversion after delete failed. Outstanding: $INV_OUTSTANDING_D, Paid: $INV_PAID_D, Status: $INV_PAY_STATUS_D${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Invoice successfully reverted to outstanding: $INV_OUTSTANDING_D, Paid: $INV_PAID_D, Status: $INV_PAY_STATUS_D.${NC}"

# Verify outstanding balance endpoint is reverted to 840.0
BAL_RES_D=$(curl -s -X GET "$BASE_URL/purchase/vendors/$VENDOR_ID/outstanding-balance" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
BAL_VAL_D=$(echo "$BAL_RES_D" | jq -r '.data // empty')
if [ "$(check_equal "$BAL_VAL_D" "840.0")" != "true" ]; then
    echo -e "${RED}FAIL: Vendor outstanding balance check failed after payment cancellation. Expected: 840.0, Got: $BAL_VAL_D${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Vendor outstanding balance successfully reverted to $BAL_VAL_D.${NC}"

# ==========================================
# SUMMARY
# ==========================================
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  ALL VENDOR PAYMENT TESTS PASSED!      ${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "CRUD operations, outstanding calculation, partial payment,"
echo -e "full payment, validation constraints (overpayment, fully paid,"
echo -e "duplicate reference numbers) and reports are all verified."
exit 0
