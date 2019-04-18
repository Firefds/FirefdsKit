package sb.firefds.pie.firefdskit.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import sb.firefds.pie.firefdskit.FirefdsKitActivity;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(getApplicationContext(),
                FirefdsKitActivity.class);
        startActivity(intent);
        finish();
    }
}
