package com.aula.fixa.ui.operations;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

/** Entrada e diagnóstico disponíveis somente no build debug. */
public final class FlowPreviewActivity extends OperationsActivity {
    @Override protected void onCreate(Bundle state) {
        getIntent().putExtra("ops_preview", true);
        super.onCreate(state);
        getWindow().getDecorView().postDelayed(this::audit, 800);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)androidx.core.view.WindowCompat.getInsetsController(getWindow(),getWindow().getDecorView())
            .hide(androidx.core.view.WindowInsetsCompat.Type.systemBars());
    }

    void audit() {
        try {
            String key=getIntent().getStringExtra("screen");
            if(key==null)return;
            JSONObject spec=ScreenCatalog.get(this).screen(key);
            JSONArray data=new JSONArray();
            View base=getWindow().getDecorView();
            androidx.fragment.app.Fragment dialog=getSupportFragmentManager().findFragmentByTag("ops_dialog");
            if(dialog instanceof androidx.fragment.app.DialogFragment) {
                android.app.Dialog window=((androidx.fragment.app.DialogFragment)dialog).getDialog();
                if(window!=null&&window.getWindow()!=null)base=window.getWindow().getDecorView();
            }
            JSONArray bindings=spec.getJSONArray("bindings");
            for(int i=0;i<bindings.length();i++) {
                JSONObject binding=bindings.getJSONObject(i);
                int id=getResources().getIdentifier(binding.getString("id"),"id",getPackageName());
                View view=base.findViewById(id);
                if(view==null)continue;
                int[] location=new int[2];view.getLocationOnScreen(location);
                JSONObject item=new JSONObject().put("id",binding.getString("id"))
                    .put("kind",binding.getString("kind"))
                    .put("bounds",new JSONArray(new int[]{location[0],location[1],view.getWidth(),view.getHeight()}));
                if(view instanceof TextView) {
                    TextView text=(TextView)view;
                    android.graphics.Paint.FontMetrics fm=text.getPaint().getFontMetrics();
                    item.put("text",text.getText()).put("textSize",text.getTextSize())
                        .put("textWidth",text.getPaint().measureText(text.getText().toString()))
                        .put("fontHeight",fm.descent-fm.ascent)
                        .put("lines",text.getLineCount());
                }
                data.put(item);
            }
            try(FileOutputStream out=new FileOutputStream(new File(getCacheDir(),"operations-audit.json"))) {
                out.write(new JSONObject().put("screen",key).put("density",getResources().getDisplayMetrics().density).put("views",data).toString().getBytes(StandardCharsets.UTF_8));
            }
        } catch(Exception e) { android.util.Log.e("FIXA-review","Não foi possível registrar a medição",e); }
    }
}
