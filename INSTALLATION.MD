Descargar el Intellij IDEA: https://www.jetbrains.com/idea/download/?section=windows
Descargar el JKD de java 17: https://adoptium.net/es/temurin/releases?version=17
Descargar el Android Studio: https://developer.android.com/studio

Descargar el gradlew y configurarlo: 

1. Darle click al botón de la hamburguesa
2. Ir a File -> Sync project with Gradlew files
3. Conectar el gradlew con el proyecto seleccionando el nombre del jdk (eclipse)
4. Crear el archivo de local.properties
5. Agregar las dos variables:
   sdk.dir=C\:\\Users\\tiity\\AppData\\Local\\Android\\Sdk -> poner aqui el archivo desde tu computadora
   WEATHER_API_KEY=801572ec977646cea6524819252802 -> Dejarlo tal cual
6. Cambiar el nombre desde App/src/main/res/values/themes.xml y lo pegas en el archivo de AndroidManifest.xml
7. Agregar el nuevo logo para Haulmaster en el archivo de AndroidManifest.xml

