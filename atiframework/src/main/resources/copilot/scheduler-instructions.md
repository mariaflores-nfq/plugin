---
description: Instrucciones y reglas de validación estrictas para los ficheros de configuración del Scheduler de ATI Framework.
applyTo:
  - "**/*.sch"
---

# Contexto Global del Agente
Eres un experto en el motor Batch Low-Code de ATI Framework. Tu objetivo es asistir al usuario en la creación, edición y validación de archivos de configuración de planificadores (Schedulers).
A pesar de tener la extensión `.sch`, el contenido de este archivo es estrictamente un JSON válido. Tu responsabilidad es garantizar que el código JSON cumpla exhaustivamente con el modelo de datos de la colección `CGKXJ_CFG_SCHEDULER`.

---

# Esquema de Datos Obligatorio: Nivel Raíz

Todo archivo `.sch` debe contener los siguientes campos en su raíz:

* **_id** (String, Obligatorio): Identificador propio de mongo para identificar de forma unívoca el documento.
* **planCode** (String, Obligatorio): Nombre de la planificación.
* **version** (String, Obligatorio): Versión de la planificación, importante para mantenimiento de diferentes versiones del Planificador.
* **description** (String, Opcional): Descripción de la configuración del plan.
* **status** (String, Obligatorio): Indica el estado actual de la planificación. Valores permitidos: `NOT_PUBLISHED`, `PUBLISHED`, `INACTIVE`, `ARCHIVED`.
* **uuaa** (String, Obligatorio): UUAA con la que está asociada la planificación. Siempre en minúsculas y de 4 caracteres (ej: "gkxj").
* **branchId** (ObjectId, Opcional): Identificador del branch al que pertenece la configuración.
* **branchCode** (String, Opcional): Código del branch al que pertenece la configuración.
* **recordVersion** (Integer, Obligatorio): Número de versión del registro, es útil para evitar la modificación concurrente desde el Front.
* **checkSum** (String, Obligatorio): Código de comprobación de redundancia, sirve para estar seguro que la configuración no ha sido modificada manualmente por nadie externo a ATI Framework.
* **calendarCode** (String, Opcional): Código del calendario asociado, en caso de que esta planificación tenga un calendario.

---

# Esquema de Datos: Auditoría (paramAuditList)

* **paramAuditList** (Array de Objetos, Obligatorio): Lista de auditoría de las distintos estados por los que pasa la planificación. Cada objeto debe incluir:
    * **status** (String, Obligatorio): Indica el cambio de estado de la planificación.
    * **audTs** (Datetime, Obligatorio): Fecha y hora del cambio de estado.
    * **audUser** (String, Obligatorio): Usuario que realizó el cambio de estado.
    * **comments** (String, Obligatorio): Comentarios asociados al cambio de estado.

---

# Esquema de Datos: Objeto Trigger

* **trigger** (Objeto, Obligatorio): Detalle de la planificación horaria del Plan. Propiedades:
    * **type** (String, Obligatorio): Tipo de desencadenador (`DAILY`, `WEEKLY`, `MONTHLY`).
    * **initTs** (Datetime, Opcional): Fecha de inicio de la planificación.
    * **endTs** (Datetime, Opcional): Fecha de fin de la planificación.
    * **repeat** (Objeto, Obligatorio): Habrá que definir los detalles de cuando se ejecutará.
        * **executionTime** (String, Obligatorio): Hora de ejecución en forma HH:MM:SS.
        * **repeatEvery** (Integer, Opcional): Se repite la ejecución cada vez que pasen los segundos indicados.
        * **maxExecutionTime** (String, Obligatorio): Máxima hora de ejecución en forma HH:MM:SS.
        * **weekDays** (Array de Strings, Opcional): Lista de días de la semana en el que se ejecutará (`MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`).
        * **monthly** (Objeto, Opcional): Configuración cuando es de tipo MONTHLY.
            * **months** (Array de Strings, Obligatorio): Lista de meses (en inglés) en los que tiene sentido la ejecución de la tarea.
            * **days** (Array de Integers, Opcional): Días del mes en los que se ejecutará la tarea.
            * **eventWeek** (String, Opcional): Determina el primer, segundo tercero o cuarto día de la semana de cada mes (`FIRST`, `SECOND`, `THRID`, `FOURTH`, `LAST`).
            * **weekDays** (Array de Strings, Opcional): Lista de días de la semana en el que se ejecutará. Este campo combina con eventWeek.

---

# Esquema de Datos: Objetos Batch y BatchError

* **batch** (Objeto, Obligatorio): Información de la ejecución que se va a lanzar para la planificación.
* **batchError** (Objeto, Obligatorio): Información de la ejecución que se va a lanzar para la planificación, cuando el plan de ejecución se ha terminado sin haber sido ejecutado. Será del mismo tipo que la propiedad batch.

Ambos objetos comparten la misma estructura:
* **batchCode** (String, Obligatorio): Nombre de la aplicación, puede ser un valor fijo o el resultado de un campo de la consulta.
* **parameterQueryList** (Array de Objetos, Obligatorio): Colección (Array) con las consulta con la que se va a extraer los datos desde la BBDD.
    * **queryCode** (String, Obligatorio): Código para identificar de que consulta de extracción se trata.
    * **sqlQuery** (String, Opcional): Consulta SQL que se va a lanzar.
    * **dbSource** (String, Opcional): Indicador del tipo de Base de datos (Oracle, Postgre o mongoDb).
    * **mongoQuery** (Objeto, Opcional): Consulta Aggregate Mongo que se va a lanzar.
        * **collection** (String, Obligatorio en Mongo): Nombre de la colección sobre la que se va a realizar la consulta.
        * **filter** (Array de Strings, Obligatorio en Mongo): Lista de filtros que se van a realizar sobre la colección de Mongo Db.
* **jobParameterList** (Array de Objetos, Opcional): Lista de parámetros de entrada de las ejecuciones a lanzar.
    * **paramName** (String, Obligatorio): Nombre del parámetro.
    * **queryParam** (String, Opcional): Nombre del campo recuperado de parameterQueryList para pasárselo a este parámetro del Job.
    * **fixedValue** (String, Opcional): Valor fijo asignado a este parámetro del Job.
    * **scriptValue** (String, Opcional): Script de javascript que permitirá fijar el valor del parámetro.

---

# Esquema de Datos: Array conditionList (Opcional)

* **conditionList** (Array de Objetos, Opcional): Lista de condiciones que deberán cumplirse previamente para poder ejecutar la tarea asociada a la planificación.
    * **name** (String, Obligatorio): Nombre o código de la condición.
    * **checkEvery** (Integer, Opcional): Número de segundos cada cuantos se va a hacer la comprobación de si esta condición se cumple.
    * **forceAtEnd** (Boolean, Opcional): Forzará la ejecución al final aunque no se haya completado la condición.
    * **script** (String, Opcional): Script que servirá para validar las consultas sobre Base de datos.
    * **fileWatcher** (Objeto, Opcional): Validación de si un Fichero que cumpla determinado patrón existe o no.
        * **path** (String, Obligatorio): Path donde se va a buscar el fichero.
        * **filePattern** (String, Obligatorio): Patrón que deberá cumplir el fichero.
        * **fileParameterName** (String, Obligatorio): Nombre del parámetro de entrada que se va a utilizar para pasarselo al Batch.
    * **query** (Objeto, Obligatorio si aplica): Consulta para comprobar si deje ejecutarse o no. (Misma estructura interna que `parameterQueryList`: `queryCode`, `sqlQuery`, `dbSource`, `mongoQuery` con `collection` y `filter`).

---

# Esquema de Datos: Objeto failedTreatment (Opcional)

* **failedTreatment** (Objeto, Opcional): En caso de que la operación se descarte sin ejecutar, se generará un criticalIssue.
    * **level** (String, Opcional): Directorio donde se generará el fichero de Flag indicando errores en la planificación.
    * **issueCode** (String, Opcional): IssueCode para el issue a generar al no validar el esquema XSD.
    * **tecnicalCode** (String, Opcional): TecnicalCode para el issue a generar al no validar el esquema XSD.

---

# Directrices de Respuesta de GitHub Copilot

1. **Cumplimiento Estricto**: Al autocompletar código o sugerir correcciones en el editor de IntelliJ para archivos `.sch`, DEBES ceñirte exclusivamente a las propiedades aquí descritas. No inventes campos que no estén presentes en esta documentación. Si el usuario pregunta por validaciones, utiliza este modelo como fuente de verdad absoluta.
2. **Formateo de Scripts**: Para los campos que contienen código JavaScript (`script` y `scriptValue`), DEBES generar el código debidamente tabulado y utilizando un retorno de carro (escapado como `\n` dentro de la cadena JSON) para cada línea de código. La estructura del script debe ser legible y mantener las indentaciones (escapadas como `\t` o espacios) correspondientes a las buenas prácticas de programación en JavaScript.
