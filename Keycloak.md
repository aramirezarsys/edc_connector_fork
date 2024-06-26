# Keycloak

Es un sistema de autenticación y autorización seguro, es compatible con muchos protocolos seguros, OAuth, SAML y JWT.

Se basa en un dos partes principales, el servidor de autenticación y el de administración:
 - Autenticación: Se encarga de la autenticación y autorización de los usuarios.
 - Administración: Se encarga de la administración de los usuarios, clientes, roles...

Funciona en base a un servidor REST, que en las llamadas valida al usuario y devuelve un token de acceso.

# Integración con Eclipse Dataspace Components

Hay un grupo de extensiones llamado IAM, que sirven para crear el servicio de identidad del sistema federado, contiene 3 extensiones: DAPS, Descentralized identity, OAuth2. Sirven para tener la identificación de los usuarios de forma centralizada (DAPS) o descentralizada (Descentralized identity), el sistema OAuth parece crear otra identificación de los usuarios de forma descentralizada, ya que no aparece ninguna documentación sobre que sea necesario un conector especifico con esta extensión.

En este caso nos importa la extensión de [OAuth2](https://github.com/eclipse-edc/Connector/tree/564d6270bb32ea751e206461be8f2c1c01dfce24/extensions/common/iam/oauth2/oauth2-core).

En la siguiente tabla se encuentran los parametros para el despliegue de un conector con esta extensión, algunos no son necesarios, pero voy a montar un servidor para probarlo. 

| Parameter name                    | Description                                                                                | Mandatory | Default value                       |
|:----------------------------------|:-------------------------------------------------------------------------------------------|:----------|:------------------------------------|
| `edc.oauth.token.url`             | URL of the authorization server                                                            | true      | null                                |
| `edc.oauth.provider.audience`     | Provider audience to be put in the outgoing token as 'aud' claim                           | false     | id of the connector                 |
| `edc.oauth.endpoint.audience`     | Endpoint audience to verify incoming token 'aud' claim                                     | false     | `edc.oauth.provider.audience` value |
| `edc.oauth.provider.jwks.url`     | URL from which well-known public keys of Authorization server can be fetched               | false     | http://localhost/empty_jwks_url     | 
| `edc.oauth.certificate.alias`     | Alias of public associated with client certificate                                         | true      | null                                |
| `edc.oauth.private.key.alias`     | Alias of private key (used to sign the token)                                              | true      | null                                |
| `edc.oauth.provider.jwks.refresh` | Interval at which public keys are refreshed from Authorization server (in minutes)         | false     | 5                                   |
| `edc.oauth.client.id`             | Public identifier of the client                                                            | true      | null                                |
| `edc.oauth.validation.nbf.leeway` | Leeway in seconds added to current time to remedy clock skew on notBefore claim validation | false     | 10                                  |

## Despliegue del servidor Keycloack

Para hacer pruebas utilizaré docker para desplegar un servidor Keycloack http en modo desarrollo. ``docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:24.0.5 start-dev``

Una vez dentro vamos a crear dos usuario, uno llamado provider y uno llamado consumer, Sigo investigando como configurar bien el servidor, añadir toda la configuración y como funciona el identificarse de esta manera... 