package edu.utah.cs4962.asteroidtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameActivity extends AppCompatActivity implements GLSurfaceView.Renderer
{
    HashMap<String, Bitmap> bitmapHashMap = new HashMap<String, Bitmap>();
    private Button leftArrow;
    private Button rightArrow;
    private Button fireButton;
    private Button thrustButton;
    Timer timer;
    TimerTask timerTask;
    boolean firstPass = true;
    boolean leftButtonPressed = false;
    boolean rightButtonPressed = false;
    boolean thrustButtonPressed = false;
    boolean fireButtonPressed = false;
    boolean hasCollided = false;
    float startOfMissleX = 0.0f;
    float startOfMissleY = 0.0f;
    float shipX = 0.0f;
    float shipY = 0.0f;
    final float MISSLE_SPEED = 0.05f;
    final float ASTEROID_SPEED = 0.009f;
    Date _lastTime = new Date();
    Sprite ship;
    Sprite missle;
    Sprite smallAsteroid;
    Sprite explosion;
    Sprite mediumAsteroid;
    Sprite largeAsteroid;
    SoundPool soundEffects;
    int lasershot;
    int explosionSound;
    int jetEngineSound;
    int jetEngineSoundStop;
    boolean shipHasBeenHit = false;
    int lives = 3;
    ArrayList<Sprite> asteroidSprites = new ArrayList<Sprite>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        soundEffects = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        lasershot = soundEffects.load(getApplicationContext(), R.raw.lasersound, 1);
        explosionSound = soundEffects.load(getApplicationContext(), R.raw.explosion, 2);
        jetEngineSound = soundEffects.load(getApplicationContext(), R.raw.afterburner, 1);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        thrustButton = new Button(this);
        leftArrow = new Button(this);
        rightArrow = new Button(this);
        fireButton = new Button(this);
        fireButton.setId(View.generateViewId());
        rightArrow.setId(View.generateViewId());
        leftArrow.setId(View.generateViewId());
        thrustButton.setId(View.generateViewId());
        GLSurfaceView glSurfaceView = new GLSurfaceView(this);

        RelativeLayout layout = new RelativeLayout(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout buttonLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams fireButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //Places the fire missle button
        fireButtonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        fireButtonParams.addRule(RelativeLayout.RIGHT_OF, leftArrow.getId());
        fireButton.setText("FIRE");
        buttonLayout.addView(fireButton, fireButtonParams);
        //Places the right arrow
        RelativeLayout.LayoutParams rightArrowParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rightArrowParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightArrowParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rightArrow.setText("RIGHT");
        buttonLayout.addView(rightArrow, rightArrowParams);
        //Places the left arrow
        RelativeLayout.LayoutParams leftArrowParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        leftArrowParams.addRule(RelativeLayout.ALIGN_LEFT);
        leftArrowParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        leftArrow.setText("LEFT");
        buttonLayout.addView(leftArrow, leftArrowParams);
        //Up arrow button
        RelativeLayout.LayoutParams upArrowParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        upArrowParams.addRule(RelativeLayout.LEFT_OF, rightArrow.getId());
        upArrowParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        thrustButton.setText("THRUSTER");
        buttonLayout.addView(thrustButton, upArrowParams);


        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        glSurfaceView.setRenderer(this);
        layout.addView(glSurfaceView);
        layout.addView(buttonLayout, lp);
        setContentView(layout);


        //If the left button is pressed rotate left
        leftArrow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        leftButtonPressed = true;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        leftButtonPressed = false;
                        break;
                    }
                }
                return true;
            }
        });

        //If the right button is pressed rotate right
        rightArrow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        rightButtonPressed = true;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        rightButtonPressed = false;
                        break;
                    }
                }
                return true;
            }
        });


        //If the thrust button is pressed find where the top point is and
        //thrust in the opposite direction
        thrustButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                    {
                        jetEngineSoundStop = soundEffects.play(jetEngineSound, 1.0f, 1.0f, 0, 0, 1.5f);
                        thrustButtonPressed = true;
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    {
                        soundEffects.stop(jetEngineSoundStop);
                        thrustButtonPressed = false;
                        break;
                    }
                }
                return true;
            }
        });

        //If the fire button is pressed find where the top of the triangle is,
        //then create a square missle and translate it in the direction where the
        //top of the triangle is pointing
        fireButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {

                        soundEffects.play(lasershot, 1.0f, 1.0f, 0, 0, 1.5f);
                        fireButtonPressed = true;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        fireButtonPressed = false;
                        break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);


        //Create a small asteroid
        smallAsteroid = new Sprite();
        smallAsteroid.set_centerX(0.7f);
        smallAsteroid.set_centerY(0.4f);
        smallAsteroid.set_width(0.2f);
        smallAsteroid.set_height(0.2f);
        smallAsteroid.setBounds(new BoundingCircle(smallAsteroid.get_centerX(), smallAsteroid.get_centerY(), (smallAsteroid.get_width() / 2)));
//        bitmapHashMap.put("smallAsteroidBitmap", BitmapFactory.decodeResource(getResources(), R.drawable.mediumasteroid));
        smallAsteroid.setTexture(BitmapFactory.decodeResource(getResources(), R.drawable.smallasteroid));
        asteroidSprites.add(smallAsteroid);

        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                timer = new Timer();
                Handler handler = new Handler();

                timerTask = new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        createAsteroids();
                    }
                };
                timer.schedule(timerTask, 0, 6000);
            }
        });

        //Create the ship sprite
        ship = new Sprite();
        ship.set_centerX(0.0f);
        ship.set_centerY(0.0f);
        ship.set_width(0.2f);
        ship.set_height(0.2f);
        ship.setBounds(new BoundingCircle(ship.get_centerX(), ship.get_centerY(), (ship.get_width() / 2)));
        ship.set_textureId(setShipTexture());


        //Create the missle sprite
        missle = new Sprite();
//        missle.set_centerX(-0.5f);
//        missle.set_centerY(-0.3f);
        missle.set_width(0.05f);
        missle.set_height(0.05f);
        missle.setBounds(new BoundingCircle(missle.get_centerX(), missle.get_centerY(), (missle.get_width() / 2)));
        bitmapHashMap.put("bulletBitmap", BitmapFactory.decodeResource(getResources(), R.drawable.bullet));
        missle.setTexture(bitmapHashMap.get("bulletBitmap"));



        mediumAsteroid = new Sprite();
        mediumAsteroid.set_centerX(-0.5f);
        mediumAsteroid.set_centerY(0.0f);
        mediumAsteroid.set_width(0.25f);
        mediumAsteroid.set_height(0.25f);
//        mediumAsteroid.setBounds(new BoundingCircle(mediumAsteroid.get_centerX(), mediumAsteroid.get_centerY(), (mediumAsteroid.get_width() / 2)));
        bitmapHashMap.put("mediumAsteroidBitmap", BitmapFactory.decodeResource(getResources(), R.drawable.mediumasteroid));
        mediumAsteroid.setTexture(bitmapHashMap.get("mediumAsteroidBitmap"));
//        asteroidSprites.add(mediumAsteroid);

        largeAsteroid = new Sprite();
        largeAsteroid.set_centerX(0.0f);
        largeAsteroid.set_centerY(0.5f);
        largeAsteroid.set_width(0.3f);
        largeAsteroid.set_height(0.3f);
//        largeAsteroid.setBounds(new BoundingCircle(largeAsteroid.get_centerX(), largeAsteroid.get_centerY(), (largeAsteroid.get_width() / 2)));
        bitmapHashMap.put("largeAsteroidBitmap", BitmapFactory.decodeResource(getResources(), R.drawable.bigasteroid));
        largeAsteroid.setTexture(bitmapHashMap.get("largeAsteroidBitmap"));
//        asteroidSprites.add(largeAsteroid);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        if (width < height)
            GLES20.glViewport((width - height) / 2, 0, height, height);
        else
            GLES20.glViewport(0, (height - width) / 2, width, width);
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);



        if(lives != 0)
        {
            //If the ship has been hit we want to erase the ship and place it back
            //in the middle of the screen blinking three times before anything starts
            //Check to see if there is an asteroid there.  If there is move it before
            //placing the ship
            if (shipHasBeenHit)
            {
                ship.getBounds().setCenter(0.0f, 0.0f);

                                        for (int i = 0; i < asteroidSprites.size(); i++)
                                        {
                                            while (BoundingCircle.overlapCircles(ship.getBounds(), asteroidSprites.get(i).getBounds())) {
                                                asteroidSprites.get(i).set_centerX(asteroidSprites.get(i).get_centerX() + ASTEROID_SPEED);
                                                asteroidSprites.get(i).set_centerY(asteroidSprites.get(i).get_centerY() + ASTEROID_SPEED);
                                                asteroidSprites.get(i).getBounds().setCenter(asteroidSprites.get(i).get_centerX(), asteroidSprites.get(i).get_centerY());
                                            }
                                        }
                                        ship.set_centerX(0.0f);
                                        ship.set_centerY(0.0f);
                                        shipHasBeenHit = false;
            }

            //Before anything check if anything has collided
            //-----------------------------------------
            //-----------------------------------------
            //Iterate over all the asteroid sprites on the board and see if any of them have been hit by the bullet
            for (int i = 0; i < asteroidSprites.size(); i++)
            {
                //If an asteroid has collided wiht the ship explode the ship
                if (BoundingCircle.overlapCircles(ship.getBounds(), asteroidSprites.get(i).getBounds())) {
                    //Time
                    lives--;
                    shipHasBeenHit = true;
                    Date date = new Date();
                    long now = date.getTime();
                    if (_lastTime == null)
                        _lastTime = date;
                    long elapsed = now - _lastTime.getTime();
                    _lastTime = date;
                    float elapsedSeconds = (float) elapsed / 1000.0f;

                    //Create explosion animation sprite
                    explosion = new Sprite();
                    explosion.set_centerX(ship.get_centerX());
                    explosion.set_centerY(ship.get_centerY());
                    explosion.set_height(0.3f);
                    explosion.set_width(0.3f);

                    explosion.setTexture(BitmapFactory.decodeResource(getResources(), R.drawable.explosion));
                    explosion.set_subTextureColumnCount(5);
                    explosion.set_subTextureRowCount(5);
                    explosion.set_subTextureIndex(0);
                    explosion.set_subTextureIndex(explosion.get_subTextureIndex() + elapsedSeconds / 0.1f);
                    soundEffects.play(explosionSound, 1.0f, 1.0f, 0, 0, 1.5f);
                    explosion.draw();
                }
                else if (BoundingCircle.overlapCircles(missle.getBounds(), asteroidSprites.get(i).getBounds())) {
//                hasCollided = true;
                    //Time
                    Date date = new Date();
                    long now = date.getTime();
                    if (_lastTime == null)
                        _lastTime = date;
                    long elapsed = now - _lastTime.getTime();
                    _lastTime = date;
                    float elapsedSeconds = (float) elapsed / 1000.0f;

                    //Create explosion animation sprite
                    explosion = new Sprite();
                    explosion.set_centerX(asteroidSprites.get(i).get_centerX());
                    explosion.set_centerY(asteroidSprites.get(i).get_centerY());
                    explosion.set_height(0.3f);
                    explosion.set_width(0.3f);

                    explosion.setTexture(BitmapFactory.decodeResource(getResources(), R.drawable.explosion));
                    explosion.set_subTextureColumnCount(5);
                    explosion.set_subTextureRowCount(5);
                    explosion.set_subTextureIndex(0);
                    explosion.set_subTextureIndex(explosion.get_subTextureIndex() + elapsedSeconds / 0.1f);
                    soundEffects.play(explosionSound, 1.0f, 1.0f, 0, 0, 1.5f);
                    explosion.draw();
                    asteroidSprites.remove(i);

                    Log.d("missle", "missle hit asteroid!!!!!");
                }
                else if (!BoundingCircle.overlapCircles(missle.getBounds(), asteroidSprites.get(i).getBounds()) /*&& hasCollided == false*/)
                {
//                    asteroidSprites.get(i).set_centerX(asteroidSprites.get(i).get_centerX() + 0.02f * ASTEROID_SPEED);
                    asteroidSprites.get(i).set_centerY(asteroidSprites.get(i).get_centerY() + -0.4f * ASTEROID_SPEED);
                    asteroidSprites.get(i).getBounds().setCenter(asteroidSprites.get(i).get_centerX(), asteroidSprites.get(i).get_centerY());
                    asteroidSprites.get(i).setTexture(BitmapFactory.decodeResource(getResources(), R.drawable.smallasteroid));
                    asteroidSprites.get(i).draw();
                    asteroidSprites.get(i).set_rotate(asteroidSprites.get(i).get_rotate() + 0.25f);
                    Log.d("missle", "not hit!!");
                }
            }

            //-----------------------------------------
            //-----------------------------------------

            //Now check which button is pressed and respond with appropriate action
            //-----------------------------------------
            //-----------------------------------------

            if (leftButtonPressed == true) {
                ship.set_rotate(ship.get_rotate() + 9.0f);
            } else if (rightButtonPressed == true) {
                ship.set_rotate(ship.get_rotate() - 9.0f);
            } else if (fireButtonPressed == true) {
                //Find how much the rotation of the ship is to tell you where the front of the
                //ship is.  Front starts at zero
                //Find roatation and then rotate the missle sprite after it is center x and y
                //missle.set_rotate(rotation);
                //Find where the ships center x and y coordinates are
                //set the missleSprites centerX and centerY at this location add 0.5 to center x to put it at the front of the ship
                firstPass = false;
                missle.set_centerX(ship.get_centerX() + (ship.get_width() * 0.5f) * (float) Math.cos(ship.get_rotate() / 180.0f * Math.PI));
                missle.set_centerY(ship.get_centerY() + (ship.get_width() * 0.5f) * (float) Math.sin(ship.get_rotate() / 180.0f * Math.PI));
                missle.getBounds().center.set(missle.get_centerX(), missle.get_centerY());
                // TODO: do what matt said and change it so the bullet is not finding the ship's middle after the first time
                //Then translate in the direction the ship is pointing
            } else if (thrustButtonPressed == true) {
                ship.set_textureId(setShipWithFireTexture());
                moveShip();
            }

            //-----------------------------------------
            //-----------------------------------------

            //Draw ship ** Will always do this regardless
            if (!shipHasBeenHit)
            {
                ship.set_textureId(setShipTexture());
                ship.draw();
            }

            //This traslates the bullet along it's course after fired
            if (!firstPass)
                fireBullet();
        }
        else
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("GAME OVER");
                    builder.setMessage("GAME OVER, thanks for playing");

                    builder.setNegativeButton("Go to start screen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent playIntent = new Intent(GameActivity.this, MainActivity.class);
                            startActivity(playIntent);
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

        }

    }

    public int setShipTexture()
    {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        int textureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, BitmapFactory.decodeResource(getResources(), R.drawable.fighter), 0);
        return textureId;
    }

        public int setShipWithFireTexture()
        {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        int textureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, BitmapFactory.decodeResource(getResources(), R.drawable.fighterwithfire), 0);
        return textureId;
    }

    public void fireBullet()
    {
        startOfMissleX = (float) (Math.cos(ship.get_rotate() * (Math.PI / 180)) * MISSLE_SPEED);
        startOfMissleY = (float) (Math.sin(ship.get_rotate() * (Math.PI / 180)) * MISSLE_SPEED);

        missle.set_centerX(missle.get_centerX() + startOfMissleX);
        missle.set_centerY(missle.get_centerY() + startOfMissleY);
        missle.getBounds().center.set(missle.get_centerX(), missle.get_centerY());
        missle.draw();
    }

    public void moveShip()
    {
        shipX = (float) (Math.cos(ship.get_rotate() * (Math.PI / 180)) * MISSLE_SPEED);
        shipY = (float) (Math.sin(ship.get_rotate() * (Math.PI / 180)) * MISSLE_SPEED);
        //The ship hits the right side of the screen
        if (ship.get_centerX() + (ship.get_width() * 0.5f) * (float) Math.cos(ship.get_rotate() / 180.0f * Math.PI)  >= 1)
        {
            ship.set_centerX(ship.get_centerX());
            ship.getBounds().center.set(ship.get_centerX(), ship.get_centerY());
        }
        //The ship hits the top side of the screen
        else if((ship.get_centerY() + (ship.get_width() * 0.5f) * (float) Math.sin(ship.get_rotate() / 180.0f * Math.PI)) >= 0.55)
        {
            ship.set_centerY(ship.get_centerY());
            ship.getBounds().center.set(ship.get_centerX(), ship.get_centerY());

        }
        //The ship hits the left side of the screen
        else if(ship.get_centerX() + (ship.get_width() * 0.5f) * (float) Math.cos(ship.get_rotate() / 180.0f * Math.PI) <= -1)
        {
            ship.set_centerX(ship.get_centerX());
            ship.getBounds().center.set(ship.get_centerX(), ship.get_centerY());
        }
        //The ship hits the bottom side of the screen
        else if(ship.get_centerY() + (ship.get_width() * 0.5f) * (float) Math.sin(ship.get_rotate() / 180.0f * Math.PI) <= -0.55)
        {
            ship.set_centerY(ship.get_centerY());
            ship.getBounds().center.set(ship.get_centerX(), ship.get_centerY());

        }
        else
        {
            ship.set_centerX(ship.get_centerX() + shipX);
            ship.set_centerY(ship.get_centerY() + shipY);
            ship.getBounds().center.set(ship.get_centerX(), ship.get_centerY());
        }

        ship.draw();
    }

    public void createAsteroids()
    {

        Sprite tempSprite = new Sprite();
        float heightOrWidth = getSizeOfAsteroidSprite();
        tempSprite.set_width(heightOrWidth);
        tempSprite.set_height(heightOrWidth);
        //Next generate the x and y of where the asteroid will start and get the negative and positive aspects
        //next get a positive or negative for that number
        int xOrYAxis = xOrYAxis();
        if(xOrYAxis == 1)
            tempSprite.set_centerX(placeOnXAxis());
        else
            tempSprite.set_centerX(gerenateDirectionOrPositionXOrY());
        if(tempSprite.get_centerX() == 1.0f || tempSprite.get_centerX() == -1.0f)
            tempSprite.set_centerY(gerenateDirectionOrPositionXOrY());
        else
            tempSprite.set_centerY(placeOnYAxis());

//        tempSprite.set_centerX(getSizeOfAsteroidSprite());
//        tempSprite.set_centerY(0.4f);
//        Bitmap bitmap = getAsteroidBitmap(tempSprite);
//        tempSprite.setTexture(getAsteroidBitmap(tempSprite));
        tempSprite.setBounds(new BoundingCircle(tempSprite.get_centerX(), tempSprite.get_centerY(), (tempSprite.get_width() / 2)));


        asteroidSprites.add(tempSprite);
        //then get a negative or positive for the speed and set the speed
    }

    public float gerenateDirectionOrPositionXOrY()
    {
        Random random = new Random();

        int posOrneg = positiveOrNegative();
        float directionOrPosition = random.nextFloat() * (1.0f - 0.5f) + 0.5f;
        //2 is positive sign
        if(posOrneg == 1)
        {
            directionOrPosition = -directionOrPosition;
        }
        return directionOrPosition;
    }

    public float placeOnYAxis()
    {

        float directionOrPosition;
        int xOrYAxis = xOrYAxis();
        if(xOrYAxis == 1)
            directionOrPosition = 0.6f;
        else
            directionOrPosition = -0.6f;

        int posOrneg = positiveOrNegative();
        //2 is positive sign
        if(posOrneg == 1)
        {
            directionOrPosition = -directionOrPosition;
        }
        return directionOrPosition;
    }

    public float placeOnXAxis()
    {

        float directionOrPosition;
        int xOrYAxis = xOrYAxis();
        if(xOrYAxis == 1)
            directionOrPosition = 1.0f;
        else
            directionOrPosition = -1.0f;

        int posOrneg = positiveOrNegative();
        //2 is positive sign
        if(posOrneg == 1)
        {
            directionOrPosition = -directionOrPosition;
        }
        return directionOrPosition;
    }

    public int positiveOrNegative()
    {
        Random random = new Random();
        return random.nextInt(3-1) + 1;
    }

    public int xOrYAxis()
    {
        Random random = new Random();
        int temp = random.nextInt(3-1) + 1;
        if(temp == 2)
            return 1;
        else
            return 0;
    }

    public float getSizeOfAsteroidSprite()
    {
        Random random = new Random();
        float tempNum = random.nextInt(4-1) + 1;
        float heightOrWidth = 0;


            if(tempNum == 1)
                heightOrWidth = 0.2f;
            else if(tempNum == 2)
                heightOrWidth = 0.25f;
            else if(tempNum == 3)
                heightOrWidth = 0.3f;

        return heightOrWidth;
    }

    public Bitmap getAsteroidBitmap(Sprite tempSprite)
    {
        float tempWidth = tempSprite.get_width();
        Bitmap bitmap;

        if(tempWidth == 0.2f)
             bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.smallasteroid);
        else if( tempWidth == 0.25f)
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mediumasteroid);
        else
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bigasteroid);

        return bitmap;
    }

}
