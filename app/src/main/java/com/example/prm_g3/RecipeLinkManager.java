package com.example.prm_g3;

import java.util.HashMap;
import java.util.Map;

/**
 * Quản lý link chia sẻ được hardcode cho các công thức cụ thể
 */
public class RecipeLinkManager {
    
    private static final Map<String, String> RECIPE_LINKS = new HashMap<>();
    
    static {
        // Hardcode link cho các công thức cụ thể
        RECIPE_LINKS.put("Phở bò Hà Nội", "https://get-qr.com/content/Zs_P8y");
        RECIPE_LINKS.put("Bánh mì thịt nướng", "https://get-qr.com/content/4ct8VS");
        
        // Có thể thêm các công thức khác ở đây
        // RECIPE_LINKS.put("Tên công thức", "https://get-qr.com/content/XXXXXX");
    }
    
    /**
     * Lấy link chia sẻ cho công thức dựa trên tên công thức
     * @param recipeTitle Tên công thức
     * @return Link chia sẻ nếu có, null nếu không tìm thấy
     */
    public static String getShareLinkForRecipe(String recipeTitle) {
        if (recipeTitle == null || recipeTitle.trim().isEmpty()) {
            return null;
        }
        
        // Tìm kiếm chính xác
        String link = RECIPE_LINKS.get(recipeTitle.trim());
        if (link != null) {
            return link;
        }
        
        // Tìm kiếm không phân biệt hoa thường
        for (Map.Entry<String, String> entry : RECIPE_LINKS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(recipeTitle.trim())) {
                return entry.getValue();
            }
        }
        
        // Tìm kiếm chứa (partial match)
        for (Map.Entry<String, String> entry : RECIPE_LINKS.entrySet()) {
            if (recipeTitle.trim().toLowerCase().contains(entry.getKey().toLowerCase()) ||
                entry.getKey().toLowerCase().contains(recipeTitle.trim().toLowerCase())) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    /**
     * Kiểm tra xem công thức có link hardcode không
     * @param recipeTitle Tên công thức
     * @return true nếu có link hardcode, false nếu không
     */
    public static boolean hasHardcodedLink(String recipeTitle) {
        return getShareLinkForRecipe(recipeTitle) != null;
    }
    
    /**
     * Thêm link mới cho công thức (có thể dùng để mở rộng)
     * @param recipeTitle Tên công thức
     * @param shareLink Link chia sẻ
     */
    public static void addRecipeLink(String recipeTitle, String shareLink) {
        if (recipeTitle != null && shareLink != null) {
            RECIPE_LINKS.put(recipeTitle.trim(), shareLink.trim());
        }
    }
}

