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
    private String qrCodeUrl;  // URL của mã QR đã lưu (nếu có)

    private ImageView imgQRCode;
    private TextView tvShareLink;
    private TextView tvRecipeTitle;
    private ImageButton btnCopyLink;
    private Button btnDownloadQR;
    private Button btnShareViaApp;
    private Button btnClose;

    public ShareRecipeDialog(Context context, String recipeId, String recipeTitle) {
        super(context);
        this.context = context;
        this.recipeId = recipeId;
        this.recipeTitle = recipeTitle;
        // Tạo deep link cho công thức (mặc định)
        this.shareLink = "prmrecipe://recipe/" + recipeId;
        this.qrCodeUrl = null;
    }

    public ShareRecipeDialog(Context context, String recipeId, String recipeTitle, String qrCodeUrl) {
        super(context);
        this.context = context;
        this.recipeId = recipeId;
        this.recipeTitle = recipeTitle;
        // Tạo deep link cho công thức (mặc định)
        this.shareLink = "prmrecipe://recipe/" + recipeId;
        this.qrCodeUrl = qrCodeUrl;
    }

    public ShareRecipeDialog(Context context, String recipeId, String recipeTitle, String qrCodeUrl, String shareLink) {
        super(context);
        this.context = context;
        this.recipeId = recipeId;
        this.recipeTitle = recipeTitle;
        // Sử dụng share_link tùy chỉnh nếu có, nếu không thì dùng deep link
        this.shareLink = (shareLink != null && !shareLink.isEmpty()) ? shareLink : "prmrecipe://recipe/" + recipeId;
        this.qrCodeUrl = qrCodeUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_share_recipe);

        initViews();
        setupClickListeners();
        loadQRCode();
        updateUI();
    }

    private void initViews() {
        imgQRCode = findViewById(R.id.imgQRCode);
        tvShareLink = findViewById(R.id.tvShareLink);
        tvRecipeTitle = findViewById(R.id.tvRecipeTitle);
        btnCopyLink = findViewById(R.id.btnCopyLink);
        btnDownloadQR = findViewById(R.id.btnDownloadQR);
        btnShareViaApp = findViewById(R.id.btnShareViaApp);
        btnClose = findViewById(R.id.btnClose);
    }

    private void setupClickListeners() {
        btnCopyLink.setOnClickListener(v -> copyLinkToClipboard());

        btnDownloadQR.setOnClickListener(v -> downloadQRCode());

        btnShareViaApp.setOnClickListener(v -> shareViaIntent());

        btnClose.setOnClickListener(v -> dismiss());
    }

    private void updateUI() {
        tvRecipeTitle.setText(recipeTitle);
        tvShareLink.setText(shareLink);
    }

    private void loadQRCode() {
        // Nếu có QR code đã lưu, load từ URL
        if (qrCodeUrl != null && !qrCodeUrl.isEmpty()) {
            android.util.Log.d("ShareRecipeDialog", "Loading saved QR code from: " + qrCodeUrl);
            com.bumptech.glide.Glide.with(context)
                    .load(qrCodeUrl)
                    .into(imgQRCode);
        } else {
            // Nếu không có, tạo mã QR mới từ link
            generateQRCode();
        }
    }

    private void generateQRCode() {
        try {
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

    private void downloadQRCode() {
        try {
            // Lấy bitmap từ ImageView
            imgQRCode.setDrawingCacheEnabled(true);
            imgQRCode.buildDrawingCache();
            Bitmap qrBitmap = imgQRCode.getDrawingCache();
            
            if (qrBitmap == null) {
                Toast.makeText(context, "Không thể tải mã QR", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Lưu vào MediaStore (Android 10+)
            String fileName = "QR_" + recipeTitle.replaceAll("[^a-zA-Z0-9]", "_") + "_" + System.currentTimeMillis() + ".png";
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/PRM_Recipe");
            
            android.content.ContentResolver resolver = context.getContentResolver();
            android.net.Uri uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            
            if (uri != null) {
                try {
                    java.io.OutputStream outputStream = resolver.openOutputStream(uri);
                    if (outputStream != null) {
                        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.close();
                        Toast.makeText(context, "Đã lưu mã QR vào thư mục Pictures/PRM_Recipe", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("ShareRecipeDialog", "Error saving QR code: " + e.getMessage(), e);
                    Toast.makeText(context, "Lỗi lưu mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Không thể lưu mã QR", Toast.LENGTH_SHORT).show();
            }
            
            imgQRCode.setDrawingCacheEnabled(false);
        } catch (Exception e) {
            android.util.Log.e("ShareRecipeDialog", "Error downloading QR code: " + e.getMessage(), e);
            Toast.makeText(context, "Lỗi tải mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareViaIntent() {
        String shareText = recipeTitle + "\n\n" + shareLink;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ công thức: " + recipeTitle);
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ công thức"));
        dismiss();
    }
}

