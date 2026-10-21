package com.aula.fixa.ui.operations;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.fragment.app.DialogFragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public final class OperationsInteractionTest {
    private ActivityScenario<OperationsActivity> launch(String key) {
        Context c=InstrumentationRegistry.getInstrumentation().getTargetContext();
        return ActivityScenario.launch(new Intent().setClassName(c,"com.aula.fixa.ui.operations.FlowPreviewActivity")
                .putExtra("screen",key).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
    private void idle(){InstrumentationRegistry.getInstrumentation().waitForIdleSync();}
    private View tagged(View view,String tag) {
        if(tag.equals(view.getTag()))return view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){
            View found=tagged(((ViewGroup)view).getChildAt(i),tag);if(found!=null)return found;
        }
        return null;
    }
    @Test public void categoryFormSavesLocalEntryAndClosesDialog() {
        try(ActivityScenario<OperationsActivity> scenario=launch("manager_categories")) {
            idle();scenario.onActivity(a->a.openName("create_category"));idle();
            scenario.onActivity(a->{
                DialogFragment dialog=(DialogFragment)a.getSupportFragmentManager().findFragmentByTag("ops_dialog");
                assertNotNull(dialog);
                View root=dialog.requireDialog().getWindow().getDecorView();
                EditText field=(EditText)tagged(root,"field:Nome");assertNotNull(field);
                field.setText("Bomba de revisão");
                View create=tagged(root,"action:Criar");assertNotNull(create);create.performClick();
                assertTrue(a.model.entries("categories").stream().anyMatch(r->"Bomba de revisão".equals(r.getString("title"))));
                assertNull(a.getSupportFragmentManager().findFragmentByTag("ops_dialog"));
            });
        }
    }
    @Test public void occurrenceDraftSurvivesActivityRecreation() {
        try(ActivityScenario<OperationsActivity> scenario=launch("manager_occurrence_service")) {
            idle();scenario.onActivity(a->{
                EditText field=(EditText)tagged(a.getWindow().getDecorView(),"field:Título");
                assertNotNull(field);field.setText("Manutenção da bomba");
            });
            scenario.recreate();idle();
            scenario.onActivity(a->{
                EditText field=(EditText)tagged(a.getWindow().getDecorView(),"field:Título");
                assertNotNull(field);assertEquals("Manutenção da bomba",field.getText().toString());
            });
        }
    }
    @Test public void approvalDialogNavigatesToConfirmationState() {
        try(ActivityScenario<OperationsActivity> scenario=launch("manager_request_details")) {
            idle();scenario.onActivity(a->a.openName("approve_request"));idle();
            scenario.onActivity(a->{
                DialogFragment dialog=(DialogFragment)a.getSupportFragmentManager().findFragmentByTag("ops_dialog");
                assertNotNull(dialog);View confirm=tagged(dialog.requireDialog().getWindow().getDecorView(),"action:Confirmar");
                assertNotNull(confirm);confirm.performClick();
            });idle();
            scenario.onActivity(a->{
                DialogFragment dialog=(DialogFragment)a.getSupportFragmentManager().findFragmentByTag("ops_dialog");
                assertNotNull(dialog);assertEquals("manager_request_approved",dialog.requireArguments().getString("screen"));
            });
        }
    }
}
