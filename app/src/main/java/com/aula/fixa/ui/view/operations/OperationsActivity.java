package com.aula.fixa.ui.view.operations;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.aula.fixa.adapter.operations.OperationsViewModel;
import com.aula.fixa.R;
import com.aula.fixa.ui.config.operations.ScreenCatalog;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Host de Gestor/Técnico; usa a mesma arquitetura de Fragments do Solicitante.
 */
public class OperationsActivity extends AppCompatActivity {
    public static final String EXTRA_ROLE = "operations_role";
    private final int[] tabs = {R.id.opsNavHome, R.id.opsNavManagement, R.id.opsNavTeam, R.id.opsNavEquipment, R.id.opsNavEvents};
    private final int[] icons = {R.id.opsNavHomeIcon, R.id.opsNavManagementIcon, R.id.opsNavTeamIcon, R.id.opsNavEquipmentIcon, R.id.opsNavEventsIcon};
    private final int[] circles = {R.id.opsNavHomeCircle, R.id.opsNavManagementCircle, R.id.opsNavTeamCircle, R.id.opsNavEquipmentCircle, R.id.opsNavEventsCircle};
    private final int[] indicators = {R.id.opsNavHomeIndicator, R.id.opsNavManagementIndicator, R.id.opsNavTeamIndicator, R.id.opsNavEquipmentIndicator, R.id.opsNavEventsIndicator};
    private NavController navigation;
    private View bar;
    private String role;
    public OperationsViewModel model;
    private final ActivityResultLauncher<String[]> photoPicker = registerForActivityResult(
            new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                org.json.JSONArray selected = new org.json.JSONArray();

                for (android.net.Uri uri : uris) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException ignored) {
                    }
                    selected.put(uri.toString());
                }

                boolean managerOccurrenceCreation = role.equals("manager") && navigation != null
                        && navigation.getCurrentDestination() != null
                        && navigation.getCurrentDestination().getId() == R.id.ops_manager_occurrence_result
                        && navigation.getCurrentBackStackEntry() != null
                        && navigation.getCurrentBackStackEntry().getArguments() != null
                        && navigation.getCurrentBackStackEntry().getArguments().getLong("orderId", 0) == 0;
                if (managerOccurrenceCreation) {
                    org.json.JSONArray current;
                    try { current = new org.json.JSONArray(model.field("manager_new_occurrence_form", "photos")); }
                    catch (Exception ignored) { current = new org.json.JSONArray(); }
                    int added = 0;
                    for (android.net.Uri uri : uris) {
                        if (current.length() >= 3) break;
                        current.put(uri.toString()); added++;
                    }
                    model.field("manager_new_occurrence_form", "photos", current.toString());
                    if (added < uris.size()) Toast.makeText(this, "É possível adicionar no máximo 3 fotos.", Toast.LENGTH_LONG).show();
                    else Toast.makeText(this, getString(R.string.ops_photos_added, added), Toast.LENGTH_SHORT).show();
                } else {
                    model.field(role + "_occurrence_result", "photos", selected.toString());
                    Toast.makeText(this, getString(R.string.ops_photos_added, uris.size()), Toast.LENGTH_SHORT).show();
                }
                refresh();
            });
    private final ActivityResultLauncher<String> exportDocument = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), uri -> {
                if (uri == null) {
                    return;
                }

                try (java.io.OutputStream stream = getContentResolver().openOutputStream(uri)) {
                    if (stream == null) {
                        throw new java.io.IOException();
                    }

                    OperationsExport.write(this, model, stream);
                    Toast.makeText(this, R.string.ops_export_done, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, R.string.ops_export_error, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(true);

        role = "technician".equals(getIntent().getStringExtra(EXTRA_ROLE)) ? "technician" : "manager";
        getLifecycle().addObserver(new com.aula.fixa.ui.config.ConnectivityWatcher(this));

        setContentView(R.layout.activity_operations);
        model = new ViewModelProvider(this).get(OperationsViewModel.class);
        bar = findViewById(R.id.operationsBar);
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.operationsHost);
        if (host == null) {
            host = new NavHostFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.operationsHost, host)
                    .setPrimaryNavigationFragment(host).commitNow();
        }

        navigation = host.getNavController();
        if (state == null || navigation.getCurrentDestination() == null) {
            NavGraph graph = navigation.getNavInflater().inflate(R.navigation.nav_graph_operations);
            String initial = role + "_home";
            graph.setStartDestination(destination(initial));
            navigation.setGraph(graph);
        }

        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            findViewById(tabs[i]).setOnClickListener(v -> openTab(index));
        }

        if (role.equals("technician")) {
            findViewById(tabs[2]).setContentDescription(getString(R.string.ops_nav_profile));
        }

        navigation.addOnDestinationChangedListener((nav, dest, args) -> {
            boolean focusScreen = isFocusDestination(dest.getId());
            bar.setVisibility(focusScreen ? View.GONE : View.VISIBLE);
            WindowInsetsControllerCompat systemBars = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            systemBars.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            if (focusScreen) {
                systemBars.hide(WindowInsetsCompat.Type.navigationBars());
            } else {
                systemBars.show(WindowInsetsCompat.Type.navigationBars());
                updateBar(args == null ? role + "_home" : args.getString("screen", role + "_home"));
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.operationsRoot), (view, insets) -> {
            Insets system = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            int keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            view.setPadding(system.left, system.top, system.right, keyboard);
            bar.setPadding(0, 0, 0, keyboard > 0 ? 0 : system.bottom);
            ViewGroup.LayoutParams params = bar.getLayoutParams();
            params.height = getResources().getDimensionPixelSize(R.dimen.ops_nav_height) + (keyboard > 0 ? 0 : system.bottom);
            bar.setLayoutParams(params);
            return WindowInsetsCompat.CONSUMED;
        });

    }

    public String role() {
        return role;
    }

    private boolean isFocusDestination(int id) {
        return id == R.id.ops_new_request_intro
                || id == R.id.ops_new_request_description
                || id == R.id.ops_new_request_location
                || id == R.id.ops_new_request_photo
                || id == R.id.ops_new_request_confirm
                || id == R.id.ops_new_request_success
                || (role.equals("manager") && (id == R.id.ops_manager_request_details
                || id == R.id.ops_manager_occurrence_result));
    }

    public void photos() {
        photoPicker.launch(new String[]{"image/*"});
    }

    public void export() {
        exportDocument.launch("FIXA_exportacao.xlsx");
    }

    public void refresh() {
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.operationsHost);
        androidx.fragment.app.Fragment fragment = host == null ? null : host.getChildFragmentManager().getPrimaryNavigationFragment();
        if (fragment instanceof OperationsScreenFragment) {
            ((OperationsScreenFragment) fragment).render();
        }
    }

    private int destination(String key) {
        int id = getResources().getIdentifier("ops_" + key, "id", getPackageName());
        if (id == 0) {
            throw new IllegalArgumentException("Destino não registrado: " + key);
        }
        return id;
    }

    public void open(String key) {
        JSONObject spec = ScreenCatalog.get(this).screen(key);
        if (spec.optBoolean("dialog")) {
            if (getSupportFragmentManager().findFragmentByTag("ops_dialog") == null) {
                OperationsDialogFragment.create(key).show(getSupportFragmentManager(), "ops_dialog");
            }
        } else {
            navigation.navigate(destination(key));
        }
    }

    public void openName(String name) {
        open(role + "_" + name);
    }

    public void openCompetencies(long technicianId) {
        Bundle args = new Bundle();
        args.putLong("technicianId", technicianId);
        navigation.navigate(R.id.ops_manager_competencies, args);
    }

    public void openCompetencyEdit(com.aula.fixa.domain.model.tecnico.CompetenciaTecnico competence) {
        Bundle args = new Bundle();
        args.putLong("competenceId", competence.id == null ? 0 : competence.id);
        args.putString("competenceName", competence.nome);
        args.putString("oldScore", competence.nota == null ? "_" : competence.nota.stripTrailingZeros().toPlainString());
        if (getSupportFragmentManager().findFragmentByTag("ops_dialog") == null) {
            OperationsDialogFragment.create("manager_edit_competency", args).show(getSupportFragmentManager(), "ops_dialog");
        }
    }

    public void openRequestDetails(long id, String title) {
        Bundle args = new Bundle();
        args.putLong("requestId", id);
        args.putString("requestTitle", title == null ? "" : title);
        navigation.navigate(destination(role + "_request_details"), args);
    }

    public void openOrderDetails(long id, String title) {
        Bundle args = new Bundle();
        args.putLong("orderId", id);
        args.putString("orderTitle", title == null ? "" : title);
        navigation.navigate(R.id.ops_manager_execution_pending, args);
    }

    public void openOccurrenceDetails(long id, String title) {
        Bundle args = new Bundle();
        args.putLong("occurrenceId", id);
        args.putString("occurrenceTitle", title == null ? "" : title);
        navigation.navigate(R.id.ops_manager_occurrence_details, args);
    }

    public void openOccurrenceEdit(com.aula.fixa.domain.model.ocorrencia.OcorrenciaItem item) {
        Bundle args = new Bundle();
        args.putLong("occurrenceId", item.id);
        args.putString("localName", item.localUnidade);
        args.putString("equipmentName", item.categoriaEquipamento);
        args.putString("categoryName", item.categoriaProblema);
        args.putString("priorityName", item.prioridade);
        long[] photoIds = new long[item.fotos.size()];
        java.util.ArrayList<String> photoUrls = new java.util.ArrayList<>();
        for (int i = 0; i < item.fotos.size(); i++) {
            photoIds[i] = item.fotos.get(i).id;
            photoUrls.add(item.fotos.get(i).url);
        }
        args.putLongArray("photoIds", photoIds);
        args.putStringArray("photoUrls", photoUrls.toArray(new String[0]));
        navigation.navigate(R.id.ops_manager_occurrence_edit_information, args);
    }

    public void openOccurrenceEditPhotos(long occurrenceId, long[] photoIds, java.util.ArrayList<String> photoUrls) {
        Bundle args = new Bundle();
        args.putLong("occurrenceId", occurrenceId);
        args.putLongArray("photoIds", photoIds);
        args.putStringArray("photoUrls", photoUrls == null ? new String[0] : photoUrls.toArray(new String[0]));
        navigation.navigate(R.id.ops_manager_occurrence_edit_photos, args);
    }

    public void finishOccurrenceEdit() {
        if (!navigation.popBackStack(R.id.ops_manager_occurrences, false)) back();
    }

    public void openOrderCompletion(long id) {
        Bundle args = new Bundle();
        args.putLong("orderId", id);
        navigation.navigate(R.id.ops_manager_occurrence_result, args);
    }

    public void openRequestCreation() {
        if (navigation.getCurrentDestination() != null
                && navigation.getCurrentDestination().getId() == R.id.ops_manager_requests) {
            navigation.navigate(R.id.action_manager_requests_to_new_request);
        } else {
            navigation.navigate(R.id.ops_new_request_intro);
        }
    }

    public void openPlanningIntro(long requestId) {
        model.clearEntries("planning_tasks");
        model.field("planning_tasks", "ordemServicoId", "");
        for (String field : new String[]{"tecnicoId", "Competência", "CompetênciaId",
                "Prioridade", "PrioridadeId", "mes", "prazo"}) {
            model.field("plan_information", field, "");
        }
        model.selected("planning:first_task_prompt_shown", false);
        Bundle args = new Bundle();
        args.putLong("requestId", requestId);
        navigation.navigate(R.id.ops_manager_planning_intro, args);
    }

    public void openPlanningStep(String screen) {
        int id = getResources().getIdentifier("ops_manager_" + screen, "id", getPackageName());
        if (id == 0) throw new IllegalArgumentException("Etapa de planejamento não registrada: " + screen);
        Bundle args = new Bundle();
        androidx.navigation.NavBackStackEntry current = navigation.getCurrentBackStackEntry();
        long requestId = current == null ? 0 : current.getArguments().getLong("requestId", 0);
        args.putLong("requestId", requestId);
        navigation.navigate(id, args);
    }

    public void back() {
        if (!navigation.popBackStack()) {
            finish();
        }
    }

    public void returnFromPlanningAssignment(boolean editingOrder) {
        int target = editingOrder ? R.id.ops_manager_plan_information : R.id.ops_manager_planning_tasks;
        if (!navigation.popBackStack(target, false)) {
            long requestId = navigation.getCurrentBackStackEntry() == null ? 0
                    : navigation.getCurrentBackStackEntry().getArguments().getLong("requestId", 0);
            Bundle arguments = new Bundle();
            arguments.putLong("requestId", requestId);
            navigation.navigate(target, arguments);
        }
    }

    private void openTab(int index) {
        String[] targets = {"home", "management", role.equals("manager") ? "team" : "profile", role.equals("manager") ? "equipment" : "checklists", "events"};
        String target = role + "_" + targets[index];
        navigation.navigate(destination(target), null, new NavOptions.Builder().setLaunchSingleTop(true)
                .setPopUpTo(destination(role + "_home"), false).build());
    }

    private void updateBar(String key) {
        JSONObject spec = ScreenCatalog.get(this).screen(key);
        bar.setVisibility(spec.optBoolean("navigation") ? View.VISIBLE : View.GONE);
        int active = spec.optInt("navActive");
        JSONArray assets = spec.optJSONArray("navAssets");
        JSONArray slots = spec.optJSONArray("navSlots");
        for (int i = 0; i < tabs.length; i++) {
            findViewById(tabs[i]).setSelected(i == active);
            findViewById(circles[i]).setVisibility(i == active ? View.VISIBLE : View.INVISIBLE);
            findViewById(indicators[i]).setVisibility(i == active ? View.VISIBLE : View.INVISIBLE);
            if (assets != null) {
                String asset = assets.optString(i).replaceFirst("\\.[^.]+$", "").replace('-', '_');
                int resource = getResources().getIdentifier("figma_" + asset, "drawable", getPackageName());
                if (resource != 0) {
                    ImageView icon = findViewById(icons[i]);
                    icon.setImageResource(resource);
                    JSONObject slot = slots == null ? null : slots.optJSONObject(i);
                    if (slot != null) {
                        float factor = getResources().getDimension(R.dimen.ops_nav_circle) / 56f;
                        ViewGroup.LayoutParams lp = icon.getLayoutParams();
                        lp.width = Math.round((float) slot.optDouble("w", 26) * factor);
                        lp.height = Math.round((float) slot.optDouble("h", 26) * factor);
                        icon.setLayoutParams(lp);
                    }
                }
            }
        }
    }
}
