package com.ojsb.caesarcipher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    private EditText etInput;
    private EditText etOutput;
    private EditText etKey;
    private SeekBar sbKey;
    private Button btEncode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etInput = (EditText) findViewById(R.id.etInput);
        etOutput = (EditText) findViewById(R.id.etOutput);
        etKey = (EditText) findViewById(R.id.etKey);
        sbKey = (SeekBar) findViewById(R.id.sbKey);
        btEncode = (Button) findViewById(R.id.btnEncode);

        sbKey.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                etKey.setText("" + (progress - 13));
                encode();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btEncode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encode();
            }
        });
    }

    private void encode(){
        int key = Integer.parseInt( etKey.getText().toString());
        String input = etInput.getText().toString();
        String output = "";
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c = (char)(c + key);
                while (c > 'Z') c = (char)( c - 26);
                while (c < 'A') c = (char)( c + 26);
            }else if (c >= 'a' && c <= 'z') {
                c = (char)(c + key);
                while (c > 'z') c = (char)( c - 26);
                while (c < 'a') c = (char)( c + 26);
            }
            output += c;
        }
        etOutput.setText(output);
    }
}
