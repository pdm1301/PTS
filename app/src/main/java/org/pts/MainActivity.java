package org.pts;

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
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

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
    private BuildableBitmapTextureAtlas pictureAtlas;
    private HashMap<String,Figure> figures =  new HashMap<String,Figure>();
    private HashMap<String,TextureRegion> textures =  new HashMap<String,TextureRegion>();
    private ArrayList<String> randFigure= new ArrayList<String>();
    String taskFigure ;
    private Sound snd_no;
    private Sound snd_yes;
    private float fSize = (float)0.4*CAMERA_HEIGHT;


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
        bgTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.backgroundAtlas, this, "blackboard.png", 0, 0);
        this.backgroundAtlas.load();


        String[] files = {"rectangle.png", "triangle.png", "circle.png", "square.png"};
        pictureAtlas = new BuildableBitmapTextureAtlas(getTextureManager(), 512, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        for(int i = 0 ; i < files.length; i++)
            textures.put(files[i], BitmapTextureAtlasTextureRegionFactory.createFromAsset(pictureAtlas, this, files[i]));

        try{ pictureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 1)); }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        pictureAtlas.load();
        Random random = new Random();
        int loadedFigures=0;
        boolean rectLoaded=false;
        int j=0;
        boolean fstline=false;

        for(int i=0;i<files.length; i++)
            randFigure.add(files[i]);

        for(int i = 0 ; i < files.length; i++) {
            int rf = random.nextInt(randFigure.size());


            if(loadedFigures<files.length/2){
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
        int numOfFigure=random.nextInt(files.length);
        taskFigure = files[numOfFigure];


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


}
