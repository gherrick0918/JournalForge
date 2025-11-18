#!/bin/bash

# Firebase Setup Verification Script for JournalForge
# This script helps verify that all Firebase configuration is in place

echo "=========================================="
echo "JournalForge Firebase Setup Verification"
echo "=========================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Track if any issues found
ISSUES_FOUND=0

# Check 1: google-services.json exists
echo "Checking google-services.json..."
if [ -f "app/google-services.json" ]; then
    echo -e "${GREEN}✓${NC} google-services.json found"
    
    # Extract project ID
    PROJECT_ID=$(grep -o '"project_id": *"[^"]*"' app/google-services.json | grep -o '"[^"]*"$' | tr -d '"')
    echo "  Project ID: $PROJECT_ID"
    
    # Extract package name
    PACKAGE_NAME=$(grep -o '"package_name": *"[^"]*"' app/google-services.json | grep -o '"[^"]*"$' | tr -d '"' | head -1)
    echo "  Package Name: $PACKAGE_NAME"
    
    if [ "$PACKAGE_NAME" != "com.journalforge.app" ]; then
        echo -e "${RED}✗${NC} Package name mismatch! Should be 'com.journalforge.app'"
        ISSUES_FOUND=$((ISSUES_FOUND + 1))
    fi
else
    echo -e "${RED}✗${NC} google-services.json NOT found!"
    echo "  Expected location: android-app/app/google-services.json"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
fi
echo ""

# Check 2: strings.xml has default_web_client_id
echo "Checking strings.xml for Web Client ID..."
if [ -f "app/src/main/res/values/strings.xml" ]; then
    if grep -q "default_web_client_id" app/src/main/res/values/strings.xml; then
        echo -e "${GREEN}✓${NC} default_web_client_id found in strings.xml"
        WEB_CLIENT=$(grep -o 'name="default_web_client_id">[^<]*' app/src/main/res/values/strings.xml | cut -d'>' -f2)
        echo "  Web Client ID: $WEB_CLIENT"
    else
        echo -e "${RED}✗${NC} default_web_client_id NOT found in strings.xml"
        ISSUES_FOUND=$((ISSUES_FOUND + 1))
    fi
else
    echo -e "${RED}✗${NC} strings.xml NOT found!"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
fi
echo ""

# Check 3: Debug keystore exists
echo "Checking debug keystore..."
DEBUG_KEYSTORE="$HOME/.android/debug.keystore"
if [ -f "$DEBUG_KEYSTORE" ]; then
    echo -e "${GREEN}✓${NC} Debug keystore found"
    echo "  Location: $DEBUG_KEYSTORE"
    
    # Try to get SHA-1
    echo ""
    echo "Getting SHA-1 fingerprint..."
    SHA1=$(keytool -list -v -keystore "$DEBUG_KEYSTORE" -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep "SHA1:" | cut -d' ' -f3)
    
    if [ -n "$SHA1" ]; then
        echo -e "${GREEN}✓${NC} SHA-1 fingerprint: $SHA1"
        echo ""
        echo -e "${YELLOW}⚠${NC}  Make sure this SHA-1 is added to Firebase Console!"
        echo "   Firebase Console → Project Settings → Your apps → Add fingerprint"
    else
        echo -e "${YELLOW}⚠${NC}  Could not extract SHA-1 fingerprint"
        echo "   Run this command manually:"
        echo "   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android"
    fi
else
    echo -e "${YELLOW}⚠${NC}  Debug keystore NOT found (will be created on first build)"
    echo "  Expected location: $DEBUG_KEYSTORE"
    echo "  Run './gradlew assembleDebug' to create it"
fi
echo ""

# Check 4: build.gradle has google-services plugin
echo "Checking build.gradle configuration..."
if [ -f "app/build.gradle" ]; then
    if grep -q "com.google.gms.google-services" app/build.gradle; then
        echo -e "${GREEN}✓${NC} google-services plugin configured"
    else
        echo -e "${RED}✗${NC} google-services plugin NOT found in build.gradle"
        ISSUES_FOUND=$((ISSUES_FOUND + 1))
    fi
    
    if grep -q "com.google.firebase:firebase-auth" app/build.gradle; then
        echo -e "${GREEN}✓${NC} Firebase Auth dependency configured"
    else
        echo -e "${RED}✗${NC} Firebase Auth dependency NOT found"
        ISSUES_FOUND=$((ISSUES_FOUND + 1))
    fi
    
    if grep -q "com.google.android.gms:play-services-auth" app/build.gradle; then
        echo -e "${GREEN}✓${NC} Google Play Services Auth dependency configured"
    else
        echo -e "${RED}✗${NC} Google Play Services Auth dependency NOT found"
        ISSUES_FOUND=$((ISSUES_FOUND + 1))
    fi
else
    echo -e "${RED}✗${NC} build.gradle NOT found!"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
fi
echo ""

# Check 5: Source files exist
echo "Checking source files..."
if [ -f "app/src/main/java/com/journalforge/app/services/GoogleAuthService.kt" ]; then
    echo -e "${GREEN}✓${NC} GoogleAuthService.kt found"
else
    echo -e "${RED}✗${NC} GoogleAuthService.kt NOT found!"
    ISSUES_FOUND=$((ISSUES_FOUND + 1))
fi
echo ""

# Summary
echo "=========================================="
if [ $ISSUES_FOUND -eq 0 ]; then
    echo -e "${GREEN}✓ All local checks passed!${NC}"
    echo ""
    echo -e "${YELLOW}📋 Firebase Console Checklist:${NC}"
    echo ""
    echo "Before testing, verify these in Firebase Console:"
    echo "  1. Your SHA-1 is added to Firebase Console"
    echo "     → Project Settings → Your apps → Add fingerprint"
    echo ""
    echo "  2. Google Sign-In provider is ENABLED"
    echo "     → Authentication → Sign-in method → Google"
    echo ""
    echo "  3. Project support email is set"
    echo "     → In Google Sign-In settings"
    echo ""
    echo -e "${GREEN}✅ If you've already done the above:${NC}"
    echo "  - If your SHA-1 was already in Firebase (no changes needed)"
    echo "  - If Google Sign-In was already enabled"
    echo "  - If google-services.json showed no changes"
    echo ""
    echo -e "${GREEN}  → You're all set! Just build and test:${NC}"
    echo "     ./gradlew clean assembleDebug installDebug"
    echo ""
    echo -e "${YELLOW}⏰ If you JUST made changes in Firebase Console:${NC}"
    echo "  - Download the updated google-services.json"
    echo "  - Wait 5-10 minutes for changes to propagate"
    echo "  - Then build and test"
    echo ""
    echo "For complete setup instructions, see:"
    echo "  ../FIREBASE_SETUP_GUIDE.md"
else
    echo -e "${RED}✗ Found $ISSUES_FOUND issue(s)${NC}"
    echo ""
    echo "Please fix the issues above and run this script again."
    echo ""
    echo "For help, see:"
    echo "  ../FIREBASE_SETUP_GUIDE.md"
fi
echo "=========================================="
