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
import android.widget.PopupWindow;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.aula.fixa.ui.adapter.chatbot.ChatAttachmentAdapter;
import com.aula.fixa.ui.adapter.chatbot.ChatMessageAdapter;
import com.aula.fixa.ui.adapter.chatbot.RecentChatsAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** UI controller shared by the requester fragment and the manager/technician chatbot screen. */
public final class ChatbotFlow {
    public interface PhotoPicker { void open(); }
    private final Fragment fragment;
    private final View root;
    private final ChatViewModel viewModel;
    private final FotoViewModel fotoViewModel;
    private final PhotoPicker photoPicker;
    private final boolean managerMode;
    private final Runnable onBack;
    private final List<ChatEntry> messages = new ArrayList<>();
    private final List<ChatHistory> histories = new ArrayList<>();
    private final List<String> attachedUrls = new ArrayList<>();
    private final List<Uri> pendingPhotoUris = new ArrayList<>();
    private ChatMessageAdapter messageAdapter;
    private ChatRecentListController recentController;
    private ChatAttachmentAdapter attachmentAdapter;
    private ChatAttachmentPreview attachmentRenderer;
    private RecyclerView messageList, recentList;
    private TextView greeting;
    private TextView headerTitle;
    private View greetingLayout;
    private EditText input, search;
    private ProgressBar progress;
    private TextView recentEmpty;
    private TextView warning;
    private RecyclerView attachmentPreview;
    private DrawerLayout drawer;
    private String selectedThreadId;
    private String selectedRecentId;
    private boolean pending;
    private boolean uploadingImages;
    private boolean destroyed;
    private String lastLiveDayKey;
    private long sendGeneration;
    private ChatPendingDraft unresolvedDraft;
    private ChatEntry unresolvedEntry;
    private ChatEntry typingEntry;

    public ChatbotFlow(Fragment fragment, View root, PhotoPicker photoPicker) {
        this(fragment, root, photoPicker, false, null);
    }

    public ChatbotFlow(Fragment fragment, View root, PhotoPicker photoPicker, boolean managerMode, Runnable onBack) {
        this.fragment=fragment; this.root=root; this.photoPicker=photoPicker;
        this.managerMode=managerMode; this.onBack=onBack;
        viewModel = new ViewModelProvider(fragment, new ChatViewModelFactory(fragment.requireContext())).get(ChatViewModel.class);
        fotoViewModel = new ViewModelProvider(fragment, new FotoViewModelFactory(fragment.requireContext())).get(FotoViewModel.class);
        bind();
        root.findViewById(R.id.btnSend).setEnabled(false);
        viewModel.loadDraft(new ChatViewModel.Listener<ChatPendingDraft>() {
            @Override public void onResultado(ChatPendingDraft draft) { post(() -> {
                unresolvedDraft = draft;
                createUnresolvedEntry();
                root.findViewById(R.id.btnSend).setEnabled(!pending && !uploadingImages);
                renderWarning();
                submitRecentItems();

            }); }
            @Override public void onErro(Throwable error) { post(() -> {
                root.findViewById(R.id.btnSend).setEnabled(!pending && !uploadingImages);
                toast(message(error));
            }); }
        });
    }

    private void bind() {
        drawer = root.findViewById(R.id.drawerChat);
        input = root.findViewById(R.id.etMessage); search = root.findViewById(R.id.etRecentSearch);
        messageList = root.findViewById(R.id.rvMessages); recentList = root.findViewById(R.id.rvRecentChats);
        greetingLayout = root.findViewById(R.id.layoutGreeting); progress = root.findViewById(R.id.chatProgress);
        recentEmpty = root.findViewById(R.id.tvRecentEmpty);
        warning = root.findViewById(R.id.tvChatWarning);
        attachmentPreview = root.findViewById(R.id.chatAttachmentsPreview);
        messageAdapter = new ChatMessageAdapter(messages);
        greeting = root.findViewById(R.id.tvGreeting);
        greeting.setText(R.string.app_chat_greeting_default);
        headerTitle = root.findViewById(R.id.tvTitle);
        renderWarning();
        messageList.setLayoutManager(new LinearLayoutManager(fragment.requireContext())); messageList.setAdapter(messageAdapter);
        recentController = new ChatRecentListController(fragment, recentList, recentEmpty, histories,
                new ChatRecentListController.Actions() {
                    @Override public void onSelect(ChatHistory history) { select(history); }
                    @Override public void onMenu(ChatHistory history, View anchor) { menu(history, anchor); }
                });
        attachmentAdapter = new ChatAttachmentAdapter(url -> { attachedUrls.remove(url); updateAttachmentsLabel(); });
        attachmentPreview.setLayoutManager(new LinearLayoutManager(fragment.requireContext(), RecyclerView.HORIZONTAL, false));
        attachmentPreview.setAdapter(attachmentAdapter);
        attachmentRenderer = new ChatAttachmentPreview(attachmentPreview, attachmentAdapter);

        View drawerContent = root.findViewById(R.id.drawerMenu);
        if (managerMode) {
            android.widget.ImageButton back = root.findViewById(R.id.btnMenu);
            back.setImageResource(R.drawable.ic_arrow_left_26);
            back.setContentDescription(fragment.getString(R.string.app_chat_back));
            back.setOnClickListener(v -> { if (onBack != null) onBack.run(); });
            View openDrawer = root.findViewById(R.id.btnOpenDrawer);
            openDrawer.setContentDescription(fragment.getString(R.string.app_chat_open_menu));
            openDrawer.setVisibility(View.VISIBLE);
            openDrawer.setOnClickListener(v -> drawer.openDrawer(Gravity.END));
            if (drawerContent != null && drawerContent.getLayoutParams() instanceof DrawerLayout.LayoutParams) {
                DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawerContent.getLayoutParams();
                params.gravity = Gravity.END;
                drawerContent.setLayoutParams(params);
            }
        } else {
            root.findViewById(R.id.btnMenu).setOnClickListener(v -> drawer.openDrawer(Gravity.START));
            root.findViewById(R.id.btnOpenDrawer).setVisibility(View.GONE);
            if (drawerContent != null && drawerContent.getLayoutParams() instanceof DrawerLayout.LayoutParams) {
                DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawerContent.getLayoutParams();
                params.gravity = Gravity.START;
                drawerContent.setLayoutParams(params);
            }
        }
        root.findViewById(R.id.btnNewChat).setOnClickListener(v -> startNewChat());
        root.findViewById(R.id.btnSend).setOnClickListener(v -> send());
        root.findViewById(R.id.btnAttach).setOnClickListener(v -> openPhotoPicker());
        root.findViewById(R.id.btnAttachFile).setOnClickListener(v -> openPhotoPicker());
        root.findViewById(R.id.btnDrawerSearch).setOnClickListener(v -> toggleSearch());
        warning.setOnClickListener(v -> confirmDiscardDraft());
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int count,int after) { }
            @Override public void onTextChanged(CharSequence s,int st,int before,int count) { recentController.filter(s.toString()); }
            @Override public void afterTextChanged(Editable e) { }
        });
    }

    public void onPhotoUris(List<Uri> uris) {
        if (destroyed || uris == null || uris.isEmpty()) return;
        int remaining = 3 - attachedUrls.size();
        if (uris.size() > remaining) { toast(fragment.getString(R.string.app_chat_error_photo_limit)); return; }
        pendingPhotoUris.addAll(uris);
        renderAttachedPreviews();
        uploadNext(new ArrayList<>(uris), 0);
    }

    public void reload() {
        if (!viewModel.hasSession()) { toast(fragment.getString(R.string.app_chat_error_session_access)); return; }
        viewModel.listar(new ChatViewModel.Listener<List<ChatHistory>>() {
            @Override public void onResultado(List<ChatHistory> data) { post(() -> { histories.clear(); histories.addAll(data); submitRecentItems();  }); }
            @Override public void onErro(Throwable error) { post(() -> toast(message(error))); }
        });
    }

    public void onResume() { if (!destroyed) reload(); }
    public void close() { destroyed = true; }

    private void startNewChat() {
        if (pending) { toast(fragment.getString(R.string.app_chat_error_wait_send)); return; }
        selectedThreadId = null; selectedRecentId = null; messages.clear(); attachedUrls.clear(); pendingPhotoUris.clear(); lastLiveDayKey = null; updateAttachmentsLabel();
        headerTitle.setText(R.string.app_chat_title);
        messageAdapter.notifyDataSetChanged(); recentController.select(null); showConversation(false); drawer.closeDrawers();
        input.setText("");
    }

    private void select(ChatHistory history) {
        if (pending) { toast(fragment.getString(R.string.app_chat_error_wait_send)); return; }
        if (RecentChatsAdapter.LOCAL_DRAFT_ID.equals(history.sessionId)) {
            selectedThreadId = null;
            selectedRecentId = RecentChatsAdapter.LOCAL_DRAFT_ID;
            messages.clear();
            lastLiveDayKey = null;
            if (unresolvedEntry != null) appendLive(unresolvedEntry);
            headerTitle.setText(R.string.app_chat_recent_draft_only);
            recentController.select(selectedRecentId);
            messageAdapter.notifyDataSetChanged();
            showConversation(!messages.isEmpty());
            scrollToBottom();
            drawer.closeDrawers();
            return;
        }
        String sessionId = history.sessionId;
        if (sessionId == null || sessionId.trim().isEmpty()) return;
        String previousThreadId = selectedThreadId;
        String previousRecentId = selectedRecentId;
        selectedThreadId = sessionId;
        selectedRecentId = sessionId;
        headerTitle.setText(recentController.title(history));
        recentController.select(selectedRecentId);
        drawer.closeDrawers();
        setLoading(true);
        viewModel.recuperar(sessionId, new ChatViewModel.Listener<ChatHistory>() {
            @Override public void onResultado(ChatHistory fresh) {
                post(() -> {
                    selectedThreadId = fresh.sessionId; setHistoryMessages(fresh.messages);
                    selectedRecentId = fresh.sessionId;
                    headerTitle.setText(recentController.title(fresh));
                    recentController.select(selectedRecentId);
                    if (unresolvedDraft != null && sessionId.equals(unresolvedDraft.threadId) && unresolvedEntry != null)
                        appendLive(unresolvedEntry);
                    messageAdapter.notifyDataSetChanged(); showConversation(!messages.isEmpty()); scrollToBottom(); setLoading(false);
                    if (unresolvedDraft != null && unresolvedDraft.threadId == null)
                        toast(fragment.getString(R.string.app_chat_error_draft_other_thread));
                });
            }
            @Override public void onErro(Throwable error) { post(() -> {
                if (sessionId.equals(selectedThreadId)) selectedThreadId = previousThreadId;
                selectedRecentId = previousRecentId;
                headerTitle.setText(recentController.titleForThread(previousThreadId));
                recentController.select(selectedRecentId); setLoading(false); toast(message(error));
            }); }
        });
    }

    private void send() {
        if (pending) return;
        if (uploadingImages) { toast(fragment.getString(R.string.app_chat_error_wait_upload)); return; }
        if (unresolvedDraft != null) { toast(fragment.getString(R.string.app_chat_error_unconfirmed_draft)); return; }
        if (!viewModel.hasSession()) { toast(fragment.getString(R.string.app_chat_error_session_chat)); return; }
        String text = input.getText() == null ? "" : input.getText().toString().trim();
        if (text.isEmpty()) { toast(fragment.getString(R.string.app_chat_error_empty_message)); input.requestFocus(); return; }
        if (text.length() > 4000) { toast(fragment.getString(R.string.app_chat_error_message_too_long)); return; }
        String originThread = selectedThreadId;
        long thisGeneration = ++sendGeneration;
        List<String> urls = new ArrayList<>(attachedUrls);
        ChatEntry draft = new ChatEntry(); draft.role="human"; draft.text=text; draft.images.addAll(urls); draft.localTimestamp=System.currentTimeMillis(); draft.localStatus=fragment.getString(R.string.app_chat_status_sending);
        unresolvedDraft = new ChatPendingDraft(); unresolvedDraft.threadId=originThread;
        unresolvedDraft.message=text; unresolvedDraft.images=new ArrayList<>(urls); unresolvedDraft.timestamp=draft.localTimestamp;
        unresolvedEntry=draft;
        setPending(true);
        viewModel.saveDraft(unresolvedDraft, new ChatViewModel.Listener<Void>() {
            @Override public void onResultado(Void ignored) { post(() -> {
                if (thisGeneration != sendGeneration || !sameOrigin(originThread)) return;
                renderWarning(); submitRecentItems();
                appendLive(draft); showConversation(true); showTypingIndicator(); scrollToBottom();
                input.setText(""); attachedUrls.clear(); updateAttachmentsLabel();
                sendPersistedDraft(text, originThread, urls, draft, thisGeneration);
            }); }
            @Override public void onErro(Throwable error) { post(() -> {
                if (thisGeneration != sendGeneration) return;
                unresolvedDraft = null; unresolvedEntry = null; setPending(false); renderWarning(); submitRecentItems();
                toast(message(error));
            }); }
        });
    }

    private void sendPersistedDraft(String text, String originThread, List<String> urls, ChatEntry draft, long generation) {
        viewModel.enviar(text, originThread, urls, new ChatViewModel.Listener<ChatTurn>() {
            @Override public void onResultado(ChatTurn turn) {
                post(() -> {
                    if (!sameOrigin(originThread)) return;
                    removeTypingIndicator();
                    viewModel.clearDraft(new ChatViewModel.Listener<Void>() {
                        @Override public void onResultado(Void ignored) { post(() -> {
                            if (generation != sendGeneration) return;
                            draft.localStatus=null; unresolvedDraft=null; unresolvedEntry=null;
                            completeConfirmedSend(turn);
                        }); }
                        @Override public void onErro(Throwable error) { post(() -> {
                            if (generation != sendGeneration) return;
                            if (unresolvedDraft != null) unresolvedDraft.threadId=turn.threadId;
                            draft.localStatus=fragment.getString(R.string.app_chat_status_send_confirmed);
                            completeConfirmedSend(turn);
                            toast(fragment.getString(R.string.app_chat_error_clear_confirmed_draft));
                        }); }
                    });
                });
            }
            @Override public void onErro(Throwable error) {
                post(() -> {
                    if (!sameOrigin(originThread)) return;
                    removeTypingIndicator();
                    setPending(false);
                    toast(fragment.getString(R.string.app_chat_error_send_uncertain));
                    draft.localStatus = fragment.getString(R.string.app_chat_status_send_unconfirmed);
                    if (unresolvedDraft != null) unresolvedDraft.threadId=originThread;
                    viewModel.saveDraft(unresolvedDraft, new ChatViewModel.Listener<Void>() {
                        @Override public void onResultado(Void ignored) { post(() -> { renderWarning(); submitRecentItems(); }); }
                        @Override public void onErro(Throwable saveError) { post(() -> toast(message(saveError))); }
                    });
                    renderWarning(); submitRecentItems();
                    messageAdapter.notifyItemChanged(messages.indexOf(draft));
                    if (originThread == null) reload(); else reconcile(originThread, originThread, draft, generation);
                });
            }
        });
    }

    private void completeConfirmedSend(ChatTurn turn) {
        selectedThreadId = turn.threadId;
        selectedRecentId = turn.threadId;
        if (turn.assistantMessage != null) appendLive(turn.assistantMessage);
        renderWarning(); submitRecentItems();
        scrollToBottom(); setPending(false); reload();
        reconcile(turn.threadId, turn.threadId, null, sendGeneration);
    }

    private void reconcile(String threadId, String expectedOrigin, ChatEntry uncertainDraft, long generation) {
        viewModel.recuperar(threadId, new ChatViewModel.Listener<ChatHistory>() {
            @Override public void onResultado(ChatHistory fresh) {
                post(() -> {
                    if (generation != sendGeneration || !sameOrigin(expectedOrigin) || !threadId.equals(selectedThreadId)) return;
                    setHistoryMessages(fresh.messages);
                    if (uncertainDraft != null) appendLive(uncertainDraft);
                    messageAdapter.notifyDataSetChanged();
                    showConversation(!messages.isEmpty()); scrollToBottom();
                });
            }
            @Override public void onErro(Throwable ignored) { /* resposta confirmada permanece visível; leitura pode ser repetida */ }
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
        if (index >= uris.size()) { uploadingImages=false; setPending(pending); updateAttachmentsLabel(); toast(fragment.getString(R.string.app_chat_images_attached)); return; }
        uploadingImages=true;
        setLoading(true);
        try {
            fotoViewModel.enviarSolicitacao(uris.get(index), new FotoViewModel.Callback() {
                @Override public void aoSucesso(String url, String publicId) {
                    post(() -> {
                        pendingPhotoUris.remove(uris.get(index));
                        if (url == null || url.trim().length() > 255) {
                            discardPendingUris(uris, index + 1); uploadingImages=false; updateAttachmentsLabel(); setPending(pending);
                            toast(fragment.getString(R.string.app_chat_error_image_url_too_long)); return;
                        }
                        attachedUrls.add(url.trim()); updateAttachmentsLabel(); uploadNext(uris, index + 1);
                    });
                }
                @Override public void aoFalhar(String error) { post(() -> { discardPendingUris(uris, index); uploadingImages=false; setPending(pending); updateAttachmentsLabel(); toast(fragment.getString(R.string.app_chat_error_image_upload)); }); }
                @Override public void aoReceberTokenExclusao(String token) { }
            });
        } catch (Exception e) { discardPendingUris(uris, index); uploadingImages=false; setPending(pending); updateAttachmentsLabel(); toast(message(e)); }
    }

    private void discardPendingUris(List<Uri> uris, int fromIndex) {
        for (int i = Math.max(0, fromIndex); i < uris.size(); i++) pendingPhotoUris.remove(uris.get(i));
    }

    private void openPhotoPicker() {
        if (pending || uploadingImages) { toast(fragment.getString(R.string.app_chat_error_wait_operation)); return; }
        if (attachedUrls.size() >= 3) { toast(fragment.getString(R.string.app_chat_error_photo_limit)); return; }
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
        finish.setText(R.string.app_chat_finish_conversation);
        delete.setText(R.string.app_chat_delete_conversation);
        PopupWindow popup = new PopupWindow(content,
                anchor.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(anchor.getContext(), R.drawable.bg_app_popup_menu));
        popup.setElevation(anchor.getResources().getDimension(R.dimen.app_popup_menu_elevation));
        finish.setOnClickListener(v -> { popup.dismiss(); finishConversation(history); });
        delete.setOnClickListener(v -> { popup.dismiss(); confirmDelete(history); });
        popup.setOutsideTouchable(true);
        popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
    }

    private void finishConversation(ChatHistory history) {
        if (pending) { toast(fragment.getString(R.string.app_chat_error_wait_send)); return; }
        if (unresolvedDraft != null && history.sessionId.equals(unresolvedDraft.threadId)) { toast(fragment.getString(R.string.app_chat_error_finish_unconfirmed)); return; }
        setLoading(true);
        viewModel.encerrar(history.sessionId, new ChatViewModel.Listener<Void>() {
            @Override public void onResultado(Void ignored) { post(() -> { setLoading(false); reload(); if (history.sessionId.equals(selectedThreadId)) select(history); }); }
            @Override public void onErro(Throwable error) { post(() -> { setLoading(false); toast(message(error)); }); }
        });
    }

    private void confirmDelete(ChatHistory history) {
        AppDialogs.showDeleteConfirm(fragment.requireContext(), () -> delete(history));
    }
    private void confirmDiscardDraft() {
        if (unresolvedDraft == null) return;
        new AlertDialog.Builder(fragment.requireContext()).setTitle(R.string.app_chat_discard_draft_title)
                .setMessage(R.string.app_chat_discard_draft_message)
                .setNegativeButton(R.string.app_chat_keep_draft, (d,w)->d.dismiss())
                .setPositiveButton(R.string.app_chat_discard_draft, (d,w)->{
                    viewModel.clearDraft(new ChatViewModel.Listener<Void>() {
                        @Override public void onResultado(Void ignored) { post(() -> {
                            if (unresolvedEntry != null) messages.remove(unresolvedEntry);
                            unresolvedEntry=null; unresolvedDraft=null;
                            if (RecentChatsAdapter.LOCAL_DRAFT_ID.equals(selectedRecentId)) {
                                selectedRecentId=null; selectedThreadId=null; messages.clear(); lastLiveDayKey=null;
                                headerTitle.setText(R.string.app_chat_title);
                            }
                            renderWarning(); submitRecentItems();
                            messageAdapter.notifyDataSetChanged(); showConversation(!messages.isEmpty());
                        }); }
                        @Override public void onErro(Throwable error) { post(() -> toast(message(error))); }
                    });
                }).show();
    }
    private void renderWarning() {
        if (warning == null) return;
        warning.setVisibility(unresolvedDraft == null || pending ? View.GONE : View.VISIBLE);
        if (unresolvedDraft == null) return;
        String value=unresolvedDraft.message == null ? "" : unresolvedDraft.message.trim();
        if (value.length() > 72) value=value.substring(0,69)+"…";
        warning.setText(fragment.getString(R.string.app_chat_warning_unconfirmed, value));
    }
    private void createUnresolvedEntry() {
        unresolvedEntry = null;
        if (unresolvedDraft == null) return;
        unresolvedEntry = new ChatEntry();
        unresolvedEntry.role = "human";
        unresolvedEntry.text = unresolvedDraft.message;
        unresolvedEntry.images.addAll(unresolvedDraft.images == null ? new ArrayList<>() : unresolvedDraft.images);
        unresolvedEntry.localTimestamp = unresolvedDraft.timestamp;
        unresolvedEntry.localStatus = fragment.getString(R.string.app_chat_status_send_unconfirmed);
    }
    private void submitRecentItems() {
        if (recentController != null) recentController.submit(unresolvedDraft, selectedRecentId);
    }
    private void delete(ChatHistory history) {
        if (pending) { toast(fragment.getString(R.string.app_chat_error_wait_send)); return; }
        if (unresolvedDraft != null && history.sessionId.equals(unresolvedDraft.threadId)) { toast(fragment.getString(R.string.app_chat_error_delete_unconfirmed)); return; }
        setLoading(true);
        viewModel.excluir(history.sessionId, new ChatViewModel.Listener<Void>() {
            @Override public void onResultado(Void ignored) { post(() -> {
                setLoading(false); histories.removeIf(h -> history.sessionId.equals(h.sessionId)); submitRecentItems();
                if (history.sessionId.equals(selectedThreadId)) startNewChat();
            }); }
            @Override public void onErro(Throwable error) { post(() -> { setLoading(false); reload(); toast(message(error)); }); }
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
        divider.text = fragment.getString(R.string.app_chat_date_divider, new SimpleDateFormat(fragment.getString(R.string.app_chat_date_format), new Locale("pt", "BR")).format(date));
        messages.add(divider);
        if (notify) messageAdapter.notifyItemInserted(messages.size()-1);
        lastLiveDayKey = dayKey;
    }
    private void scrollToBottom() { if (!messages.isEmpty()) messageList.post(() -> messageList.scrollToPosition(messages.size()-1)); }
    private void updateAttachmentsLabel() {
        if (attachedUrls.isEmpty()) input.setHint(R.string.app_chat_input_hint);
        else input.setHint(fragment.getString(R.string.app_chat_attachments_hint, attachedUrls.size()));
        renderAttachedPreviews();
    }
    private void renderAttachedPreviews() {
        if (attachmentRenderer != null) attachmentRenderer.render(attachedUrls, pendingPhotoUris);
    }
    private void post(Runnable action) { if (destroyed) return; fragment.requireActivity().runOnUiThread(() -> { if (!destroyed && fragment.getView()!=null) action.run(); }); }
    private void toast(String text) { if (!destroyed && fragment.getContext()!=null) Toast.makeText(fragment.requireContext(), text, Toast.LENGTH_LONG).show(); }
    private String message(Throwable e) {
        if (e instanceof IllegalArgumentException || e instanceof ChatException) return e.getMessage();
        return fragment.getString(R.string.app_chat_error_general);
    }
}
