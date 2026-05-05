# RegistroLineasCampo

Aplicación Android orientada al control de líneas realizadas por un apuntador en campo. El proyecto está pensado para llevar un registro local de trabajo agrícola, permitir el acceso mediante usuario y contraseña, y centralizar la navegación entre las pantallas de registro, configuración y reportes.

El proyecto utiliza una base de datos local con Room, por lo que no depende de un backend externo para iniciar sesión o validar usuarios. En el arranque inicial se crean usuarios de demostración automáticamente si la base de datos está vacía.

## Objetivo del proyecto

El objetivo principal de la app es ofrecer una interfaz simple para registrar y consultar el trabajo realizado en campo, con un enfoque visual claro y una navegación rápida entre módulos. La aplicación está estructurada como un sistema base que puede crecer con funcionalidades como captura de registros, exportación de reportes y configuración operativa.

## Funcionalidades actuales

Actualmente el proyecto incluye:

- Inicio de sesión con usuario y contraseña.
- Persistencia local de usuarios mediante Room.
- Creación automática de usuarios base si la tabla está vacía.
- Pantalla principal de registros con barra de navegación inferior.
- Pantalla de configuración con opciones visuales para parámetros del trabajo.
- Pantalla de reportes con resumen diario y botones de exportación.
- Diseño visual con colores verdes y tarjetas para una interfaz más clara en campo.

## Flujo de uso

1. La app abre en la pantalla de inicio de sesión.
2. El usuario ingresa credenciales válidas.
3. Si el acceso es correcto, la app navega a la pantalla de registros.
4. Desde la barra inferior se puede cambiar entre Registros, Configuración y Reportes.
5. Los datos y usuarios se mantienen en el dispositivo mediante la base local.

## Credenciales de prueba

Si el proyecto se ejecuta por primera vez y la base de datos aún no contiene usuarios, la app crea automáticamente estas cuentas de prueba:

- Usuario: `Apuntador1_2025` | Contraseña: `Poda_2025_1`
- Usuario: `Apuntador2_2025` | Contraseña: `Poda_2025_2`
- Usuario: `Admin123` | Contraseña: `12345`

## Pantallas

### Login

Pantalla de acceso principal. Solicita usuario y contraseña, valida que los campos no estén vacíos y verifica las credenciales contra la base local.

### Registros

Pantalla orientada al trabajo diario. Muestra un encabezado con información del apuntador, un panel de configuración del día, un buscador y una barra de navegación inferior.

### Configuración

Pantalla enfocada en ajustes operativos. Incluye espacios para cargar logo, configurar tipo de trabajo, cuadro y variedad, además de acciones como guardar configuración, limpiar historial y cerrar sesión.

### Reportes

Pantalla para métricas y exportación. Presenta un resumen del día, tarjetas con totales, un bloque de ranking y botones para exportar en TXT o CSV.

## Tecnologías utilizadas

- Lenguaje: Java 11
- Android SDK: compileSdk 35
- minSdk: 23
- Interfaz: AndroidX, Material Components, ConstraintLayout
- Persistencia local: Room
- Base de datos: SQLite a través de Room

## Requisitos del entorno

- Android Studio instalado.
- SDK de Android 35 o compatible.
- JDK 11.
- Emulador Android o dispositivo físico para pruebas.

## Instalación y ejecución

### Desde Android Studio

1. Abre el proyecto en Android Studio.
2. Espera a que Gradle termine de sincronizar dependencias.
3. Conecta un dispositivo o inicia un emulador.
4. Ejecuta la app desde el botón de Run.

### Desde línea de comandos

Si deseas compilar desde terminal, puedes usar Gradle Wrapper:

```bash
./gradlew assembleDebug
```

En Windows también puedes usar:

```bat
gradlew.bat assembleDebug
```

## Persistencia local

La app define una base de datos Room llamada `db_campo` con una entidad principal llamada `usuarios`.

### Entidad `Usuario`

- `id`: identificador autogenerado.
- `nombreUsuario`: nombre de inicio de sesión.
- `contrasena`: contraseña asociada.

### Operaciones disponibles en `UsuarioDao`

- Registrar un usuario nuevo.
- Buscar un usuario por nombre y contraseña.
- Contar cuántos usuarios existen en la base.

### Inicialización automática

Al abrir el login, la aplicación verifica si existen usuarios guardados. Si la tabla está vacía, inserta usuarios de demostración para facilitar las pruebas iniciales.

## Estructura del proyecto

```text
RegistroLineasCampo/
├── app/
│   ├── src/main/java/com/example/registrolineascampo/
│   │   ├── AppDatabase.java
│   │   ├── Configuracion.java
│   │   ├── Login.java
│   │   ├── Registros.java
│   │   ├── Reportes.java
│   │   ├── Usuario.java
│   │   └── UsuarioDao.java
│   ├── src/main/res/layout/
│   │   ├── activity_configuracion.xml
│   │   ├── activity_registros.xml
│   │   ├── activity_reportes.xml
│   │   └── login.xml
│   └── src/main/AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Dependencias principales

El proyecto usa, entre otras, estas dependencias relevantes:

- `androidx.appcompat`
- `com.google.android.material`
- `androidx.constraintlayout`
- `androidx.room:room-runtime:2.8.4`
- `androidx.room:room-compiler:2.8.4`

## Configuración declarada

- `applicationId`: `com.example.registrolineascampo`
- `versionName`: `1.0`
- `versionCode`: `1`
- `targetSdk`: `35`
- `minSdk`: `23`

## Estado actual del proyecto

El proyecto ya tiene la estructura base de navegación, autenticación local y diseño visual de las pantallas principales. Algunas secciones muestran interfaz preparada para funcionalidad futura, por lo que el proyecto puede considerarse una base funcional en expansión.

## Posibles mejoras futuras

- Guardar y consultar registros reales de trabajo.
- Asociar cada línea con fecha, trabajador, tipo de labor y observaciones.
- Implementar exportación funcional a TXT y CSV.
- Persistir configuración del día en la base local o en preferencias.
- Agregar historial de trabajos y filtros de búsqueda.
- Mostrar el usuario autenticado en todas las pantallas.

## Nota

El proyecto está orientado a uso local en dispositivo Android. Si se desea trabajar con sincronización en la nube, multiusuario avanzado o reportes automáticos, habría que agregar una capa adicional de backend y almacenamiento remoto.
