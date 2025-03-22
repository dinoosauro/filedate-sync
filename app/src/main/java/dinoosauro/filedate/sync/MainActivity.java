package dinoosauro.filedate.sync;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    public boolean isInApplySection = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());
        setContentView(R.layout.activity_main);
        EditText text = findViewById(R.id.path);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        TextView welcome = findViewById(R.id.welcomeStr);
        TextView instructions = findViewById(R.id.pickerInstructions);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.apply_menu) {
                isInApplySection = true;
                welcome.setText(getApplicationContext().getString(R.string.applyMainStr));
                instructions.setText(getApplicationContext().getString(R.string.applyPickStr));
                return true;
            } else if (item.getItemId() == R.id.generate_menu) {
                welcome.setText(getApplicationContext().getString(R.string.createMainStr));
                instructions.setText(getApplicationContext().getString(R.string.createPickStr));
                isInApplySection = false;
                return true;
            }
            return false;
        });


        LinearLayout infoLayout = findViewById(R.id.infoContainer);

        ActivityResultLauncher<Intent> saveFile = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                String path = text.getText().toString();
                File mainFolder = new File(path);
                if (mainFolder.exists()) {
                    StringBuilder output = new StringBuilder();
                    ArrayList<File> files = recursiveRead(mainFolder);
                    for (File file : files) {
                        Log.d("CurrentFile", file.getPath());
                        output.append(file.lastModified()).append(" ").append(file.getPath().replace(path, "").substring(1)).append("\n");
                    }
                    try {
                        ContentResolver resolver = getContentResolver();
                        OutputStream outputStream = resolver.openOutputStream(result.getData().getData());
                        if (outputStream != null) {
                            String writeThis = output.toString();
                            outputStream.write(writeThis.substring(0, writeThis.length() - 1).getBytes(StandardCharsets.UTF_8));
                            outputStream.close();
                            TextView successfulInfo = new TextView(infoLayout.getContext());
                            successfulInfo.setText("Written modified date file to " + result.getData().getData().getPath());
                            infoLayout.addView(successfulInfo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        ActivityResultLauncher<Intent> folderResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
           if (result.getResultCode() == Activity.RESULT_OK) {
               String uriPath = result.getData().getData().getPath();  // Example: /tree/primary:MyFolder
               if (uriPath != null && uriPath.startsWith("/tree/primary:")) {
                   String relativePath = uriPath.replace("/tree/primary:", "");
                   text.setText(Environment.getExternalStorageDirectory() + "/" + relativePath);
               }

           }
        });
        findViewById(R.id.folderPicker).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            folderResult.launch(intent);
        });
        ActivityResultLauncher<Intent> fileResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Uri uri = result.getData().getData();
                InputStream inputStream = null;
                try {
                    String path = text.getText().toString();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    formatter.setTimeZone(TimeZone.getDefault()); // Use system timezone
                    inputStream = getContentResolver().openInputStream(uri);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        long millis = Long.parseLong(line.substring(0, line.indexOf(" ")));
                        String filePath = line.substring(line.indexOf(" ") + 1);
                        File file = new File(path + "/" + filePath);
                        if (file.exists()) {
                            File file1 = new File(file.getPath());
                            boolean results = file1.setLastModified(millis);
                            TextView successfulInfo = new TextView(infoLayout.getContext());
                            successfulInfo.setText((results ? "Successful" : "Unsuccessful") + " edit of " + filePath + " to " + formatter.format(new Date(millis)));
                            infoLayout.addView(successfulInfo);
                        }
                    }
                    reader.close();
                    inputStream.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        findViewById(R.id.filePicker).setOnClickListener(view -> {
            if (!isInApplySection) {
                Intent create = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                create.addCategory(Intent.CATEGORY_OPENABLE);
                create.setType("text/plain"); // Set file type (change as needed)
                String suggestedPath = text.getText().toString();
                create.putExtra(Intent.EXTRA_TITLE, suggestedPath.substring(suggestedPath.lastIndexOf("/") + 1) + ".txt");
                saveFile.launch(create);
                return;
            }
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            fileResult.launch(chooseFile);
        });
    }

    ArrayList<File> recursiveRead(File file) {
        ArrayList<File> output = new ArrayList<>();
        if (!file.isDirectory()) return output;
        for (File newFiles : file.listFiles()) {
            if (newFiles.isDirectory()) output.addAll(recursiveRead(newFiles)); else output.add(newFiles);
        }
        return output;
    }


}