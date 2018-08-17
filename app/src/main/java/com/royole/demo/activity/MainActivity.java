package com.royole.demo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.royole.demo.R;
import com.royole.demo.view.RightPageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RightPageView rightPageView;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindView();
    }

    private void bindView(){
        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        rightPageView = findViewById(R.id.right_page);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_next:
                Intent intent = new Intent(MainActivity.this, LastPageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("TurnPageMode",rightPageView.turnPageMode2);
                bundle.putFloat("StartY",rightPageView.postAHeight);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        super.onTouchEvent(event);
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                Log.i("###touch","down");
//                break;
//            case MotionEvent.ACTION_MOVE:
//                Log.i("###touch","move");
//                break;
//            case MotionEvent.ACTION_UP:
//                Log.i("###touch","up");
//                break;
//            default:
//                break;
//        }
//        return true;
//    }
}
