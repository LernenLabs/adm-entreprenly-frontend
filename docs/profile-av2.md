# Profile en Android · AV2

La variante **Entreprenly AV2** usa el identificador independiente
`online.entreprenly.entreprenlyapp.av2` y se conecta al backend local de prueba.
Puede convivir con la app habitual en el mismo dispositivo.

## Probar en un teléfono por USB

1. En el repo Spring Boot, compila y arranca el entorno local.

   ```powershell
   .\mvnw.cmd -q package
   java -jar target/platform-1.0.0.jar --spring.profiles.active=av2
   ```

2. En el repo Android, compila el APK. Con el teléfono conectado y la depuración USB autorizada, conecta el puerto e instala la variante.

   ```powershell
   .\gradlew.bat :app:assembleAv2
   $adb = "$env:LOCALAPPDATA\Android\sdk\platform-tools\adb.exe"
   & $adb reverse tcp:8092 tcp:8092
   & $adb install -r app/build/outputs/apk/av2/app-av2.apk
   ```

3. Abre **Entreprenly AV2** y registra una cuenta de prueba. El perfil local usa una base H2 separada.

Para un emulador sin redirección USB, compila con
`.gradlew.bat :app:assembleAv2 "-Pav2ApiUrl=http://10.0.2.2:8092/"`.

## Verificar los cuatro flujos

- **US-64.** En Más → Perfil → Editar, elige o toma una foto y pulsa Guardar foto. Reabre el perfil y comprueba que se conserva. Prueba también un archivo inválido o de más de 5 MB.
- **US-65.** Solicita un enlace para un correo de prueba nuevo. Abre el archivo más reciente `email-*.txt` en `%LOCALAPPDATA%\Entreprenly-AV2\inbox`, abre el enlace en el navegador de la PC y pulsa **Confirmar cambio de correo**. Cierra la sesión de Android e inicia sesión con el nuevo correo. Comprueba un correo ya registrado y un enlace vencido o usado.
- **US-66.** Inicia sesión con la misma cuenta en dos clientes. Cambia la contraseña desde uno y comprueba que su sesión sigue activa, que el segundo pierde acceso y que puedes iniciar sesión con la contraseña nueva.
- **US-69.** Toca el teléfono en Perfil y solicita un código. Léelo en el archivo más reciente `phone-*.txt` de la misma bandeja e introdúcelo en la app. Prueba también un código incorrecto y uno vencido o usado.

El modo AV2 escribe las confirmaciones en una bandeja de prueba de la PC. Usa correos y teléfonos ficticios. Los enlaces y códigos duran 10 minutos y se pueden volver a solicitar después de 30 segundos.

## Contrato de integración

| HU | Solicitud desde Android | Respuesta |
| --- | --- | --- |
| US-64 | POST /api/v1/profiles/{profileId}/avatar con la parte multipart image. | Perfil actualizado con la ruta de la foto. |
| US-65 | POST /api/v1/users/me/email-change-requests con email. | 202 al aceptar el envío. El cambio se aplica al confirmar el enlace. |
| US-69 | POST /api/v1/profiles/{profileId}/phone-verifications con phone. POST al subrecurso /confirm con phone y code. | 202 al aceptar el envío y perfil actualizado al confirmar. |
| US-66 | PUT /api/v1/users/me/password con currentPassword y newPassword. | Usuario actualizado. El JWT de la sesión que realizó el cambio conserva el acceso. |

Al desplegar este cambio, las sesiones creadas por el formato anterior de JWT deben iniciar sesión nuevamente. El entorno público requiere integrar proveedores de correo y SMS para habilitar la entrega de confirmaciones.
