package com.dgomez.testing;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HelloApplication extends Application {
    private TextField txtClave;
    private TextArea txtMsg;
    private TextArea txtCifMsg;
    private final OneTimePadService otpService = new OneTimePadService();

    @Override
    public void start(Stage stage) {
        txtClave = new TextField();
        txtClave.setPromptText("CLAVE (misma longitud en bytes que el mensaje)");

        txtMsg = new TextArea();
        txtMsg.setPromptText("Mensaje original...");
        txtMsg.setWrapText(true);
        VBox.setVgrow(txtMsg, Priority.ALWAYS);

        Button btnCif = new Button("Cifrar");
        btnCif.setPrefWidth(120);
        btnCif.setStyle("-fx-background-radius: 20;");
        btnCif.setOnAction(e -> cifrar());

        Button btnDescif = new Button("Descifrar");
        btnDescif.setPrefWidth(120);
        btnDescif.setStyle("-fx-background-radius: 20;");
        btnDescif.setOnAction(e -> descifrar());

        HBox filaBotones = new HBox(30, btnCif, btnDescif);
        filaBotones.setAlignment(Pos.CENTER);
        filaBotones.setPadding(new Insets(5, 0, 5, 0));

        txtCifMsg = new TextArea();
        txtCifMsg.setPromptText("Mensaje cifrado (Base64)...");
        txtCifMsg.setWrapText(true);
        VBox.setVgrow(txtCifMsg, Priority.ALWAYS);

        VBox root = new VBox(15, txtClave, txtMsg, filaBotones, txtCifMsg);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 480, 500);
        stage.setTitle("One-Time Pad - JavaFX");
        stage.setScene(scene);
        stage.show();
    }

    private void cifrar() {
        try {
            String resultadoBase64 = otpService.cifrar(txtMsg.getText(), txtClave.getText());
            txtCifMsg.setText(resultadoBase64);
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void descifrar() {
        try {
            String mensajeRecuperado = otpService.descifrar(txtCifMsg.getText(), txtClave.getText());
            txtMsg.setText(mensajeRecuperado);
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de validación / OTP");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
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