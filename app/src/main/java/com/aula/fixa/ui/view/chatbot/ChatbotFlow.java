package com.aula.fixa.ui.view.chatbot;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.PopupWindow;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.aula.fixa.R;
import com.aula.fixa.ui.config.AppDialogs;
import com.aula.fixa.domain.model.chat.ChatEntry;
import com.aula.fixa.domain.model.chat.ChatHistory;
import com.aula.fixa.domain.model.chat.ChatTurn;
import com.aula.fixa.domain.model.chat.ChatPendingDraft;
import com.aula.fixa.domain.model.chat.ChatException;
import com.aula.fixa.adapter.chat.ChatViewModel;
import com.aula.fixa.adapter.chat.ChatViewModelFactory;
import com.aula.fixa.adapter.foto.FotoViewModel;
import com.aula.fixa.adapter.foto.FotoViewModelFactory;
import com.aula.fixa.ui.adapter.chat.ChatMessageAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** UI controller shared by the requester fragment and the manager/technician chatbot screen. */
public final class ChatbotFlow {
    public interface PhotoPicker { void open(); }
    private final Fragment fragment;
    private final View root;
    private final ChatViewModel viewModel;
    private final FotoViewModel fotoViewModel;
    private final PhotoPicker photoPicker;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final List<ChatEntry> messages = new ArrayList<>();
    private final List<ChatHistory> histories = new ArrayList<>();
    private final List<String> attachedUrls = new ArrayList<>();
    private final List<Uri> pendingPhotoUris = new ArrayList<>();
    private ChatMessageAdapter messageAdapter;
    private RecentAdapter recentAdapter;
    private RecyclerView messageList, recentList;
    private TextView greeting, inputHint;
    private TextView headerTitle;
    private View greetingLayout;
    private EditText input, search;
    private ProgressBar progress;
    private TextView recentEmpty;
    private TextView warning;
    private HorizontalScrollView attachmentPreview;
    private LinearLayout attachmentItems;
    private DrawerLayout drawer;
    private String selectedThreadId;
    private boolean pending;
    private boolean uploadingImages;
    private boolean destroyed;
    private String lastLiveDayKey;
    private long sendGeneration;
    private ChatPendingDraft unresolvedDraft;
    private ChatEntry unresolvedEntry;
    private ChatEntry typingEntry;

    public ChatbotFlow(Fragment fragment, View root, PhotoPicker photoPicker) {
        this.fragment=fragment; this.root=root; this.photoPicker=photoPicker;
        viewModel = new ViewModelProvider(fragment, new ChatViewModelFactory(fragment.requireContext())).get(ChatViewModel.class);
        fotoViewModel = new ViewModelProvider(fragment, new FotoViewModelFactory(fragment.requireContext())).get(FotoViewModel.class);
        unresolvedDraft = viewModel.loadDraft();
        if (unresolvedDraft != null) {
            unresolvedEntry = new ChatEntry(); unresolvedEntry.role="human"; unresolvedEntry.text=unresolvedDraft.message;
            unresolvedEntry.images.addAll(unresolvedDraft.images == null ? new ArrayList<>() : unresolvedDraft.images);
            unresolvedEntry.localTimestamp=unresolvedDraft.timestamp; unresolvedEntry.localStatus="Envio não confirmado";
        }
        bind();
    }

    private void bind() {
        drawer = root.findViewById(R.id.drawerChat);
        input = root.findViewById(R.id.etMessage); search = root.findViewById(R.id.etRecentSearch);
        messageList = root.findViewById(R.id.rvMessages); recentList = root.findViewById(R.id.rvRecentChats);
        greetingLayout = root.findViewById(R.id.layoutGreeting); progress = root.findViewById(R.id.chatProgress);
        recentEmpty = root.findViewById(R.id.tvRecentEmpty);
        warning = root.findViewById(R.id.tvChatWarning);
        attachmentPreview = root.findViewById(R.id.chatAttachmentsPreview);
        attachmentItems = root.findViewById(R.id.layoutChatAttachmentItems);
        messageAdapter = new ChatMessageAdapter(messages);
        greeting = root.findViewById(R.id.tvGreeting);
        greeting.setText("Olá, como posso te ajudar hoje?");
        headerTitle = root.findViewById(R.id.tvTitle);
        renderWarning();
        messageList.setLayoutManager(new LinearLayoutManager(fragment.requireContext())); messageList.setAdapter(messageAdapter);
        recentAdapter = new RecentAdapter(); recentList.setLayoutManager(new LinearLayoutManager(fragment.requireContext())); recentList.setAdapter(recentAdapter);

        root.findViewById(R.id.btnMenu).setOnClickListener(v -> drawer.openDrawer(Gravity.START));
        root.findViewById(R.id.btnNewChat).setOnClickListener(v -> startNewChat());
        root.findViewById(R.id.btnSend).setOnClickListener(v -> send());
        root.findViewById(R.id.btnAttach).setOnClickListener(v -> openPhotoPicker());
        root.findViewById(R.id.btnAttachFile).setOnClickListener(v -> openPhotoPicker());
        root.findViewById(R.id.btnDrawerSearch).setOnClickListener(v -> toggleSearch());
        warning.setOnClickListener(v -> confirmDiscardDraft());
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int count,int after) { }
            @Override public void onTextChanged(CharSequence s,int st,int before,int count) { recentAdapter.filter(s.toString()); }
            @Override public void afterTextChanged(Editable e) { }
        });
    }

    public void onPhotoUris(List<Uri> uris) {
        if (destroyed || uris == null || uris.isEmpty()) return;
        int remaining = 3 - attachedUrls.size();
        if (uris.size() > remaining) { toast("É possível anexar até 3 imagens por mensagem."); return; }
        pendingPhotoUris.addAll(uris);
        renderAttachedPreviews();
        uploadNext(new ArrayList<>(uris), 0);
    }

    public void reload() {
        if (!viewModel.hasSession()) { toast("Entre novamente para acessar a Manu."); return; }
        io.execute(() -> {
            try {
                List<ChatHistory> data = viewModel.listar();
                post(() -> { histories.clear(); histories.addAll(data); recentAdapter.replace(data); });
            } catch (Exception e) { post(() -> toast(message(e))); }
        });
    }

    public void onResume() { if (!destroyed) reload(); }
    public void close() { destroyed = true; io.shutdownNow(); }

    private void startNewChat() {
        if (pending) { toast("Aguarde o envio atual terminar."); return; }
        selectedThreadId = null; messages.clear(); attachedUrls.clear(); pendingPhotoUris.clear(); lastLiveDayKey = null; updateAttachmentsLabel();
        headerTitle.setText(R.string.app_chat_title);
        messageAdapter.notifyDataSetChanged(); recentAdapter.notifySelectionChanged(); showConversation(false); drawer.closeDrawers();
        input.setText("");
    }

    private void select(ChatHistory history) {
        if (pending) { toast("Aguarde o envio atual terminar."); return; }
        String sessionId = history.sessionId;
        if (sessionId == null || sessionId.trim().isEmpty()) return;
        String previousThreadId = selectedThreadId;
        selectedThreadId = sessionId;
        headerTitle.setText(displayTitle(history));
        recentAdapter.notifySelectionChanged();
        drawer.closeDrawers();
        setLoading(true);
        io.execute(() -> {
            try {
                ChatHistory fresh = viewModel.recuperar(sessionId);
                post(() -> {
                    selectedThreadId = fresh.sessionId; setHistoryMessages(fresh.messages);
                    headerTitle.setText(displayTitle(fresh));
                    recentAdapter.notifySelectionChanged();
                    if (unresolvedDraft != null && sessionId.equals(unresolvedDraft.threadId) && unresolvedEntry != null)
                        appendLive(unresolvedEntry);
                    messageAdapter.notifyDataSetChanged(); showConversation(!messages.isEmpty()); scrollToBottom(); setLoading(false);
                    if (unresolvedDraft != null && unresolvedDraft.threadId == null)
                        toast("Há um envio sem confirmação separado deste histórico. Confira as conversas antes de descartá-lo.");
                });
            } catch (Exception e) { post(() -> {
                if (sessionId.equals(selectedThreadId)) selectedThreadId = previousThreadId;
                headerTitle.setText(titleForThread(previousThreadId));
                recentAdapter.notifySelectionChanged(); setLoading(false); toast(message(e));
            }); }
        });
    }

    private void send() {
        if (pending) return;
        if (uploadingImages) { toast("Aguarde o envio das imagens terminar."); return; }
        if (unresolvedDraft != null) { toast("Confira a conversa no histórico antes de enviar outra mensagem. Toque no aviso para descartar o rascunho após conferir."); return; }
        if (!viewModel.hasSession()) { toast("Entre novamente para conversar com a Manu."); return; }
        String text = input.getText() == null ? "" : input.getText().toString().trim();
        if (text.isEmpty()) { toast("Escreva uma mensagem para enviar."); input.requestFocus(); return; }
        if (text.length() > 4000) { toast("A mensagem pode ter até 4.000 caracteres."); return; }
        String originThread = selectedThreadId;
        long thisGeneration = ++sendGeneration;
        List<String> urls = new ArrayList<>(attachedUrls);
        ChatEntry draft = new ChatEntry(); draft.role="human"; draft.text=text; draft.images.addAll(urls); draft.localTimestamp=System.currentTimeMillis(); draft.localStatus="Enviando";
        unresolvedDraft = new ChatPendingDraft(); unresolvedDraft.threadId=originThread;
        unresolvedDraft.message=text; unresolvedDraft.images=new ArrayList<>(urls); unresolvedDraft.timestamp=draft.localTimestamp;
        unresolvedEntry=draft; viewModel.saveDraft(unresolvedDraft); renderWarning();
        appendLive(draft); showConversation(true); showTypingIndicator(); scrollToBottom();
        input.setText(""); attachedUrls.clear(); updateAttachmentsLabel();
        setPending(true);
        io.execute(() -> {
            try {
                ChatTurn turn = viewModel.enviar(text, originThread, urls);
                post(() -> {
                    if (!sameOrigin(originThread)) return;
                    removeTypingIndicator();
                    draft.localStatus=null; viewModel.clearDraft(); unresolvedDraft=null; unresolvedEntry=null; renderWarning();
                    selectedThreadId = turn.threadId; appendLive(turn.assistantMessage);
                    scrollToBottom(); setPending(false); reload();
                    reconcile(turn.threadId, turn.threadId, null, thisGeneration);
                });
            } catch (Exception e) {
                post(() -> {
                    if (!sameOrigin(originThread)) return;
                    removeTypingIndicator();
                    setPending(false);
                    toast("Não foi possível confirmar o envio. A mensagem ficou preservada; atualize o histórico antes de tentar novamente.");
                    draft.localStatus = "Envio não confirmado";
                    if (unresolvedDraft != null) unresolvedDraft.threadId=originThread;
                    viewModel.saveDraft(unresolvedDraft); renderWarning();
                    messageAdapter.notifyItemChanged(messages.indexOf(draft));
                    if (originThread == null) reload(); else reconcile(originThread, originThread, draft, thisGeneration);
                });
            }
        });
    }

    private void reconcile(String threadId, String expectedOrigin, ChatEntry uncertainDraft, long generation) {
        io.execute(() -> {
            try {
                ChatHistory fresh = viewModel.recuperar(threadId);
                post(() -> {
                    if (generation != sendGeneration || !sameOrigin(expectedOrigin) || !threadId.equals(selectedThreadId)) return;
                    setHistoryMessages(fresh.messages);
                    if (uncertainDraft != null) appendLive(uncertainDraft);
                    messageAdapter.notifyDataSetChanged();
                    showConversation(!messages.isEmpty()); scrollToBottom();
                });
            } catch (Exception ignored) { /* resposta confirmada permanece visível; leitura pode ser repetida */ }
        });
    }

    private boolean sameOrigin(String origin) { return origin == null ? selectedThreadId == null : origin.equals(selectedThreadId); }

    private void showTypingIndicator() {
        typingEntry = new ChatEntry(); typingEntry.role="assistant"; typingEntry.localStatus="typing-indicator";
        messages.add(typingEntry); messageAdapter.notifyItemInserted(messages.size()-1);
    }

    private void removeTypingIndicator() {
        if (typingEntry == null) return;
        int position = messages.indexOf(typingEntry);
        if (position >= 0) { messages.remove(position); messageAdapter.notifyItemRemoved(position); }
        typingEntry = null;
    }

    private void uploadNext(List<Uri> uris, int index) {
        if (index >= uris.size()) { uploadingImages=false; setPending(pending); updateAttachmentsLabel(); toast("Imagem(ns) anexada(s)."); return; }
        uploadingImages=true;
        setLoading(true);
        try {
            fotoViewModel.enviarSolicitacao(uris.get(index), new FotoViewModel.Callback() {
                @Override public void aoSucesso(String url, String publicId) {
                    post(() -> {
                        pendingPhotoUris.remove(uris.get(index));
                        if (url == null || url.trim().length() > 255) {
                            discardPendingUris(uris, index + 1); uploadingImages=false; updateAttachmentsLabel(); setPending(pending);
                            toast("A URL retornada para a imagem ultrapassa o limite aceito pelo chat."); return;
                        }
                        attachedUrls.add(url.trim()); updateAttachmentsLabel(); uploadNext(uris, index + 1);
                    });
                }
                @Override public void aoFalhar(String error) { post(() -> { discardPendingUris(uris, index); uploadingImages=false; setPending(pending); updateAttachmentsLabel(); toast("Não foi possível enviar a imagem."); }); }
                @Override public void aoReceberTokenExclusao(String token) { }
            });
        } catch (Exception e) { discardPendingUris(uris, index); uploadingImages=false; setPending(pending); updateAttachmentsLabel(); toast(message(e)); }
    }

    private void discardPendingUris(List<Uri> uris, int fromIndex) {
        for (int i = Math.max(0, fromIndex); i < uris.size(); i++) pendingPhotoUris.remove(uris.get(i));
    }

    private void openPhotoPicker() {
        if (pending || uploadingImages) { toast("Aguarde a operação atual terminar."); return; }
        if (attachedUrls.size() >= 3) { toast("O limite é 3 imagens por mensagem."); return; }
        if (photoPicker != null) photoPicker.open();
    }

    private void toggleSearch() {
        if (search.getVisibility() != View.VISIBLE) {
            search.setVisibility(View.VISIBLE); search.requestFocus();
            search.post(() -> ((InputMethodManager) fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(search, InputMethodManager.SHOW_IMPLICIT));
        } else {
            ((InputMethodManager) fragment.requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(), 0);
            search.setText(""); search.setVisibility(View.GONE);
        }
    }

    private void menu(ChatHistory history, View anchor) {
        View content = LayoutInflater.from(anchor.getContext()).inflate(R.layout.popup_app_request_menu, null, false);
        TextView finish = content.findViewById(R.id.tvPopupEdit);
        TextView delete = content.findViewById(R.id.tvPopupDelete);
        finish.setText("Finalizar conversa");
        delete.setText("Excluir conversa");
        PopupWindow popup = new PopupWindow(content,
                anchor.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(anchor.getContext(), R.drawable.bg_app_popup_menu));
        popup.setElevation(anchor.getResources().getDisplayMetrics().density * 6);
        finish.setOnClickListener(v -> { popup.dismiss(); finishConversation(history); });
        delete.setOnClickListener(v -> { popup.dismiss(); confirmDelete(history); });
        popup.setOutsideTouchable(true);
        popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
    }

    private void finishConversation(ChatHistory history) {
        if (pending) { toast("Aguarde o envio atual terminar."); return; }
        if (unresolvedDraft != null && history.sessionId.equals(unresolvedDraft.threadId)) { toast("Confira o histórico antes de finalizar esta conversa."); return; }
        setLoading(true); io.execute(() -> {
            try { viewModel.encerrar(history.sessionId); post(() -> { setLoading(false); reload(); if (history.sessionId.equals(selectedThreadId)) select(history); }); }
            catch (Exception e) { post(() -> { setLoading(false); toast(message(e)); }); }
        });
    }

    private void confirmDelete(ChatHistory history) {
        AppDialogs.showDeleteConfirm(fragment.requireContext(), () -> delete(history));
    }
    private void confirmDiscardDraft() {
        if (unresolvedDraft == null) return;
        new AlertDialog.Builder(fragment.requireContext()).setTitle("Descartar rascunho incerto?")
                .setMessage("A mensagem não será reenviada. Confirme que você já verificou o histórico antes de descartá-la.")
                .setNegativeButton("Manter", (d,w)->d.dismiss())
                .setPositiveButton("Descartar", (d,w)->{
                    if (unresolvedEntry != null) messages.remove(unresolvedEntry);
                    unresolvedEntry=null; unresolvedDraft=null; viewModel.clearDraft(); renderWarning();
                    messageAdapter.notifyDataSetChanged(); showConversation(!messages.isEmpty());
                }).show();
    }
    private void renderWarning() {
        if (warning == null) return;
        warning.setVisibility(unresolvedDraft == null || pending ? View.GONE : View.VISIBLE);
        if (unresolvedDraft == null) return;
        String value=unresolvedDraft.message == null ? "" : unresolvedDraft.message.trim();
        if (value.length() > 72) value=value.substring(0,69)+"…";
        warning.setText("Envio não confirmado: “"+value+"”. Confira o histórico; toque aqui para descartar o rascunho.");
    }
    private void delete(ChatHistory history) {
        if (pending) { toast("Aguarde o envio atual terminar."); return; }
        if (unresolvedDraft != null && history.sessionId.equals(unresolvedDraft.threadId)) { toast("Confira o histórico antes de excluir esta conversa."); return; }
        setLoading(true); io.execute(() -> {
            try { viewModel.excluir(history.sessionId); post(() -> {
                setLoading(false); histories.removeIf(h -> history.sessionId.equals(h.sessionId)); recentAdapter.replace(histories);
                if (history.sessionId.equals(selectedThreadId)) startNewChat();
            }); } catch (Exception e) { post(() -> { setLoading(false); reload(); toast(message(e)); }); }
        });
    }

    private void setPending(boolean value) {
        pending=value;
        root.findViewById(R.id.btnSend).setEnabled(!value && !uploadingImages);
        root.findViewById(R.id.btnAttach).setEnabled(!value && !uploadingImages);
        root.findViewById(R.id.btnAttachFile).setEnabled(!value && !uploadingImages);
        input.setEnabled(!value); setLoading(uploadingImages); renderWarning();
    }
    private void setLoading(boolean value) { if (progress != null) progress.setVisibility(value ? View.VISIBLE : View.GONE); }
    private void showConversation(boolean hasMessages) {
        if (greetingLayout != null) greetingLayout.setVisibility(hasMessages ? View.GONE : View.VISIBLE);
        else if (greeting != null) greeting.setVisibility(hasMessages ? View.GONE : View.VISIBLE);
        messageList.setVisibility(hasMessages ? View.VISIBLE : View.GONE);
    }
    private void appendLive(ChatEntry entry) {
        appendDateDivider(entry.localTimestamp, true);
        messages.add(entry); messageAdapter.notifyItemInserted(messages.size()-1);
    }

    private void setHistoryMessages(List<ChatEntry> history) {
        messages.clear();
        lastLiveDayKey = null;
        if (history == null) return;
        for (ChatEntry entry : history) {
            if (entry.localTimestamp > 0) appendDateDivider(entry.localTimestamp, false);
            else lastLiveDayKey = null;
            messages.add(entry);
        }
    }

    private void appendDateDivider(long timestamp, boolean notify) {
        if (timestamp <= 0) return;
        Date date = new Date(timestamp);
        String dayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        if (dayKey.equals(lastLiveDayKey)) return;
        ChatEntry divider = new ChatEntry(); divider.role="system"; divider.localStatus="date-divider";
        divider.text = "────────  " + new SimpleDateFormat("dd MMMM 'de' yyyy", new Locale("pt", "BR")).format(date) + "  ────────";
        messages.add(divider);
        if (notify) messageAdapter.notifyItemInserted(messages.size()-1);
        lastLiveDayKey = dayKey;
    }
    private void scrollToBottom() { if (!messages.isEmpty()) messageList.post(() -> messageList.scrollToPosition(messages.size()-1)); }
    private void updateAttachmentsLabel() {
        if (attachedUrls.isEmpty()) input.setHint(R.string.app_chat_input_hint);
        else input.setHint("Como posso ajudar? ("+attachedUrls.size()+" imagem(ns) anexada(s))");
        renderAttachedPreviews();
    }
    private void renderAttachedPreviews() {
        if (attachmentItems == null || attachmentPreview == null) return;
        attachmentItems.removeAllViews();
        for (int i = 0; i < attachedUrls.size(); i++) {
            final String url = attachedUrls.get(i);
            addAttachmentPreview(url, null, () -> { attachedUrls.remove(url); updateAttachmentsLabel(); });
        }
        for (Uri uri : pendingPhotoUris) addAttachmentPreview(null, uri, null);
        attachmentPreview.setVisibility(attachmentItems.getChildCount() == 0 ? View.GONE : View.VISIBLE);
    }
    private void addAttachmentPreview(String url, Uri uri, Runnable remove) {
        int size = dp(42);
        FrameLayout frame = new FrameLayout(fragment.requireContext());
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(size, size);
        frameParams.setMargins(0, 0, dp(7), 0);
        frame.setLayoutParams(frameParams);
        ImageView image = new ImageView(fragment.requireContext());
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        frame.addView(image, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (uri != null) Glide.with(image).load(uri).centerCrop().into(image);
        else Glide.with(image).load(url).centerCrop().into(image);
        GradientDrawable shape = new GradientDrawable(); shape.setColor(Color.WHITE); shape.setCornerRadius(dp(10));
        frame.setBackground(shape); frame.setClipToOutline(true);
        if (remove != null) {
            TextView close = new TextView(fragment.requireContext());
            close.setText("×"); close.setTextColor(Color.WHITE); close.setTextSize(12); close.setGravity(Gravity.CENTER);
            GradientDrawable closeBg = new GradientDrawable(); closeBg.setColor(Color.rgb(19, 9, 67)); closeBg.setShape(GradientDrawable.OVAL); close.setBackground(closeBg);
            FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(18), dp(18), Gravity.TOP | Gravity.END);
            frame.addView(close, closeParams); close.setOnClickListener(v -> remove.run());
        }
        attachmentItems.addView(frame);
    }
    private int dp(int value) {
        return (int) (value * fragment.getResources().getDisplayMetrics().density + .5f);
    }
    private void post(Runnable action) { if (destroyed) return; fragment.requireActivity().runOnUiThread(() -> { if (!destroyed && fragment.getView()!=null) action.run(); }); }
    private void toast(String text) { if (!destroyed && fragment.getContext()!=null) Toast.makeText(fragment.requireContext(), text, Toast.LENGTH_LONG).show(); }
    private String message(Exception e) {
        if (e instanceof IllegalArgumentException || e instanceof ChatException) return e.getMessage();
        return "Não foi possível acessar a Manu. Verifique sua conexão e atualize a conversa.";
    }

    private final class RecentAdapter extends RecyclerView.Adapter<RecentHolder> {
        private final List<ChatHistory> shown = new ArrayList<>();
        private final List<ChatHistory> all = new ArrayList<>();
        void replace(List<ChatHistory> values) { all.clear(); all.addAll(values); filter(search.getText().toString()); }
        void filter(String query) {
            String q=query==null?"":query.trim().toLowerCase(Locale.ROOT); shown.clear();
            for (ChatHistory h:all) { String title=displayTitle(h).toLowerCase(Locale.ROOT); if(q.isEmpty()||title.contains(q)) shown.add(h); }
            if (recentEmpty != null) {
                recentEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
                recentEmpty.setText(all.isEmpty() ? "Você ainda não possui conversas." : "Nenhuma conversa encontrada.");
            }
            notifyDataSetChanged();
        }
        void notifySelectionChanged() { notifyDataSetChanged(); }
        @NonNull @Override public RecentHolder onCreateViewHolder(@NonNull ViewGroup parent,int type) {
            return new RecentHolder(android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_chat_recent,parent,false));
        }
        @Override public void onBindViewHolder(@NonNull RecentHolder holder,int position) {
            ChatHistory h=shown.get(position); holder.title.setText(displayTitle(h));
            holder.title.setSelected(h.sessionId!=null&&h.sessionId.equals(selectedThreadId));
            holder.title.setBackgroundResource(holder.title.isSelected()?R.drawable.bg_app_chat_recent_selected:android.R.color.transparent);
            holder.title.setOnClickListener(v->select(h)); holder.menu.setOnClickListener(v->menu(h,holder.menu));
        }
        @Override public int getItemCount(){return shown.size();}
    }
    private String displayTitle(ChatHistory h) {
        if(h.title!=null&&!h.title.trim().isEmpty()) return h.title.trim();
        String date=sessionDate(h.updatedAt);
        return date.isEmpty()?"Nova conversa":"Nova conversa · "+date;
    }
    private String titleForThread(String threadId) {
        if (threadId == null) return fragment.getString(R.string.app_chat_title);
        for (ChatHistory history : histories) {
            if (threadId.equals(history.sessionId)) return displayTitle(history);
        }
        return fragment.getString(R.string.app_chat_title);
    }
    private String sessionDate(String iso) {
        if(iso==null||iso.trim().isEmpty()) return "";
        try {
            String normalized=iso.trim(); if(!normalized.endsWith("Z")&&!normalized.matches(".*[+-]\\d\\d:\\d\\d$")) normalized += "Z";
            java.time.Instant instant=java.time.OffsetDateTime.parse(normalized).toInstant();
            return new SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(Date.from(instant));
        } catch(Exception ignored) { return ""; }
    }
    private static final class RecentHolder extends RecyclerView.ViewHolder {
        final TextView title; final TextView menu;
        RecentHolder(View item){super(item);title=item.findViewById(R.id.tvRecentChat);menu=item.findViewById(R.id.btnRecentMenu);}
    }
}
