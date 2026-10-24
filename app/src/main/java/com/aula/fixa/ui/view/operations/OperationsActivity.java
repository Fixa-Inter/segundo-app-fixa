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

                model.field(role + "_occurrence_result", "photos", selected.toString());
                Toast.makeText(this, getString(R.string.ops_photos_added, uris.size()), Toast.LENGTH_SHORT).show();
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
                || (role.equals("manager") && id == R.id.ops_manager_request_details);
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

    public void openRequestDetails(long id, String title) {
        Bundle args = new Bundle();
        args.putLong("requestId", id);
        args.putString("requestTitle", title == null ? "" : title);
        navigation.navigate(destination(role + "_request_details"), args);
    }

    public void openRequestCreation() {
        navigation.navigate(R.id.ops_new_request_intro);
    }

    public void back() {
        if (!navigation.navigateUp()) {
            finish();
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
