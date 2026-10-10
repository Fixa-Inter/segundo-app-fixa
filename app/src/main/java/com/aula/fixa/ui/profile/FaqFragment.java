package com.aula.fixa.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.model.FaqItem;

import java.util.ArrayList;
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
        List<FaqItem> items = new ArrayList<>();
        for (String question : getResources().getStringArray(R.array.app_faq_questions)) {
            items.add(new FaqItem(question, answer));
        }
        // Figma: a primeira pergunta aparece expandida.
        items.get(0).expanded = true;
        RecyclerView list = view.findViewById(R.id.rvFaq);
        list.setAdapter(new FaqAdapter(items));
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
            // Figma: expandido mostra "—"; recolhido, o chevron.
            holder.chevron.setImageResource(item.expanded ? R.drawable.ic_minus_24 : R.drawable.ic_chevron_down_22);
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
            final View questionRow;
            final ImageView chevron;
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
