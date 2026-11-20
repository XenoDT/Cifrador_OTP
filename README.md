# Cifrador OTP (JavaFX)

Aplicación de escritorio en **Java 21 + JavaFX** para cifrar y descifrar mensajes de texto mediante una operación **XOR** inspirada en el esquema **One-Time Pad (OTP)**, con generación de claves y exportación a archivo.

> **Aviso de uso:** Proyecto de práctica / aprendizaje. No está diseñado para proteger información sensible o crítica en producción.

## Características

- **Cifrado y descifrado XOR (UTF-8):** Convierte el mensaje a bytes UTF-8, aplica XOR byte a byte contra los bytes UTF-8 de la clave y codifica el resultado en **Base64** para su visualización y copia.
- **Validación de longitud mínima:** Impide cifrar o descifrar si la clave tiene menos bytes (en UTF-8) que el mensaje o el texto cifrado decodificado.
- **Generador de claves (`SecureRandom`):** Genera una clave de 5.000 caracteres basada en bytes aleatorios criptográficos (`java.security.SecureRandom`) codificados en Base64.
- **Exportación a disco:** Permite guardar el mensaje cifrado en texto plano (UTF-8) y la clave actual en archivo desde un selector de archivos (`FileChooser`).
- **Interfaz JavaFX:** Incluye soporte para hoja de estilos `cyberpunk.css`.

## Reglas del One-Time Pad y limitaciones de esta implementación

Para que un OTP cumpla su modelo teórico de seguridad, deben cumplirse condiciones estrictas; ten en cuenta cómo se comporta esta versión:

1. **Longitud de la clave:** La clave debe tener **al menos tantos bytes como el mensaje** (en codificación UTF-8). El botón de generar crea una clave de 5.000 caracteres; para mensajes mayores debes ampliar o suministrar una clave de longitud suficiente.
2. **Un solo uso:** **No reutilices nunca** la misma clave para cifrar más de un mensaje. Cifrar dos mensajes distintos con la misma clave permite relacionar ambos textos cifrados mediante XOR.
3. **Aleatoriedad y codificación:** Se utiliza `SecureRandom`, pero al pasar los bytes aleatorios por Base64 y recortar a 5.000 caracteres se trabaja sobre caracteres ASCII codificados en UTF-8 en lugar de bytes aleatorios puros sin restricción de alfabeto.
4. **Intercambio seguro:** El programa no gestiona el transporte ni el borrado seguro de la clave; compartir clave y mensaje por el mismo canal anula la seguridad del esquema.
5. **Consistencia en exportación de clave:** La acción etiquetada como exportación binaria escribe los bytes UTF-8 del campo de clave actual y propone por defecto el nombre `clave_otp.txt`.

## Requisitos y ejecución

- **Java 21** (JDK 21) y **JavaFX** configurado en tu proyecto o herramienta de construcción.
- Archivo `cyberpunk.css` en el mismo paquete/classpath (`com/dgomez/testing/cyberpunk.css`) si deseas aplicar los estilos visuales.

```bash
git clone [https://github.com/XenoDT/Cifrador_OTP.git](https://github.com/XenoDT/Cifrador_OTP.git)
cd Cifrador_OTP