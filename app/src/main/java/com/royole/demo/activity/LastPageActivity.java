package com.royole.demo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.royole.demo.R;
import com.royole.demo.view.LeftPageView;

/**
 * @author HZLI02
 * @date 2018/8/16
 */

public class LastPageActivity extends AppCompatActivity {
    private Button btn_next;
    private float turnPageStartY;
    private int turnPageMode;
    private LeftPageView leftPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_page);
        bindView();
        turnPage();
    }

    private void bindView(){
        leftPage = findViewById(R.id.left_page);
        btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftPage.turnLeft(turnPageStartY,turnPageMode,null);
            }
        });
    }

    private void turnPage(){
        Bundle bundle = this.getIntent().getExtras();
        turnPageStartY = bundle.getFloat("StartY");
        turnPageMode = bundle.getInt("TurnPageMode");
    }
}
