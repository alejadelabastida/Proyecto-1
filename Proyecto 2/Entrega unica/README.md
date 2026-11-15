# Club Deportivo Séneca – Proyecto 2, Entrega única

## Participantes

1. Alejandra de la Bastida
2. Juan Jose Castro
3. Tomas Rivera

## Proyecto 2 – Consola del administrador

1. Abra `src/consola/ConsolaAdministrador.java`, clic derecho > **Run As > Java Application**.
2. Entre con usuario `admin` y clave `admin123`. Si entra un socio o un empleado, el programa le avisa que use la consola de su rol y se cierra.
3. El menú principal tiene seis partes:
   - **Torneos internos**: crear, ver por estado, ver inscritos, asignar entrenador a un equipo, iniciar, registrar resultados, posiciones, finalizar (premios o bonos) e historial.
   - **Equipos y jugadores**: ver equipos, crear equipo, inscribir un socio en un equipo, dar de baja un jugador o un equipo.
   - **Inventario, tienda e instalaciones**: ver inventario (todo o por ubicación), ver instalaciones y si están libres en una fecha y hora, reabastecer la tienda, mover artículos entre Tienda y Bodega, y aprobar o rechazar sugerencias de los empleados.
   - **Turnos del personal**: ver los turnos y aprobar o rechazar solicitudes de cambio (no deja aprobar si quedan menos de 2 entrenadores y 1 fisioterapeuta).
   - **Reportes e historiales**: ventas por rubro (tienda, cafetería y mensualidades), asistencia, resultados, evolución del ranking e historial de reservas, partidos y sesiones, siempre entre dos fechas.
   - **Competencias oficiales**: registrar una competencia (liga o ASCUN), inscribir un equipo con su nómina y ver la tabla de posiciones.
4. Los datos se cargan y se guardan solos en `datos/club.dat`. La primera vez se crean datos de ejemplo (torneos, inventario, instalaciones y turnos).
5. Para probar un torneo completo: inscriba al menos dos socios desde `ConsolaParticipante` (deben estar en la disciplina y el nivel del torneo), vuelva a esta consola, inicie el torneo, registre los resultados y finalícelo.

### Pruebas JUnit del administrador

Clic derecho sobre cada archivo > **Run As > JUnit Test**. Ninguna escribe archivos.
- `GestionTorneosTest`: 15 pruebas de torneos (crear, iniciar, resultados, premios, bonos e historial).
- `AdministracionTest`: 12 pruebas de inventario (reabastecer y mover) y de reportes (ventas con mensualidades, asistencia, resultados y ranking).
- `ReservasPerfilTest`: 6 pruebas de reservas, de que el administrador no puede reservar y de la consulta del perfil.

## Proyecto 2 – Inscripciones y participantes

### Cómo ejecutar
1. Clic derecho sobre `ConsolaParticipante.java` o `ConsolaEmpleado.java` > **Run As > Java Application**.
2. Los datos se cargan y se guardan solos en `datos/club.dat`. La primera vez se crean datos de ejemplo. Para empezar de cero, borre la carpeta `datos`.

### Usuarios de prueba (todos con clave `1234`)

**Consola de participantes**
- Socios: `sofia` y `juan` (tenis intermedio), `maria` y `pedro` (pádel avanzado).
- Estudiante: `diana`.
- Persona externa: `esteban`.

**Consola de empleados**
- Entrenadores: `carlos` (responsable del equipo Halcones Sub-15) y `laura`.
- Fisioterapeuta: `mateo`.

También se pueden registrar nuevos estudiantes y personas externas desde el menú inicial de la consola de participantes. El usuario no se puede repetir y la clave debe tener mínimo 4 caracteres.

Si alguien entra a una consola que no es de su rol, el programa se lo avisa. El login da 3 intentos y no dice cuál dato falló.

### Cómo probar un torneo con la consola del asministrador
1. En `ConsolaParticipante`, inscriba al menos dos socios en un torneo.
2. En `ConsolaAdministrador`, asigne el entrenador responsable a un equipo (opción 4) si va a probar equipos.
3. En `ConsolaAdministrador`, inicie el torneo, registre los resultados y finalícelo.
4. Si el torneo es recreativo, vuelva a `ConsolaParticipante`: el ganador verá su bono en Compras > Mis bonos de descuento y podrá usarlo en la tienda o la cafetería.


### Pruebas JUnit
Clic derecho sobre cada archivo de `src/pruebas` > **Run As > JUnit Test**:
- `TorneoTest`: 10 pruebas unitarias (cupos preferenciales y reglas del torneo).
- `InscripcionesTorneoTest`: 55 pruebas de integración, una o más por historia de usuario.
- `BonosTest`: 9 pruebas (bono en tienda y cafetería, y academias).
- `PersistenciaTorneosTest`: 1 prueba (guardar y volver a cargar). Escribe en una carpeta temporal.



## Cómo ejecutar (anterior entrega)

1. Descargue o clone el repositorio.
2. En el proyecto, abra src > consola. Allí están los programas Prueba1 a Prueba10.
3. Clic derecho sobre una prueba (por ejemplo Prueba1.java) > **Run As > Java Application**.
4. La consola pregunta por la carpeta de datos. Pulse Enter para usar la predeterminada (datos/prueba1, datos/prueba2, etc.).
5. Lea la salida. Cada escenario muestra un título y su resultado:
   `[OK]`: la operación válida funcionó,
   `[RECHAZADO esperado: motivo]`: la operación inválida fue rechazada por una regla de negocio,
   `[ERROR]`: no se obtuvo el resultado esperado 

   También se imprime el estado de los datos antes y después de los escenarios.
7. Para comprobar la persistencia, ejecute la misma prueba por segunda vez. El programa dice "Se cargaron datos existentes" y muestra el estado que quedó guardado en `datos/pruebaN/club.dat`.
8. Para repetir una prueba desde cero, borre su carpeta `datos/pruebaN` y vuelva a ejecutarla. 
9. Repita los pasos 4 a 8 con las demás pruebas.
