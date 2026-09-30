package com.aula.fixa;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Host das telas pós-login. Mantém a bottom nav customizada do Figma (círculo ativo + indicador)
 * sincronizada com o NavController e a esconde nas telas secundárias.
 * Diferente das telas de autenticação, aqui a barra de navegação do sistema fica visível:
 * o conteúdo recebe o inset da status bar e a bottom nav recebe o inset da barra de gestos.
 */
public class MainActivity extends AppCompatActivity {

    private static final Set<Integer> TOP_LEVEL = new HashSet<>(Arrays.asList(
            R.id.homeFragment, R.id.requestsFragment, R.id.eventsFragment, R.id.chatbotFragment));

    private NavController navController;
    private View bottomNav;
    private ConstraintLayout bottomNavRoot;
    private int bottomNavBaseHeight;
    private int bottomInset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(true);
        setContentView(R.layout.activity_main);

        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (host == null) return;
        navController = host.getNavController();

        View navHost = findViewById(R.id.navHostFragment);
        bottomNav = findViewById(R.id.bottomNav);
        // O <include> tem android:id="@+id/bottomNav", que substitui o id da raiz incluída.
        bottomNavRoot = (ConstraintLayout) bottomNav;
        bottomNavBaseHeight = getResources().getDimensionPixelSize(R.dimen.app_bottom_nav_height);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            navHost.setPadding(bars.left, bars.top, bars.right, 0);
            bottomInset = bars.bottom;
            applyBottomNavHeight();
            return WindowInsetsCompat.CONSUMED;
        });

        bindNavItem(R.id.navHome, R.id.homeFragment);
        bindNavItem(R.id.navRequests, R.id.requestsFragment);
        bindNavItem(R.id.navEvents, R.id.eventsFragment);
        bindNavItem(R.id.navChat, R.id.chatbotFragment);

        navController.addOnDestinationChangedListener((controller, destination, args) ->
                onDestinationChanged(destination));
    }

    /** Barra cresce para acomodar a barra de gestos, mantendo os ícones na posição do Figma. */
    private void applyBottomNavHeight() {
        ViewGroup.LayoutParams params = bottomNav.getLayoutParams();
        params.height = bottomNavBaseHeight + bottomInset;
        bottomNav.setLayoutParams(params);
        bottomNav.setPadding(0, 0, 0, bottomInset);
    }

    private void bindNavItem(@IdRes int viewId, @IdRes int destinationId) {
        bottomNav.findViewById(viewId).setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == destinationId) return;
            NavOptions options = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(R.id.homeFragment, false)
                    .build();
            navController.navigate(destinationId, null, options);
        });
    }

    private void onDestinationChanged(@NonNull NavDestination destination) {
        boolean topLevel = TOP_LEVEL.contains(destination.getId());
        bottomNav.setVisibility(topLevel ? View.VISIBLE : View.GONE);
        if (!topLevel) return;

        int activeItem = destination.getId() == R.id.requestsFragment ? R.id.navRequests
                : destination.getId() == R.id.eventsFragment ? R.id.navEvents
                : destination.getId() == R.id.chatbotFragment ? R.id.navChat
                : R.id.navHome;

        setSelected(R.id.navHomeCircle, R.id.navHomeIcon, activeItem == R.id.navHome);
        setSelected(R.id.navRequestsCircle, R.id.navRequestsIcon, activeItem == R.id.navRequests);
        setSelected(R.id.navEventsCircle, R.id.navEventsIcon, activeItem == R.id.navEvents);
        setSelected(R.id.navChatCircle, R.id.navChatIcon, activeItem == R.id.navChat);

        ConstraintSet set = new ConstraintSet();
        set.clone(bottomNavRoot);
        set.connect(R.id.navIndicator, ConstraintSet.START, activeItem, ConstraintSet.START);
        set.connect(R.id.navIndicator, ConstraintSet.END, activeItem, ConstraintSet.END);
        set.applyTo(bottomNavRoot);
    }

    private void setSelected(@IdRes int circleId, @IdRes int iconId, boolean selected) {
        bottomNav.findViewById(circleId).setSelected(selected);
        bottomNav.findViewById(iconId).setSelected(selected);
    }
}
