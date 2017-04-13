package com.ojsb.guessinggame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText etGuess;
    private Button btnGuess;
    private TextView textHint;
    private int target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etGuess = (EditText) findViewById(R.id.etGuess);
        btnGuess = (Button) findViewById(R.id.btnGuess);
        textHint = (TextView) findViewById(R.id.textHint);
        startGame();
        btnGuess.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkGuess();
                    }
                }
        );
    }

    private void startGame(){
        target = (int) (Math.random() * 100 + 1);
        etGuess.setText("");
    }

    private void checkGuess(){
        String guessString = etGuess.getText().toString();
        int guess = 0;
        try {
            guess = Integer.parseInt(guessString);
        }catch(Exception e){
            textHint.setText("Must enter a valid number");
            return;
        }

        if (guess > target) {
            textHint.setText("Too hight, try again!");
        }else if (guess < target){
            textHint.setText("Too low, try again!");
        }else{
            textHint.setText("Correct. You win! Play agin!");
            startGame();
        }
    }
}
