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
echo -e "${BLUE}     INVENTORY MODULE INTEGRATION TESTS   ${NC}"
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
echo -e "\n${BLUE}Phase 2: Setup Reference Data Injection${NC}"
TS=$(date +%s)

# Fetch first seeded Material Grade
echo "Fetching material grade..."
GRADE_RES=$(curl -s -X GET "$BASE_URL/inventory/material-grades" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
GRADE_ID=$(echo "$GRADE_RES" | jq -r '.data.content[0].id // empty')

if [ -z "$GRADE_ID" ] || [ "$GRADE_ID" = "null" ]; then
    echo -e "${RED}FAIL: No Material Grade found. Response: $GRADE_RES${NC}"; exit 1
fi
GRADE_CODE=$(echo "$GRADE_RES" | jq -r '.data.content[0].code // empty')
echo -e "${GREEN}PASS: Material Grade found: $GRADE_CODE (ID: $GRADE_ID)${NC}"

# Fetch first seeded Item
echo "Fetching inventory item..."
ITEM_RES=$(curl -s -X GET "$BASE_URL/inventory/items" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
ITEM_ID=$(echo "$ITEM_RES" | jq -r '.data.content[0].id // empty')

if [ -z "$ITEM_ID" ] || [ "$ITEM_ID" = "null" ]; then
    echo -e "${RED}FAIL: No Inventory Item found. Response: $ITEM_RES${NC}"; exit 1
fi
ITEM_SKU=$(echo "$ITEM_RES" | jq -r '.data.content[0].sku // empty')
echo -e "${GREEN}PASS: Inventory Item found: $ITEM_SKU (ID: $ITEM_ID)${NC}"

# --- PHASE 3: INITIAL STOCK VERIFICATION ---
echo -e "\n${BLUE}Phase 3: Verify Initial Stock level is empty/zero${NC}"
STOCK_RES=$(curl -s -X GET "$BASE_URL/inventory/stocks/query?itemId=$ITEM_ID&materialGradeId=$GRADE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
INITIAL_QTY=$(echo "$STOCK_RES" | jq -r '.data.quantity // 0')
echo -e "Initial Stock Quantity: ${YELLOW}$INITIAL_QTY${NC} (Expected: 0)"

# --- PHASE 4: STOCK ADJUSTMENT (ADDITION) ---
echo -e "\n${BLUE}Phase 4: Stock Adjustment - Adding Initial Stock (10.0 rods)${NC}"
ADJ_RES=$(curl -s -X POST "$BASE_URL/inventory/stock-adjustments" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"itemId\":\"$ITEM_ID\", \"materialGradeId\":\"$GRADE_ID\", \"quantity\":10.0, \"adjustmentType\":\"ADD\", \"reason\":\"EXCESS\", \"remarks\":\"Adding initial rods for testing\"}")
ADJ_QTY=$(echo "$ADJ_RES" | jq -r '.data.quantity // empty')

if [ -z "$ADJ_QTY" ] || [ "$ADJ_QTY" = "null" ]; then
    echo -e "${RED}FAIL: Stock adjustment failed. Response: $ADJ_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Stock adjustment ADD successful. Quantity adjusted: $ADJ_QTY${NC}"

# Verify current stock is now initial + 10
EXPECTED_QTY=$(jq -n "$INITIAL_QTY + 10.0")
STOCK_RES=$(curl -s -X GET "$BASE_URL/inventory/stocks/query?itemId=$ITEM_ID&materialGradeId=$GRADE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
CURRENT_QTY=$(echo "$STOCK_RES" | jq -r '.data.quantity // 0')
if [ "$(check_equal "$CURRENT_QTY" "$EXPECTED_QTY")" != "true" ]; then
    echo -e "${RED}FAIL: Current stock quantity is not $EXPECTED_QTY, but $CURRENT_QTY${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Stock verified as $CURRENT_QTY.${NC}"

# --- PHASE 5: MATERIAL ISSUE & OFFCUT CREATION (FULL_ROD) ---
echo -e "\n${BLUE}Phase 5: Material Issue - Consume Full Rod (100mm out of 330mm)${NC}"
ISS_RES=$(curl -s -X POST "$BASE_URL/inventory/material-issues" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"itemId\":\"$ITEM_ID\", \"materialGradeId\":\"$GRADE_ID\", \"issueType\":\"FULL_ROD\", \"requiredLength\":100.0, \"fullRodLength\":330.0}")
NEW_CUT_PIECE_ID=$(echo "$ISS_RES" | jq -r '.data.newCutPieceId // empty')
NEW_CUT_PIECE_CODE=$(echo "$ISS_RES" | jq -r '.data.newCutPieceCode // empty')

if [ -z "$NEW_CUT_PIECE_ID" ] || [ "$NEW_CUT_PIECE_ID" = "null" ]; then
    echo -e "${RED}FAIL: Material issue failed or did not generate a new offcut cut piece. Response: $ISS_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Material issue successful. Issued 1 full rod. New offcut cut piece generated: $NEW_CUT_PIECE_CODE (ID: $NEW_CUT_PIECE_ID).${NC}"

# Verify stock has decreased by 1 full rod
EXPECTED_QTY=$(jq -n "$EXPECTED_QTY - 1.0")
STOCK_RES=$(curl -s -X GET "$BASE_URL/inventory/stocks/query?itemId=$ITEM_ID&materialGradeId=$GRADE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
CURRENT_QTY=$(echo "$STOCK_RES" | jq -r '.data.quantity // 0')
if [ "$(check_equal "$CURRENT_QTY" "$EXPECTED_QTY")" != "true" ]; then
    echo -e "${RED}FAIL: Stock count was not reduced by 1 full rod. Current stock: $CURRENT_QTY (Expected: $EXPECTED_QTY)${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Stock successfully reduced to $CURRENT_QTY full rods.${NC}"

# --- PHASE 6: CUT PIECE RECOMMENDATIONS ---
echo -e "\n${BLUE}Phase 6: Cut Piece Recommendation Verification${NC}"
REC_RES=$(curl -s -X GET "$BASE_URL/inventory/cut-pieces/recommendations?itemId=$ITEM_ID&materialGradeId=$GRADE_ID&requiredLength=80.0" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
REC_FOUND=$(echo "$REC_RES" | jq -r ".data[]? | select(.id==\"$NEW_CUT_PIECE_ID\") | .id // empty")

if [ -z "$REC_FOUND" ]; then
    echo -e "${RED}FAIL: Generated cut piece of 230.0mm remaining was not recommended for required length of 80.0mm. Response: $REC_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Generated cut piece ($NEW_CUT_PIECE_CODE) correctly recommended for required length of 80.0mm.${NC}"

# --- PHASE 7: MATERIAL ISSUE (CUT_PIECE) ---
echo -e "\n${BLUE}Phase 7: Material Issue - Consume from existing Cut Piece (50mm)${NC}"
ISS_CP_RES=$(curl -s -X POST "$BASE_URL/inventory/material-issues" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"itemId\":\"$ITEM_ID\", \"materialGradeId\":\"$GRADE_ID\", \"issueType\":\"CUT_PIECE\", \"cutPieceId\":\"$NEW_CUT_PIECE_ID\", \"requiredLength\":50.0}")
CP_ISS_NUMBER=$(echo "$ISS_CP_RES" | jq -r '.data.issueNumber // empty')

if [ -z "$CP_ISS_NUMBER" ] || [ "$CP_ISS_NUMBER" = "null" ]; then
    echo -e "${RED}FAIL: Material issue of cut piece failed. Response: $ISS_CP_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Cut piece material issue successful (Issue: $CP_ISS_NUMBER).${NC}"

# Verify remaining length of the offcut is now 180.0mm (230.0 - 50.0)
CP_RES=$(curl -s -X GET "$BASE_URL/inventory/cut-pieces/$NEW_CUT_PIECE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
REMAINING_LEN=$(echo "$CP_RES" | jq -r '.data.remainingLength // 0')
if [ "$(check_equal "$REMAINING_LEN" "180.0")" != "true" ]; then
    echo -e "${RED}FAIL: Remaining cut piece length was not correctly reduced. Remaining: $REMAINING_LEN (Expected: 180.0)${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Offcut remaining length correctly reduced to $REMAINING_LEN mm.${NC}"

# --- PHASE 8: PHYSICAL STOCK TAKE & RECONCILIATION ---
# Reconcile physical count to 2.0 rods less than current EXPECTED_QTY
TARGET_QTY=$(jq -n "$EXPECTED_QTY - 1.0")
echo -e "\n${BLUE}Phase 8: Physical Stock Take & Reconciliation (Reconcile stock count to $TARGET_QTY)${NC}"

# Create stock take sheet with physical count
echo "Creating stock take sheet..."
ST_RES=$(curl -s -X POST "$BASE_URL/inventory/stock-takes" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"remarks\":\"Integration test stock take reconciliation\", \"lines\":[{\"itemId\":\"$ITEM_ID\", \"materialGradeId\":\"$GRADE_ID\", \"physicalQuantity\":$TARGET_QTY}]}")
ST_ID=$(echo "$ST_RES" | jq -r '.data.id // empty')

if [ -z "$ST_ID" ] || [ "$ST_ID" = "null" ]; then
    echo -e "${RED}FAIL: Stock take creation failed. Response: $ST_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Stock take sheet created. ID: $ST_ID${NC}"

# Complete stock take
echo "Completing stock take..."
ST_COMP_RES=$(curl -s -X POST "$BASE_URL/inventory/stock-takes/$ST_ID/complete" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
ST_COMP_STATUS=$(echo "$ST_COMP_RES" | jq -r '.data.status // empty')
if [ "$ST_COMP_STATUS" != "COMPLETED" ]; then
    echo -e "${RED}FAIL: Stock take completion failed. Response: $ST_COMP_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Stock take status changed to $ST_COMP_STATUS.${NC}"

# Approve stock take to reconcile
echo "Approving stock take..."
ST_APP_RES=$(curl -s -X POST "$BASE_URL/inventory/stock-takes/$ST_ID/approve" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
ST_APP_STATUS=$(echo "$ST_APP_RES" | jq -r '.data.status // empty')
if [ "$ST_APP_STATUS" != "APPROVED" ]; then
    echo -e "${RED}FAIL: Stock take approval failed. Response: $ST_APP_RES${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Stock take status changed to $ST_APP_STATUS (Reconciled).${NC}"

# Verify final stock is adjusted to TARGET_QTY
STOCK_RES=$(curl -s -X GET "$BASE_URL/inventory/stocks/query?itemId=$ITEM_ID&materialGradeId=$GRADE_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
CURRENT_QTY=$(echo "$STOCK_RES" | jq -r '.data.quantity // 0')
if [ "$(check_equal "$CURRENT_QTY" "$TARGET_QTY")" != "true" ]; then
    echo -e "${RED}FAIL: Final reconciled stock quantity is not $TARGET_QTY, but $CURRENT_QTY${NC}"; exit 1
fi
echo -e "${GREEN}PASS: Stock take verified and stock successfully reconciled to $CURRENT_QTY full rods.${NC}"

# --- SUMMARY ---
echo -e "\n${GREEN}========================================${NC}"
echo -e "${GREEN} ALL INVENTORY MODULE TESTS PASSED!     ${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "Stocks adjusted, full rod issued,"
echo -e "cut-pieces generated and recommended, cut-piece issued,"
echo -e "and physical stock takes successfully reconciled."
exit 0
