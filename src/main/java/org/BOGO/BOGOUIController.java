package org.BOGO;

import javafx.animation.StrokeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.BOGO.domain.booking.Path;
import org.BOGO.domain.transport.Location;
import org.BOGO.domain.transport.Route;
import org.BOGO.domain.transport.Stop;
import org.BOGO.domain.user.Driver;
import org.BOGO.domain.user.Passenger;
import org.BOGO.domain.user.User;
import org.BOGO.service.BusServices;
import org.BOGO.service.HandleDriverService;
import org.BOGO.service.ManageStopsService;
import org.BOGO.service.PathBuildingService;
import org.BOGO.service.RouteReviseService;
import org.BOGO.service.ViewMapService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BOGOUIController {
    public Label lbl_total_Drivers;
    @FXML private StackPane content_area;
    @FXML private Button btn_nav_dashboard;
    @FXML private Button btn_nav_analytics;
    @FXML private Button btn_nav_alerts;
    @FXML private Button btn_nav_add_resource;
    @FXML private Button btn_nav_add_stops_routes;
    @FXML private Button btn_nav_assign_route;
    @FXML private Button btn_nav_send_alert;
    @FXML private Button btn_nav_view_route;
    @FXML private Button btn_nav_bus_metrics;
    @FXML private Button btn_nav_book;
    @FXML private Button btn_nav_multi_stop;

    @FXML private ListView<String> list_system_alerts;
    @FXML private ListView<String> list_employees;
    @FXML private ListView<String> list_drivers;
    @FXML private ListView<String> list_active_alerts;
    @FXML private ListView<String> list_boarding_passengers;
    @FXML private Pane pane_detail_overlay;
    @FXML private Pane pane_resolution_dialog;
    @FXML private Pane pane_cancel_ride;
    @FXML private Label lbl_detail_name;
    @FXML private Label lbl_detail_id;
    @FXML private Label lbl_total_passengers;
    @FXML private Label lbl_total_revenue;
    @FXML private TextField txt_search_employee;
    @FXML private TextField txt_search_driver;
    @FXML private ComboBox<Stop> combo_stop_1;
    @FXML private ComboBox<Stop> combo_stop_2;
    @FXML private ComboBox<String> choice_max_capacity;
    @FXML private ComboBox<Integer> combo_stop_count;
    @FXML private VBox box_dynamic_stops;
    @FXML private VBox box_active_bookings;
    @FXML private Label lbl_booking_status;
    @FXML private Label lbl_multi_booking_status;
    @FXML private ProgressIndicator gauge_tyres;
    @FXML private ProgressIndicator gauge_engine;
    @FXML private ProgressIndicator gauge_chassis;
    @FXML private Label lbl_tyres_percent;
    @FXML private Label lbl_engine_percent;
    @FXML private Label lbl_chassis_percent;
    // Driver — Send Alert form
    @FXML private TextArea txt_alert_message;
    @FXML private javafx.scene.control.ToggleGroup priority_group;
    @FXML private javafx.scene.control.ToggleGroup type_group;
    @FXML private Label lbl_alert_status;
    @FXML private ToggleButton toggle_login;
    @FXML private ToggleButton toggle_signup;
    @FXML private TextField txt_email;
    @FXML private TextField txt_driver_id;
    @FXML private PasswordField txt_password;
    @FXML private Label lbl_driver_login_status;
    @FXML private TextField txt_login_email;
    @FXML private PasswordField txt_login_password;
    @FXML private TextField txt_fname;
    @FXML private TextField txt_lname;
    @FXML private TextField txt_signup_email;
    @FXML private TextField txt_signup_cnic;
    @FXML private Label lbl_signup_status;
    @FXML private PasswordField pf_password;
    @FXML private PasswordField pf_confirm_password;
    @FXML private Pane pane_login;
    @FXML private Pane pane_signup;
    @FXML private Pane booking_map_canvas;

    // Cancel-ride tracking
    private int pendingCancelBookingId = -1;
    private String pendingCancelPath = null;
    @FXML private Label lbl_cancel_path;

    // New Canvas-based booking map (replaces old Pane+node approach)
    private org.BOGO.map.BookingMapRenderer bookingMapRenderer;
    private org.BOGO.map.CoordinateNormalizer bookingNormalizer;
    private java.util.List<org.BOGO.domain.transport.Route> bookingAllRoutes;
    private javafx.scene.canvas.Canvas bookingCanvas;

    // AddStopsRoutes page fields
    @FXML private TextField txt_new_stop_name;
    @FXML private TextField txt_new_stop_lat;
    @FXML private TextField txt_new_stop_lon;
    // 4 adjacent-stop combos (replaces old single combo_connect_stop)
    @FXML private ComboBox<Stop> combo_adj_stop1;
    @FXML private ComboBox<Stop> combo_adj_stop2;
    @FXML private ComboBox<Stop> combo_adj_stop3;
    @FXML private ComboBox<Stop> combo_adj_stop4;
    @FXML private Label lbl_stop_status;
    @FXML private TextField txt_new_route_name;
    @FXML private ComboBox<Stop> combo_route_stop1;
    @FXML private ComboBox<Stop> combo_route_stop2;
    @FXML private ComboBox<Stop> combo_route_stop3;
    @FXML private TextField txt_route_time;
    @FXML private Label lbl_route_status;

    // AssignRoute page fields
    @FXML private ComboBox<Driver> combo_assign_driver;
    @FXML private ComboBox<Route> combo_assign_route;
    @FXML private ComboBox<org.BOGO.domain.transport.Bus> combo_assign_bus;
    @FXML private Label lbl_assign_status;
    @FXML private javafx.scene.control.ListView<String> list_current_assignments;

    // Map canvas pane (MapView_ALL.fxml / Passenger and Driver Dashboards)
    @FXML private Pane map_canvas_pane;
    // Driver-route-specific canvas (Driver_ViewRoute.fxml only)
    @FXML private Pane driver_route_map_pane;

    // AddResource page fields
    @FXML private TextField txt_bus_number;
    @FXML private TextField txt_bus_company;
    @FXML private TextField txt_bus_year;
    @FXML private TextField txt_driver_fname;
    @FXML private TextField txt_driver_lname;
    @FXML private TextField txt_driver_cnic;
    @FXML private TextField txt_driver_email;
    @FXML private Label lbl_bus_status;
    @FXML private Label lbl_driver_status;

    // Tracks the alert selected in the Resolve Alerts list
    private org.BOGO.domain.communication.Alert selectedAlert;

    // Services for new pages
    private final ManageStopsService manageStopsService = new ManageStopsService();
    private final RouteReviseService routeReviseService = new RouteReviseService();
    private final HandleDriverService handleDriverService = new HandleDriverService();
    private final BusServices busServices = new BusServices();
    private final ViewMapService viewMapService = new ViewMapService();

    private org.BOGO.domain.transport.Map bookingTransportMap;
    private PathBuildingService bookingPathService;
    // bookingStopNodes / bookingMapLines removed â€” Canvas rendering replaces them

    // Live map rendering fields
    private final java.util.Map<Integer, StackPane> liveStopNodes  = new HashMap<>();
    private final java.util.Map<Integer, StackPane> liveBusMarkers = new HashMap<>();
    private org.BOGO.controller.GeneralMapController generalMapController;

    @FXML
    private void initialize() {
        seedPreviewData();
        // ── General live map (all routes + all buses) ─────────────────────────
        if (map_canvas_pane != null) {
            javafx.scene.canvas.Canvas gmCanvas = new javafx.scene.canvas.Canvas();
            gmCanvas.widthProperty().bind(map_canvas_pane.widthProperty());
            gmCanvas.heightProperty().bind(map_canvas_pane.heightProperty());
            map_canvas_pane.getChildren().setAll(gmCanvas);
            generalMapController = new org.BOGO.controller.GeneralMapController(gmCanvas, null);
            generalMapController.initialize();
        }
        // ── Driver route map (driver-specific: View Route page only) ──────────
        if (driver_route_map_pane != null) {
            javafx.scene.canvas.Canvas drCanvas = new javafx.scene.canvas.Canvas();
            drCanvas.widthProperty().bind(driver_route_map_pane.widthProperty());
            drCanvas.heightProperty().bind(driver_route_map_pane.heightProperty());
            driver_route_map_pane.getChildren().setAll(drCanvas);
            org.BOGO.domain.user.User me = BOGOApplication.getInstance().getCurrentUser();
            if (me instanceof org.BOGO.domain.user.Driver driver) {
                new org.BOGO.controller.DriverRouteMapController(
                        drCanvas, driver.getUserID(), null).initialize();
            } else {
                // Fallback: general map if no driver is logged in
                new org.BOGO.controller.GeneralMapController(drCanvas, null).initialize();
            }
        }
        if (content_area != null) {
            if (btn_nav_dashboard != null && btn_nav_book != null) {
                loadPassengerDashboard();
            } else if (btn_nav_dashboard != null && btn_nav_send_alert != null) {
                loadDriverDashboard();
            } else if (btn_nav_dashboard != null) {
                loadAdminDashboard();
            }
        }
    }

    @FXML private void goMain() { switchScene("/Main.fxml"); }
    @FXML private void goAdminLogin() { switchScene("/AdminUI/AdminLogin.fxml"); }
    @FXML private void goPassengerLogin() { switchScene("/PassengerUI/PassengerLogin_SignUp.fxml"); }
    @FXML private void goDriverLogin() { switchScene("/DriverUI/DriverSignIn.fxml"); }

    @FXML
    private void loginAdmin() {
        if (authenticateFromFields(txt_email, txt_password)) {
            switchScene("/AdminUI/AdminShell.fxml");
        }
    }

    @FXML
    private void loginDriver() {
        if (txt_driver_id == null || txt_password == null) return;
        String driverId = txt_driver_id.getText().trim();
        String password = txt_password.getText();

        if (driverId.isEmpty() || password.isEmpty()) {
            if (lbl_driver_login_status != null)
                lbl_driver_login_status.setText("⚠ Enter your Driver ID and password.");
            return;
        }

        // Look up driver by their DriverID string (e.g. DR-001), then verify password
        org.BOGO.repository.UserRepository repo = new org.BOGO.repository.UserRepository();
        User user = repo.findUserByDriverId(driverId);
        if (user == null || !user.getPassword().equals(password)) {
            if (lbl_driver_login_status != null)
                lbl_driver_login_status.setText("⚠ Invalid Driver ID or password.");
            return;
        }
        BOGOApplication.getInstance().setCurrentUser(user);
        switchScene("/DriverUI/DriverShell.fxml");
    }

    @FXML
    private void loginPassenger() {
        if (authenticateFromFields(txt_login_email, txt_login_password)) {
            switchScene("/PassengerUI/PassengerShell.fxml");
        }
    }

    @FXML
    private void signupPassenger() {
        if (lbl_signup_status != null) lbl_signup_status.setText("");

        String firstName = txt_fname == null ? "" : txt_fname.getText().trim();
        String lastName  = txt_lname == null ? "" : txt_lname.getText().trim();
        String name      = (firstName + " " + lastName).trim();
        String email     = txt_signup_email == null ? "" : txt_signup_email.getText().trim();
        String cnic      = txt_signup_cnic  == null ? "" : txt_signup_cnic.getText().trim();
        String password  = pf_password == null ? "" : pf_password.getText();
        String confirm   = pf_confirm_password == null ? "" : pf_confirm_password.getText();

        // â”€â”€ Client-side validation â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (name.isBlank()) { setSignupError("Name is required."); return; }
        if (!email.contains("@")) { setSignupError("Enter a valid email address."); return; }
        if (!password.equals(confirm)) { setSignupError("Passwords do not match."); return; }
        if (password.length() < 8 || password.length() > 20)
            { setSignupError("Password must be 8â€“20 characters."); return; }
        if (!password.matches(".*[A-Z].*"))
            { setSignupError("Password must contain at least one uppercase letter."); return; }
        if (!password.matches(".*[0-9].*"))
            { setSignupError("Password must contain at least one digit."); return; }
        if (!cnic.matches("\\d{13}"))
            { setSignupError("CNIC must be exactly 13 digits (no dashes)."); return; }

        User user = BOGOApplication.getInstance().getUserController()
                .registerPassenger(name, email, password, cnic);
        if (user != null) {
            BOGOApplication.getInstance().setCurrentUser(user);
            switchScene("/PassengerUI/PassengerShell.fxml");
        } else {
            setSignupError("Registration failed â€” email may already be in use.");
        }
    }

    private void setSignupError(String msg) {
        if (lbl_signup_status != null) lbl_signup_status.setText(msg);
    }

    private boolean authenticateFromFields(TextField emailField, PasswordField passwordField) {
        if (emailField == null || passwordField == null) {
            return false;
        }
        User user = BOGOApplication.getInstance().getUserController().login(emailField.getText(), passwordField.getText());
        if (user == null) {
            return false;
        }
        BOGOApplication.getInstance().setCurrentUser(user);
        return true;
    }

    @FXML private void loadAdminDashboard() { loadContent("/AdminUI/Admin_Dashboard.fxml", btn_nav_dashboard); }
    @FXML private void loadAdminAnalytics() {
        loadContent("/AdminUI/Admin_Analytics.fxml", btn_nav_analytics);
        Platform.runLater(this::loadEmployeesFromDB);
    }
    @FXML private void loadAdminAlerts() { loadContent("/AdminUI/Admin_ResolveAlert.fxml", btn_nav_alerts); }
    @FXML private void loadAdminAddResource() { loadContent("/AdminUI/Admin_AddResource.fxml", btn_nav_add_resource); }
    @FXML private void loadAdminAddStopsRoutes() {
        loadContent("/AdminUI/Admin_AddStopsRoutes.fxml", btn_nav_add_stops_routes);
        Platform.runLater(this::populateStopCombos);
    }
    @FXML private void loadAdminAssignRoute() {
        loadContent("/AdminUI/Admin_AssignRoute.fxml", btn_nav_assign_route);
        Platform.runLater(this::populateAssignmentCombos);
    }
    @FXML private void loadDriverDashboard() { loadContent("/DriverUI/Driver_Dashboard.fxml", btn_nav_dashboard); }
    @FXML private void loadDriverSendAlert() { loadContent("/DriverUI/Driver_SendAlerts.fxml", btn_nav_send_alert); }
    @FXML private void loadDriverRoute() { loadContent("/DriverUI/Driver_ViewRoute.fxml", btn_nav_view_route); }
    @FXML private void loadDriverMetrics() {
        loadContent("/DriverUI/Driver_BusMetrics.fxml", btn_nav_bus_metrics);
        Platform.runLater(this::loadDriverHealthMetrics);
    }
    @FXML private void loadPassengerDashboard() { loadContent("/PassengerUI/Passenger_Dashboard.fxml", btn_nav_dashboard); }
    @FXML private void loadPassengerBookRide() { loadContent("/PassengerUI/Passenger_BookRide.fxml", btn_nav_book); }
    @FXML private void loadPassengerMultiStop() { loadContent("/PassengerUI/Passenger_MultiStopBooking.fxml", btn_nav_multi_stop); }

    @FXML
    private void showLoginPane() {
        if (pane_login != null && pane_signup != null) {
            pane_login.setVisible(true);
            pane_signup.setVisible(false);
        }
    }

    @FXML
    private void showSignupPane() {
        if (pane_login != null && pane_signup != null) {
            pane_login.setVisible(false);
            pane_signup.setVisible(true);
        }
    }

    @FXML private void showResolutionDialog() { if (pane_resolution_dialog != null) pane_resolution_dialog.setVisible(true); }
    @FXML private void hideResolutionDialog() { if (pane_resolution_dialog != null) pane_resolution_dialog.setVisible(false); }
    @FXML private void showCancelRidePane() {
        if (lbl_cancel_path != null)
            lbl_cancel_path.setText(pendingCancelPath != null && !pendingCancelPath.isBlank()
                    ? pendingCancelPath : "Route info unavailable");
        if (pane_cancel_ride != null) pane_cancel_ride.setVisible(true);
    }
    @FXML private void hideCancelRidePane() {
        pendingCancelBookingId = -1;
        pendingCancelPath = null;
        if (pane_cancel_ride != null) pane_cancel_ride.setVisible(false);
    }

    @FXML
    private void cancelRideConfirmed() {
        if (pendingCancelBookingId < 0) { hideCancelRidePane(); return; }
        User cu = BOGOApplication.getInstance().getCurrentUser();
        if (cu instanceof Passenger p) {
            boolean ok = new org.BOGO.service.CancelationService()
                    .cancelBooking(pendingCancelBookingId, p.getUserID());
            if (ok) System.out.println("[CancelRide] Booking #" + pendingCancelBookingId + " cancelled.");
        }
        pendingCancelBookingId = -1;
        hideCancelRidePane();
        loadPassengerBookingsFromDB(); // refresh the list
    }

    private void loadContent(String resource, Button activeButton) {
        if (content_area == null) {
            return;
        }

        // TODO: Navigation Logic - Update style of active button and load respective FXML into content_area.
        try {
            content_area.getChildren().setAll((Node) FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resource))));
            setActive(activeButton);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load " + resource, ex);
        }
    }

    private void setActive(Button activeButton) {
        Button[] buttons = {
                btn_nav_dashboard, btn_nav_analytics, btn_nav_alerts, btn_nav_add_resource,
                btn_nav_add_stops_routes, btn_nav_assign_route,
                btn_nav_send_alert, btn_nav_view_route, btn_nav_bus_metrics,
                btn_nav_book, btn_nav_multi_stop
        };
        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("nav-button-active");
                if (!button.getStyleClass().contains("nav-button")) {
                    button.getStyleClass().add("nav-button");
                }
            }
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("nav-button-active")) {
            activeButton.getStyleClass().add("nav-button-active");
        }
    }

    private void switchScene(String resource) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(resource)));
            Scene scene = new Scene(root, 1180, 760);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/NeonTheme.css")).toExternalForm());
            Stage stage = Stage.getWindows().stream()
                    .filter(window -> window instanceof Stage && window.isShowing())
                    .map(window -> (Stage) window)
                    .findFirst()
                    .orElseThrow();
            stage.setScene(scene);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load " + resource, ex);
        }
    }

    private void seedPreviewData() {
        // â”€â”€ Dashboard: system alerts from DB â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (list_system_alerts != null) {
            loadDashboardAlertsFromDB();
        }
        // â”€â”€ Analytics: passenger/driver counts and lists from DB â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (lbl_total_passengers != null || list_employees != null) {
            loadPassengersFromDB();
        }
        if (lbl_total_Drivers != null || list_drivers != null) {
            loadDriversFromDB();
        }
        // â”€â”€ Resolve Alerts page â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (list_active_alerts != null) {
            loadAlertsFromDB();
        }
        // â”€â”€ Passenger booking map (Book Ride + Multi-Stop) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (booking_map_canvas != null) {
            initializeBookingMap();
        }
        // â”€â”€ Passenger dashboard bookings from DB â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (box_active_bookings != null) {
            loadPassengerBookingsFromDB();
        }
        // ── Driver dashboard: boarding passengers from DB ───────────────────
        if (list_boarding_passengers != null) {
            Platform.runLater(this::loadBoardingPassengersFromDB);
        }
        // â”€â”€ Passenger multi-stop combo â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (combo_stop_count != null) {
            combo_stop_count.setItems(FXCollections.observableArrayList(3, 4, 5));
            combo_stop_count.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, value) -> {
                if (value != null) populateDynamicStops(value);
            });
            combo_stop_count.getSelectionModel().select(Integer.valueOf(3));
        }
        // â”€â”€ Add Bus capacity combo â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (choice_max_capacity != null) {
            choice_max_capacity.setItems(FXCollections.observableArrayList("30", "50"));
            choice_max_capacity.getSelectionModel().selectFirst();
        }
        // â”€â”€ Driver bus-metrics gauges â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (gauge_tyres != null) {
            // Default to 0 (no bus assigned). loadDriverHealthMetrics() overrides with DB data.
            setGaugeValue(gauge_tyres,   lbl_tyres_percent,   0.0);
            setGaugeValue(gauge_engine,  lbl_engine_percent,  0.0);
            setGaugeValue(gauge_chassis, lbl_chassis_percent, 0.0);
            Platform.runLater(this::loadDriverHealthMetrics);
        }
        // â”€â”€ Search filter wiring (works regardless of data source) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (txt_search_employee != null && list_employees != null) {
            list_employees.getSelectionModel().selectedItemProperty()
                    .addListener((obs, o, v) -> showDetail(v));
        }
        if (txt_search_driver != null && list_drivers != null) {
            list_drivers.getSelectionModel().selectedItemProperty()
                    .addListener((obs, o, v) -> showDetail(v));
        }
        // â”€â”€ Add Stops / Routes page â€” populate from DB and wire cascades â”€â”€â”€â”€â”€â”€â”€
        if (combo_adj_stop1 != null || combo_route_stop1 != null) {
            populateStopCombos();
            setupRouteCascade();
        }
        // â”€â”€ Assign Route page â€” populate combos from DB â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (combo_assign_driver != null) {
            populateAssignmentCombos();
        }
    }

    private void initializeBookingMap() {
        // â”€â”€ All DB work on a background thread â€” FX thread never blocks â”€â”€
        new Thread(() -> {
            org.BOGO.domain.transport.Map map = buildBookingMapFromDB();
            // PathBuildingService must use ALL stops (even those without GPS coords)
            // so the BFS graph is complete and can find paths through intermediate stops.
            // buildBookingMapFromDB() excludes location-less stops (rendering-only);
            // initializeFromDatabase() loads EVERY stop from the DB.
            PathBuildingService pathSvc = new PathBuildingService();
            pathSvc.initializeFromDatabase();
            // Also load routes for the renderer (cached â€” not called during draws)
            java.util.List<org.BOGO.domain.transport.Route> routes =
                    new org.BOGO.repository.RouteRepository().findAll();

            Platform.runLater(() -> {
                bookingTransportMap = map;
                bookingPathService  = pathSvc;
                bookingAllRoutes    = routes;

                // Wire stop combos (Book Ride page only)
                if (combo_stop_1 != null && combo_stop_2 != null) {
                    configureStopCombo(combo_stop_1);
                    configureStopCombo(combo_stop_2);
                    combo_stop_1.setItems(FXCollections.observableArrayList(bookingTransportMap.getStops()));
                    combo_stop_2.setItems(FXCollections.observableArrayList(bookingTransportMap.getStops()));
                    combo_stop_1.getSelectionModel().selectFirst();
                    if (bookingTransportMap.getStops().size() > 1)
                        combo_stop_2.getSelectionModel().select(bookingTransportMap.getStops().size() - 1);
                    combo_stop_1.getSelectionModel().selectedItemProperty()
                            .addListener((obs, ov, v) -> highlightBookingPath());
                    combo_stop_2.getSelectionModel().selectedItemProperty()
                            .addListener((obs, ov, v) -> highlightBookingPath());
                }

                if (booking_map_canvas != null) {
                    // Create a Canvas that fills the Pane â€” no nodes, no StackPanes
                    bookingCanvas = new javafx.scene.canvas.Canvas();
                    bookingCanvas.widthProperty().bind(booking_map_canvas.widthProperty());
                    bookingCanvas.heightProperty().bind(booking_map_canvas.heightProperty());
                    booking_map_canvas.getChildren().setAll(bookingCanvas);

                    bookingNormalizer = new org.BOGO.map.CoordinateNormalizer();
                    bookingNormalizer.calibrate(
                            new java.util.ArrayList<>(bookingTransportMap.getStops()),
                            booking_map_canvas.getWidth(), booking_map_canvas.getHeight());

                    bookingMapRenderer = new org.BOGO.map.BookingMapRenderer(bookingCanvas, bookingNormalizer);
                    redrawBookingMap();

                    // Re-calibrate and redraw only on resize
                    bookingCanvas.widthProperty().addListener((obs, o, n) -> {
                        bookingNormalizer.calibrate(
                                new java.util.ArrayList<>(bookingTransportMap.getStops()),
                                n.doubleValue(), bookingCanvas.getHeight());
                        highlightBookingPath();
                    });
                    bookingCanvas.heightProperty().addListener((obs, o, n) -> {
                        bookingNormalizer.calibrate(
                                new java.util.ArrayList<>(bookingTransportMap.getStops()),
                                bookingCanvas.getWidth(), n.doubleValue());
                        highlightBookingPath();
                    });
                }
            });
        }, "BookingMapInit").start();
    }

    /** Redraws the static base map (routes + stops) without a highlighted path. */
    private void redrawBookingMap() {
        if (bookingMapRenderer == null || bookingAllRoutes == null) return;
        bookingMapRenderer.draw(
                new java.util.ArrayList<>(bookingTransportMap.getStops()), bookingAllRoutes);
    }

    @FXML
    private void highlightBookingPath() {
        if (bookingMapRenderer == null || bookingPathService == null) return;

        Stop source = combo_stop_1 == null ? null : combo_stop_1.getSelectionModel().getSelectedItem();
        Stop dest   = combo_stop_2 == null ? null : combo_stop_2.getSelectionModel().getSelectedItem();

        if (source == null || dest == null || source.getStopID() == dest.getStopID()) {
            bookingMapRenderer.clearSelection();
            redrawBookingMap();
            return;
        }

        // BFS path â€” works for non-adjacent stops across any number of hops
        Path path = bookingPathService.buildPath(source.getStopID(), dest.getStopID());
        if (path == null || path.getStops().isEmpty()) {
            bookingMapRenderer.clearSelection();
        } else {
            bookingMapRenderer.setSelectedPath(path, source, dest);
        }
        redrawBookingMap();
    }

    @FXML
    private void handleBookingAction() {
        if (lbl_booking_status != null) lbl_booking_status.setText("");
        highlightBookingPath();
        User currentUser = BOGOApplication.getInstance().getCurrentUser();
        if (!(currentUser instanceof Passenger passenger)) {
            if (lbl_booking_status != null) lbl_booking_status.setText("Please log in as a passenger.");
            return;
        }
        Stop start = combo_stop_1 == null ? null : combo_stop_1.getValue();
        Stop end   = combo_stop_2 == null ? null : combo_stop_2.getValue();
        if (start == null || end == null) {
            if (lbl_booking_status != null) lbl_booking_status.setText("Select a pickup and destination stop.");
            return;
        }
        if (start.getStopID() == end.getStopID()) {
            if (lbl_booking_status != null) lbl_booking_status.setText("Pickup and destination cannot be the same.");
            return;
        }
        // BFS finds path through any number of hops â€” stops do NOT need to be adjacent
        Path path = bookingPathService != null
                ? bookingPathService.buildPath(start.getStopID(), end.getStopID()) : null;
        if (path == null || path.getStops().isEmpty()) {
            if (lbl_booking_status != null) lbl_booking_status.setText("No route found between those stops.");
            return;
        }
        String pathDesc = buildPathDescription(path);

        // Save booking with path description
        org.BOGO.service.BookingService bookingService = new org.BOGO.service.BookingService();
        org.BOGO.domain.booking.Booking booking = bookingService.createBooking(
                passenger.getUserID(), start.getStopID(), end.getStopID(), path);
        if (booking != null) {
            // Persist the path text so the cancel overlay can display it
            new org.BOGO.repository.BookingRepository()
                    .saveWithPath(passenger.getUserID(),
                            booking.getBusID(), booking.getCost(), pathDesc);
            // Note: createBooking already saved a basic record; update its PathDescription
            updateBookingPath(booking.getBookingID(), pathDesc);
            if (lbl_booking_status != null)
                lbl_booking_status.setText("âœ“ Booked! #BG-" + booking.getBookingID()
                        + "  Rs. " + String.format("%.0f", booking.getCost())
                        + "\nRoute: " + pathDesc);
        } else {
            if (lbl_booking_status != null) lbl_booking_status.setText("Booking failed â€” try again.");
        }
    }

    /** Formats a Path as "Stop A â†’ Stop B â†’ Stop C". */
    private String buildPathDescription(Path path) {
        if (path == null || path.getStops().isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.getStops().size(); i++) {
            if (i > 0) sb.append(" â†’ ");
            sb.append(path.getStops().get(i).getStopName());
        }
        return sb.toString();
    }

    /** Updates the PathDescription of an already-inserted booking row. */
    private void updateBookingPath(int bookingId, String pathDesc) {
        String sql = "UPDATE BOOKING SET PathDescription = ? WHERE BookingID = ?";
        try (java.sql.Connection conn = org.BOGO.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pathDesc);
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            System.err.println("[BOGOUIController] updateBookingPath failed: " + e.getMessage());
        }
    }

    // â”€â”€ OLD Pane-based rendering removed â€” replaced by BookingMapRenderer (Canvas) â”€â”€

    // â”€â”€ Canvas-based rendering is fully handled by BookingMapRenderer+CoordinateNormalizer â”€â”€

    private double scale(double value, double min, double max, double targetMin, double targetMax) {
        if (Double.compare(min, max) == 0) {
            return (targetMin + targetMax) / 2.0;
        }
        return targetMin + ((value - min) / (max - min)) * (targetMax - targetMin);
    }

    private void configureStopCombo(ComboBox<Stop> comboBox) {
        comboBox.setButtonCell(new StopListCell());
        comboBox.setCellFactory(listView -> new StopListCell());
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Stop stop) {
                return stop == null ? "" : "Stop " + stop.getStopName();
            }

            @Override
            public Stop fromString(String text) {
                if (text == null || bookingTransportMap == null) return null;
                String normalized = text.trim().replaceFirst("(?i)^stop\\s+", "");
                return bookingTransportMap.getStops().stream()
                        .filter(s -> s.getStopName().equalsIgnoreCase(normalized))
                        .findFirst().orElse(null);
            }
        });
    }



    private org.BOGO.domain.transport.Map buildBookingDemoMap() {
        org.BOGO.domain.transport.Map map = new org.BOGO.domain.transport.Map();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int stopID = row * 10 + col + 1;
                ArrayList<Integer> connections = new ArrayList<>();
                ArrayList<Double> fares = new ArrayList<>();
                ArrayList<Integer> routeIds = new ArrayList<>();

                if (col > 0) {
                    connections.add(stopID - 1);
                    fares.add(1.0);
                    routeIds.add(row + 1);
                }
                if (row > 0) {
                    connections.add(stopID - 10);
                    fares.add(1.1);
                    routeIds.add(20 + col);
                }
                if (row > 0 && col > 0 && (row == col || row + col == 9 || col == 4)) {
                    connections.add(stopID - 11);
                    fares.add(1.6);
                    routeIds.add(50 + row);
                }

                map.addStop(
                        stopID,
                        String.valueOf(stopID),
                        new Location(gpsLatitude(row, col), gpsLongitude(row, col)),
                        connections,
                        fares,
                        routeIds
                );
            }
        }

        return map;
    }

    private double gpsLatitude(int row, int col) {
        double offset = ((row * 17 + col * 11) % 7 - 3) * 0.025;
        return col + 1 + offset;
    }

    private double gpsLongitude(int row, int col) {
        double offset = ((row * 13 + col * 19) % 7 - 3) * 0.025;
        return row + 1 + offset;
    }

    private static class StopListCell extends ListCell<Stop> {
        @Override
        protected void updateItem(Stop stop, boolean empty) {
            super.updateItem(stop, empty);
            setText(empty || stop == null ? null : "Stop " + stop.getStopName());
        }
    }

    private void setGaugeValue(ProgressIndicator gauge, Label percentLabel, double value) {
        gauge.setProgress(value);
        if (percentLabel != null) {
            percentLabel.setText(Math.round(value * 100) + "%");
        }
    }

    private void populateDynamicStops(int stopCount) {
        if (box_dynamic_stops == null) return;
        box_dynamic_stops.getChildren().clear();
        // Fetch real stops from DB
        java.util.List<Stop> dbStops = viewMapService.getStopsOnMap();
        javafx.util.StringConverter<Stop> sc = new javafx.util.StringConverter<>() {
            public String toString(Stop s)   { return s == null ? "" : s.getStopName(); }
            public Stop fromString(String t) {
                if (t == null) return null;
                return dbStops.stream()
                        .filter(s -> s.getStopName().equalsIgnoreCase(t.trim()))
                        .findFirst().orElse(null);
            }
        };
        for (int i = 1; i <= stopCount; i++) {
            ComboBox<Stop> stopInput = new ComboBox<>();
            stopInput.setEditable(true);
            stopInput.setPromptText("Stop " + i);
            stopInput.setMaxWidth(Double.MAX_VALUE);
            stopInput.setItems(FXCollections.observableArrayList(dbStops));
            stopInput.setConverter(sc);
            // Live path highlight as stops are selected (same as Book Ride page)
            stopInput.getSelectionModel().selectedItemProperty()
                    .addListener((obs, ov, nv) -> highlightMultiStopPath());
            box_dynamic_stops.getChildren().add(stopInput);
        }
    }

    /** Highlights the full BFS path through all selected multi-stop combos on the map. */
    @SuppressWarnings("unchecked")
    private void highlightMultiStopPath() {
        if (bookingMapRenderer == null || bookingPathService == null || box_dynamic_stops == null) return;

        java.util.List<Stop> selected = new java.util.ArrayList<>();
        for (javafx.scene.Node node : box_dynamic_stops.getChildren()) {
            if (node instanceof ComboBox<?> cb && cb.getValue() instanceof Stop s)
                selected.add(s);
        }

        if (selected.size() < 2) {
            bookingMapRenderer.clearSelection();
            redrawBookingMap();
            return;
        }

        // Stitch all segment paths into one combined path
        java.util.List<Stop> allStops = new java.util.ArrayList<>();
        for (int i = 0; i < selected.size() - 1; i++) {
            Stop from = selected.get(i);
            Stop to   = selected.get(i + 1);
            if (from.getStopID() == to.getStopID()) continue;
            Path seg = bookingPathService.buildPath(from.getStopID(), to.getStopID());
            if (seg == null || seg.getStops().isEmpty()) continue;
            java.util.List<Stop> segStops = seg.getStops();
            // Avoid duplicating the junction stop between segments
            if (!allStops.isEmpty()) segStops = segStops.subList(1, segStops.size());
            allStops.addAll(segStops);
        }

        if (allStops.isEmpty()) {
            bookingMapRenderer.clearSelection();
        } else {
            Path combined = new Path();
            combined.setStops(new java.util.ArrayList<>(allStops));
            bookingMapRenderer.setSelectedPath(combined, selected.get(0), selected.get(selected.size() - 1));
        }
        redrawBookingMap();
    }

    @FXML
    @SuppressWarnings("unchecked")
    private void handleMultiStopBooking() {
        if (lbl_multi_booking_status != null) lbl_multi_booking_status.setText("");
        User currentUser = BOGOApplication.getInstance().getCurrentUser();
        if (!(currentUser instanceof Passenger passenger)) {
            if (lbl_multi_booking_status != null)
                lbl_multi_booking_status.setText("Please log in as a passenger.");
            return;
        }
        if (box_dynamic_stops == null) return;

        java.util.List<Stop> selected = new java.util.ArrayList<>();
        for (javafx.scene.Node node : box_dynamic_stops.getChildren()) {
            if (node instanceof ComboBox<?> cb && cb.getValue() instanceof Stop s)
                selected.add(s);
        }
        if (selected.size() < 2) {
            if (lbl_multi_booking_status != null)
                lbl_multi_booking_status.setText("Select at least 2 stops.");
            return;
        }

        org.BOGO.service.BookingService svc = new org.BOGO.service.BookingService();
        org.BOGO.repository.BookingRepository repo = new org.BOGO.repository.BookingRepository();
        int booked = 0;
        double totalCost = 0;
        StringBuilder fullPath = new StringBuilder();
        for (int i = 0; i < selected.size() - 1; i++) {
            Stop from = selected.get(i);
            Stop to   = selected.get(i + 1);
            if (from.getStopID() == to.getStopID()) continue;
            // BFS handles non-adjacent stops automatically
            Path path = bookingPathService != null
                    ? bookingPathService.buildPath(from.getStopID(), to.getStopID()) : null;
            if (path == null || path.getStops().isEmpty()) continue;
            String segDesc = buildPathDescription(path);
            org.BOGO.domain.booking.Booking b =
                    svc.createBooking(passenger.getUserID(), from.getStopID(), to.getStopID(), path);
            if (b != null) {
                updateBookingPath(b.getBookingID(), segDesc);
                booked++;
                totalCost += b.getCost();
                if (fullPath.length() > 0) fullPath.append("  |  ");
                fullPath.append(segDesc);
            }
        }
        if (lbl_multi_booking_status != null) {
            if (booked > 0)
                lbl_multi_booking_status.setText("âœ“ " + booked + " segment(s) booked | Total Rs. "
                        + String.format("%.0f", totalCost) + "\n" + fullPath);
            else
                lbl_multi_booking_status.setText("Booking failed â€” check stop connectivity.");
        }
    }

    private void showDetail(String value) {
        if (value != null && pane_detail_overlay != null) {
            lbl_detail_name.setText(value.substring(value.indexOf(' ') + 1).trim());
            lbl_detail_id.setText(value.substring(0, value.indexOf(' ')).trim());
            pane_detail_overlay.setVisible(true);
        }
    }

    // =========================================================================
    // ADD STOPS / ROUTES handlers
    // =========================================================================

    @FXML
    private void handleAddStop() {
        if (txt_new_stop_name == null) return;
        String name   = txt_new_stop_name.getText().trim();
        String latText = txt_new_stop_lat != null ? txt_new_stop_lat.getText().trim() : "0";
        String lonText = txt_new_stop_lon != null ? txt_new_stop_lon.getText().trim() : "0";
        if (name.isBlank()) {
            if (lbl_stop_status != null) lbl_stop_status.setText("Stop name is required.");
            return;
        }
        try {
            double lat = latText.isBlank() ? 0 : Double.parseDouble(latText);
            double lon = lonText.isBlank() ? 0 : Double.parseDouble(lonText);
            Stop stop = new Stop();
            stop.setStopName(name);
            stop.setLocation(new Location(lat, lon));

            // Collect up to 4 adjacent stops â€” add as connections before saving
            // so Connections column is serialised correctly on first insert
            java.util.List<Stop> adjStops = new java.util.ArrayList<>();
            java.util.List<ComboBox<Stop>> adjCombos = new java.util.ArrayList<>();
            adjCombos.add(combo_adj_stop1);
            adjCombos.add(combo_adj_stop2);
            adjCombos.add(combo_adj_stop3);
            adjCombos.add(combo_adj_stop4);
            for (ComboBox<Stop> cb : adjCombos) {
                if (cb != null && cb.getValue() != null) adjStops.add(cb.getValue());
            }
            for (Stop adj : adjStops) stop.addConnection(adj, 1.0, 0);

            org.BOGO.repository.StopRepository repo = new org.BOGO.repository.StopRepository();
            int id = repo.save(stop, lat, lon);
            if (id > 0) {
                // Bidirectional: update every adjacent stop to include the new stop
                for (Stop adj : adjStops) repo.addConnection(adj.getStopID(), id);
                if (lbl_stop_status != null)
                    lbl_stop_status.setText("Stop added (ID=" + id + ") with " + adjStops.size() + " connection(s).");
                txt_new_stop_name.clear();
                if (txt_new_stop_lat != null) txt_new_stop_lat.clear();
                if (txt_new_stop_lon != null) txt_new_stop_lon.clear();
                for (ComboBox<Stop> cb : adjCombos) { if (cb != null) cb.getSelectionModel().clearSelection(); }
                populateStopCombos(); // refresh combos so new stop appears
            } else {
                if (lbl_stop_status != null) lbl_stop_status.setText("Failed to add stop.");
            }
        } catch (NumberFormatException e) {
            if (lbl_stop_status != null) lbl_stop_status.setText("Invalid coordinates â€” use decimal degrees.");
        }
    }

    @FXML
    private void handleAddRoute() {
        if (txt_new_route_name == null) return;
        String name = txt_new_route_name.getText().trim();
        if (name.isBlank()) {
            if (lbl_route_status != null) lbl_route_status.setText("Route name is required.");
            return;
        }
        // Use ArrayList to avoid NPE â€” List.of() rejects null elements
        java.util.List<ComboBox<Stop>> routeCombos = new java.util.ArrayList<>();
        routeCombos.add(combo_route_stop1);
        routeCombos.add(combo_route_stop2);
        routeCombos.add(combo_route_stop3);
        java.util.List<Integer> stopIds = new java.util.ArrayList<>();
        for (ComboBox<Stop> cb : routeCombos) {
            if (cb != null && cb.getValue() != null) stopIds.add(cb.getValue().getStopID());
        }
        if (stopIds.size() < 2) {
            if (lbl_route_status != null) lbl_route_status.setText("Select at least Origin + Stop 2.");
            return;
        }
        int timePerStop = 120;
        try {
            if (txt_route_time != null && !txt_route_time.getText().isBlank())
                timePerStop = Integer.parseInt(txt_route_time.getText().trim());
        } catch (NumberFormatException ignored) {}

        Route route = routeReviseService.addRoute(name, stopIds, timePerStop);
        if (route != null) {
            if (lbl_route_status != null) lbl_route_status.setText("Route created (ID=" + route.getRouteID() + ")");
            txt_new_route_name.clear();
            for (ComboBox<Stop> cb : routeCombos) { if (cb != null) cb.getSelectionModel().clearSelection(); }
        } else {
            if (lbl_route_status != null)
                lbl_route_status.setText("Failed â€” ensure each stop is directly connected to the previous one.");
        }
    }

    // =========================================================================
    // ASSIGN ROUTE handlers
    // =========================================================================

    @FXML
    private void handleAssignRoute() {
        if (combo_assign_driver == null || combo_assign_route == null) return;
        Driver driver = combo_assign_driver.getValue();
        Route route = combo_assign_route.getValue();
        org.BOGO.domain.transport.Bus bus = combo_assign_bus != null ? combo_assign_bus.getValue() : null;
        if (driver == null || route == null) {
            if (lbl_assign_status != null) lbl_assign_status.setText("Select driver and route.");
            return;
        }
        handleDriverService.assignDriverToRoute(driver, route);
        if (bus != null) handleDriverService.assignDriverToBus(driver, bus);
        // Start a new trip
        int busId = bus != null ? bus.getBusID() : 1;
        new org.BOGO.service.TripService().createTrip(route.getRouteID(), busId, driver.getUserID());
        if (lbl_assign_status != null)
            lbl_assign_status.setText("Assigned: " + driver.getName() + " â†’ Route " + route.getRouteID());
        refreshAssignments();
    }

    @FXML
    private void refreshAssignments() {
        if (list_current_assignments == null) return;
        java.util.List<Driver> drivers = handleDriverService.getAllDrivers();
        javafx.collections.ObservableList<String> items = FXCollections.observableArrayList();
        // Fetch route names for display
        org.BOGO.repository.RouteRepository routeRepo = new org.BOGO.repository.RouteRepository();
        org.BOGO.repository.BusRepository busRepo = new org.BOGO.repository.BusRepository();
        String sql = "SELECT d.UserId, d.AssignedRouteId, d.AssignedBusId, pd.Name, d.DriverID " +
                     "FROM DRIVER d JOIN USERS u ON u.UserId = d.UserId " +
                     "JOIN PERSONAL_DETAILS pd ON pd.PdId = u.PdId ORDER BY pd.Name";
        try (java.sql.Connection conn = org.BOGO.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name   = rs.getString("Name");
                String lic    = rs.getString("DriverID");
                int routeId   = rs.getInt("AssignedRouteId");
                int busId     = rs.getInt("AssignedBusId");
                String routeLabel = (routeId > 0) ? "Route " + routeId : "No route";
                String busLabel   = (busId > 0)   ? "Bus " + busId     : "No bus";
                items.add(name + " (" + lic + ") â†’ " + routeLabel + " | " + busLabel);
            }
        } catch (Exception e) {
            System.err.println("[refreshAssignments] " + e.getMessage());
        }
        list_current_assignments.setItems(items);
    }

    // =========================================================================
    // DB wiring: Analytics page
    // =========================================================================

    /** Loads passengers into list_employees + updates lbl_total_passengers. */
    private void loadPassengersFromDB() {
        int count = 0;
        String sql = "SELECT u.UserId, pd.Name, pd.Email FROM PASSENGER p " +
                     "JOIN USERS u ON u.UserId = p.UserId " +
                     "JOIN PERSONAL_DETAILS pd ON pd.PdId = u.PdId " +
                     "ORDER BY pd.Name";
        javafx.collections.ObservableList<String> items = FXCollections.observableArrayList();
        try (java.sql.Connection conn = org.BOGO.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                count++;
                items.add("PASS-" + rs.getInt("UserId") + "  " + rs.getString("Name") +
                           " <" + rs.getString("Email") + ">");
            }
        } catch (Exception ignored) {}
        if (list_employees != null) list_employees.setItems(items);
        if (lbl_total_passengers != null) lbl_total_passengers.setText(String.valueOf(count));
    }

    /** Loads drivers into list_drivers + updates lbl_total_Drivers. */
    private void loadDriversFromDB() {
        java.util.List<Driver> drivers = handleDriverService.getAllDrivers();
        javafx.collections.ObservableList<String> items = FXCollections.observableArrayList();
        for (Driver d : drivers)
            items.add(d.getLicenseNumber() + "  " + d.getName());
        if (list_drivers != null) list_drivers.setItems(items);
        if (lbl_total_Drivers != null) lbl_total_Drivers.setText(String.valueOf(drivers.size()));
    }

    /** Legacy entry-point kept for compatibility. */
    private void loadEmployeesFromDB() {
        loadPassengersFromDB();
        loadDriversFromDB();
    }

    // =========================================================================
    // DB wiring: Driver Bus Metrics page
    // =========================================================================

    private void loadDriverHealthMetrics() {
        if (gauge_tyres == null) return;
        User currentUser = BOGOApplication.getInstance().getCurrentUser();
        if (currentUser == null) return;
        int driverId = currentUser.getUserID();

        // Primary: find bus via active TRIP
        org.BOGO.service.TripService tripService = new org.BOGO.service.TripService();
        org.BOGO.domain.transport.Trip trip = tripService.getTripForDriver(driverId);
        org.BOGO.domain.transport.Bus bus = null;
        if (trip != null) {
            bus = new org.BOGO.repository.BusRepository().findById(trip.getBusID());
        } else {
            // Fallback: check DRIVER.AssignedBusId (set by BusAllocationService)
            bus = findAssignedBusForDriver(driverId);
        }
        if (bus == null) return; // No bus — gauges stay at 0 ("not assigned" state)
        setGaugeValue(gauge_tyres,   lbl_tyres_percent,   bus.getTyreHealth()    / 100.0);
        setGaugeValue(gauge_engine,  lbl_engine_percent,  bus.getEngineHealth()  / 100.0);
        setGaugeValue(gauge_chassis, lbl_chassis_percent, bus.getChassisHealth() / 100.0);
    }

    /** Fetches the bus linked directly via DRIVER.AssignedBusId (auto-assignment fallback). */
    private org.BOGO.domain.transport.Bus findAssignedBusForDriver(int driverId) {
        String sql = "SELECT d.AssignedBusId FROM DRIVER d WHERE d.UserId = ? AND d.AssignedBusId IS NOT NULL";
        try (java.sql.Connection conn = org.BOGO.config.DatabaseConfig.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new org.BOGO.repository.BusRepository().findById(rs.getInt("AssignedBusId"));
                }
            }
        } catch (java.sql.SQLException e) {
            // Column may not exist in older schemas — fail silently
        }
        return null;
    }

    /** Sends a driver alert to the admin queue via AlertRepository. */
    @FXML
    private void handleDriverSendAlert() {
        if (lbl_alert_status != null) lbl_alert_status.setText("");
        if (txt_alert_message == null) return;

        String message = txt_alert_message.getText().trim();
        if (message.isEmpty()) {
            if (lbl_alert_status != null) lbl_alert_status.setText("⚠ Please describe the issue.");
            return;
        }

        // Accept any authenticated user in the Driver shell — DriverShell is driver-only
        User currentUser = BOGOApplication.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (lbl_alert_status != null) lbl_alert_status.setText("⚠ Session expired. Please log in again.");
            return;
        }

        // Read priority toggle (default HIGH)
        String priority = "HIGH";
        if (priority_group != null && priority_group.getSelectedToggle() instanceof ToggleButton tb) {
            priority = tb.getText().toLowerCase().contains("low") ? "LOW" : "HIGH";
        }
        // Read type toggle (default BUS)
        String alertType = "BUS";
        if (type_group != null && type_group.getSelectedToggle() instanceof ToggleButton tb) {
            alertType = tb.getText().equalsIgnoreCase("Driver") ? "DRIVER" : "BUS";
        }

        // Save alert directly — no Driver cast needed, just use the user ID
        org.BOGO.domain.communication.Alert alert = new org.BOGO.domain.communication.Alert();
        alert.setSenderDriverId(currentUser.getUserID());
        alert.setAlertType(alertType);
        alert.setPriority(priority);
        alert.setMessage(message);
        alert.setStatus("OPEN");
        int alertId = new org.BOGO.repository.AlertRepository().save(alert);

        if (alertId > 0) {
            if (lbl_alert_status != null)
                lbl_alert_status.setText("✔ Alert sent! Ref: #AL-" + alertId);
            txt_alert_message.clear();
        } else {
            if (lbl_alert_status != null)
                lbl_alert_status.setText("❌ Failed to send — check DB connection.");
        }
    }

    // =========================================================================
    // Populate combos for Add Stops/Routes and Assign Route pages
    // =========================================================================

    private void populateStopCombos() {
        java.util.List<Stop> allStops = viewMapService.getStopsOnMap();
        javafx.collections.ObservableList<Stop> stopList = FXCollections.observableArrayList(allStops);
        javafx.util.StringConverter<Stop> sc = new javafx.util.StringConverter<>() {
            public String toString(Stop s)   { return s == null ? "" : s.getStopName() + " [ID:" + s.getStopID() + "]"; }
            public Stop fromString(String s) { return null; }
        };
        // Populate all 4 adjacent-stop combos (Add Stop panel) with every stop
        java.util.List<ComboBox<Stop>> adjCombos = new java.util.ArrayList<>();
        adjCombos.add(combo_adj_stop1);
        adjCombos.add(combo_adj_stop2);
        adjCombos.add(combo_adj_stop3);
        adjCombos.add(combo_adj_stop4);
        for (ComboBox<Stop> cb : adjCombos) {
            if (cb != null) { cb.setItems(stopList); cb.setConverter(sc); }
        }
        // Route origin (Stop 1) always shows all stops
        if (combo_route_stop1 != null) {
            combo_route_stop1.setItems(stopList);
            combo_route_stop1.setConverter(sc);
        }
        // Stop 2 and 3 start empty â€” they are filled by setupRouteCascade() listeners
        if (combo_route_stop2 != null) { combo_route_stop2.setConverter(sc); }
        if (combo_route_stop3 != null) { combo_route_stop3.setConverter(sc); }
    }

    /**
     * Wires cascading selection for the Add Route combos:
     *  - Selecting Stop 1 populates Stop 2 with its adjacent stops.
     *  - Selecting Stop 2 populates Stop 3 with its adjacent stops.
     * Falls back to all stops if the selected stop has no connections yet.
     */
    private void setupRouteCascade() {
        javafx.util.StringConverter<Stop> sc = new javafx.util.StringConverter<>() {
            public String toString(Stop s)   { return s == null ? "" : s.getStopName() + " [ID:" + s.getStopID() + "]"; }
            public Stop fromString(String s) { return null; }
        };

        if (combo_route_stop1 != null && combo_route_stop2 != null) {
            combo_route_stop1.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, selected) -> {
                        if (selected == null) return;
                        // Populate Stop 2 with adjacent stops; fall back to all stops if none
                        java.util.List<Stop> adj = new java.util.ArrayList<>(selected.getConnections());
                        if (adj.isEmpty()) adj = viewMapService.getStopsOnMap().stream()
                                .filter(s -> s.getStopID() != selected.getStopID())
                                .collect(java.util.stream.Collectors.toList());
                        combo_route_stop2.setItems(FXCollections.observableArrayList(adj));
                        combo_route_stop2.setConverter(sc);
                        combo_route_stop2.getSelectionModel().clearSelection();
                        // Clear Stop 3 whenever Stop 1 changes
                        if (combo_route_stop3 != null) {
                            combo_route_stop3.setItems(FXCollections.observableArrayList());
                            combo_route_stop3.getSelectionModel().clearSelection();
                        }
                    });
        }

        if (combo_route_stop2 != null && combo_route_stop3 != null) {
            combo_route_stop2.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, selected) -> {
                        if (selected == null) return;
                        // Populate Stop 3 with adjacent stops; exclude Stop 1 to avoid loops
                        Stop origin = combo_route_stop1 != null ? combo_route_stop1.getValue() : null;
                        java.util.List<Stop> adj = new java.util.ArrayList<>(selected.getConnections());
                        if (adj.isEmpty()) {
                            adj = viewMapService.getStopsOnMap().stream()
                                    .filter(s -> s.getStopID() != selected.getStopID()
                                                 && (origin == null || s.getStopID() != origin.getStopID()))
                                    .collect(java.util.stream.Collectors.toList());
                        } else {
                            // Remove origin from adjacent list to avoid circular route
                            if (origin != null) {
                                int originId = origin.getStopID();
                                adj.removeIf(s -> s.getStopID() == originId);
                            }
                        }
                        combo_route_stop3.setItems(FXCollections.observableArrayList(adj));
                        combo_route_stop3.setConverter(sc);
                        combo_route_stop3.getSelectionModel().clearSelection();
                    });
        }
    }

    private void populateAssignmentCombos() {
        if (combo_assign_driver != null) {
            // Show ALL drivers so admin can reassign anyone, not just unassigned ones
            java.util.List<Driver> drivers = handleDriverService.getAllDrivers();
            combo_assign_driver.setItems(FXCollections.observableArrayList(drivers));
            combo_assign_driver.setConverter(new StringConverter<>() {
                public String toString(Driver d) { return d == null ? "" : d.getName() + " (" + d.getLicenseNumber() + ")"; }
                public Driver fromString(String s) { return null; }
            });
        }
        if (combo_assign_route != null) {
            java.util.List<Route> routes = viewMapService.getActiveRoutes();
            combo_assign_route.setItems(FXCollections.observableArrayList(routes));
            combo_assign_route.setConverter(new StringConverter<>() {
                public String toString(Route r) { return r == null ? "" : "Route " + r.getRouteID() + (r.getRouteName() != null ? " - " + r.getRouteName() : ""); }
                public Route fromString(String s) { return null; }
            });
        }
        if (combo_assign_bus != null) {
            java.util.List<org.BOGO.domain.transport.Bus> buses = busServices.getAllBuses();
            combo_assign_bus.setItems(FXCollections.observableArrayList(buses));
            combo_assign_bus.setConverter(new StringConverter<>() {
                public String toString(org.BOGO.domain.transport.Bus b) { return b == null ? "" : b.getRegistration() + " (" + b.getBusCompany() + ")"; }
                public org.BOGO.domain.transport.Bus fromString(String s) { return null; }
            });
        }
        refreshAssignments();
    }

    // =========================================================================
    // Live map rendering on map_canvas_pane
    // =========================================================================

    private void renderLiveMap(java.util.List<Stop> stops,
                               java.util.List<org.BOGO.domain.transport.Trip> activeTrips) {
        if (map_canvas_pane == null || stops == null || stops.isEmpty()) return;
        map_canvas_pane.getChildren().clear();
        liveStopNodes.clear();
        liveBusMarkers.clear();

        double w = map_canvas_pane.getWidth()  > 10 ? map_canvas_pane.getWidth()  : 800;
        double h = map_canvas_pane.getHeight() > 10 ? map_canvas_pane.getHeight() : 500;

        // Coordinate bounds
        double minLat = stops.stream().filter(s -> s.getLocation() != null)
                .mapToDouble(s -> s.getLocation().getLatitude()).min().orElse(0);
        double maxLat = stops.stream().filter(s -> s.getLocation() != null)
                .mapToDouble(s -> s.getLocation().getLatitude()).max().orElse(1);
        double minLon = stops.stream().filter(s -> s.getLocation() != null)
                .mapToDouble(s -> s.getLocation().getLongitude()).min().orElse(0);
        double maxLon = stops.stream().filter(s -> s.getLocation() != null)
                .mapToDouble(s -> s.getLocation().getLongitude()).max().orElse(1);
        // Avoid division-by-zero when all stops share the same coordinate
        if (maxLat == minLat) { maxLat = minLat + 0.01; }
        if (maxLon == minLon) { maxLon = minLon + 0.01; }

        // Pre-compute pixel position for every stop (used by all 4 layers)
        java.util.Map<Integer, double[]> pos = new java.util.HashMap<>();
        for (Stop s : stops) {
            if (s.getLocation() == null) continue;
            double sx = scale(s.getLocation().getLatitude(),  minLat, maxLat, 60, w - 60);
            double sy = scale(s.getLocation().getLongitude(), minLon, maxLon, h - 60, 60);
            pos.put(s.getStopID(), new double[]{sx, sy});
        }

        // â”€â”€ Layer 1: faint graph connection lines (entire network) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Set<String> drawnEdges = new HashSet<>();
        for (Stop from : stops) {
            double[] fp = pos.get(from.getStopID());
            if (fp == null) continue;
            for (Stop to : from.getConnections()) {
                double[] tp = pos.get(to.getStopID());
                if (tp == null) continue;
                String key = Math.min(from.getStopID(), to.getStopID())
                           + "-" + Math.max(from.getStopID(), to.getStopID());
                if (drawnEdges.add(key)) {
                    Line line = new Line(fp[0], fp[1], tp[0], tp[1]);
                    line.setStroke(javafx.scene.paint.Color.web("#334155"));
                    line.setStrokeWidth(1.5);
                    line.setOpacity(0.6);
                    map_canvas_pane.getChildren().add(line);
                }
            }
        }

        // â”€â”€ Layer 2: highlighted active-route paths (one colour per route) â”€â”€â”€â”€â”€
        String[] ROUTE_COLORS = {"#f59e0b", "#10b981", "#8b5cf6", "#ef4444", "#3b82f6",
                                  "#ec4899", "#14b8a6", "#f97316"};
        Set<Integer> drawnRoutes = new HashSet<>();
        int colorIdx = 0;
        if (activeTrips != null) {
            org.BOGO.repository.RouteRepository rRepo = new org.BOGO.repository.RouteRepository();
            for (org.BOGO.domain.transport.Trip trip : activeTrips) {
                int routeId = trip.getRouteID();
                if (!drawnRoutes.add(routeId)) continue;          // already drawn
                org.BOGO.domain.transport.Route route = rRepo.findById(routeId);
                if (route == null || route.getStopIDs().size() < 2) continue;
                String color = ROUTE_COLORS[colorIdx % ROUTE_COLORS.length];
                colorIdx++;
                java.util.List<Integer> ids = route.getStopIDs();
                for (int i = 0; i < ids.size() - 1; i++) {
                    double[] fp = pos.get(ids.get(i));
                    double[] tp = pos.get(ids.get(i + 1));
                    if (fp == null || tp == null) continue;
                    Line rl = new Line(fp[0], fp[1], tp[0], tp[1]);
                    rl.setStroke(javafx.scene.paint.Color.web(color));
                    rl.setStrokeWidth(4.5);
                    rl.setOpacity(0.88);
                    rl.getStrokeDashArray().addAll(14.0, 6.0);
                    map_canvas_pane.getChildren().add(rl);
                }
            }
        }

        // â”€â”€ Layer 3: stop circles with tooltips â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        for (Stop s : stops) {
            double[] sp = pos.get(s.getStopID());
            if (sp == null) continue;
            Circle c = new Circle(7);
            c.setFill(javafx.scene.paint.Color.web("#38bdf8"));
            c.setStroke(javafx.scene.paint.Color.web("#0ea5e9"));
            c.setStrokeWidth(2);
            StackPane node = new StackPane(c);
            node.setLayoutX(sp[0] - 7);
            node.setLayoutY(sp[1] - 7);
            Tooltip.install(node, new Tooltip(s.getStopName() + " [ID:" + s.getStopID() + "]"));
            map_canvas_pane.getChildren().add(node);
            liveStopNodes.put(s.getStopID(), node);
        }

        // â”€â”€ Layer 4: bus markers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        if (activeTrips != null) {
            org.BOGO.repository.RouteRepository rRepo2 = new org.BOGO.repository.RouteRepository();
            for (org.BOGO.domain.transport.Trip trip : activeTrips) {
                double bx, by;
                Location loc = (trip.getBus() != null) ? trip.getBus().getLocation() : null;

                if (loc != null
                        && (Math.abs(loc.getLatitude()) > 0.0001 || Math.abs(loc.getLongitude()) > 0.0001)) {
                    // Use GPS from simulation
                    bx = scale(loc.getLatitude(),  minLat, maxLat, 60, w - 60);
                    by = scale(loc.getLongitude(), minLon, maxLon, h - 60, 60);
                } else {
                    // GPS not set yet â€” place bus at its current stop index
                    org.BOGO.domain.transport.Route route = rRepo2.findById(trip.getRouteID());
                    if (route == null || route.getStopIDs().isEmpty()) continue;
                    int idx = Math.min(trip.getCurrentStopIndex(), route.getStopIDs().size() - 1);
                    double[] sp = pos.get(route.getStopIDs().get(idx));
                    if (sp == null) continue;
                    bx = sp[0];
                    by = sp[1];
                }

                Circle busCircle = new Circle(12);
                busCircle.setFill(javafx.scene.paint.Color.web("#f59e0b"));
                busCircle.setStroke(javafx.scene.paint.Color.web("#d97706"));
                busCircle.setStrokeWidth(2.5);
                busCircle.setEffect(new Glow(0.8));
                Text busLabel = new Text("ðŸšŒ");
                busLabel.setStyle("-fx-font-size: 15px;");
                StackPane busNode = new StackPane(busCircle, busLabel);
                busNode.setLayoutX(bx - 12);
                busNode.setLayoutY(by - 12);
                Tooltip.install(busNode, new Tooltip(
                        "Bus " + trip.getBusID() + " | Route " + trip.getRouteID()
                        + " | Stop " + trip.getCurrentStopIndex()));
                map_canvas_pane.getChildren().add(busNode);
                liveBusMarkers.put(trip.getBusID(), busNode);
            }
        }
    }


    // =========================================================================
    // Add Resource â€” Bus and Driver handlers (write to DB)
    // =========================================================================

    @FXML
    private void handleAddBus() {
        if (txt_bus_number == null) return;
        String reg     = txt_bus_number.getText().trim();
        String company = txt_bus_company  != null ? txt_bus_company.getText().trim()  : "BOGO Transit";
        String yearStr = txt_bus_year     != null ? txt_bus_year.getText().trim()     : "";
        String capStr  = choice_max_capacity != null ? choice_max_capacity.getValue() : "50";
        if (reg.isBlank()) {
            if (lbl_bus_status != null) lbl_bus_status.setText("Bus registration is required.");
            return;
        }
        try {
            int year = yearStr.isBlank() ? java.time.Year.now().getValue() : Integer.parseInt(yearStr);
            int cap  = (capStr == null || capStr.isBlank()) ? 50 : Integer.parseInt(capStr);
            int id   = busServices.addBus(company.isBlank() ? "BOGO Transit" : company, reg, year, cap);
            if (id > 0) {
                if (lbl_bus_status  != null) lbl_bus_status.setText("Bus added (ID=" + id + ").");
                if (txt_bus_number  != null) txt_bus_number.clear();
                if (txt_bus_company != null) txt_bus_company.clear();
                if (txt_bus_year    != null) txt_bus_year.clear();
            } else {
                if (lbl_bus_status != null) lbl_bus_status.setText("Failed â€” check registration is unique.");
            }
        } catch (NumberFormatException e) {
            if (lbl_bus_status != null) lbl_bus_status.setText("Year must be a 4-digit number.");
        }
    }

    @FXML
    private void handleAddDriver() {
        if (txt_driver_fname == null) return;
        String fname = txt_driver_fname.getText().trim();
        String lname = txt_driver_lname != null ? txt_driver_lname.getText().trim() : "";
        String cnic  = txt_driver_cnic  != null ? txt_driver_cnic.getText().trim()  : "";
        String email = txt_driver_email != null ? txt_driver_email.getText().trim() : "";
        String name  = (fname + " " + lname).trim();
        if (name.isBlank() || email.isBlank() || cnic.isBlank()) {
            if (lbl_driver_status != null) lbl_driver_status.setText("Name, CNIC and Email are required.");
            return;
        }
        // Temp password: must satisfy DB CHECK constraint (upper + lower + digit + symbol)
        String tempPassword = "Bogo@" + cnic;
        Driver d = handleDriverService.addDriver(name, email, cnic, tempPassword, cnic, 0, 0);
        if (d != null) {
            if (lbl_driver_status != null)
                lbl_driver_status.setText("Driver added (ID=" + d.getUserID() + "). Temp password = CNIC.");
            if (txt_driver_fname != null) txt_driver_fname.clear();
            if (txt_driver_lname != null) txt_driver_lname.clear();
            if (txt_driver_cnic  != null) txt_driver_cnic.clear();
            if (txt_driver_email != null) txt_driver_email.clear();
        } else {
            if (lbl_driver_status != null) lbl_driver_status.setText("Failed â€” email may already be in use.");
        }
    }

    // =========================================================================
    // Alerts â€” load from DB and resolve
    // =========================================================================

    /** Dashboard list_system_alerts: all recent alerts (summary). */
    private void loadDashboardAlertsFromDB() {
        org.BOGO.repository.AlertRepository repo = new org.BOGO.repository.AlertRepository();
        java.util.List<org.BOGO.domain.communication.Alert> alerts = repo.findPending();
        javafx.collections.ObservableList<String> items = FXCollections.observableArrayList();
        for (org.BOGO.domain.communication.Alert a : alerts) {
            items.add(a.getPriority() + " | " + a.getAlertType() + " | " + a.getStatus()
                      + (a.getMessage() != null ? " â€” " + a.getMessage() : ""));
        }
        if (items.isEmpty()) items.add("No alerts on record.");
        if (list_system_alerts != null) list_system_alerts.setItems(items);
    }

    /** Resolve Alerts page list_active_alerts: pending/open alerts with selection tracking. */
    private void loadAlertsFromDB() {
        org.BOGO.repository.AlertRepository repo = new org.BOGO.repository.AlertRepository();
        java.util.List<org.BOGO.domain.communication.Alert> pending = repo.findPending();
        final java.util.List<org.BOGO.domain.communication.Alert> alertList =
                new java.util.ArrayList<>(pending);
        javafx.collections.ObservableList<String> items = FXCollections.observableArrayList();
        for (org.BOGO.domain.communication.Alert a : alertList) {
            items.add("[#" + a.getAlertId() + "] " + a.getPriority()
                      + " | " + a.getAlertType() + " â€” " + a.getMessage());
        }
        if (items.isEmpty()) items.add("No pending alerts.");
        if (list_active_alerts != null) {
            list_active_alerts.setItems(items);
            list_active_alerts.getSelectionModel().selectedIndexProperty()
                    .addListener((obs, o, idx) -> {
                        int i = idx.intValue();
                        selectedAlert = (i >= 0 && i < alertList.size()) ? alertList.get(i) : null;
                    });
        }
    }

    @FXML
    private void handleResolveAlert() {
        if (selectedAlert == null) {
            if (pane_resolution_dialog != null) pane_resolution_dialog.setVisible(false);
            return;
        }
        org.BOGO.repository.AlertRepository repo = new org.BOGO.repository.AlertRepository();
        boolean ok = repo.markResolved(selectedAlert.getAlertId());
        if (ok) {
            selectedAlert = null;
            loadAlertsFromDB(); // refresh list
        }
        if (pane_resolution_dialog != null) pane_resolution_dialog.setVisible(false);
    }

    // =========================================================================
    // Live map: renders stops + active buses, hooks simulation engine ticks
    // =========================================================================

    private void renderAdminLiveMap() {
        // Replaced by GeneralMapController — kept as hook for onShow() calls
        if (generalMapController != null) generalMapController.onShow();
    }

    // =========================================================================
    // PASSENGER â€” load bookings from DB into dashboard cards
    // =========================================================================

    private void loadPassengerBookingsFromDB() {
        if (box_active_bookings == null) return;
        User currentUser = BOGOApplication.getInstance().getCurrentUser();
        if (!(currentUser instanceof Passenger passenger)) {
            Label msg = new Label("Log in as a passenger to see bookings.");
            msg.getStyleClass().add("muted-text");
            box_active_bookings.getChildren().setAll(msg);
            return;
        }

        new Thread(() -> {
            org.BOGO.repository.BookingRepository repo = new org.BOGO.repository.BookingRepository();
            java.util.List<org.BOGO.domain.booking.Booking> list =
                    repo.findByPassengerId(passenger.getUserID());
            java.util.HashMap<Integer, String> paths =
                    repo.findPathDescriptionsByPassengerId(passenger.getUserID());

            javafx.application.Platform.runLater(() -> {
                box_active_bookings.getChildren().clear();

                if (list.isEmpty()) {
                    Label empty = new Label("No bookings found.");
                    empty.getStyleClass().add("muted-text");
                    box_active_bookings.getChildren().add(empty);
                    return;
                }

                for (org.BOGO.domain.booking.Booking b : list) {
                    javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(6);
                    card.getStyleClass().add("neon-card");
                    card.setPadding(new javafx.geometry.Insets(14));

                    final int bookingId = b.getBookingID();
                    final String pathDesc = paths.getOrDefault(bookingId, "");
                    card.setOnMouseClicked(e -> {
                        pendingCancelBookingId = bookingId;
                        pendingCancelPath = pathDesc;
                        showCancelRidePane();
                    });

                    Label title = new Label("Booking  #BG-" + bookingId);
                    title.getStyleClass().add("subheader-text");

                    // Fix: show CANCELLED (not COMPLETED) when active=false
                    String statusStr = b.isActive() ? "ACTIVE" : "CANCELLED";
                    Label cost = new Label("Rs. " + String.format("%.0f", b.getCost())
                                         + "  |  " + statusStr);
                    cost.getStyleClass().add("body-text");

                    String dateStr = b.getCreatedAt() != null
                            ? b.getCreatedAt().toLocalDate().toString() : "N/A";
                    Label date = new Label(dateStr);
                    date.getStyleClass().add("muted-text");

                    // Show route if available
                    javafx.scene.layout.VBox inner = new javafx.scene.layout.VBox(4,
                            title, cost, date);
                    if (!pathDesc.isBlank()) {
                        Label route = new Label(pathDesc);
                        route.setStyle("-fx-text-fill:#00f2ff; -fx-font-size:11px;");
                        route.setWrapText(true);
                        inner.getChildren().add(route);
                    }
                    card.getChildren().add(inner);
                    box_active_bookings.getChildren().add(card);
                }
            });
        }, "BookingLoader").start();
    }

    /**
     * Driver Dashboard — populates list_boarding_passengers with the names of
     * passengers who have ACTIVE bookings on the driver's current bus.
     *
     * Bus lookup order:
     *   1. DRIVER.AssignedBusId  (set by auto-assign or admin)
     *   2. Active TRIP.BusId     (fallback if AssignedBusId is null)
     */
    private void loadBoardingPassengersFromDB() {
        if (list_boarding_passengers == null) return;
        User currentUser = BOGOApplication.getInstance().getCurrentUser();
        if (currentUser == null) {
            list_boarding_passengers.setItems(
                    FXCollections.observableArrayList("Not logged in."));
            return;
        }
        int driverId = currentUser.getUserID();

        new Thread(() -> {
            javafx.collections.ObservableList<String> items =
                    FXCollections.observableArrayList();
            try (java.sql.Connection conn =
                         org.BOGO.config.DatabaseConfig.getConnection()) {

                // Step 1: find the bus this driver is operating
                int busId = 0;

                // Primary: DRIVER.AssignedBusId
                String busSql =
                    "SELECT AssignedBusId FROM DRIVER WHERE UserId = ? AND AssignedBusId IS NOT NULL";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(busSql)) {
                    ps.setInt(1, driverId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) busId = rs.getInt("AssignedBusId");
                    }
                }

                // Fallback: active TRIP
                if (busId == 0) {
                    String tripSql =
                        "SELECT TOP 1 BusId FROM TRIP " +
                        "WHERE DriverId = ? AND TripStatus = 'IN_PROGRESS'";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(tripSql)) {
                        ps.setInt(1, driverId);
                        try (java.sql.ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) busId = rs.getInt("BusId");
                        }
                    }
                }

                if (busId == 0) {
                    items.add("No bus assigned.");
                } else {
                    // Step 2: fetch passengers with active bookings on this bus
                    String sql =
                        "SELECT pd.Name, b.BookingID " +
                        "FROM BOOKING b " +
                        "JOIN USERS u              ON u.UserId = b.PassengerID " +
                        "JOIN PERSONAL_DETAILS pd  ON pd.PdId  = u.PdId " +
                        "WHERE b.BusID = ? AND b.Active = 1 " +
                        "ORDER BY pd.Name";
                    try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, busId);
                        try (java.sql.ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                items.add(rs.getString("Name")
                                          + "  [#" + rs.getInt("BookingID") + "]");
                            }
                        }
                    }
                    if (items.isEmpty()) items.add("No passengers boarded yet.");
                }
            } catch (java.sql.SQLException e) {
                System.err.println("[BOGOUIController] loadBoardingPassengers failed: "
                                   + e.getMessage());
                items.add("Error loading passengers.");
            }

            final javafx.collections.ObservableList<String> finalItems = items;
            Platform.runLater(() -> list_boarding_passengers.setItems(finalItems));
        }, "BoardingPassengersLoader").start();
    }

    // =========================================================================
    // PASSENGER â€” build booking-map graph from real DB stops
    // =========================================================================

    private org.BOGO.domain.transport.Map buildBookingMapFromDB() {
        java.util.List<Stop> dbStops = viewMapService.getStopsOnMap();
        if (dbStops == null || dbStops.isEmpty()) {
            return buildBookingDemoMap(); // graceful fallback
        }

        org.BOGO.domain.transport.Map map = new org.BOGO.domain.transport.Map();

        // â”€â”€ Pass 1: Add every stop with EMPTY connections â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // Map.addStop() looks up each connection ID in the already-added stops
        // list; if a neighbour hasn't been added yet it returns index -1 and
        // crashes.  By passing empty lists here every stop is safely registered.
        for (Stop stop : dbStops) {
            if (stop.getLocation() == null) continue;
            map.addStop(stop.getStopID(), stop.getStopName(), stop.getLocation(),
                        new java.util.ArrayList<>(),
                        new java.util.ArrayList<>(),
                        new java.util.ArrayList<>());
        }

        // â”€â”€ Pass 2: Wire adjacency now that every stop is in the map â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // stop.addConnection() is public and bypasses Map.addStop()'s index check.
        final double FLAT_FARE = 10.0;
        for (Stop dbStop : dbStops) {
            Stop mapFrom = map.getStopById(dbStop.getStopID());
            if (mapFrom == null) continue;

            for (Stop dbConn : dbStop.getConnections()) {
                Stop mapTo = map.getStopById(dbConn.getStopID());
                if (mapTo == null) continue;

                // Skip if already wired (DB adjacency is bidirectional)
                boolean exists = mapFrom.getConnections().stream()
                        .anyMatch(c -> c.getStopID() == dbConn.getStopID());
                if (!exists) {
                    mapFrom.addConnection(mapTo, FLAT_FARE, 0);
                }
            }
        }

        return map;
    }

}

