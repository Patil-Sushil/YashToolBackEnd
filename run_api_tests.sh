#!/usr/bin/env bash

# ==============================================================================
# YashTools API Integration Test Runner (Bash)
# Runs the comprehensive Postman collection using Newman
# ==============================================================================

# Exit immediately if a command exits with a non-zero status
set -e

# Default configuration
BASE_URL="http://localhost:8080"
COLLECTION_PATH="documentation/YashTools_Comprehensive_Postman_Collection.json"

# Print colored messages
print_info() {
    echo -e "\033[1;34m[INFO]\033[0m $1"
}

print_success() {
    echo -e "\033[1;32m[SUCCESS]\033[0m $1"
}

print_error() {
    echo -e "\033[1;31m[ERROR]\033[0m $1"
}

print_warning() {
    echo -e "\033[1;33m[WARNING]\033[0m $1"
}

# Display help message
show_help() {
    echo "Usage: ./run_api_tests.sh [options]"
    echo ""
    echo "Options:"
    echo "  -u, --url <url>       Specify the backend Base URL (default: http://localhost:8080)"
    echo "  -c, --collection <p>  Specify a custom Postman collection file path"
    echo "  -h, --help            Show this help message"
    echo ""
}

# Parse command line arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -u|--url) BASE_URL="$2"; shift ;;
        -c|--collection) COLLECTION_PATH="$2"; shift ;;
        -h|--help) show_help; exit 0 ;;
        *) print_error "Unknown parameter: $1"; show_help; exit 1 ;;
    esac
    shift
done

# Ensure collection file exists
if [ ! -f "$COLLECTION_PATH" ]; then
    print_error "Collection file not found at: $COLLECTION_PATH"
    exit 1
fi

print_info "YashTools Integration Test Runner"
print_info "---------------------------------"
print_info "Target Base URL: $BASE_URL"
print_info "Collection File: $COLLECTION_PATH"
print_info "---------------------------------"

# Check if node & npm are installed
if ! command -v node &> /dev/null; then
    print_error "Node.js is not installed. Node.js (with npm) is required to run Newman."
    print_warning "Please install Node.js from https://nodejs.org/"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    print_error "npm is not installed."
    exit 1
fi

# Ensure newman is run via npx
print_info "Checking/Running tests using Newman via npx..."
set +e
npx --yes newman run "$COLLECTION_PATH" \
    --env-var "baseUrl=$BASE_URL" \
    --reporters cli

NEWMAN_EXIT_CODE=$?
set -e

if [ $NEWMAN_EXIT_CODE -eq 0 ]; then
    print_success "All API endpoints tested successfully!"
    exit 0
else
    print_error "Some API endpoint tests failed. Check the Newman CLI output above."
    exit $NEWMAN_EXIT_CODE
fi
