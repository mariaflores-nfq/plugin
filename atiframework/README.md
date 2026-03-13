
# Module Type Sample [![JetBrains IntelliJ Platform SDK Docs](https://jb.gg/badges/docs.svg)][docs]
*Reference: [Project Wizard Tutorial in IntelliJ SDK Docs][docs:wizard]*

## Quickstart

Este plugin desarrollado para ATI Framework en una versión inicial solo permite la creación de un servicio anfigrión a partir de la extensión de `com.intellij.moduleType`, el cual añade un nevo module type to the *New Module* Project Wizard.
El Module con el nombre de `ATI Framework: Client Generator`, solicita la información relevante para poder construir un cliente básico en un único demonio (en esta versión no permite configrar atiTasklet ni servicios separados en distintos demonios).

### Extension Points

| Name                                        | Implementation                      | Extension Point Class |
|---------------------------------------------|-------------------------------------|-----------------------|
| `com.bbva.gkxj.atiframework.moduletemplate` | [AtiModuleType][file:AtiModuleType] | `ModuleType`          |

*Reference: [Plugin Extension Points in IntelliJ SDK Docs][docs:ep]*

#### Importante: 
Para que este plugin se pueda construir dentro del IDE se deberá configurar en el fichero `gradle.properties` el **usuario** y **password** correspondiente para que gradle pueda obtener las referencias a través del **proxyvip**.
```
systemProp.http.proxyUser=tuUsuario
systemProp.http.proxyPassword=tuPassword
...
systemProp.https.proxyUser=tuUsuario
systemProp.https.proxyPassword=tuPassword
```


[docs]: https://plugins.jetbrains.com/docs/intellij/
[docs:wizard]: https://plugins.jetbrains.com/docs/intellij/intro-project-wizard.html
[docs:ep]: https://plugins.jetbrains.com/docs/intellij/plugin-extensions.html
[file:AtiModuleType]: ./src/main/java/com/bbva/gkxj/atiframework/moduletemplate/AtiModuleType.java
