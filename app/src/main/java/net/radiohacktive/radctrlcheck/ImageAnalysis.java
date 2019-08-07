package net.radiohacktive.radctrlcheck;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ImageAnalysis extends AppCompatActivity {

    //// Global vars
    // Intent code
    public final static int LOAD_FULLAREA = 100;
    //
    // Img Analysis filepath
    public Bitmap IMGBITMAP;
    public String IMGFILEPATH = "";
    //
    // ImageView touch coordinaes and hold length
    public int X = 0, Y = 0, CLICKX = 0, CLICKY = 0, VIEWX = 0, VIEWY = 0;
    //
    // Vars
    public int SID = 100, AREAWIDTH = 15, AREAHEIGHT = 15; // cm

    public void noSelection() {

        // Instantiate, populate and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Cannot perform calculations;\nno selection made!")
                .setTitle("QC Error");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /** Push button events */
    //
    public void eventMeasureXRay(View arg0) {

        // Ignore if no selection
        if ( CLICKX == 0 && X == 0 && CLICKY == 0 && Y == 0 ) { noSelection(); }
        else {

            // Get selection dimensions
            float rectWidth = Math.abs((float) X - (float) CLICKX);
            float rectHeight = Math.abs((float) X - (float) CLICKX);

            // Obtain image vars
            ImageView imageView = (ImageView) findViewById(R.id.imgArea);
            Bitmap XRay_Image = IMGBITMAP;

            // Height and width of the x-ray and scaled view images
            float imageWidth = XRay_Image.getWidth();
            float imageHeight = XRay_Image.getHeight();
            float viewWidth = imageView.getWidth();
            float viewHeight = imageView.getHeight();

            // Calculate scaling ratios
            float widthRatio = imageWidth/viewWidth;
            float heightRatio = imageHeight/viewHeight;

            // Compute vars
            float XRayWidth = (float) AREAWIDTH*rectWidth/imageWidth;
            float XRayHeight = (float) AREAHEIGHT*rectHeight/imageHeight;
            // Reinterpret rectangle to determine offset from diagonal coordinates
            float XRayLeftError, XRayRightError, XRayTopError, XRayBottomError;
            if ( CLICKX <= X ) { XRayLeftError = (float) AREAWIDTH*CLICKX/imageWidth; XRayRightError = (float) AREAWIDTH*(imageWidth - X)/imageWidth; }
            else { XRayLeftError = (float) AREAWIDTH*X/imageWidth; XRayRightError = (float) AREAWIDTH*(imageWidth - CLICKX)/imageWidth; }
            if ( CLICKY <= Y ) { XRayTopError = (float) AREAHEIGHT*CLICKY/imageHeight; XRayBottomError = (float) AREAHEIGHT*(imageHeight - Y)/imageHeight; }
            else { XRayTopError = (float) AREAHEIGHT*Y/imageHeight; XRayBottomError = (float) AREAHEIGHT*(imageHeight - CLICKY)/imageHeight; }

            // Compliance checks
            boolean isXRayLeftCompliant = (XRayLeftError <= 0.02*SID);
            boolean isXRayRightCompliant = (XRayRightError <= 0.02*SID);
            boolean isXRayTopCompliant = (XRayTopError <= 0.02*SID);
            boolean isXRayBottomCompliant = (XRayBottomError <= 0.02*SID);

            // Format compliance text output
            String COMPLIANCETEXT = "SID = " + Float.toString(SID) + " cm\n\n" +
                    "VDF Width = " + Integer.toString(AREAWIDTH) + " cm\n" +
                    "VDF Height = " + Integer.toString(AREAHEIGHT) + " cm\n\n" +
                    "X-Ray Width = " + Float.toString(XRayWidth) + " cm\n" +
                    "X-Ray Height = " + Float.toString(XRayHeight) + " cm\n\n" +
                    "Left Error = " + Float.toString(XRayLeftError) + " cm\n" +
                    "  Compliant?:  " + Boolean.toString(isXRayLeftCompliant) + "\n" +
                    "Right Error = " + Float.toString(XRayRightError) + " cm\n" +
                    "  Compliant?:  " + Boolean.toString(isXRayRightCompliant) + "\n" +
                    "Top Error = " + Float.toString(XRayTopError) + " cm\n" +
                    "  Compliant?:  " + Boolean.toString(isXRayTopCompliant) + "\n" +
                    "Bottom Error = " + Float.toString(XRayBottomError) + " cm\n" +
                    "  Compliant?:  " + Boolean.toString(isXRayBottomCompliant);

            // Instantiate, populate and show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(COMPLIANCETEXT)
                    .setTitle("X-Ray Compliance");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialize
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);

        // Immediately send intent for image gallery import
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, LOAD_FULLAREA);

        // Touch select coordinate in area
        ImageView imageView = (ImageView) findViewById(R.id.imgArea);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // Obtain image vars
                ImageView imageView = (ImageView) findViewById(R.id.imgArea);
                Bitmap XRay_Image = IMGBITMAP;
                //Bitmap XRay_Image = BitmapFactory.decode(IMGFILEPATH);
                //Bitmap XRay_Image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                // Height and width of the x-ray and scaled view images
                float imageWidth = XRay_Image.getWidth();
                float imageHeight = XRay_Image.getHeight();
                float viewWidth = imageView.getWidth();
                float viewHeight = imageView.getHeight();

                // Calculate scaling ratios
                float widthRatio = imageWidth/viewWidth;
                float heightRatio = imageHeight/viewHeight;

                // Image rescale automatically allows for image minor axis padding-to-image matrix translation
                // Therefore, disable padding clickability and re-rescale translation on image touch
                // Aqcuire single pad thickness
                int padX = 0, padY = 0;
                if ( widthRatio > heightRatio ) // horizontal padding at top and bottom
                { padY = (int) (0.5*(viewHeight - (imageHeight/widthRatio))); }
                else // vertical padding at left and right
                { padX = (int) (0.5*(viewWidth - (imageWidth/heightRatio))); }

                //Get new coordinate on original image matrix from touch, discluding padding
                if ( event.getX() != VIEWX && event.getY() != VIEWY &&
                        event.getY() >= padY && event.getY() < (viewHeight-padY) &&
                        event.getX() >= padX && event.getX() < (viewWidth-padX) ) {

                    // View coordinate displacement
                    VIEWX = (int) event.getX();
                    VIEWY = (int) event.getY();

                    // Image Coordinates (round)
                    X = (int) ((VIEWX - padX)*widthRatio/(1 - 2*padX/viewWidth));
                    Y = (int) ((VIEWY - padY)*heightRatio/(1 - 2*padY/viewHeight));

                    // Paint rectangle on image when moving
                    switch ( event.getAction() ) {

                        case MotionEvent.ACTION_DOWN: // Initial click

                            // Obtain click coordinate
                            CLICKX = X;
                            CLICKY = Y;

                            break;

                        case MotionEvent.ACTION_MOVE: // Any movements after click

                            // Create new bitmap from image and attach to a canvas
                            Bitmap tempBitmap = XRay_Image.copy(Bitmap.Config.ARGB_8888, true);
                            Canvas tempCanvas = new Canvas(tempBitmap);

                            // Paint/Repaint canvas
                            Paint paint = new Paint();
                            paint.setAntiAlias(true);
                            paint.setColor(Color.BLUE);

                            // Draw rectangle on point from click-point
                            paint.setStyle(Paint.Style.STROKE); // Unfilled
                            tempCanvas.drawRect(CLICKX, CLICKY, X, Y, paint); // Intelligently draws rectangle along diagonal

                            // Attach canvas to the ImageView
                            imageView.setImageBitmap(tempBitmap);

                            break;
                    }
                }

                return true;
            }
        });
    }

    /** Process incoming intents */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageData) {

        // Initialize
        super.onActivityResult(requestCode, resultCode, imageData);

        // Img processes or bust
        if ( resultCode == RESULT_OK ) {

            // Update dimensions of selected img
            switch ( requestCode ) {
                case LOAD_FULLAREA:

                    try {
                        // Load image uri from intent data
                        Uri selectedImage = imageData.getData();
                        IMGBITMAP = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);

                        // Set imageView from URI
                        ImageView imageView = (ImageView) findViewById(R.id.imgArea);
//                    imageView.setImageURI(IMGURI);
                        imageView.setImageBitmap(IMGBITMAP);
                    } catch ( IOException e ) {

                    }
            }
        } else { finish(); }
    }
}
