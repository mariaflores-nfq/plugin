{
  "_id": "66b40b8ea8fe263d24de8447",
  "failedTreatment": {
    "level": "No level",
    "technicalCode": "",
    "issueCode": ""
  },
  "description": "Scheduler para probar la subida de ficheros a Epsilon.",
  "trigger": {
    "repeat": {
      "executionTime": "09:00",
      "maxExecutionTime": "21:00",
      "repeatEvery": 3600
    },
    "type": "DAILY"
  },
  "version": "v1.0.2",
  "planCode": "file2EpsilonsourceFilePrices",
  "paramAuditList": [
    {
      "audTs": "2024-08-08T20:19:24.786+02:00",
      "comments": "",
      "audUser": "TXXXXXX",
      "status": "NOT_PUBLISHED"
    },
    {
      "audTs": "2024-08-14T12:18:40.489+02:00",
      "comments": "Published configuration",
      "audUser": "XE83678",
      "status": "PUBLISHED"
    },
    {
      "audTs": "2024-08-21T08:41:59.790+02:00",
      "comments": "Desactived configuration",
      "audUser": "XE83678",
      "status": "INACTIVE"
    }
  ],
  "conditionList": [
    {
      "name": "COMREG1Watcher",
      "forceAtEnd": false,
      "checkEvery": 31,
      "script": "pturn",
      "fileWatcher": {
        "path": "/MRRF/mrrf/incoming/",
        "filePattern": "MX3_SA_GROSS_JTD*",
        "fileParameterName": "fileName"
      }
    },
    {
      "name": "PruebaQuery",
      "forceAtEnd": false,
      "checkEvery": 15,
      "script": "var\nconlon.",
      "query": {
        "dbSource": "Data",
        "mongoQuery": {
          "collection": "CGKXJ_COLECTION",
          "filter": [
            "{ $match : {\u0027uuss\u0027 : \u0027,ttg\u0027}}",
            "{$project : {0uuaa\u0027 : 1}}"
          ]
        }
      }
    },
    {
      "name": "NovaTransferCond",
      "forceAtEnd": false,
      "checkEvery": 3600,
      "script": "",
      "novaTransferWatcher": {
        "path": "/XCTT/incoming",
        "filePattern": "",
        "fileParameterName": "",
        "transferName": "TRANSFER_LOG"
      }
    }
  ],
  "recordVersion": 6,
  "uuaa": "mrrf",
  "status": "INACTIVE",
  "calendarCode": "CALENDAR1",
  "batch": {
    "batchCode": "file2Epsilon",
    "jobParameterList": [
      {
        "paramName": "sourceFilePrices",
        "queryParam": "",
        "fixedValue": "",
        "scriptValue": ""
      }
    ],
    "parameterQueryList": [
      {
        "queryCode": "sourceFilePrices",
        "dbSource": "Config",
        "mongoQuery": {
          "collection": "",
          "filter": []
        }
      }
    ]
  }
}