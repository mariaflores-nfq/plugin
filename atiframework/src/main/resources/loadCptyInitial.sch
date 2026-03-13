{
  "_id": "ObjectId(\"68c125bc9cfafb0194e3ec47\")",
  "failedTreatment": {
    "level": "",
    "technicalCode": "",
    "issueCode": ""
  },
  "batch": {
    "batchCode": "loadCptyInitial",
    "jobParameterList": [
      {
        "paramName": "sourceFileCpty",
        "queryParam": "",
        "fixedValue": "",
        "scriptValue": ""
      },
      {
        "paramName": "processDate",
        "queryParam": "",
        "fixedValue": "",
        "scriptValue": ""
      },
      {
        "paramName": "auditTime",
        "queryParam": "",
        "fixedValue": "",
        "scriptValue": ""
      },
      {
        "paramName": "brokerSource",
        "queryParam": "",
        "fixedValue": "",
        "scriptValue": ""
      },
      {
        "paramName": "sourceSystemFlag",
        "queryParam": "",
        "fixedValue": "",
        "scriptValue": ""
      }
    ],
    "parameterQueryList": []
  },
  "description": "Prueba de concepto N2TR test.",
  "trigger": {
    "repeat": {
      "executionTime": "09:15",
      "maxExecutionTime": "22:05",
      "repeatEvery": 60
    },
    "type": "MONTHLY",
    "months": [
      "JANUARY",
      "FEBRUARY",
      "MARCH"
    ],
    "days": [
      1,
      2,
      3,
      4,
      7
    ],
    "eventWeek": "Second week",
    "weekDays": [
      "MONDAY",
      "TUESDAY",
      "WEDNESDAY",
      "THURSDAY",
      "FRIDAY"
    ]
  },
  "version": "1.0.1",
  "planCode": "loadCptyInitial",
  "paramAuditList": [
    {
      "audTs": "2025-09-10T09:16:12.497+02:00",
      "comments": "Commit by user T044139",
      "audUser": "T044139",
      "status": "NOT_PUBLISHED"
    },
    {
      "audTs": "2025-09-10T09:17:41.633+02:00",
      "comments": "Commit by user T044139",
      "audUser": "T044139",
      "status": "PUBLISHED"
    }
  ],
  "conditionList": [
    {
      "name": "findCptyFile",
      "fileWatcher": {
        "path": "/N2TR/incoming/",
        "filePattern": "*cpty*.csv",
        "fileParameterName": "sourceFileCpty"
      },
      "forceAtEnd": false,
      "checkEvery": 60,
      "script": ""
    }
  ],
  "recordVersion": 1,
  "uuaa": "n2tr",
  "status": "PUBLISHED",
  "_class": "com.bbva.gkxj.apirestgen.schedulerconfig.model.PlanConfig",
  "calendarCode": "CALENDAR2"
}