package com.aula.fixa.ui.operations;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.DialogFragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.Assert.*;

/** Verifica inflação e navegação reais; a fidelidade é revisada nas capturas. */
@RunWith(AndroidJUnit4.class)
public final class OperationsScreensTest {
    @Test public void inflateAndCaptureDesignStates() throws Exception {
        Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();
        Context context=instrumentation.getTargetContext();
        Bundle args=InstrumentationRegistry.getArguments();
        String selection=args.getString("screens","");
        String variant=args.getString("variant","412");
        File out=new File(context.getFilesDir(),"operations-review/"+variant);
        assertTrue(out.exists()||out.mkdirs());
        JSONArray report=new JSONArray();
        int failures=0;
        for(String role:new String[]{"manager","technician"}) {
            Intent intent=new Intent().setClassName(context,"com.aula.fixa.ui.operations.FlowPreviewActivity")
                    .putExtra("screen",role+"_home").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            try(ActivityScenario<OperationsActivity> scenario=ActivityScenario.launch(intent)) {
                instrumentation.waitForIdleSync();
                for(String key:ScreenCatalog.get(context).all().keySet()) {
                    if(!key.startsWith(role+"_"))continue;
                    if(!selection.isEmpty()&&!java.util.Arrays.asList(selection.split(",")).contains(key))continue;
                    JSONObject result=new JSONObject().put("key",key).put("frame",ScreenCatalog.get(context).screen(key).getString("frame"));
                    try {
                        scenario.onActivity(activity->{
                            androidx.fragment.app.Fragment dialog=activity.getSupportFragmentManager().findFragmentByTag("ops_dialog");
                            if(dialog instanceof DialogFragment)((DialogFragment)dialog).dismissNow();
                            activity.getIntent().putExtra("screen",key);
                            activity.open(key);
                        });
                        instrumentation.waitForIdleSync();
                        Thread.sleep(350);
                        scenario.onActivity(activity->{
                            JSONObject spec=ScreenCatalog.get(activity).screen(key);
                            View base=activity.getWindow().getDecorView();
                            androidx.fragment.app.Fragment f=activity.getSupportFragmentManager().findFragmentByTag("ops_dialog");
                            if(f instanceof DialogFragment&&((DialogFragment)f).getDialog()!=null)
                                base=((DialogFragment)f).requireDialog().getWindow().getDecorView();
                            int found=0;
                            JSONArray bindings=spec.optJSONArray("bindings");
                            for(int i=0;bindings!=null&&i<bindings.length();i++) {
                                int id=activity.getResources().getIdentifier(bindings.optJSONObject(i).optString("id"),"id",activity.getPackageName());
                                if(base.findViewById(id)!=null)found++;
                            }
                            assertTrue("Nenhum componente nativo encontrado em "+key,found>0);
                            if(activity instanceof FlowPreviewActivity)((FlowPreviewActivity)activity).audit();
                            try{result.put("visibleBindings",found).put("uiVerified",true);}catch(Exception e){throw new AssertionError(e);}
                        });
                        instrumentation.waitForIdleSync();
                        Bitmap bitmap=instrumentation.getUiAutomation().takeScreenshot();
                        assertNotNull("Captura ausente: "+key,bitmap);
                        try(FileOutputStream stream=new FileOutputStream(new File(out,key+".png"))){bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);}
                        bitmap.recycle();
                        File source=new File(context.getCacheDir(),"operations-audit.json");
                        if(source.exists()) {
                            try(FileInputStream from=new FileInputStream(source);FileOutputStream to=new FileOutputStream(new File(out,key+"-geometry.json"))) {
                                byte[] buffer=new byte[8192];int count;while((count=from.read(buffer))!=-1)to.write(buffer,0,count);
                            }
                        }
                        result.put("captured",true).put("image",key+".png");
                    } catch(Throwable failure) {
                        failures++;result.put("captured",false).put("error",failure.toString());
                    }
                    report.put(result);
                    try(FileOutputStream stream=new FileOutputStream(new File(out,"capture-report.json"))) {
                        stream.write(report.toString(2).getBytes(StandardCharsets.UTF_8));
                    }
                    Bundle progress=new Bundle();progress.putString("stream",key+" "+(result.optBoolean("captured")?"OK":"FALHOU")+"\n");
                    instrumentation.sendStatus(0,progress);
                }
            }
        }
        assertEquals("Confira capture-report.json para as telas que falharam",0,failures);
    }
}
