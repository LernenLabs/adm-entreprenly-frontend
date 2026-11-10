# Profile en Android · AV2

Rama de integración para US-64, US-65, US-66 y US-69. Los contratos siguientes son una propuesta del cliente Android para coordinar con Spring Boot.

| HU | Solicitud desde Android | Respuesta esperada |
| --- | --- | --- |
| US-64 | POST /api/v1/profiles/{profileId}/avatar. Multipart con campo image y una imagen JPEG, PNG o WebP de hasta 5120 KB. | Perfil actualizado con avatarUrl HTTPS que sirva directamente la imagen. Spring Boot verifica el dueño, valida el archivo y guarda la foto. |
| US-65 | POST /api/v1/users/me/email-change-requests con email. Enviar de nuevo solicita otro enlace. | Respuesta 2xx tras aceptar el envío. El correo cambia después de confirmar el enlace. Spring Boot comprueba duplicados y vigencia, y permite solicitar otro enlace si vence. |
| US-69 | POST /api/v1/profiles/{profileId}/phone-verifications con phone. Para confirmar, POST al subrecurso /confirm con phone y code de seis dígitos. | Respuesta 2xx al aceptar el envío y perfil actualizado al verificar el código. Spring Boot verifica el dueño, limita intentos y reenvíos, y rechaza códigos incorrectos o vencidos. |
| US-66 | PUT /api/v1/users/me/password con currentPassword y newPassword, según el contrato existente. | La app confirma la nueva contraseña antes de enviar y conserva la sesión actual. La revocación de otras sesiones sigue pendiente en IAM. |

Para S2-TS01, ejecutar las pruebas unitarias y el build de depuración. Una vez disponible el backend, comprobar cámara y galería, archivo inválido y mayor al límite, foto tras volver a iniciar sesión, correo duplicado y enlace vencido, contraseña incorrecta y acceso con la nueva, y código telefónico correcto, inválido y vencido. Para la contraseña, usar dos sesiones de la misma cuenta y verificar cuál conserva acceso.
