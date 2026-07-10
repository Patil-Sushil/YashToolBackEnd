#!/bin/bash
# ==============================================================================
# YashTools Test Suite Runner
# ==============================================================================

# Print styled header
echo -e "\033[1;36m======================================================================\033[0m"
echo -e "\033[1;36m                      YASHTOOLS TEST SUITE RUNNER                     \033[0m"
echo -e "\033[1;36m======================================================================\033[0m"

# Locate maven wrapper or maven install
if [ -f "./mvnw" ]; then
    MAVEN_CMD="./mvnw"
    echo -e "\033[0;32m[INFO] Using local Maven Wrapper (mvnw)\033[0m"
else
    MAVEN_CMD="mvn"
    echo -e "\033[0;33m[WARN] Maven Wrapper not found. Falling back to global 'mvn'\033[0m"
fi

# Clean and run all tests
echo -e "\033[0;32m[INFO] Running: $MAVEN_CMD clean test\033[0m"
$MAVEN_CMD clean test
RESULT=$?

# Report status
echo -e "\033[1;36m----------------------------------------------------------------------\033[0m"
if [ $RESULT -eq 0 ]; then
    echo -e "\033[1;32m[SUCCESS] All tests compiled and passed successfully!\033[0m"
else
    echo -e "\033[1;31m[FAILURE] Test run failed. Please check the logs above.\033[0m"
fi
echo -e "\033[1;36m======================================================================\033[0m"

exit $RESULT
