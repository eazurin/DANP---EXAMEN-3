# COVID‑19 Proximity Tracing App

## Descripción
Aplicación Android de trazado de contactos basada en BLE que detecta encuentros cercanos entre dispositivos, almacena localmente los eventos y los sincroniza con Firebase Firestore. Incluye módulo de administración en Jetpack Compose para:
- Visualizar lista de “hosts” (dispositivos que han iniciado rastreo)
- Marcar dispositivos como positivos
- Filtrar y listar encuentros por PID
- Generar grafo de contagio interactivo con nodos y aristas que muestran ID parcial y distancia estimada

## Tecnologías empleadas
| Capa                   | Herramienta / Biblioteca                           |
|:-----------------------|:---------------------------------------------------|
| **Bluetooth LE**       | `android.bluetooth.le.ScanCallback`, Advertise API |
| **Persistencia local** | Room (`AppDatabase`, `EncounterDao`, `EncounterEntity`) |
| **Sincronización**     | WorkManager (CoroutineWorker), Firestore BOM       |
| **Auth & Backend**     | Firebase Auth (anónimo), Firestore (`encounters`, `positives`) |
| **Front‑end Admin**    | Jetpack Compose, Navigation‑Compose, Material3      |
| **Gráficos**           | `Canvas` + `nativeCanvas` para nodos/aristas       |
| **Coroutines & Flow**  | kotlinx.coroutines, Flow, `snapshots()`             |
| **Detección exposición** | SharedPreferences, algoritmo de riesgo (`RiskScorer`) |

## Estructura principal
app/
├─ src/main/java/com/example/examen3
│ ├─ MainActivity.kt
│ ├─ ProximityService.kt
│ ├─ ProximityViewModel.kt
│ ├─ data/local/… # Room DB, DAO, Entity
│ ├─ data/EncounterRepository.kt
│ ├─ data/ExposureDetector.kt
│ ├─ data/GraphBuilder.kt
│ ├─ ui/admin/… # HostList, Details, Graph
│ ├─ util/PidStore.kt
│ └─ work/… # SyncWorker, ExposureWorker

## Módulos clave

- **ProximityService**
    - `startAdvertising()`: publica PID anónimo como ServiceData BLE
    - `startScanning()`: filtra por RSSI ≥ –70 dBm
    - `handleScanResult()`: almacena en Room y emite eventos via `ProximityEventBus`

- **EncounterRepository**
    - `saveLocal()`: inserta encuentros en DB local
    - `syncPending()`: sube encuentros a Firestore y marca como sincronizados

- **HostListViewModel & HostListScreen.kt**
    - Lista únicos `pid_a` desde Firestore
    - Botón “Marcar COVID‑19” crea doc en `positives/`, deshabilita botón y colorea card
    - Invoca `GraphScreen` al marcar para mostrar grafo inmediato

- **EncounterDetailsViewModel & EncounterDetailsScreen.kt**
    - `EncounterDetailsViewModel.kt`: stream de encuentros (`snapshots()`) y filtra por distancia
    - `EncounterDetailsScreen.kt`: slider (0–5 m) y lista de items (`EncounterItem`)

- **GraphViewModel & GraphScreen.kt**
    - `GraphViewModel.kt`: combina flujos `pid_a` y `pid_b` con `snapshots()`
    - `GraphBuilder.kt`: agrupa por peer, calcula distancia media, duración y recencia
    - `GraphScreen.kt`: `Canvas` dibuja nodos (PID abreviado) y aristas (distancia etiquetada)

- **ExposureWorker**
    - Descarga `positives`, ejecuta `ExposureDetector.detectAndSave()`, y muestra notificación via `NotificationHelper`

## Instalación y despliegue
1. Clonar repositorio
2. Añadir `google-services.json` en `app/`
3. Compilar con Android SDK ≥ 24
4. Ejecutar en dispositivo/emulador

## Uso
- **Usuario**: conceder permisos BLE, pulsar “Iniciar Rastreo” en `MainActivity.kt`.
- **Administrador**: pulsar “Loguearse” en `MainActivity.kt`, acceder a `AdminActivity.kt`, gestionar hosts y grafo.

## Credenciales de Administrador
- **Usuario:** admin@gmail.com
- **Contraseña:** Admin123

---

## Participación del equipo
| Integrante                          | % Participación |
|:------------------------------------|:---------------:|
| Azurin Zuñiga, Eberth Wilfredo      |       25 %      |
| Canal Mendoza, Fernando Rubén       |       25 %      |
| Galvez Quilla, Henry Isaías         |       25 %      |
| Huamani Luque, Diego Alonso         |       25 %      |
