package com.example.prm_g3;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prm_g3.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class ShareRecipeDialog extends Dialog {

    private Context context;
    private String recipeId;
    private String recipeTitle;
    private String shareLink;

    private ImageView imgQRCode;
    private TextView tvShareLink;
    private TextView tvRecipeTitle;
    private ImageButton btnCopyLink;
    private Button btnShareViaApp;
    private Button btnClose;

    public ShareRecipeDialog(Context context, String recipeId, String recipeTitle) {
        super(context);
        this.context = context;
        this.recipeId = recipeId;
        this.recipeTitle = recipeTitle;
        resolveShareLink();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_share_recipe);

        initViews();
        setupClickListeners();
        generateQRCode();
        updateUI();
    }

    private void initViews() {
        imgQRCode = findViewById(R.id.imgQRCode);
        tvShareLink = findViewById(R.id.tvShareLink);
        tvRecipeTitle = findViewById(R.id.tvRecipeTitle);
        btnCopyLink = findViewById(R.id.btnCopyLink);
        btnShareViaApp = findViewById(R.id.btnShareViaApp);
        btnClose = findViewById(R.id.btnClose);
    }

    private void setupClickListeners() {
        btnCopyLink.setOnClickListener(v -> copyLinkToClipboard());

        btnShareViaApp.setOnClickListener(v -> shareViaIntent());

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void updateUI() {
        String titleToDisplay = recipeTitle != null && !recipeTitle.trim().isEmpty()
                ? recipeTitle.trim()
                : context.getString(R.string.app_name);
        tvRecipeTitle.setText(titleToDisplay);
        tvShareLink.setText(shareLink);
    }

    private void generateQRCode() {
        try {
            if (shareLink == null || shareLink.trim().isEmpty()) {
                android.util.Log.w("ShareRecipeDialog", "shareLink is empty, skip QR generation");
                Toast.makeText(context, "Không có link để tạo mã QR", Toast.LENGTH_SHORT).show();
                imgQRCode.setImageBitmap(null);
                return;
            }
            android.util.Log.d("ShareRecipeDialog", "Generating QR code for: " + shareLink);
            int width = 800;
            int height = 800;
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(shareLink, BarcodeFormat.QR_CODE, width, height, hints);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            imgQRCode.setImageBitmap(bitmap);
            android.util.Log.d("ShareRecipeDialog", "QR code generated successfully");
        } catch (WriterException e) {
            android.util.Log.e("ShareRecipeDialog", "Error generating QR code: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(context, "Lỗi tạo mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void copyLinkToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Recipe Link", shareLink);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Đã sao chép link", Toast.LENGTH_SHORT).show();
    }

    private void shareViaIntent() {
        String titleForShare = recipeTitle != null && !recipeTitle.trim().isEmpty()
                ? recipeTitle.trim()
                : context.getString(R.string.app_name);
        String shareText = titleForShare + "\n\n" + shareLink;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ công thức: " + titleForShare);
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ công thức"));
        dismiss();
    }

    private void resolveShareLink() {
        android.util.Log.d("ShareRecipeDialog", "resolveShareLink - recipeId: " + recipeId + ", recipeTitle: " + recipeTitle);
        String hardcodedLink = RecipeLinkManager.getShareLinkForRecipe(recipeTitle);
        android.util.Log.d("ShareRecipeDialog", "Hardcoded link from RecipeLinkManager: " + hardcodedLink);
        if (hardcodedLink != null && !hardcodedLink.trim().isEmpty()) {
            shareLink = hardcodedLink.trim();
        } else {
            // Fallback deep link nếu không có link hardcode
            shareLink = "prmrecipe://recipe/" + recipeId;
        }
    }
}

