package com.creators7.mobile;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Surface;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Offline 9:16 illustration + on-device TTS + H.264/AAC muxing. */
public final class ShortRenderer {
    private static final int W=720,H=1280,FPS=30,SECONDS=12;

    public static Uri render(Context context,String topic,int style)throws Exception {
        File dir=new File(context.getCacheDir(),"s7-render");if(!dir.exists()&&!dir.mkdirs())throw new IllegalStateException("Cannot create render cache");
        File silent=new File(dir,"video.mp4"),wav=new File(dir,"voice.wav"),aac=new File(dir,"voice-aac.mp4"),finalFile=new File(dir,"final.mp4");
        String narration="Let's explore "+topic+". First, the idea begins. Then something changes. Watch the final reveal. That's your story, made with Creator S7.";
        synthesize(context,narration,wav);
        renderVideo(silent,topic,style);
        encodeAac(wav,aac);
        mux(silent,aac,finalFile);
        String stamp=new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date());ContentValues values=new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME,"CreatorS7_"+stamp+".mp4");values.put(MediaStore.Video.Media.MIME_TYPE,"video/mp4");
        if(Build.VERSION.SDK_INT>=29){values.put(MediaStore.Video.Media.RELATIVE_PATH,Environment.DIRECTORY_MOVIES+"/Creator S7");values.put(MediaStore.Video.Media.IS_PENDING,1);}
        Uri uri=context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);if(uri==null)throw new IllegalStateException("Android could not create the video file");
        try(OutputStream out=context.getContentResolver().openOutputStream(uri,"w");InputStream in=new FileInputStream(finalFile)){
            if(out==null)throw new IllegalStateException("Cannot save finished video");byte[] buf=new byte[64*1024];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
        }catch(Exception e){context.getContentResolver().delete(uri,null,null);throw e;}
        if(Build.VERSION.SDK_INT>=29){ContentValues done=new ContentValues();done.put(MediaStore.Video.Media.IS_PENDING,0);context.getContentResolver().update(uri,done,null,null);}
        silent.delete();wav.delete();aac.delete();finalFile.delete();return uri;
    }

    private static void synthesize(Context ctx,String words,File out)throws Exception {
        CountDownLatch init=new CountDownLatch(1);final TextToSpeech[] ref=new TextToSpeech[1];final int[] result={TextToSpeech.ERROR};
        Handler main=new Handler(Looper.getMainLooper());main.post(()->ref[0]=new TextToSpeech(ctx.getApplicationContext(),status->{result[0]=status;init.countDown();}));
        if(!init.await(20,TimeUnit.SECONDS)||result[0]!=TextToSpeech.SUCCESS)throw new IllegalStateException("Phone text-to-speech engine did not start");
        TextToSpeech tts=ref[0];CountDownLatch done=new CountDownLatch(1);final String id="creator-s7-voice";final int[] err={0};
        if(tts.setLanguage(Locale.US)<TextToSpeech.LANG_AVAILABLE)throw new IllegalStateException("Install an English voice in Android Text-to-speech settings first");
        tts.setSpeechRate(0.92f);tts.setOnUtteranceProgressListener(new UtteranceProgressListener(){@Override public void onStart(String s){}@Override public void onDone(String s){if(id.equals(s))done.countDown();}@Override public void onError(String s){err[0]=1;if(id.equals(s))done.countDown();}@Override public void onError(String s,int code){onError(s);}});
        int submitted=tts.synthesizeToFile(words,new Bundle(),out,id);if(submitted!=TextToSpeech.SUCCESS){tts.shutdown();throw new IllegalStateException("Phone could not synthesize the narration");}
        if(!done.await(40,TimeUnit.SECONDS)){tts.stop();tts.shutdown();throw new IllegalStateException("Narration timed out");}tts.shutdown();if(err[0]!=0||!out.exists()||out.length()<48)throw new IllegalStateException("Narration file was empty; check installed TTS voice data");
    }

    private static void renderVideo(File out,String topic,int style)throws Exception {
        MediaRecorder rec=Build.VERSION.SDK_INT>=31?new MediaRecorder():new MediaRecorder();
        try(FileOutputStream file=new FileOutputStream(out)){
            rec.setVideoSource(MediaRecorder.VideoSource.SURFACE);rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);rec.setVideoEncoder(MediaRecorder.VideoEncoder.H264);rec.setVideoSize(W,H);rec.setVideoFrameRate(FPS);rec.setVideoEncodingBitRate(3_500_000);rec.setOutputFile(file.getFD());rec.prepare();rec.start();Surface surface=rec.getSurface();FramePainter painter=new FramePainter(topic,style);
            for(int i=0;i<FPS*SECONDS;i++){Canvas c=surface.lockCanvas(null);painter.draw(c,(float)i/FPS);surface.unlockCanvasAndPost(c);Thread.sleep(1000L/FPS);}rec.stop();surface.release();
        }finally{rec.release();}
    }

    private static void encodeAac(File wav,File output)throws Exception {
        byte[] data=read(wav);int dataAt=findData(data);if(dataAt<0)throw new IllegalStateException("Phone TTS did not create a PCM WAV voice");
        ByteBuffer head=ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);int channels=head.getShort(22)&0xffff,rate=head.getInt(24),bits=head.getShort(34)&0xffff;
        if(bits!=16||channels<1||channels>2)throw new IllegalStateException("Unsupported phone TTS audio format: "+bits+" bit, "+channels+" channel");
        MediaFormat fmt=MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,rate,channels);fmt.setInteger(MediaFormat.KEY_AAC_PROFILE,MediaCodecInfo.CodecProfileLevel.AACObjectLC);fmt.setInteger(MediaFormat.KEY_BIT_RATE,96000);fmt.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,16384);
        MediaCodec codec=MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);MediaMuxer mux=new MediaMuxer(output.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);boolean started=false,inputDone=false,outputDone=false;int track=-1,offset=dataAt,limit=data.length;
        codec.configure(fmt,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);codec.start();MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();long samples=0;
        try {
            while(!outputDone){
                if(!inputDone){int ix=codec.dequeueInputBuffer(10000);if(ix>=0){ByteBuffer in=codec.getInputBuffer(ix);in.clear();int cap=in.remaining();int n=Math.min(cap,limit-offset);n-=n%(channels*2);if(n>0){in.put(data,offset,n);long pts=samples*1000000L/rate/channels/2;codec.queueInputBuffer(ix,0,n,pts,0);samples+=n;offset+=n;}else{long pts=samples*1000000L/rate/channels/2;codec.queueInputBuffer(ix,0,0,pts,MediaCodec.BUFFER_FLAG_END_OF_STREAM);inputDone=true;}}}
                int oi=codec.dequeueOutputBuffer(info,10000);if(oi==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){track=mux.addTrack(codec.getOutputFormat());mux.start();started=true;}else if(oi>=0){ByteBuffer b=codec.getOutputBuffer(oi);if(info.size>0&&started){b.position(info.offset);b.limit(info.offset+info.size);mux.writeSampleData(track,b,info);}outputDone=(info.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0;codec.releaseOutputBuffer(oi,false);}
            }
        }finally{try{codec.stop();}catch(Exception ignored){}codec.release();if(started){mux.stop();}mux.release();}
    }

    private static void mux(File video,File audio,File output)throws Exception {
        MediaExtractor ve=new MediaExtractor(),ae=new MediaExtractor();MediaMuxer mux=new MediaMuxer(output.getAbsolutePath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);boolean started=false;
        try(FileInputStream vf=new FileInputStream(video);FileInputStream af=new FileInputStream(audio)){
            ve.setDataSource(vf.getFD());ae.setDataSource(af.getFD());int vt=findTrack(ve,"video/"),at=findTrack(ae,"audio/");if(vt<0||at<0)throw new IllegalStateException("Could not combine narration and video tracks");ve.selectTrack(vt);ae.selectTrack(at);int outV=mux.addTrack(ve.getTrackFormat(vt)),outA=mux.addTrack(ae.getTrackFormat(at));mux.start();started=true;
            copyTrack(ve,mux,outV);copyTrack(ae,mux,outA);
        }finally{ve.release();ae.release();if(started)mux.stop();mux.release();}
    }
    private static int findTrack(MediaExtractor e,String prefix){for(int i=0;i<e.getTrackCount();i++)if(e.getTrackFormat(i).getString(MediaFormat.KEY_MIME).startsWith(prefix))return i;return -1;}
    private static void copyTrack(MediaExtractor e,MediaMuxer mux,int track){ByteBuffer buf=ByteBuffer.allocate(1024*1024);MediaCodec.BufferInfo info=new MediaCodec.BufferInfo();while(true){buf.clear();int size=e.readSampleData(buf,0);if(size<0)break;info.set(0,size,e.getSampleTime(),e.getSampleFlags());mux.writeSampleData(track,buf,info);e.advance();}}
    private static byte[] read(File f)throws Exception{try(InputStream in=new FileInputStream(f);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))>0)out.write(b,0,n);return out.toByteArray();}}
    private static int findData(byte[] b){for(int i=12;i+8<b.length;i++)if(b[i]=='d'&&b[i+1]=='a'&&b[i+2]=='t'&&b[i+3]=='a')return i+8;return -1;}

    private static final class FramePainter {
        final String topic;final int style;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final int[] bg={Color.rgb(244,241,234),Color.rgb(13,14,21),Color.rgb(250,229,205),Color.rgb(33,44,61),Color.rgb(245,232,194),Color.rgb(170,218,242)},accent={Color.rgb(79,209,232),Color.rgb(214,40,57),Color.rgb(79,209,232),Color.rgb(255,194,91),Color.rgb(189,48,57),Color.rgb(255,128,82)};
        FramePainter(String t,int s){topic=t;style=Math.max(0,Math.min(s,5));}
        void draw(Canvas c,float t){int base=bg[style],a=accent[style];c.drawColor(base);float cx=W/2f;int scene=Math.min(5,(int)(t/2));
            p.setColor(a);p.setAlpha(38);c.drawCircle(cx,420,210+8*(float)Math.sin(t*2),p);p.setAlpha(255);
            p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));p.setTextSize(23);c.drawText("CREATOR S7  ·  YOUR SHORT",cx,88,p);
            illustration(c,scene,t,a,base);
            String[] heads={"WHAT IF…",topic.toUpperCase(Locale.getDefault()),"THE STORY STARTS", "THEN IT CHANGES", "HERE'S THE REVEAL", "MADE ON YOUR PHONE"};String[] caps={"A QUESTION WORTH ASKING","CASE FILE: NEW CLUE","FIRST, THE SETUP","WATCH WHAT HAPPENS","THE SURPRISING RESULT","CREATOR S7 TEST"};
            p.setColor(style==1?Color.rgb(255,230,198):Color.rgb(26,26,30));p.setTextSize(scene==1?37:42);wrapped(c,heads[scene],cx,770,590,p);p.setColor(style==1?Color.rgb(38,34,37):Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(25);c.drawRoundRect(new RectF(60,900,660,978),24,24,p);p.setColor(style==1?Color.rgb(214,40,57):base);c.drawText(caps[scene],cx,949,p);
            p.setColor(Color.rgb(205,214,230));p.setTextSize(19);c.drawText("VOICE + ANIMATED SCENES",cx,1110,p);p.setColor(a);c.drawRoundRect(new RectF(70,1175,650,1188),9,9,p);p.setColor(Color.WHITE);c.drawRoundRect(new RectF(70,1175,70+580*t/SECONDS,1188),9,9,p);
        }
        void illustration(Canvas c,int scene,float t,int a,int base){
            if(style==1){nightScene(c,scene,t,a);return;}if(style==3){stickScene(c,scene,t,a);return;}if(style==4){atlasScene(c,scene,t,a,base);return;}if(style==5){tallTale(c,scene,t,a);return;}
            if(scene<2){person(c,260,500,a);bubble(c,460,350,scene==0?"?":"GUM",a);}
            else { // friendly side-profile head and a bright path down to a stomach
                p.setColor(Color.rgb(244,190,149));c.drawCircle(290,390,108,p);p.setColor(Color.rgb(18,25,42));c.drawCircle(250,377,12,p);c.drawCircle(329,377,12,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(10);p.setColor(a);Path path=new Path();path.moveTo(290,490);path.cubicTo(350,535,310,565,360,605);path.cubicTo(420,650,345,655,330,690);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);p.setColor(a);c.drawCircle(355+(float)Math.sin(t*3)*8,600,18,p);p.setColor(Color.rgb(244,190,149));c.drawOval(new RectF(260,610,420,710),p);p.setColor(base);c.drawOval(new RectF(295,645,390,695),p);p.setColor(a);c.drawCircle(340,665,20,p);}
        }
        void person(Canvas c,float x,float y,int a){p.setColor(Color.rgb(244,190,149));c.drawCircle(x,y-85,62,p);p.setColor(Color.rgb(64,112,176));c.drawRoundRect(new RectF(x-65,y-25,x+65,y+140),35,35,p);p.setColor(Color.WHITE);c.drawCircle(x-20,y-92,7,p);c.drawCircle(x+20,y-92,7,p);p.setColor(Color.rgb(45,35,40));c.drawLine(x-30,y+135,x-55,y+205,p);c.drawLine(x+30,y+135,x+55,y+205,p);p.setStrokeWidth(14);c.drawLine(x-55,y+10,x-100,y+92,p);c.drawLine(x+55,y+10,x+100,y+92,p);}
        void bubble(Canvas c,float x,float y,String word,int a){p.setColor(Color.WHITE);c.drawRoundRect(new RectF(x-95,y-50,x+95,y+50),25,25,p);p.setColor(a);p.setTextSize(36);p.setTextAlign(Paint.Align.CENTER);c.drawText(word,x,y+12,p);}
        void stickScene(Canvas c,int s,float t,int a){p.setColor(Color.rgb(238,206,160));c.drawCircle(360,370,62,p);p.setColor(Color.WHITE);c.drawCircle(340,360,8,p);c.drawCircle(380,360,8,p);p.setColor(a);p.setStrokeWidth(16);p.setStrokeCap(Paint.Cap.ROUND);c.drawLine(360,432,360,635,p);c.drawLine(360,490,250,565,p);c.drawLine(360,490,470,550,p);c.drawLine(360,635,285,730,p);c.drawLine(360,635,435,730,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);c.drawRoundRect(new RectF(170,260,550,750),40,40,p);p.setStyle(Paint.Style.FILL);bubble(c,530,295,s==0?"GO!":"STEP "+s,a);}
        void nightScene(Canvas c,int s,float t,int a){p.setColor(Color.rgb(214,40,57));p.setAlpha(80);c.drawCircle(360,470,190+8*(float)Math.sin(t),p);p.setAlpha(255);p.setColor(Color.rgb(18,19,25));c.drawRoundRect(new RectF(155,275,565,720),18,18,p);p.setColor(Color.rgb(238,222,198));p.setTextSize(25);p.setTextAlign(Paint.Align.LEFT);c.drawText("CASE 0"+(s+1),190,335,p);p.setColor(Color.rgb(190,190,180));c.drawRect(190,375,500,570,p);p.setColor(a);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(8);c.drawCircle(350,465,55+4*(float)Math.sin(t*2),p);p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(214,40,57));c.drawText("UNSOLVED",270,665,p);}
        void atlasScene(Canvas c,int s,float t,int a,int base){p.setColor(Color.rgb(238,222,182));c.drawRoundRect(new RectF(120,250,600,735),22,22,p);p.setColor(a);Path route=new Path();route.moveTo(200,650);route.cubicTo(280,570,240,480,340,450);route.cubicTo(440,410,380,330,520,300);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(12);c.drawPath(route,p);p.setStyle(Paint.Style.FILL);p.setColor(a);for(int i=0;i<4;i++)c.drawCircle(200+i*95,640-i*90,12,p);p.setColor(base);p.setTextSize(27);p.setTextAlign(Paint.Align.CENTER);c.drawText(s<2?"A STORY THROUGH TIME":"THE FINAL CHAPTER",360,700,p);}
        void tallTale(Canvas c,int s,float t,int a){p.setColor(Color.rgb(95,176,119));c.drawRect(0,600,W,760,p);p.setColor(Color.rgb(244,190,102));c.drawCircle(570,315,65,p);p.setColor(Color.rgb(240,176,111));c.drawRect(100,440,260,620,p);c.drawRect(430,390,590,620,p);p.setColor(Color.rgb(245,116,99));c.drawRoundRect(new RectF(220,450,365,660),45,45,p);p.setColor(Color.rgb(253,225,190));c.drawCircle(293,420,58,p);p.setColor(Color.WHITE);c.drawCircle(276,410,12,p);c.drawCircle(310,410,12,p);p.setColor(Color.rgb(24,28,38));c.drawCircle(278,410,5,p);c.drawCircle(312,410,5,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(8);p.setColor(a);c.drawArc(new RectF(270,424,320,460),0,180,false,p);p.setStyle(Paint.Style.FILL);}
        void wrapped(Canvas c,String text,float x,float y,float max,Paint paint){StringBuilder line=new StringBuilder();int row=0;for(String word:text.split(" ")){String v=line.length()==0?word:line+" "+word;if(paint.measureText(v)>max&&line.length()>0){c.drawText(line.toString(),x,y+row++*58,paint);line.setLength(0);if(row>=2)break;}if(line.length()>0)line.append(' ');line.append(word);}if(line.length()>0&&row<3)c.drawText(line.toString(),x,y+row*58,paint);}
    }
}
