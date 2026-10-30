package com.aula.fixa.ui.view.operations;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.adapter.operations.OperationsViewModel;
import com.aula.fixa.domain.model.home.ManagerHomeData;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.text.SimpleDateFormat;

/** Estado de apresentação local para a revisão dos fluxos, sem simular gravações na API. */
public final class OperationsData {
    private OperationsData(){}
    public static void bind(OperationsActivity activity,View root,JSONObject screen) {
        String key=screen.optString("key"), name=screen.optString("name"), scope=OperationsViewModel.scope(name);
        if(Arrays.asList("categories","models","devices","planning_tasks","checklist_fields").contains(name)) {
            JSONArray bindings=screen.optJSONArray("bindings");
            double bottom=160;
            for(int i=0;bindings!=null&&i<bindings.length();i++) {
                JSONObject b=bindings.optJSONObject(i);if(b==null||!b.optString("kind").equals("card"))continue;
                int id=activity.getResources().getIdentifier(b.optString("id"),"id",activity.getPackageName());
                View card=root.findViewById(id);if(card==null)continue;
                if(activity.model.deleted(b.optString("id")))card.setVisibility(View.GONE);
                for(Bundle entry:activity.model.entries(scope))if(b.optString("id").equals(entry.getString("id"))) {
                    TextView title=largest(card);if(title!=null)title.setText(entry.getString("title"));
                }
                JSONArray bounds=b.optJSONArray("bounds");bottom=Math.max(bottom,bounds.optDouble(1)+bounds.optDouble(3));
            }
            ConstraintLayout content=findContent(root);
            if(content!=null) {
                LinearLayout rows=new LinearLayout(activity);rows.setOrientation(LinearLayout.VERTICAL);rows.setId(View.generateViewId());
                ConstraintLayout.LayoutParams params=new ConstraintLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.startToStart=ConstraintLayout.LayoutParams.PARENT_ID;params.endToEnd=ConstraintLayout.LayoutParams.PARENT_ID;
                params.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
                params.topMargin=(int)((bottom+16)*scale(activity));
                params.setMarginStart(activity.getResources().getDimensionPixelSize(R.dimen.ops_list_side));
                params.setMarginEnd(activity.getResources().getDimensionPixelSize(R.dimen.ops_list_side));
                content.addView(rows,params);
                for(Bundle entry:activity.model.entries(scope)) {
                    if(entry.getString("id","").startsWith("ops_"))continue;
                    View row=activity.getLayoutInflater().inflate(R.layout.item_ops_local_entry,rows,false);
                    ((TextView)row.findViewById(R.id.opsEntryTitle)).setText(entry.getString("title"));
                    ((TextView)row.findViewById(R.id.opsEntryDescription)).setText(entry.getString("description"));
                    row.setOnClickListener(v->{activity.model.selectEntry(scope,entry.getString("id"),entry.getString("title"));activity.openName(editor(scope));});
                    row.setOnLongClickListener(v->{activity.model.selectEntry(scope,entry.getString("id"),entry.getString("title"));activity.openName("delete_confirm");return true;});
                    rows.addView(row);
                }
            }
        }
        if(name.equals("occurrence_result")) photos(activity,root,screen);
        if(name.equals("occurrence_review"))review(activity,root,screen);
        if(name.startsWith("execution_")) {
            JSONArray bindings=screen.optJSONArray("bindings");
            for(int i=0;bindings!=null&&i<bindings.length();i++) {
                JSONObject item=bindings.optJSONObject(i);if(item==null||!item.optString("kind").equals("card"))continue;
                String id=item.optString("id");
                if(activity.model.selected("task:"+id)) {
                    View card=root.findViewById(activity.getResources().getIdentifier(id,"id",activity.getPackageName()));
                    if(card!=null){card.setSelected(true);card.setForeground(new android.graphics.drawable.ColorDrawable(androidx.core.content.ContextCompat.getColor(activity,R.color.ops_selected_overlay)));}
                }
            }
        }
        if ("manager_home".equals(key)) bindEmptyManagerHome(activity, root);
    }

    public static void bindEmptyManagerHome(OperationsActivity activity, View root) {
        TextView greeting = root.findViewById(R.id.ops_5717_7299);
        if (greeting != null) greeting.setText("");
        for (int id : new int[]{7266, 7283}) {
            View mock = root.findViewById(activity.getResources().getIdentifier(
                    "ops_5717_" + id, "id", activity.getPackageName()));
            if (mock != null) mock.setVisibility(View.GONE);
        }
        bindOrderBigNumbers(activity, root, R.id.ops_5717_7307, R.id.ops_5717_7309, 0, 0, 0, 0);
    }

    public static void bindManagerHome(OperationsActivity activity, View root, ManagerHomeData data) {
        TextView greeting = root.findViewById(R.id.ops_5717_7299);
        if (greeting != null) greeting.setText(activity.getString(R.string.ops_home_greeting,
                data.nomeGestor.trim().isEmpty() ? activity.getString(R.string.ops_home_unknown_manager) : data.nomeGestor.trim()));
        loadAvatar(activity, root.findViewById(R.id.ops_5717_7336), data.fotoGestor);

        int completed = 0, active = 0, notStarted = 0, overdue = 0;
        for (ManagerHomeData.OrdemServicoHomeItem order : data.ordens) {
            String status = normalizeStatus(order.status);
            if (status.contains("CONCLUID")) completed++;
            else if (status.contains("ANDAMENTO")) active++;
            else if (status.contains("NAO_INICIADA") || status.equals("NAOINICIADA")) notStarted++;
            else if (status.contains("ATRASAD")) overdue++;
        }
        bindOrderBigNumbers(activity, root, R.id.ops_5717_7307, R.id.ops_5717_7309,
                notStarted, completed, active, overdue);
        bindRecentRequests(activity, root, data.solicitacoes);
    }

    public static void bindTechnicianHome(OperationsActivity activity, View root, com.aula.fixa.domain.model.Perfil profile,
                                          List<com.aula.fixa.domain.model.os.OrdemServicoQuadro> orders) {
        TextView greeting = root.findViewById(R.id.ops_5717_9921);
        String name = profile == null ? "" : profile.nomeCompleto;
        if (greeting != null) greeting.setText(profile == null ? "" : activity.getString(R.string.ops_home_greeting,
                name.trim().isEmpty() ? "usuário" : name.trim()));
        if (greeting != null) greeting.setOnClickListener(v -> activity.openName("profile"));
        ImageView avatar = loadAvatar(activity, root.findViewById(R.id.ops_5717_9958), profile == null ? null : profile.urlFotoPerfil);
        if (avatar != null) avatar.setOnClickListener(v -> activity.openName("profile"));
        View notifications = root.findViewById(R.id.ops_5717_9922);
        if (notifications != null) notifications.setOnClickListener(v -> activity.openName("notifications"));
        int completed = 0, active = 0, notStarted = 0, overdue = 0;
        List<com.aula.fixa.domain.model.os.OrdemServicoQuadro> recent = new ArrayList<>();
        List<com.aula.fixa.domain.model.os.OrdemServicoQuadro> sorted = orders == null
                ? new ArrayList<>() : new ArrayList<>(orders);
        sorted.sort((a, b) -> Long.compare(orderTimestamp(b.dataCriacao), orderTimestamp(a.dataCriacao)));
        for (com.aula.fixa.domain.model.os.OrdemServicoQuadro order : sorted) {
            String status = normalizeStatus(order.status);
            if (status.contains("CONCLUID")) completed++;
            else if (status.contains("ANDAMENTO")) active++;
            else if (status.contains("NAO_INICIADA") || status.equals("NAOINICIADA")) notStarted++;
            else if (status.contains("ATRASAD")) overdue++;
            if (!status.contains("CONCLUID") && recent.size() < 5) recent.add(order);
        }
        bindOrderBigNumbers(activity, root, R.id.ops_5717_9929, R.id.ops_5717_9931,
                notStarted, completed, active, overdue);
        for (int id : new int[]{R.id.ops_5717_9888, R.id.ops_5717_9905}) {
            View mock = root.findViewById(id);
            if (mock != null) mock.setVisibility(View.GONE);
        }
        if (root.findViewById(R.id.ops_5717_9920).getParent() instanceof ConstraintLayout) {
            ConstraintLayout parent = (ConstraintLayout) root.findViewById(R.id.ops_5717_9920).getParent();
            View old = parent.findViewWithTag("technician_home_recent_orders");
            if (old != null) parent.removeView(old);
            RecyclerView list = new RecyclerView(activity);
            list.setTag("technician_home_recent_orders");
            list.setNestedScrollingEnabled(false);
            list.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(activity));
            list.setAdapter(new com.aula.fixa.ui.adapter.os.QuadroOSAdapter(new com.aula.fixa.ui.adapter.os.QuadroOSAdapter.Listener() {
                @Override public void onClick(com.aula.fixa.domain.model.os.OrdemServicoQuadro order) { activity.openOrderDetails(order.id, order.titulo); }
                @Override public void onMenuClick(View anchor, com.aula.fixa.domain.model.os.OrdemServicoQuadro order) { }
            }, false));
            ((com.aula.fixa.ui.adapter.os.QuadroOSAdapter) list.getAdapter()).submit(recent);
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topToBottom = R.id.ops_5717_9920;
            lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            lp.topMargin = Math.round(12 * scale(activity));
            parent.addView(list, lp);
        }
    }

    private static long orderTimestamp(String value) {
        if (value == null || value.trim().isEmpty()) return Long.MIN_VALUE;
        try { return java.time.OffsetDateTime.parse(value).toInstant().toEpochMilli(); }
        catch (Exception ignored) { }
        try { return java.time.LocalDateTime.parse(value).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(); }
        catch (Exception ignored) { }
        try { return java.time.LocalDate.parse(value).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(); }
        catch (Exception ignored) { return Long.MIN_VALUE; }
    }

    private static String normalizeStatus(String value) {
        return value == null ? "" : java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private static void bindOrderBigNumbers(OperationsActivity activity, View root, int cardId, int titleId,
                                             int notStarted, int completed, int active, int overdue) {
        View cardView = root.findViewById(cardId);
        if (!(cardView instanceof ConstraintLayout)) return;
        ConstraintLayout card = (ConstraintLayout) cardView;
        TextView title = card.findViewById(titleId);
        if (title != null) title.setVisibility(View.GONE);

        View previous = card.findViewWithTag("ops_home_order_big_numbers");
        if (previous != null) card.removeView(previous);
        for (int i = 0; i < card.getChildCount(); i++) {
            View child = card.getChildAt(i);
            if (child != title) child.setVisibility(View.GONE);
        }

        card.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        card.setElevation(0f);
        ViewGroup.LayoutParams cardParams = card.getLayoutParams();
        if (cardParams != null) {
            cardParams.height = dp(activity, 318);
            card.setLayoutParams(cardParams);
        }

        LinearLayout grid = new LinearLayout(activity);
        grid.setTag("ops_home_order_big_numbers");
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setClipChildren(false);
        grid.setClipToPadding(false);
        grid.setPadding(0, dp(activity, 4), 0, dp(activity, 4));
        ConstraintLayout.LayoutParams gridParams = new ConstraintLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT);
        gridParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        gridParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        gridParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        gridParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        gridParams.setMarginStart(dp(activity, 6));
        gridParams.setMarginEnd(dp(activity, 6));
        card.addView(grid, gridParams);

        addOrderStatusRow(activity, grid,
                new OrderStatus(R.string.ops_home_order_not_started_short, R.drawable.lupa_analise, notStarted),
                new OrderStatus(R.string.ops_home_order_completed, R.drawable.check_concluido, completed), false);
        addOrderStatusRow(activity, grid,
                new OrderStatus(R.string.ops_home_order_in_progress_short, R.drawable.joia_aprovado, active),
                new OrderStatus(R.string.ops_home_order_overdue, R.drawable.x_reprovado, overdue), true);
    }

    private static void addOrderStatusRow(OperationsActivity activity, LinearLayout grid,
                                           OrderStatus first, OrderStatus second, boolean secondRow) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 148));
        if (secondRow) rowParams.topMargin = dp(activity, 14);
        grid.addView(row, rowParams);
        addOrderStatusCard(activity, row, first, true);
        addOrderStatusCard(activity, row, second, false);
    }

    private static void addOrderStatusCard(OperationsActivity activity, LinearLayout row,
                                            OrderStatus status, boolean firstColumn) {
        View card = activity.getLayoutInflater().inflate(R.layout.item_app_home_order_status, row, false);
        ((ImageView) card.findViewById(R.id.ivOrderStatusIcon)).setImageResource(status.icon);
        ((TextView) card.findViewById(R.id.tvOrderStatusLabel)).setText(status.label);
        ((TextView) card.findViewById(R.id.tvOrderStatusValue)).setText(String.valueOf(status.count));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, dp(activity, 140), 1f);
        params.setMargins(dp(activity, firstColumn ? 0 : 12), 0,
                dp(activity, firstColumn ? 12 : 0), 0);
        row.addView(card, params);
    }

    private static int dp(OperationsActivity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    private static final class OrderStatus {
        @StringRes final int label;
        final int icon;
        final int count;

        OrderStatus(@StringRes int label, int icon, int count) {
            this.label = label;
            this.icon = icon;
            this.count = count;
        }
    }

    private static void bindRecentRequests(OperationsActivity activity, View root,
                                           List<ManagerHomeData.SolicitacaoHomeItem> requests) {
        ConstraintLayout content = findContent(root);
        if (content == null) return;
        View previous = content.findViewWithTag("manager_home_recent_requests");
        if (previous != null) content.removeView(previous);
        LinearLayout list = new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setTag("manager_home_recent_requests");
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToBottom = R.id.ops_5717_7298;
        params.topMargin = Math.round(13 * scale(activity));
        int sideMargin = activity.getResources().getDimensionPixelSize(
                R.dimen.ops_manager_home_card_side_margin);
        params.setMarginStart(sideMargin);
        params.setMarginEnd(sideMargin);
        content.addView(list, params);
        if (requests.isEmpty()) {
            list.setGravity(android.view.Gravity.CENTER);
            ImageView image = new ImageView(activity);
            image.setImageResource(R.drawable.img_empty_results);
            image.setAdjustViewBounds(true);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            int imageSize = Math.round(92 * scale(activity));
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageSize, imageSize);
            imageParams.bottomMargin = Math.round(4 * scale(activity));
            list.addView(image, imageParams);

            TextView empty = new TextView(activity);
            empty.setText(R.string.app_home_requests_empty);
            empty.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            empty.setTextColor(ContextCompat.getColor(activity, R.color.app_text_navy));
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setIncludeFontPadding(false);
            list.addView(empty, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return;
        }
        for (ManagerHomeData.SolicitacaoHomeItem request : requests) {
            View card = activity.getLayoutInflater().inflate(R.layout.item_ops_manager_home_request, list, false);
            ((TextView) card.findViewById(R.id.opsHomeRequestDate)).setText(formatDate(request.dataCriacao));
            ((TextView) card.findViewById(R.id.opsHomeRequestTitle)).setText(request.titulo.trim().isEmpty()
                    ? activity.getString(R.string.ops_home_untitled_request) : request.titulo.trim());
            String location = (request.local + (request.descricaoLocal.isEmpty() ? "" : " - " + request.descricaoLocal)).trim();
            ((TextView) card.findViewById(R.id.opsHomeRequestLocation)).setText(location.isEmpty()
                    ? activity.getString(R.string.ops_home_unknown_location) : location);
            String requester = request.solicitante.trim().isEmpty()
                    ? activity.getString(R.string.ops_home_unknown_requester) : request.solicitante.trim();
            ((TextView) card.findViewById(R.id.opsHomeRequestOwner)).setText(
                    activity.getString(R.string.ops_home_request_owner, requester));
            loadAvatar(activity, card.findViewById(R.id.opsHomeRequestAvatar), null);
            card.setContentDescription(request.titulo + ", " + location + ", " + requester);
            list.addView(card);
        }
    }

    private static ImageView loadAvatar(OperationsActivity activity, ImageView image, String url) {
        if (image != null) Glide.with(activity)
                .load(url == null || url.trim().isEmpty() ? null : url.trim())
                .placeholder(R.drawable.img_avatar_115).fallback(R.drawable.img_avatar_115)
                .error(R.drawable.img_avatar_115).circleCrop().into(image);
        return image;
    }

    private static String formatDate(String value) {
        if (value == null || value.trim().isEmpty()) return "Data não informada";
        try {
            String date = value.trim();
            Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(date.substring(0, Math.min(10, date.length())));
            return new SimpleDateFormat("dd MMM yyyy", new Locale("pt", "BR")).format(parsed);
        } catch (Exception ignored) { return "Data não informada"; }
    }

    private static void review(OperationsActivity activity,View root,JSONObject screen) {
        String role=activity.role(), title=activity.model.field(role+"_occurrence_service","Título"), description=activity.model.field(role+"_occurrence_service","Problema encontrado");
        String place=activity.model.field(role+"_occurrence_location","Local"),date=activity.model.field(role+"_occurrence_location","Data"),solution=activity.model.field(role+"_occurrence_result","Solução aplicada"),difficulty=activity.model.field(role+"_occurrence_result","Grau de dificuldade");
        JSONArray bindings=screen.optJSONArray("bindings");
        for(int i=0;bindings!=null&&i<bindings.length();i++) {
            JSONObject b=bindings.optJSONObject(i);if(b==null||!b.optString("kind").equals("text"))continue;
            View view=root.findViewById(activity.getResources().getIdentifier(b.optString("id"),"id",activity.getPackageName()));
            if(!(view instanceof TextView))continue;TextView text=(TextView)view;String original=b.optString("text");
            if(original.equals("Ar-condicionado quebrado")&&title!=null)text.setText(title);
            else if(original.startsWith("O ar-condicionado")&&description!=null)text.setText(description);
            else if(original.startsWith("Local:")&&place!=null)text.setText("Local: "+place+(date==null?"":" - "+date));
            else if(original.startsWith("Solução aplicada:")&&solution!=null)text.setText("Solução aplicada: "+solution);
            else if(original.startsWith("Dificuldade:")&&difficulty!=null)text.setText("Dificuldade: "+difficulty);
        }
    }
    private static float scale(OperationsActivity activity) {
        float dp=activity.getResources().getDisplayMetrics().widthPixels/activity.getResources().getDisplayMetrics().density;
        return activity.getResources().getDisplayMetrics().density*Math.min(dp,640f)/412f;
    }
    private static String editor(String scope) {
        switch(scope){case "categories":return "edit_category";case "models":return "edit_model";case "devices":return "edit_device";case "tasks":return "edit_task";default:return "edit_field";}
    }
    private static ConstraintLayout findContent(View root) {
        if(root instanceof ConstraintLayout)return (ConstraintLayout)root;
        if(root instanceof ViewGroup)for(int i=0;i<((ViewGroup)root).getChildCount();i++){ConstraintLayout found=findContent(((ViewGroup)root).getChildAt(i));if(found!=null)return found;}
        return null;
    }
    private static TextView largest(View root) {
        TextView result=root instanceof TextView?(TextView)root:null;
        if(root instanceof ViewGroup)for(int i=0;i<((ViewGroup)root).getChildCount();i++) {
            TextView candidate=largest(((ViewGroup)root).getChildAt(i));
            if(candidate!=null&&(result==null||candidate.getTextSize()>result.getTextSize()))result=candidate;
        }
        return result;
    }
    private static void photos(OperationsActivity activity,View root,JSONObject screen) {
        String value=activity.model.field(screen.optString("key"),"photos");if(value==null)return;
        try {
            JSONArray uris=new JSONArray(value);if(uris.length()==0)return;
            JSONArray bindings=screen.optJSONArray("bindings");
            for(int i=0;i<bindings.length();i++) {
                JSONObject binding=bindings.getJSONObject(i);
                if(!binding.optString("text").equals("Adicionar fotos"))continue;
                View selected=root.findViewById(activity.getResources().getIdentifier(binding.optString("id"),"id",activity.getPackageName()));
                if(selected==null)continue;
                View parent=selected.getParent() instanceof View?(View)selected.getParent():selected;
                while(!(parent instanceof ConstraintLayout)&&parent.getParent() instanceof View)parent=(View)parent.getParent();
                if(!(parent instanceof ConstraintLayout))continue;
                ConstraintLayout holder=(ConstraintLayout)parent;
                for(int c=0;c<holder.getChildCount();c++)holder.getChildAt(c).setVisibility(View.GONE);
                View strip=activity.getLayoutInflater().inflate(R.layout.view_ops_photos,holder,false);
                ConstraintLayout.LayoutParams params=new ConstraintLayout.LayoutParams(0,0);
                params.topToTop=0;params.bottomToBottom=0;params.startToStart=0;params.endToEnd=0;holder.addView(strip,params);
                LinearLayout row=strip.findViewById(R.id.opsPhotos);
                for(int u=0;u<uris.length();u++) {
                    ImageView thumb=(ImageView)activity.getLayoutInflater().inflate(R.layout.item_ops_photo,row,false);
                    Glide.with(activity).load(android.net.Uri.parse(uris.getString(u))).into(thumb);row.addView(thumb);
                }
                ImageView add=new ImageView(activity);int size=activity.getResources().getDimensionPixelSize(R.dimen.ops_touch_target);
                add.setLayoutParams(new LinearLayout.LayoutParams(size,size));add.setImageResource(R.drawable.figma_58b2a);
                add.setContentDescription(activity.getString(R.string.ops_add_photo));add.setOnClickListener(v->activity.photos());row.addView(add);
                break;
            }
        }catch(Exception ignored){ /* URI indisponível: o campo original continua utilizável. */ }
    }
}
