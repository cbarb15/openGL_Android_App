package edu.utah.cs4962.asteroidtest;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by CharlieBarber on 4/25/16.
 */
public class Sprite
{
    private static int POSITION_ATTRIBUTE = 0;
    private static int TEXTURE_COORDINATE_ATTRIBUTE = 1;

    private static int _program = -1;
    private static int _transformMatrixUniformVariableLocation = -1;
    private static int _textureUnitUniformVariableLocation = -1;
    private static int _textureTranslationUnivormVariableLocation = -1;
    private static int _textureScaleUnivormVariableLocation = -1;
    float[] transformMatrix;
    private float x = 0;
    private float y = 0;
    private float startOfMissleX = 0;
    private float startOfMissleY = 0;
    private BoundingCircle bounds;
    private float directionOfTravelX = 0;
    private float directionofTravelY = 0;

    public float getDirectionofTravelY() {
        return directionofTravelY;
    }

    public void setDirectionofTravelY(float directionofTravelY) {
        this.directionofTravelY = directionofTravelY;
    }

    public float getDirectionOfTravelX() {
        return directionOfTravelX;
    }

    public void setDirectionOfTravelX(float directionOfTravelX) {
        this.directionOfTravelX = directionOfTravelX;
    }

    public BoundingCircle getBounds() {
        return bounds;
    }

    public void setBounds(BoundingCircle bounds) {
        this.bounds = bounds;
    }

    public float getStartOfMissleX() {
        return startOfMissleX;
    }

    public void setStartOfMissleX(float startOfMissleX)
    {
        this.startOfMissleX = startOfMissleX;
    }

    public float getStartOfMissleY()
    {
        return startOfMissleY;
    }

    public void setStartOfMissleY(float startOfMissleY) {
        this.startOfMissleY = startOfMissleY;
    }

    public PointF getRocketPlace() {
        PointF point = new PointF();
        point.x = x;
        point.y = y;
        return point;
    }

    private static void init() {
        String vertexShaderSource = "" +
                "uniform mat4 transformMatrix; \n" +
                "uniform vec2 textureTranslate; \n" +
                "uniform vec2 textureScale; \n" +
                "attribute vec2 position; \n" +
                "attribute vec2 textureCoordinate; \n" +
                "varying vec2 textureCoordinateInterpolated; \n" +
                "\n" +
                "void main() \n" +
                "{ \n" +
                "   gl_Position = transformMatrix * vec4(position.x, position.y , 0.0, 1.0); \n" +
                "   textureCoordinateInterpolated = vec2(textureCoordinate.x * textureScale.x + textureTranslate.x, textureCoordinate.y * textureScale.y + textureTranslate.y); \n" +
                "} \n" +
                "\n";

        String fragmentShaderSource = "" +
                "uniform sampler2D textureUnit; \n" +
                "varying vec2 textureCoordinateInterpolated; \n" +
                "\n" +
                "void main() \n" +
                "{ \n" +
                "   gl_FragColor = texture2D(textureUnit, textureCoordinateInterpolated); \n" +
                "} \n" +
                "\n";

        //Write and compile a vertex shader object
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderSource);
        GLES20.glCompileShader(vertexShader);
        Log.i("Vertex Shader", "Compile Log: " + GLES20.glGetShaderInfoLog(vertexShader));

        //Write and compile a fragment shader object
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderSource);
        GLES20.glCompileShader(fragmentShader);
        Log.i("FragmentShader", "Compile log: " + GLES20.glGetShaderInfoLog(fragmentShader));

        //Link the shaders into a program object
        _program = GLES20.glCreateProgram();
        GLES20.glAttachShader(_program, vertexShader);
        GLES20.glAttachShader(_program, fragmentShader);
        GLES20.glBindAttribLocation(_program, POSITION_ATTRIBUTE, "position");
        GLES20.glBindAttribLocation(_program, TEXTURE_COORDINATE_ATTRIBUTE, "textureCoordinate");
        GLES20.glLinkProgram(_program);
        Log.i("Link", "Link log: " + GLES20.glGetProgramInfoLog(_program));

        GLES20.glUseProgram(_program);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnableVertexAttribArray(POSITION_ATTRIBUTE);
        GLES20.glEnableVertexAttribArray(TEXTURE_COORDINATE_ATTRIBUTE);

        //Give OpenGL a triangle to render
        float[] geometry = new float[]{
                //Ship Rear Right Coordinates
                -0.5f, -0.5f,
                //Ship Front Right
                0.5f, -0.5f,
                //Ship Rear Left
                -0.5f, 0.5f,
                //Ship Front Left
                0.5f, 0.5f,
        };

        float[] textureCoordinates = new float[]{
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };

        ByteBuffer geometryByteBuffer = ByteBuffer.allocateDirect(geometry.length * 4);
        geometryByteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer geometryBuffer = geometryByteBuffer.asFloatBuffer();
        geometryBuffer.put(geometry);
        geometryBuffer.rewind();
        GLES20.glVertexAttribPointer(POSITION_ATTRIBUTE, 2, GLES20.GL_FLOAT, false, 0, geometryBuffer);

        ByteBuffer textureCoordinatesByteBuyffer = ByteBuffer.allocateDirect(textureCoordinates.length * 4);
        textureCoordinatesByteBuyffer.order(ByteOrder.nativeOrder());
        FloatBuffer texturCoordinatesBuffer = textureCoordinatesByteBuyffer.asFloatBuffer();
        texturCoordinatesBuffer.put(textureCoordinates);
        texturCoordinatesBuffer.rewind();
        GLES20.glVertexAttribPointer(TEXTURE_COORDINATE_ATTRIBUTE, 2, GLES20.GL_FLOAT, false, 0, texturCoordinatesBuffer);

        _transformMatrixUniformVariableLocation = GLES20.glGetUniformLocation(_program, "transformMatrix");
        _textureUnitUniformVariableLocation = GLES20.glGetUniformLocation(_program, "textureUnit");
        _textureTranslationUnivormVariableLocation = GLES20.glGetUniformLocation(_program, "textureTranslate");
        _textureScaleUnivormVariableLocation = GLES20.glGetUniformLocation(_program, "textureScale");
    }

    private float _centerX;
    private float _centerY;
    private float _width;
    private float _height;
    private float _rotate;

    //Everytime you set the texture Id you should set the column count and row count
    private int _textureId;
    private int _subTextureColumnCount = 1;
    private int _subTextureRowCount = 1;
    private float _subTextureIndex = 0;

    public float get_rotate() {
        return _rotate;
    }

    public void set_rotate(float _rotate) {
        this._rotate = _rotate;
    }

    public int get_subTextureColumnCount() {
        return _subTextureColumnCount;
    }

    public void set_subTextureColumnCount(int _subTextureColumnCount) {
        this._subTextureColumnCount = _subTextureColumnCount;
    }

    public int get_subTextureRowCount() {
        return _subTextureRowCount;
    }

    public void set_subTextureRowCount(int _subTextureRowCount) {
        this._subTextureRowCount = _subTextureRowCount;
    }

    public float get_subTextureIndex() {
        return _subTextureIndex;
    }

    public void set_subTextureIndex(float _subTextureIndex) {
        this._subTextureIndex = _subTextureIndex;
    }


    public float get_width() {
        return _width;
    }

    public void set_width(float _width) {
        this._width = _width;
    }

    public float get_height() {
        return _height;
    }

    public void set_height(float _height) {
        this._height = _height;
    }

    public int get_textureId() {
        return _textureId;
    }

    public void set_textureId(int _textureId) {
        this._textureId = _textureId;
    }

    public float get_centerX() {
        return _centerX;
    }

    public void set_centerX(float _centerX) {
        this._centerX = _centerX;
    }

    public float get_centerY() {
        return _centerY;
    }

    public void set_centerY(float _centerY) {
        this._centerY = _centerY;
    }

    public void setTexture(Bitmap texture)
    {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        int textureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0);
        set_textureId(textureId);
    }

    public void draw() {
        if (_program <= 0)
            init();

        float textureScaleX = 1.0f / (float) _subTextureColumnCount;
        float textureScaleY = 1.0f / (float) _subTextureRowCount;
        float textureTranslateX = (Math.round(_subTextureIndex) % _subTextureColumnCount) * textureScaleX;
        float textureTranslateY = (Math.round(_subTextureIndex) / _subTextureColumnCount) * textureScaleY;

        transformMatrix = new float[16];
        Matrix.setIdentityM(transformMatrix, 0);
        Matrix.translateM(transformMatrix, 0, _centerX, _centerY, 0.0f);
        Matrix.scaleM(transformMatrix, 0, _width, _height, 1.0f);

        Matrix.rotateM(transformMatrix, 0, _rotate, 0.0f, 0.0f, 1.0f);

        GLES20.glUniformMatrix4fv(_transformMatrixUniformVariableLocation, 1, false, transformMatrix, 0);
        GLES20.glUniform1i(_textureUnitUniformVariableLocation, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _textureId);
        GLES20.glUniform2f(_textureTranslationUnivormVariableLocation, textureTranslateX, textureTranslateY);
        GLES20.glUniform2f(_textureScaleUnivormVariableLocation, textureScaleX, textureScaleY);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }
}
