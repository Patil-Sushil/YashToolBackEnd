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
echo -e "${BLUE}     PURCHASE MODULE INTEGRATION TESTS  ${NC}"
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
PTYPE_ID=$(echo "$PTYPE_RES" | jq -r '.data.id // empty')
if [ -z "$PTYPE_ID" ] || [ "$PTYPE_ID" = "null" ]; then
    echo -e "${RED}FAIL: Purchase type creation failed. Response: $PTYPE_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Purchase Type created: $PTYPE_CODE (ID: $PTYPE_ID)${NC}"

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

# --- PHASE 4: CREATE PURCHASE ORDER ---
echo -e "\n${BLUE}Phase 4: Create Purchase Order (Draft)${NC}"
PO_RES=$(curl -s -X POST "$BASE_URL/purchase/purchase-orders" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"vendorId\":\"$VENDOR_ID\", \"expectedDeliveryDate\":\"$(date -v+7d +%Y-%m-%d 2>/dev/null || date -d "+7 days" +%Y-%m-%d)\", \"paymentTermsId\":\"$PT_ID\", \"purchaseTypeId\":\"$PTYPE_ID\", \"remarks\":\"Initial PO for Integration Test\", \"items\":[{\"itemId\":\"$ITEM_ID\", \"materialGradeId\":\"$GRADE_ID\", \"orderedQuantity\":10.0, \"unit\":\"PCS\", \"rate\":100.0, \"gstPercentage\":18.0, \"discount\":10.0}]}")
PO_ID=$(echo "$PO_RES" | jq -r '.data.id // empty')
PO_NUMBER=$(echo "$PO_RES" | jq -r '.data.poNumber // empty')
PO_ITEM_ID=$(echo "$PO_RES" | jq -r '.data.items[0].id // empty')
PO_STATUS=$(echo "$PO_RES" | jq -r '.data.status // empty')

if [ -z "$PO_ID" ] || [ "$PO_ID" = "null" ]; then
    echo -e "${RED}FAIL: Purchase Order creation failed. Response: $PO_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Purchase Order created: $PO_NUMBER (ID: $PO_ID) with Status: $PO_STATUS${NC}"
echo -e "PO Item ID resolved: $PO_ITEM_ID"

# Verify PO Item lineTotal calculation: (10.0 * 100.0 - 10.0) * 1.18 = 990.0 * 1.18 = 1168.20
PO_LINE_TOTAL=$(echo "$PO_RES" | jq -r '.data.items[0].lineTotal // 0')
if [ "$(check_equal "$PO_LINE_TOTAL" "1168.2")" != "true" ]; then
    echo -e "${RED}FAIL: PO Item Line Total calculation error. Calculated: $PO_LINE_TOTAL (Expected: 1168.2)${NC}"; exit 1
fi
echo -e "${GREEN}PASS: PO Item Line Total correctly calculated: $PO_LINE_TOTAL${NC}"

# --- PHASE 5: PO STATUS TRANSITIONS & VALIDATIONS ---
echo -e "\n${BLUE}Phase 5: Approve Purchase Order${NC}"
PO_STATUS_RES=$(curl -s -X PATCH "$BASE_URL/purchase/purchase-orders/$PO_ID/status" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"status\":\"APPROVED\", \"remarks\":\"Approving PO for GRN inwarding\"}")
NEW_PO_STATUS=$(echo "$PO_STATUS_RES" | jq -r '.data.status // empty')

if [ "$NEW_PO_STATUS" != "APPROVED" ]; then
    echo -e "${RED}FAIL: PO Approval failed. Response: $PO_STATUS_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Purchase Order status updated to $NEW_PO_STATUS.${NC}"

# --- PHASE 6: INITIAL STOCK VERIFICATION ---
echo -e "\n${BLUE}Phase 6: Fetch Initial Inventory Stock Level${NC}"
STOCK_RES=$(curl -s -X GET "$BASE_URL/inventory/stocks/query?itemId=$ITEM_ID&materialGradeId=$GRADE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
INITIAL_QTY=$(echo "$STOCK_RES" | jq -r '.data.quantity // 0')
echo -e "Initial Inventory Stock Level: ${YELLOW}$INITIAL_QTY${NC}"

# --- PHASE 7: GOODS RECEIPT NOTE (GRN) INWARD ---
echo -e "\n${BLUE}Phase 7: Goods Receipt Note Inward (Partial receipt - 6 units)${NC}"
# Out of 10 units: Accepted = 5, Rejected = 1
GRN_RES=$(curl -s -X POST "$BASE_URL/purchase/goods-receipts" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"purchaseOrderId\":\"$PO_ID\", \"warehouse\":\"Main Store\", \"receivedBy\":\"Manager Admin\", \"remarks\":\"Partial GRN receipt\", \"items\":[{\"poItemReferenceId\":\"$PO_ITEM_ID\", \"acceptedQuantity\":5.0, \"rejectedQuantity\":1.0}]}")
GRN_ID=$(echo "$GRN_RES" | jq -r '.data.id // empty')
GRN_NUMBER=$(echo "$GRN_RES" | jq -r '.data.grnNumber // empty')

if [ -z "$GRN_ID" ] || [ "$GRN_ID" = "null" ]; then
    echo -e "${RED}FAIL: Goods Receipt Note creation failed. Response: $GRN_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: GRN created successfully: $GRN_NUMBER (ID: $GRN_ID).${NC}"

# Verify PO status became PARTIALLY_RECEIVED
PO_DETAIL_RES=$(curl -s -X GET "$BASE_URL/purchase/purchase-orders/$PO_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
UPDATED_PO_STATUS=$(echo "$PO_DETAIL_RES" | jq -r '.data.status // empty')
if [ "$UPDATED_PO_STATUS" != "PARTIALLY_RECEIVED" ]; then
    echo -e "${RED}FAIL: PO status did not update to PARTIALLY_RECEIVED. Status: $UPDATED_PO_STATUS${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Purchase Order status successfully updated to $UPDATED_PO_STATUS.${NC}"

# Verify Inventory increased ONLY by Accepted Quantity (5.0 units)
EXPECTED_QTY=$(jq -n "$INITIAL_QTY + 5.0")
STOCK_RES=$(curl -s -X GET "$BASE_URL/inventory/stocks/query?itemId=$ITEM_ID&materialGradeId=$GRADE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
CURRENT_QTY=$(echo "$STOCK_RES" | jq -r '.data.quantity // 0')
if [ "$(check_equal "$CURRENT_QTY" "$EXPECTED_QTY")" != "true" ]; then
    echo -e "${RED}FAIL: Inventory stock was not increased by 5.0 accepted units. Current stock: $CURRENT_QTY (Expected: $EXPECTED_QTY)${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Inventory stock successfully updated to $CURRENT_QTY units (increased by accepted quantity).${NC}"

# --- PHASE 8: RECORD PURCHASE INVOICE ---
echo -e "\n${BLUE}Phase 8: Record Purchase Invoice with Warnings${NC}"
# Supplier Invoice contains 8 units (PO ordered 10, GRN accepted 5, so warnings should trigger)
INV_RES=$(curl -s -X POST "$BASE_URL/purchase/purchase-invoices" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"supplierInvoiceNumber\":\"SUP-INV-$TS\", \"purchaseOrderId\":\"$PO_ID\", \"goodsReceiptIds\":[\"$GRN_ID\"], \"gstDetails\":\"GST Inward details\", \"freight\":50.0, \"otherCharges\":10.0, \"items\":[{\"itemId\":\"$ITEM_ID\", \"materialGradeId\":\"$GRADE_ID\", \"invoiceQuantity\":8.0, \"invoiceRate\":100.0}]}")
INV_ID=$(echo "$INV_RES" | jq -r '.data.id // empty')
INV_NUMBER=$(echo "$INV_RES" | jq -r '.data.invoiceNumber // empty')
INV_WARNINGS=$(echo "$INV_RES" | jq -r '.data.warnings // []')

if [ -z "$INV_ID" ] || [ "$INV_ID" = "null" ]; then
    echo -e "${RED}FAIL: Purchase Invoice recording failed. Response: $INV_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Purchase Invoice recorded: $INV_NUMBER (ID: $INV_ID).${NC}"

# Check for discrepancy warnings (should have warnings since Invoice qty 8 != PO qty 10, and Invoice qty 8 != GRN accepted 5)
WARNINGS_COUNT=$(echo "$INV_RES" | jq '.data.warnings | length')
if [ "$WARNINGS_COUNT" -eq 0 ]; then
    echo -e "${RED}FAIL: Discrepancy warnings were not generated for differing quantities. Warnings: $INV_WARNINGS${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Quantity discrepancy warnings correctly generated: $INV_WARNINGS${NC}"

# --- PHASE 9: RECORD PURCHASE RETURN ---
echo -e "\n${BLUE}Phase 9: Record Purchase Return (Return 2 accepted units)${NC}"
RET_RES=$(curl -s -X POST "$BASE_URL/purchase/purchase-returns" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"goodsReceiptId\":\"$GRN_ID\", \"purchaseInvoiceId\":\"$INV_ID\", \"remarks\":\"Returning 2 defect units\", \"items\":[{\"itemId\":\"$ITEM_ID\", \"materialGradeId\":\"$GRADE_ID\", \"quantity\":2.0, \"unit\":\"PCS\", \"rate\":100.0, \"remarks\":\"Defective\"}]}")
RET_ID=$(echo "$RET_RES" | jq -r '.data.id // empty')
RET_NUMBER=$(echo "$RET_RES" | jq -r '.data.returnNumber // empty')

if [ -z "$RET_ID" ] || [ "$RET_ID" = "null" ]; then
    echo -e "${RED}FAIL: Purchase Return recording failed. Response: $RET_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Purchase Return recorded successfully: $RET_NUMBER (ID: $RET_ID).${NC}"

# Verify Inventory decreased by returned quantity (2.0 units)
EXPECTED_QTY=$(jq -n "$EXPECTED_QTY - 2.0")
STOCK_RES=$(curl -s -X GET "$BASE_URL/inventory/stocks/query?itemId=$ITEM_ID&materialGradeId=$GRADE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
CURRENT_QTY=$(echo "$STOCK_RES" | jq -r '.data.quantity // 0')
if [ "$(check_equal "$CURRENT_QTY" "$EXPECTED_QTY")" != "true" ]; then
    echo -e "${RED}FAIL: Inventory stock was not decreased by 2.0 returned units. Current stock: $CURRENT_QTY (Expected: $EXPECTED_QTY)${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Inventory stock successfully decreased to $CURRENT_QTY units after return.${NC}"

# --- PHASE 10: COMPLETE PURCHASE ORDER RECEIPT ---
echo -e "\n${BLUE}Phase 10: Goods Receipt Note (Remaining 4 units to complete PO)${NC}"
# Total received previously was 6 (accepted 5 + rejected 1). PO ordered 10. Remaining is 4 units.
GRN_COMP_RES=$(curl -s -X POST "$BASE_URL/purchase/goods-receipts" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"purchaseOrderId\":\"$PO_ID\", \"warehouse\":\"Main Store\", \"receivedBy\":\"Manager Admin\", \"remarks\":\"Completing GRN receipt\", \"items\":[{\"poItemReferenceId\":\"$PO_ITEM_ID\", \"acceptedQuantity\":4.0, \"rejectedQuantity\":0.0}]}")
GRN_COMP_ID=$(echo "$GRN_COMP_RES" | jq -r '.data.id // empty')
GRN_COMP_NUMBER=$(echo "$GRN_COMP_RES" | jq -r '.data.grnNumber // empty')

if [ -z "$GRN_COMP_ID" ] || [ "$GRN_COMP_ID" = "null" ]; then
    echo -e "${RED}FAIL: Completing GRN failed. Response: $GRN_COMP_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Completion GRN created: $GRN_COMP_NUMBER (ID: $GRN_COMP_ID).${NC}"

# Verify PO status became FULLY_RECEIVED automatically
PO_FINAL_RES=$(curl -s -X GET "$BASE_URL/purchase/purchase-orders/$PO_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
FINAL_PO_STATUS=$(echo "$PO_FINAL_RES" | jq -r '.data.status // empty')
if [ "$FINAL_PO_STATUS" != "FULLY_RECEIVED" ]; then
    echo -e "${RED}FAIL: PO status did not automatically update to FULLY_RECEIVED. Status: $FINAL_PO_STATUS${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Purchase Order status successfully updated to $FINAL_PO_STATUS.${NC}"

# Verify Final Inventory increased by remaining Accepted Quantity (4.0 units)
EXPECTED_QTY=$(jq -n "$EXPECTED_QTY + 4.0")
STOCK_RES=$(curl -s -X GET "$BASE_URL/inventory/stocks/query?itemId=$ITEM_ID&materialGradeId=$GRADE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
CURRENT_QTY=$(echo "$STOCK_RES" | jq -r '.data.quantity // 0')
if [ "$(check_equal "$CURRENT_QTY" "$EXPECTED_QTY")" != "true" ]; then
    echo -e "${RED}FAIL: Final inventory stock was not increased by 4.0 accepted units. Current stock: $CURRENT_QTY (Expected: $EXPECTED_QTY)${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Final inventory stock successfully verified as $CURRENT_QTY units.${NC}"

# --- SUMMARY ---
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN}  ALL PURCHASE MODULE TESTS PASSED!      ${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "Payment terms, Purchase types, Vendor master, Draft PO,"
echo -e "PO Approval, Partial GRN with stock update, Purchase Invoice"
echo -e "quantity warning, Purchase Return with stock reduction, and"
echo -e "final completion GRN with PO fully received status verified."
exit 0
