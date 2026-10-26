package com.aula.fixa.ui.view.os;

import android.net.Uri;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.R;
import com.aula.fixa.adapter.foto.FotoViewModel;
import com.aula.fixa.adapter.foto.FotoViewModelFactory;
import com.aula.fixa.adapter.os.ExecucaoOSViewModel;
import com.aula.fixa.adapter.os.ExecucaoOSViewModelFactory;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.aula.fixa.ui.view.operations.OperationsScreenFragment;
import java.util.ArrayList;
import java.util.List;

/** Envia a observação de conclusão da OS com fotos já hospedadas. */
public final class ConclusaoOSFluxo {
    private final OperationsScreenFragment host;
    private final OperationsActivity activity;
    private final View root;
    private final long orderId;
    private final ExecucaoOSViewModel osViewModel;
    private final FotoViewModel fotoViewModel;
    private final List<String> fotos = new ArrayList<>();
    private int pendingPhotoUploads;

    public ConclusaoOSFluxo(OperationsScreenFragment host, OperationsActivity activity, View root, long orderId) {
        this.host=host; this.activity=activity; this.root=root; this.orderId=orderId;
        osViewModel=new ViewModelProvider(host,new ExecucaoOSViewModelFactory(host.requireContext())).get(ExecucaoOSViewModel.class);
        fotoViewModel=new ViewModelProvider(host,new FotoViewModelFactory(host.requireContext())).get(FotoViewModel.class);
    }

    public void bind() {
        TextView title=root.findViewById(R.id.ops_5717_6300); if(title!=null)title.setText("Finalizar OS");
        View back=root.findViewWithTag("image:arrow-left"); if(back!=null)back.setOnClickListener(v->activity.back());
        for(int id:new int[]{R.id.ops_5717_6312,R.id.ops_5717_6318,R.id.ops_5717_6315,
                R.id.ops_5717_6313,R.id.ops_5717_6319,R.id.ops_5717_6316,
                R.id.ops_shape_4,R.id.ops_shape_5,R.id.ops_shape_6,
                R.id.ops_5717_6308,R.id.ops_5717_6309,R.id.ops_5717_6310}){
            View v=root.findViewById(id);if(v!=null)v.setVisibility(View.GONE);
        }
        EditText difficulty=root.findViewById(R.id.ops_5717_6328_input);
        if(difficulty!=null){difficulty.setFocusable(false);difficulty.setOnClickListener(v->new AlertDialog.Builder(host.requireContext()).setTitle("Grau de dificuldade").setItems(new String[]{"Fácil","Médio","Difícil"},(d,which)->difficulty.setText(new String[]{"Fácil","Médio","Difícil"}[which])).show());}
        View addPhotos=root.findViewById(R.id.ops_5717_6298); if(addPhotos!=null)addPhotos.setOnClickListener(v->host.selectCompletionPhotos());
        TextView photoLabel=root.findViewById(R.id.ops_5717_6299); if(photoLabel!=null)photoLabel.setText("Adicionar fotos (obrigatório)");
        View cancel=root.findViewById(R.id.ops_5717_6323);if(cancel!=null)cancel.setOnClickListener(v->activity.back());
        View submit=root.findViewById(R.id.ops_5717_6325);if(submit!=null){if(submit instanceof TextView)((TextView)submit).setText("Finalizar");submit.setOnClickListener(v->submit(difficulty,(EditText)root.findViewById(R.id.ops_5717_6332),(EditText)root.findViewById(R.id.ops_5717_6336)));}
    }

    public void uploadPhotos(List<Uri> uris) {
        if(uris==null||uris.isEmpty())return;
        pendingPhotoUploads+=uris.size();updatePhotoCount();
        for(Uri uri:uris)fotoViewModel.enviarSolicitacao(uri,new FotoViewModel.Callback(){
            @Override public void aoSucesso(String url,String publicId){
                if(!host.isAdded())return;
                host.requireActivity().runOnUiThread(()->{synchronized(fotos){fotos.add(url);}pendingPhotoUploads--;updatePhotoCount();});
            }
            @Override public void aoReceberTokenExclusao(String token){}
            @Override public void aoFalhar(String message){if(host.isAdded())host.requireActivity().runOnUiThread(()->{pendingPhotoUploads--;updatePhotoCount();Toast.makeText(host.requireContext(),message,Toast.LENGTH_LONG).show();});}
        });
    }

    private void updatePhotoCount(){TextView label=root.findViewById(R.id.ops_5717_6299);if(label!=null)label.setText(pendingPhotoUploads>0?"Carregando fotos…":fotos.size()+" foto(s) adicionada(s)");}
    private void submit(EditText difficulty,EditText solution,EditText issues){
        String value=difficulty==null?"":String.valueOf(difficulty.getText()).trim();String code="Fácil".equals(value)?"FACIL":"Médio".equals(value)?"MEDIO":"Difícil".equals(value)?"DIFICIL":"";
        String observation=String.valueOf(solution==null?"":solution.getText()).trim();
        if(code.isEmpty()){Toast.makeText(host.requireContext(),"Selecione o grau de dificuldade.",Toast.LENGTH_SHORT).show();return;}
        if(pendingPhotoUploads>0){Toast.makeText(host.requireContext(),"Aguarde o envio das fotos.",Toast.LENGTH_SHORT).show();return;}
        if(fotos.isEmpty()){Toast.makeText(host.requireContext(),"Adicione ao menos uma foto.",Toast.LENGTH_SHORT).show();return;}
        if(!String.valueOf(issues==null?"":issues.getText()).trim().isEmpty())observation+=(observation.isEmpty()?"":"\n\n")+"Dificuldades encontradas: "+issues.getText();
        android.app.Dialog loading=AppLoading.show(host.requireContext());
        List<String> uploaded; synchronized(fotos){uploaded=new ArrayList<>(fotos);}
        osViewModel.finalizar(orderId,code,observation,uploaded,new ExecucaoOSViewModel.Callback<Void>(){
            @Override public void success(Void ignored){AppLoading.hide(loading);activity.openName("execution_completed");}
            @Override public void failure(Throwable error){AppLoading.hide(loading);Toast.makeText(host.requireContext(),com.aula.fixa.adapter.auth.AutenticacaoViewModel.messageFrom(error),Toast.LENGTH_LONG).show();}
        });
    }
}
