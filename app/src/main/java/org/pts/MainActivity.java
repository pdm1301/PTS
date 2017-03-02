package org.pts;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.util.Log;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;
import org.pts.BitmapTextureAtlasSource;
import org.pts.SVGParser;
import org.pts.SVG;
//import org.poehali.BitmapTextureAtlasSource;
//import com.larvalabs.svgandroid.SVGParser;
//import com.larvalabs.svgandroid.SVG;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class MainActivity extends SimpleBaseGameActivity {

    private Camera camera;
    private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;


    private TextureRegion bgTexture;
    private BitmapTextureAtlas backgroundAtlas;
    //private BuildableBitmapTextureAtlas pictureAtlas;
    private Bitmap currentBitmap;
    private HashMap<String,Figure> figures =  new HashMap<String,Figure>();
    private HashMap<String,TextureRegion> textures =  new HashMap<String,TextureRegion>();
    private ArrayList<String> randFigure= new ArrayList<String>();
    String taskFigure ;
    private Sound snd_no;
    private Sound snd_yes;
    private float fSize = (float)0.4*CAMERA_HEIGHT;
    private ArrayList<String> files =new ArrayList<String>();
    BitmapTextureAtlas textureAtlas;

    class Task{
        String nameOfTask;
        String pathToResources;
        String question;
        String answer;
        int random;

        void setNameOfTask(String name){
            this.nameOfTask=name;
        }
        void setPathToResources(String path){
            this.pathToResources=path;
        }
        void setQuestion(String quest){
            this.question=quest;
        }
        void setAnswer(String answer){
            this.answer=answer;
        }
        void setRandom(int rand){
            this.random=rand;
        }

        String getNameOfTask(){
            return this.nameOfTask;
        }
        String getPathToResources(){
            return this.pathToResources;
        }
        String getQuestion(){
            return this.question;
        }
        String getAnswer(){
            return this.answer;
        }
        int getRandom(){
            return this.random;
        }
        Task(){
            this.setNameOfTask(null);
            this.setPathToResources(null);
            this.setQuestion(null);
            this.setAnswer(null);
            this.setRandom(0);
        }
        Task(String name,String path, String quest,String answer,int rand){
            this.setNameOfTask(name);
            this.setPathToResources(path);
            this.setQuestion(quest);
            this.setAnswer(answer);
            this.setRandom(rand);
        }
    }

    @Override public EngineOptions onCreateEngineOptions() {
        camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
                new FillResolutionPolicy(), camera);
        engineOptions.getAudioOptions().setNeedsMusic(true);
        engineOptions.getAudioOptions().setNeedsSound(true);
        return engineOptions;
    }

    @Override protected void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        this.backgroundAtlas = new BitmapTextureAtlas(this.getTextureManager(),1024, 1024,TextureOptions.DEFAULT);
        Bitmap backgroundImage = decodeSampledBitmapFromResource(getResources(), R.drawable.blackboard,CAMERA_WIDTH,CAMERA_HEIGHT);
        backgroundImage = Bitmap.createScaledBitmap(backgroundImage, CAMERA_WIDTH,CAMERA_HEIGHT, false);
        IBitmapTextureAtlasSource bgTex= new BitmapTextureAtlasSource(backgroundImage);
        backgroundAtlas.addTextureAtlasSource(bgTex,0,0);

        bgTexture = BitmapTextureAtlasTextureRegionFactory.createFromSource(backgroundAtlas, bgTex, 0, 0);
        this.backgroundAtlas.load();

        try {

            for (String file_name : getAssets().list("svg")) {
                    file_name=file_name.substring(0,file_name.length()-4);  //Обрезаем расширение .svg
                    files.add(file_name);
                }

            textureAtlas = new BitmapTextureAtlas(getTextureManager(), (int)fSize*files.size(), (int)fSize);
            for(int i=0; i<files.size();i++) {
                String path_to_file =  "svg/" + files.get(i)+".svg";
                Log.i("poehali", path_to_file);
                final SVG svg = SVGParser.getSVGFromAsset(getAssets(), path_to_file);
                Picture svgPic = svg.resizePicture((int) fSize, (int) fSize);

                //Bitmap bitmap = Bitmap.createBitmap(pictureDrawable.getIntrinsicWidth(), pictureDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Bitmap bitmap = Bitmap.createBitmap((int)fSize, (int)fSize, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawPicture(svgPic);
                currentBitmap = Bitmap.createBitmap(bitmap, 0, 0, (int)fSize, (int)fSize);

                Log.i("poehali", "adding to atlas");
                BitmapTextureAtlasSource source = new BitmapTextureAtlasSource(currentBitmap);

                textureAtlas.addTextureAtlasSource(source, i*(int)fSize,0);
                TextureRegion textureRegion = (TextureRegion) TextureRegionFactory.createFromSource(textureAtlas, source, i*(int)fSize,0);
                textures.put(files.get(i), textureRegion);
            }
        }
        catch (IOException e){
            Debug.e(e);
            Log.i("poehali", e.toString());
        }
        textureAtlas.load();

        Random random = new Random();
        int loadedFigures=0;
        boolean rectLoaded=false;
        int j=0;
        boolean fstline=false;

        for(int i=0;i<files.size(); i++)
            randFigure.add(files.get(i));

        for(int i = 0 ; i < files.size(); i++) {
            int rf = random.nextInt(randFigure.size());


            if(loadedFigures<files.size()/2){
                if(!rectLoaded)
                    figures.put(randFigure.get(rf), new Figure(i * fSize + 2, 32, randFigure.get(rf), textures.get(randFigure.get(rf)), this.getVertexBufferObjectManager()));

                else
                    figures.put(randFigure.get(rf), new Figure(i*fSize+96, 32, randFigure.get(rf), textures.get(randFigure.get(rf)), this.getVertexBufferObjectManager()));

                if(randFigure.get(rf).equals("rectangle.png")) {
                    rectLoaded = true;
                    fstline = true;
                }
                else{
                    rectLoaded=false;
                    fstline=false;
                }
            }
            else{
                if(!rectLoaded)
                    figures.put(randFigure.get(rf), new Figure(j * fSize+2, 256, randFigure.get(rf), textures.get(randFigure.get(rf)), this.getVertexBufferObjectManager()));

                else {
                    if(fstline) {
                        figures.put(randFigure.get(rf), new Figure(j * fSize + 2, 256, randFigure.get(rf), textures.get(randFigure.get(rf)), this.getVertexBufferObjectManager()));
                        fstline=false;
                    }
                    else
                        figures.put(randFigure.get(rf), new Figure(j * fSize + 96, 256, randFigure.get(rf), textures.get(randFigure.get(rf)), this.getVertexBufferObjectManager()));
                }
                if(randFigure.get(rf).equals("rectangle.png"))
                    rectLoaded=true;
                else rectLoaded=false;
                j++;
            }
            loadedFigures++;
            randFigure.remove(rf);
        }
        int numOfFigure=random.nextInt(files.size());
        taskFigure = files.get(numOfFigure);


        try {
            SoundFactory.setAssetBasePath("snd/");
            snd_no = SoundFactory.createSoundFromAsset(this.getSoundManager(), this.getApplicationContext(), "no.wav");
            snd_yes = SoundFactory.createSoundFromAsset(this.getSoundManager(), this.getApplicationContext(), "yes.wav");
        }
        catch (final IOException e) {
            Debug.e(e);
        }

    }

    @Override protected Scene onCreateScene() {
        Scene scene = new Scene();
        final int centerX = (CAMERA_WIDTH / 2);
        final int centerY = (CAMERA_HEIGHT  / 2);
        SpriteBackground bg = new SpriteBackground(new Sprite(0, 0, bgTexture, getVertexBufferObjectManager()));
        scene.setBackground(bg);

        Set set = figures.entrySet();
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry mentry = (Map.Entry)iterator.next();
            Figure f = (Figure)mentry.getValue();
            Log.i("poehali", f.figureName);
            if(mentry.getKey().equals("rectangle.png"))
                f.setSize((float)(fSize*1.5),fSize);
            else
                f.setSize(fSize,fSize);
            scene.registerTouchArea(f);
            scene.attachChild(f);
        }

        return scene;
    }
    public class Figure extends Sprite{
        public String figureName;
        public Figure(final float pX, final float pY , String figureName_, final TextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
            super(pX,pY,pTextureRegion,pVertexBufferObjectManager);
            this.figureName = figureName_;
        }

        @Override
         public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY){
            if (pSceneTouchEvent.isActionUp()) {
                if(figureName.equals(taskFigure)) {
                   // text.setText("Молодец!");
                    snd_yes.play();
                }
                else {
                   // text.setText("Неверно!");
                    snd_no.play();
                }
            }
            return true;
        }

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inScaled = false;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Log.i("poehali", reqWidth+" "+reqHeight);
        Log.i("poehali", options.inSampleSize+"");

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        options.inScaled = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

     Task formTask(String taskName) throws Exception
     {
         Task newTask =new Task();
         newTask.setNameOfTask(taskName);
         int[] taskForming = new int[5];

         try
         {
             FileReader reader = new FileReader(taskName);
             BufferedReader buff = new BufferedReader(reader);
             String line=buff.readLine();
             while(line!=null)
             {
                if(line.contains("<path>")) {
                    newTask.setPathToResources(line.substring(6, line.length()));
                    taskForming[1] = 1;
                }
                 if(line.contains("<question>")){
                     newTask.setQuestion((line.substring(10,line.length())));
                     taskForming[2] = 1;
                 }
                 if(line.contains("<answer>")){
                     newTask.setAnswer(line.substring(8,line.length()));
                     taskForming[3] = 1;
                 }
                 if(line.contains("<rand>")){
                     newTask.setRandom(((int)line.charAt(6)));
                     taskForming[4] = 1;
                 }
             }
         }
        catch(IOException ex) {
            taskForming[0]=0;
        }
         for(int i=0; i<5;i++)
             if(taskForming[i]==0)
                throw new Exception("Ошибка открытия файла");
        return newTask;
    }
}
