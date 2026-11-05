# Club Deportivo Séneca – Proyecto 1, Entrega 2

## Participantes

1. Alejandra de la Bastida
2. Juan Jose Castro

## Cómo ejecutar

1. Descargue o clone el repositorio.
2. En Eclipse, vaya a File > Import > General > Existing Projects into Workspace, elija la carpeta Proyecto 1/Entrega 2 y pulse Finish
3. En el proyecto, abra src > consola. Allí están los programas Prueba1 a Prueba10.
4. Clic derecho sobre una prueba (por ejemplo Prueba1.java) > **Run As > Java Application**.
5. La consola pregunta por la carpeta de datos. Pulse **Enter** para usar la predeterminada (datos/prueba1, datos/prueba2, etc.).
6. Lea la salida. Cada escenario muestra un título y su resultado:
   `[OK]`: la operación válida funcionó,
   `[RECHAZADO esperado: motivo]`: la operación inválida fue rechazada por una regla de negocio,
   `[ERROR]`: no se obtuvo el resultado esperado 

   También se imprime el estado de los datos antes y después de los escenarios.
7. Para comprobar la persistencia, ejecute la misma prueba por segunda vez. El programa dice "Se cargaron datos existentes" y muestra el estado que quedó guardado en `datos/pruebaN/club.dat`.
8. Para repetir una prueba desde cero, borre su carpeta `datos/pruebaN` y vuelva a ejecutarla. 
9. Repita los pasos 4 a 8 con las demás pruebas.

