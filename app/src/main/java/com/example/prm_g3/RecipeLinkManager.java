package com.example.prm_g3;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý danh sách link chia sẻ được hardcode cho các công thức cụ thể.
 * <p>
 * Lý do: một số công thức demo cần dẫn tới landing page có sẵn (được tạo bằng get-qr.com)
 * thay vì deep link nội bộ. Những công thức còn lại tiếp tục dùng deep link mặc định.
 */
public final class RecipeLinkManager {

    private static final Map<String, String> RECIPE_LINKS = new HashMap<>();

    static {
        // Hardcode link cho các công thức cụ thể
        RECIPE_LINKS.put("Phở bò Hà Nội", "https://get-qr.com/content/Zs_P8y");
        RECIPE_LINKS.put("Bánh mì thịt nướng", "https://get-qr.com/content/4ct8VS");

        // Có thể thêm các công thức khác ở đây
        // RECIPE_LINKS.put("Tên công thức", "https://get-qr.com/content/XXXXXX");
    }

    private RecipeLinkManager() {
        // Ngăn tạo instance
    }

    private static String normalize(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        // Loại bỏ dấu (ký tự tổ hợp)
        String withoutDiacritics = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return withoutDiacritics.toLowerCase();
    }

    /**
     * Lấy link chia sẻ cho công thức dựa trên tên công thức.
     *
     * @param recipeTitle Tên công thức
     * @return Link chia sẻ nếu có, null nếu không tìm thấy
     */
    public static String getShareLinkForRecipe(String recipeTitle) {
        if (recipeTitle == null || recipeTitle.trim().isEmpty()) {
            return null;
        }

        String normalizedTitle = recipeTitle.trim();
        String normalizedNoAccent = normalize(recipeTitle);

        // Tìm kiếm chính xác (có phân biệt hoa thường)
        String link = RECIPE_LINKS.get(normalizedTitle);
        if (link != null) {
            return link;
        }

        // Tìm kiếm không phân biệt hoa thường
        for (Map.Entry<String, String> entry : RECIPE_LINKS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(normalizedTitle)) {
                return entry.getValue();
            }
        }

        // Tìm kiếm chứa (partial match)
        String lowerTitle = normalizedTitle.toLowerCase();
        for (Map.Entry<String, String> entry : RECIPE_LINKS.entrySet()) {
            String keyLower = entry.getKey().toLowerCase();
            if (lowerTitle.contains(keyLower) || keyLower.contains(lowerTitle)) {
                return entry.getValue();
            }
        }

        // Tìm kiếm bằng so khớp bỏ dấu
        for (Map.Entry<String, String> entry : RECIPE_LINKS.entrySet()) {
            String keyNoAccent = normalize(entry.getKey());
            if (keyNoAccent.equals(normalizedNoAccent)) {
                return entry.getValue();
            }
        }

        // Partial match bỏ dấu
        for (Map.Entry<String, String> entry : RECIPE_LINKS.entrySet()) {
            String keyNoAccent = normalize(entry.getKey());
            if (normalizedNoAccent.contains(keyNoAccent) || keyNoAccent.contains(normalizedNoAccent)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Kiểm tra xem công thức có link hardcode không.
     *
     * @param recipeTitle Tên công thức
     * @return true nếu có link hardcode, false nếu không
     */
    public static boolean hasHardcodedLink(String recipeTitle) {
        return getShareLinkForRecipe(recipeTitle) != null;
    }

    /**
     * Thêm link mới cho công thức (có thể dùng để mở rộng).
     *
     * @param recipeTitle Tên công thức
     * @param shareLink   Link chia sẻ
     */
    public static void addRecipeLink(String recipeTitle, String shareLink) {
        if (recipeTitle != null && shareLink != null) {
            RECIPE_LINKS.put(recipeTitle.trim(), shareLink.trim());
        }
    }
}


