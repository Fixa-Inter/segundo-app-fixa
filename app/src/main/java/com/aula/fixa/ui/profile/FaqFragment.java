package com.aula.fixa.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.model.FaqItem;

import java.util.Arrays;
import java.util.List;

public class FaqFragment extends Fragment {

    public FaqFragment() {
        super(R.layout.fragment_faq);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.tvPageTitle)).setText(R.string.app_faq_title);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        String answer = getString(R.string.app_faq_answer_sample);
        RecyclerView list = view.findViewById(R.id.rvFaq);
        list.setAdapter(new FaqAdapter(Arrays.asList(
                new FaqItem(getString(R.string.app_faq_question_sample), answer),
                new FaqItem("Como abrir uma solicitação?", answer),
                new FaqItem("Como acompanhar o status?", answer),
                new FaqItem("Quem aprova as solicitações?", answer),
                new FaqItem("Posso editar uma solicitação?", answer),
                new FaqItem("Como falar com a Manu?", answer),
                new FaqItem("Como alterar meus dados?", answer)
        )));
    }

    static class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.ViewHolder> {
        private final List<FaqItem> items;

        FaqAdapter(List<FaqItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app_faq, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FaqItem item = items.get(position);
            holder.question.setText(item.question);
            holder.answer.setText(item.answer);
            holder.answer.setVisibility(item.expanded ? View.VISIBLE : View.GONE);
            holder.chevron.setRotation(item.expanded ? 180f : 0f);
            holder.questionRow.setOnClickListener(v -> {
                item.expanded = !item.expanded;
                notifyItemChanged(holder.getBindingAdapterPosition());
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final View questionRow, chevron;
            final TextView question, answer;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                questionRow = itemView.findViewById(R.id.layoutQuestion);
                chevron = itemView.findViewById(R.id.ivChevron);
                question = itemView.findViewById(R.id.tvQuestion);
                answer = itemView.findViewById(R.id.tvAnswer);
            }
        }
    }
}
