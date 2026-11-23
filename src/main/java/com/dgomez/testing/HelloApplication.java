package com.dgomez.testing;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Base64;

public class HelloApplication extends Application {

    private TextField txtClave;
    private TextArea txtMsg;
    private TextArea txtCifMsg;
    private final OneTimePadService otpService = new OneTimePadService();
    private final ExportadorOtpService exportadorService = new ExportadorOtpService();
    @Override
    public void start(Stage stage) {
        txtClave = new TextField();
        txtClave.setPromptText("CLAVE (al menos la misma longitud en bytes que el mensaje)");


        txtMsg = new TextArea();
        txtMsg.setPromptText("Mensaje original...");
        txtMsg.setWrapText(true);

        VBox.setVgrow(txtMsg, Priority.ALWAYS);

        Button btnGenClave = new Button("Generar Clave");
        btnGenClave.setOnAction(e -> txtClave.setText(GeneradorClaveOtp.generarClave5k()));

        Button btnCif = new Button("Cifrar");
        btnCif.setPrefWidth(110);

        btnCif.setOnAction(e -> cifrar());

        Button btnDescif = new Button("Descifrar");
        btnDescif.setPrefWidth(110);

        btnDescif.setOnAction(e -> descifrar());

        Button btnExportTxt = new Button("Exportar TXT");
        btnExportTxt.setOnAction(e -> exportarMensajeCifrado(stage));

        Button btnExportBin = new Button("Exportar Clave");
        btnExportBin.setOnAction(e -> exportarClave(stage));

        HBox filaAcciones = new HBox(15, btnCif, btnDescif, btnGenClave);
        filaAcciones.setAlignment(Pos.CENTER);
        filaAcciones.setPadding(new Insets(5, 0, 0, 0));

        HBox filaExportar = new HBox(15, btnExportTxt, btnExportBin);
        filaExportar.setAlignment(Pos.CENTER);
        filaExportar.setPadding(new Insets(0, 0, 5, 0));

        txtCifMsg = new TextArea();
        txtCifMsg.setPromptText("Mensaje cifrado (Base64)...");
        txtCifMsg.setWrapText(true);

        VBox.setVgrow(txtCifMsg, Priority.ALWAYS);

        VBox root = new VBox(12, txtClave, txtMsg, filaAcciones, filaExportar, txtCifMsg);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 520, 540);
        var resource = getClass().getResource("style.css");
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        } else {
            System.err.println("Aviso: no se encontró style.css en el classpath/módulo.");
        }
        String cssUrl = String.valueOf(resource);
        scene.getStylesheets().add(cssUrl);

        btnGenClave.getStyleClass().add("cyber-magenta");
        stage.setTitle("One-Time Pad - JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    private void cifrar() {
        try {
            String resultadoBase64 = otpService.cifrar(txtMsg.getText(), txtClave.getText());
            txtCifMsg.setText(resultadoBase64);
        } catch (IllegalArgumentException ex) {
            mostrarError("Error de validación / OTP", ex.getMessage());
        }
    }

    private void descifrar() {
        try {
            String mensajeRecuperado = otpService.descifrar(txtCifMsg.getText(), txtClave.getText());
            txtMsg.setText(mensajeRecuperado);
        } catch (IllegalArgumentException ex) {
            mostrarError("Error de validación / OTP", ex.getMessage());
        }
    }

    private void exportarMensajeCifrado(Stage stage) {
        String cifrado = txtCifMsg.getText();
        if (cifrado == null || cifrado.isBlank()) {
            mostrarError("Exportación cancelada", "No hay ningún mensaje cifrado para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar mensaje cifrado");
        fileChooser.setInitialFileName("mensaje_cifrado.txt");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos de texto (*.txt)", "*.txt")
        );

        java.io.File seleccionado = fileChooser.showSaveDialog(stage);
        if (seleccionado == null) {
            return;
        }

        try {
            Path destino = seleccionado.toPath();
            exportadorService.exportarTextoUtf8(destino, cifrado);
            mostrarInfo("Exportación completada", "Mensaje cifrado guardado en:\n" + destino.toAbsolutePath());
        } catch (IOException ex) {
            mostrarError("Error de E/S", "No se pudo guardar el archivo de texto: " + ex.getMessage());
        }
    }

    private void exportarClave(Stage stage) {
        String clave = txtClave.getText();
        if (clave == null || clave.isEmpty()) {
            mostrarError("Exportación cancelada", "No hay ninguna clave para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar clave");
        fileChooser.setInitialFileName("clave_otp.txt");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivox txt (*.txt)", "*.txt")
        );

        java.io.File seleccionado = fileChooser.showSaveDialog(stage);
        if (seleccionado == null) {
            return;
        }

        try {
            Path destino = seleccionado.toPath();
            byte[] claveBytes = clave.getBytes(StandardCharsets.UTF_8);
            exportadorService.exportarBinario(destino, claveBytes);
            mostrarInfo("Exportación completada", "Clave guardada en:\n" + destino.toAbsolutePath());
        } catch (IOException ex) {
            mostrarError("Error de E/S", "No se pudo guardar el archivo: " + ex.getMessage());
        }
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class ExportadorOtpService {

    public void exportarTextoUtf8(Path destino, String contenido) throws IOException {
        Path parent = destino.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(
                destino,
                contenido,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public void exportarBinario(Path destino, byte[] datos) throws IOException {
        Path parent = destino.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(
                destino,
                datos,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
}

class OneTimePadService {

    public String cifrar(String mensajePlano, String clave) {
        if (mensajePlano == null || mensajePlano.isEmpty()) {
            throw new IllegalArgumentException("El mensaje original no puede estar vacío.");
        }
        byte[] msgBytes = mensajePlano.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = validarYConvertirClave(clave, msgBytes.length);

        byte[] cifrado = xorBytes(msgBytes, keyBytes);
        return Base64.getEncoder().encodeToString(cifrado);
    }

    public String descifrar(String mensajeCifradoBase64, String clave) {
        if (mensajeCifradoBase64 == null || mensajeCifradoBase64.isBlank()) {
            throw new IllegalArgumentException("El mensaje cifrado no puede estar vacío.");
        }

        byte[] cifradoBytes;
        try {
            cifradoBytes = Base64.getDecoder().decode(mensajeCifradoBase64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El mensaje cifrado no es una cadena Base64 válida.");
        }

        byte[] keyBytes = validarYConvertirClave(clave, cifradoBytes.length);
        byte[] planoBytes = xorBytes(cifradoBytes, keyBytes);
        return new String(planoBytes, StandardCharsets.UTF_8);
    }

    private byte[] validarYConvertirClave(String clave, int longitudRequeridaBytes) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede estar vacía.");
        }
        byte[] keyBytes = clave.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < longitudRequeridaBytes) {
            throw new IllegalArgumentException(
                    "Clave demasiado corta para OTP: se requieren al menos " + longitudRequeridaBytes +
                            " bytes y se proporcionaron " + keyBytes.length + "."
            );
        }
        return keyBytes;
    }

    private byte[] xorBytes(byte[] datos, byte[] clave) {
        byte[] salida = new byte[datos.length];
        for (int i = 0; i < datos.length; i++) {
            salida[i] = (byte) (datos[i] ^ clave[i]);
        }
        return salida;
    }
}

class GeneradorClaveOtp {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int LONGITUD_CLAVE = 5_000;

    public static String generarClave5k() {
        int numBytes = ((LONGITUD_CLAVE + 3) / 4) * 3;
        byte[] randomBytes = new byte[numBytes];
        SECURE_RANDOM.nextBytes(randomBytes);

        String base64 = Base64.getEncoder().encodeToString(randomBytes);
        return base64.substring(0, LONGITUD_CLAVE);
    }
}