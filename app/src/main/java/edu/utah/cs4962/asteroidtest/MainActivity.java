package edu.utah.cs4962.asteroidtest;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ImageView title = new ImageView(this);
        Button newGameButton = new Button(this);
        ImageView background = new ImageView(this);
        Button resumeGameButton = new Button(this);
        Button topScoresButton = new Button(this);

        super.onCreate(savedInstanceState);

        RelativeLayout layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        title.setImageResource(R.drawable.logo);
        RelativeLayout.LayoutParams titleLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLayout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        titleLayout.addRule(RelativeLayout.CENTER_HORIZONTAL);

        newGameButton.setText("Play");
        newGameButton.setId(View.generateViewId());
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        resumeGameButton.setText("Resume Game");
        RelativeLayout.LayoutParams resumeButtonLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resumeButtonLayout.addRule(RelativeLayout.RIGHT_OF, newGameButton.getId());
        resumeButtonLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        topScoresButton.setText("High Scores");
        RelativeLayout.LayoutParams topScoresLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        topScoresLayout.addRule(RelativeLayout.LEFT_OF, newGameButton.getId());
        topScoresLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);


//        background.setImageResource(R.drawable.space);
        layout.addView(background, layoutParams);
        layout.addView(title, titleLayout);
        layout.addView(newGameButton, buttonParams);
        layout.addView(resumeGameButton, resumeButtonLayout);
        layout.addView(topScoresButton, topScoresLayout);

        setContentView(layout);

        newGameButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent playIntent = new Intent(v.getContext(), GameActivity.class);
                startActivity(playIntent);
            }
        });
    }
}
