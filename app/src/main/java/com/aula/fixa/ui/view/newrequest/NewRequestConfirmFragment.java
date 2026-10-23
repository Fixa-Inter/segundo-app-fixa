package com.aula.fixa.ui.view.newrequest;

import com.aula.fixa.adapter.newrequest.SolicitacaoFormularioViewModel;

import android.os.Bundle;
import android.app.Dialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.AppText;
import com.aula.fixa.ui.config.FragmentUi;
import com.aula.fixa.ui.config.DataChangeNotifier;
import com.google.android.material.button.MaterialButton;
import com.aula.fixa.adapter.foto.FotoViewModel;
import com.aula.fixa.adapter.foto.FotoViewModelFactory;
import com.bumptech.glide.Glide;
import com.aula.fixa.adapter.solicitacao.SolicitacaoViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoViewModelFactory;
import com.aula.fixa.domain.model.NovaSolicitacao;
import com.aula.fixa.domain.model.Problema;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;

public class NewRequestConfirmFragment extends Fragment {
    private FotoViewModel fotoViewModel;

    public NewRequestConfirmFragment() {
        super(R.layout.fragment_new_request_confirm);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fotoViewModel = new ViewModelProvider(this, new FotoViewModelFactory(requireContext())).get(FotoViewModel.class);
        SolicitacaoFormularioViewModel form = new ViewModelProvider(NavHostFragment.findNavController(this)
                .getViewModelStoreOwner(R.id.nav_graph_app)).get(SolicitacaoFormularioViewModel.class);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        view.findViewById(R.id.btnStepBack).setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        MaterialButton finish = view.findViewById(R.id.btnStepNext);
        SolicitacaoViewModel viewModel = new androidx.lifecycle.ViewModelProvider(
                this, new SolicitacaoViewModelFactory(requireContext()))
                .get(SolicitacaoViewModel.class);
        finish.setText(R.string.app_finish);
        finish.setOnClickListener(v -> {
            if (form.localId <= 0 || form.categoryId <= 0 || form.photoUrls.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Selecione o local, a categoria e pelo menos uma foto.", android.widget.Toast.LENGTH_LONG).show();
                return;
            }

            NovaSolicitacao solicitacao = new NovaSolicitacao(form.title, form.description,
                    form.localId, form.locationDescription, form.categoryId, form.photoUrls);
            viewModel.criar(solicitacao, new SolicitacaoViewModel.Listener() {
                @Override public void aoCarregar(boolean carregando) {
                    FragmentUi.runIfViewActive(NewRequestConfirmFragment.this, view,
                            () -> finish.setEnabled(!carregando));
                }
                @Override public void aoCriar(Problema problema) {
                    FragmentUi.runIfViewActive(NewRequestConfirmFragment.this, view, () -> {
                        DataChangeNotifier.notifyChanged();
                        form.clear();
                        Navigation.findNavController(v).navigate(R.id.action_confirm_to_success);
                    });
                }
                @Override public void aoListar(java.util.List<Problema> problemas) { }
                @Override public void aoFalhar(Throwable erro) {
                    FragmentUi.runIfViewActive(NewRequestConfirmFragment.this, view,
                            () -> android.widget.Toast.makeText(view.getContext(),
                                    AutenticacaoViewModel.messageFrom(erro),
                                    android.widget.Toast.LENGTH_LONG).show());
                }
            });
        });

        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_confirm_title);
        AppText.setHtml(view.findViewById(R.id.tvSummaryDescriptionLabel), R.string.app_confirm_description);

        ((TextView) view.findViewById(R.id.tvSummaryTitle)).setText(form.title);
        ((TextView) view.findViewById(R.id.tvSummaryDescription)).setText(form.description);
        ((TextView) view.findViewById(R.id.tvSummaryLocationDescription)).setText(form.locationDescription);

        AppText.setHtml(view.findViewById(R.id.tvSummaryDevice), R.string.app_confirm_device, form.device);
        AppText.setHtml(view.findViewById(R.id.tvSummaryLocation), R.string.app_confirm_location, form.location);

        RecyclerView photos = view.findViewById(R.id.rvSummaryPhotos);
        photos.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        photos.setAdapter(new PhotoAdapter(form.photoUrls));
        view.findViewById(R.id.tvSummaryImagesLabel).setVisibility(form.photoUrls.isEmpty() ? View.GONE : View.VISIBLE);
        photos.setVisibility(form.photoUrls.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void deletePendingPhotos(SolicitacaoFormularioViewModel form) {
        for (String token : new java.util.ArrayList<>(form.photoDeleteTokens)) {
            fotoViewModel.excluir(token);
        }
        form.clear();
    }

    static class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
        private final java.util.List<String> urls;

        PhotoAdapter(java.util.List<String> urls) {
            this.urls = urls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app_photo_thumb, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext()).load(urls.get(position)).centerCrop().into((ImageView) holder.itemView);
            holder.itemView.setOnClickListener(v -> showPhoto(v.getContext(), urls.get(position)));
        }

        private void showPhoto(android.content.Context context, String url) {
            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            ImageView image = new ImageView(context);
            image.setBackgroundColor(Color.BLACK);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(context).load(url).into(image);
            image.setOnClickListener(v -> dialog.dismiss());
            dialog.setContentView(image);
            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.black);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                WindowManager.LayoutParams params = window.getAttributes();
                params.dimAmount = 0.85f;
                window.setAttributes(params);
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
