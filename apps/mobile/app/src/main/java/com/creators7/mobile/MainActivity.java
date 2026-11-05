package com.creators7.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.widget.*;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final java.util.concurrent.ExecutorService worker = Executors.newSingleThreadExecutor();
    private EditText topic; private Spinner style; private TextView status; private Button make;
    private final String[] styles={"Inside Out · science","Night File · mystery storyboard","Deep Dive · science storyboard","Stick Story · survival","Paper Atlas · history","Tall Tale · comedy storyboard"};
    @Override public void onCreate(Bundle saved){super.onCreate(saved);getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().setStatusBarColor(Color.rgb(12,14,24));getWindow().setNavigationBarColor(Color.rgb(12,14,24));
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(24),dp(30),dp(24),dp(24));root.setBackgroundColor(Color.rgb(12,14,24));ScrollView scroll=new ScrollView(this);scroll.addView(root);setContentView(scroll);
        root.addView(label("CREATOR S7",13,Color.rgb(95,230,196),true));root.addView(label("Make your first Short",29,Color.WHITE,true),margin(14,0));root.addView(label("Enter a topic and render a vertical MP4 on this phone.",15,Color.rgb(176,184,204),false),margin(8,22));
        root.addView(label("VIDEO TOPIC",12,Color.rgb(176,184,204),true));topic=new EditText(this);topic.setHint("e.g. What happens if you swallow gum?");topic.setHintTextColor(Color.rgb(112,120,143));topic.setTextColor(Color.WHITE);topic.setTextSize(16);topic.setMinLines(2);topic.setMaxLines(3);topic.setGravity(Gravity.TOP);topic.setPadding(dp(14),dp(12),dp(14),dp(12));topic.setBackgroundColor(Color.rgb(29,33,49));topic.setText("What happens if you swallow gum?");root.addView(topic,margin(8,20));
        root.addView(label("VISUAL STYLE",12,Color.rgb(176,184,204),true));style=new Spinner(this);style.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,styles));root.addView(style,margin(8,24));
        TextView info=label("12 seconds  ·  9:16 portrait  ·  720 × 1280\nOn-device narration + illustrated scenes. No model or API download.",13,Color.rgb(199,207,225),false);info.setPadding(dp(16),dp(15),dp(16),dp(15));info.setBackgroundColor(Color.rgb(25,30,43));root.addView(info,margin(0,22));
        make=new Button(this);make.setText("Create test video");make.setTextSize(16);make.setAllCaps(false);make.setTextColor(Color.rgb(9,22,24));make.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(95,230,196)));root.addView(make,new LinearLayout.LayoutParams(-1,dp(54)));status=label("Ready when you are.",14,Color.rgb(176,184,204),false);root.addView(status,margin(16,18));
        make.setOnClickListener(v->{String value=topic.getText().toString().trim();int selectedStyle=style.getSelectedItemPosition();if(value.isEmpty()){topic.setError("Enter a topic first");return;}make.setEnabled(false);status.setText("Preparing narration and drawing scenes… keep Creator S7 open for about 20 seconds.");worker.execute(()->{try{ShortRenderer.render(this,value,selectedStyle);runOnUiThread(()->{status.setText("Video with narration saved to Movies/Creator S7.\nOpen Gallery or Photos to play it.");make.setEnabled(true);Toast.makeText(this,"Narrated video created",Toast.LENGTH_LONG).show();});}catch(Exception e){runOnUiThread(()->{status.setText("Could not render: "+(e.getMessage()==null?"unknown error":e.getMessage()));make.setEnabled(true);});}});});
        root.addView(label("USE THE FULL FACELESS SHORTS RENDERER",12,Color.rgb(95,230,196),true),margin(0,8));
        root.addView(label("Open the Colab notebook in Chrome. Sign in, choose T4 if offered, then run its cells. First setup downloads about 0.9 GB—use Wi-Fi.",13,Color.rgb(176,184,204),false),margin(0,10));
        Button colab=new Button(this);colab.setText("Open Colab Renderer");colab.setTextSize(15);colab.setAllCaps(false);root.addView(colab,new LinearLayout.LayoutParams(-1,dp(50)));
        colab.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://colab.research.google.com/github/SparrowO7/Creator-S7/blob/main/notebooks/CreatorS7-Colab-Render.ipynb"));startActivity(i);});
    }
    private TextView label(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout.LayoutParams margin(int top,int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(top),0,dp(bottom));return p;}
    private int dp(int v){return(int)(v*getResources().getDisplayMetrics().density);}
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}
