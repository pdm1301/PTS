package org.pts;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.font.IFont;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;

import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.opengl.vbo.attribute.VertexBufferObjectAttribute;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder;
import org.andengine.util.debug.Debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends SimpleBaseGameActivity {

    private Camera camera;
    private static final int CAMERA_WIDTH = 800;
    private static final int CAMERA_HEIGHT = 480;


    private TextureRegion bgTexture;
    private BitmapTextureAtlas backgroundAtlas;
    private ITexture fontTexture;
    private IFont font;
    private TiledTextureRegion circleTextureRegion;
    private BuildableBitmapTextureAtlas mBitmapTextureAtlas;
    private TiledTextureRegion triangleTextureRegion;
    private TiledTextureRegion squareTextureRegion;
    private TiledTextureRegion rectangleTextureRegion;
    //Map<Integer,Figure>  Figures  =  new Map<Integer,Figure>; //Figures - ассоциативный массив string -> TextureRegion
    private Map Figures ;
    Text text;
    String taskFigure = "square";
    String task;
    private Sound snd_no;
    private Sound snd_yes;
    Figure temp;


    @Override public EngineOptions onCreateEngineOptions() {
        camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
                new FillResolutionPolicy(), camera);
        return engineOptions;
    }


    @Override protected void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");


        this.backgroundAtlas = new BitmapTextureAtlas(this.getTextureManager(),1024, 1024,TextureOptions.DEFAULT);
        bgTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.backgroundAtlas, this, "blackboard.png", 0, 0);
        this.backgroundAtlas.load();


        FontFactory.setAssetBasePath("font/");
        this.fontTexture = new BitmapTextureAtlas(this.getTextureManager(),256,256,TextureOptions.BILINEAR);
        font = FontFactory.createFromAsset(this.getFontManager(), this.getTextureManager(), 1024, 1024, this.getAssets(),
                "Round Script.ttf", 90, true, android.graphics.Color.WHITE);

        this.fontTexture.load();
        font.load();


        this.mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 512, 256, TextureOptions.DEFAULT);

        this.triangleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas,this,"rectangle.png",0,0); // size: 232x122
        this.rectangleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas,this,"triangle.png",0,123);  // size: 79x69
        this.circleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas,this,"circle.png",80,123);  // size: 80x80
        this.squareTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas,this,"square.png",161,123);  //size:  80x80
        this.mBitmapTextureAtlas.load();

        this.Figures = new HashMap<Integer,Figure>();
        Figure triangle = new Figure(400, 120,"triangle",triangleTextureRegion,this.getVertexBufferObjectManager());
        Figures.put(1,triangle);

        Figure rectangle = new Figure(400, 200,"rectangle",rectangleTextureRegion,this.getVertexBufferObjectManager());
        Figures.put(2,rectangle);

        Figure circle = new Figure(500, 120,"circle",circleTextureRegion,this.getVertexBufferObjectManager());
        Figures.put(3,circle);

        Figure square = new Figure(500, 200,"square",squareTextureRegion,this.getVertexBufferObjectManager());
        Figures.put(4,square);

        Random random = new Random();
        int numOfFigure=random.nextInt(3)+1;

        switch (numOfFigure) {
            case 1: {
                taskFigure = "triangle";
                task = "Найди треугольник!";
            }
                break;
            case 2:{
                taskFigure = "rectangle";
                task = "Найди прямоугольник!";
            }
                break;
            case 3:{
                taskFigure = "circle";
                task = "Найди круг!";
            }
                break;
            case 4:
            {
                taskFigure = "square";
                task = "Найди квадрат!";
            }
                break;
        }

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
        SpriteBackground bg = new SpriteBackground(new Sprite(centerX, centerY, bgTexture,getVertexBufferObjectManager()));
        scene.setBackground(bg);


        for(int i=1; i<5;i++){
             temp = Figures.get(i);
            scene.registerTouchArea(Figures.get(i));
            scene.attachChild(Figures.get(i));
        }

        text = new Text(50,50, font,task,getVertexBufferObjectManager());
        scene.attachChild(text);
        return scene;
    }
    public class Figure extends Sprite{
        public String name;
        public Figure(final float pX, final float pY , String figure, final TiledTextureRegion pTextureRegion, final VertexBufferObjectManager pVertexBufferObjectManager) {
            super(pX,pY,pTextureRegion,pVertexBufferObjectManager);
            this.name = figure;
        }

        @Override
         public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY){
            if (pSceneTouchEvent.isActionUp()) {
                if(name.equals(taskFigure)) {
                    text.setText("Молодец!");
                    snd_yes.play();
                }
                else {
                    text.setText("Неверно!");
                    snd_no.play();
                }
            }
            return true;
        }

    }


}
