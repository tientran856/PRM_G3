#!/bin/bash
# Script test deep link cho Linux/Mac
# Sử dụng: ./test_deep_link.sh {recipeId}

echo "========================================"
echo "Test Deep Link - PRM Recipe App"
echo "========================================"
echo ""

if [ -z "$1" ]; then
    echo "ERROR: Vui lòng nhập Recipe ID"
    echo ""
    echo "Sử dụng: ./test_deep_link.sh {recipeId}"
    echo "Ví dụ: ./test_deep_link.sh -N1234567890"
    echo ""
    exit 1
fi

RECIPE_ID=$1
DEEP_LINK="prmrecipe://recipe/$RECIPE_ID"

echo "Recipe ID: $RECIPE_ID"
echo "Deep Link: $DEEP_LINK"
echo ""

# Kiểm tra thiết bị đã kết nối
echo "Kiểm tra thiết bị..."
adb devices
echo ""

# Test deep link
echo "Đang mở deep link..."
adb shell am start -a android.intent.action.VIEW -d "$DEEP_LINK"

echo ""
echo "========================================"
echo "Hoàn thành! Kiểm tra ứng dụng đã mở chưa."
echo "========================================"
echo ""

