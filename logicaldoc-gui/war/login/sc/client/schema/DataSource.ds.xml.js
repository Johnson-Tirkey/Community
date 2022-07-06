isc.DataSource.create({
    Constructor:"DataSource",
    isServerDS:true,
    allowAdvancedCriteria:true,
    addGlobalId:"false",
    ID:"DataSource",
    fields:[
        {
            type:"string",
            xmlAttribute:"true",
            name:"ID",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"description",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"autoIdField",
            validators:[
            ]
        },
        {
            title:"Superclass",
            type:"string",
            name:"inheritsFrom",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"useParentFieldOrder",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"useLocalFieldsOnly",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"restrictToParentFields",
            validators:[
            ]
        },
        {
            valueMap:{
                custom:"Custom Binding",
                iscServer:"ISC Java Server",
                json:"JSON Web Service",
                xml:"XML / WSDL Web Service"
            },
            title:"DataFormat",
            type:"string",
            xmlAttribute:"true",
            name:"dataFormat",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"useStrictJSON",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"apidoc",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"isSampleDS",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"noAutoFetch",
            validators:[
            ]
        },
        {
            valueMap:{
                exec:"executes a command external to the JVM",
                generic:"Requests will be delivered to the server and you are expected to write Java code to create a valid response",
                hibernate:"ISC Server Hibernate Connectors",
                jpa:"Use SmartClient's built-in JPA 2.0 connector",
                jpa1:"Use SmartClient's built-in JPA 1.0 connector",
                rest:"Server-side REST implementation",
                sql:"ISC Server SQL Connectors",
                stream:"DataSource connected to a messaging broker"
            },
            title:"Server Type",
            type:"string",
            xmlAttribute:"true",
            name:"serverType",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"schemaBean",
            validators:[
            ]
        },
        {
            title:"Callback Parameter",
            type:"string",
            xmlAttribute:"true",
            name:"callbackParam",
            validators:[
            ]
        },
        {
            type:"Object",
            name:"requestProperties",
            validators:[
            ]
        },
        {
            childTagName:"field",
            multiple:"true",
            propertiesOnly:"true",
            type:"DataSourceField",
            name:"fields",
            validators:[
            ]
        },
        {
            title:"Add Global ID",
            type:"boolean",
            name:"addGlobalId",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"showPrompt",
            validators:[
            ]
        },
        {
            title:"DataSource Version",
            type:"number",
            visibility:"internal",
            xmlAttribute:"true",
            name:"dataSourceVersion",
            validators:[
            ]
        },
        {
            title:"Database Name",
            type:"string",
            xmlAttribute:"true",
            name:"dbName",
            validators:[
            ]
        },
        {
            title:"Schema",
            type:"string",
            xmlAttribute:"true",
            name:"schema",
            validators:[
            ]
        },
        {
            title:"Table Name",
            type:"string",
            xmlAttribute:"true",
            name:"tableName",
            validators:[
            ]
        },
        {
            title:"Quote Table Name?",
            type:"boolean",
            xmlAttribute:"true",
            name:"quoteTableName",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"tableCode",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"tableCharacterSet",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"strictSQLFiltering",
            validators:[
            ]
        },
        {
            type:"string",
            name:"relatedTableAlias",
            validators:[
            ]
        },
        {
            title:"Stream realtime updates for changes to this datasource?",
            type:"boolean",
            xmlAttribute:"true",
            name:"realtimeUpdates",
            validators:[
            ]
        },
        {
            type:"ServerObject",
            name:"serverObject",
            validators:[
            ]
        },
        {
            type:"string",
            name:"serverConstructor",
            validators:[
            ]
        },
        {
            type:"Object",
            name:"serverConfig",
            validators:[
            ]
        },
        {
            multiple:"true",
            type:"OperationBinding",
            name:"operationBindings",
            validators:[
            ]
        },
        {
            hidden:"true",
            propertiesOnly:"true",
            type:"DataSourceField",
            moveTo:"fields",
            name:"field",
            validators:[
            ]
        },
        {
            hidden:"true",
            type:"OperationBinding",
            moveTo:"operationBindings",
            name:"operationBinding",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"serviceNamespace",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"dataURL",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"dataProtocol",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"dataTransport",
            validators:[
            ]
        },
        {
            type:"Object",
            name:"defaultParams",
            validators:[
            ]
        },
        {
            type:"string",
            name:"soapAction",
            validators:[
            ]
        },
        {
            type:"string",
            name:"jsonPrefix",
            validators:[
            ]
        },
        {
            type:"string",
            name:"jsonSuffix",
            validators:[
            ]
        },
        {
            type:"string",
            name:"messageTemplate",
            validators:[
            ]
        },
        {
            propertiesOnly:"true",
            type:"Object",
            visibility:"internal",
            name:"defaultCriteria",
            validators:[
            ]
        },
        {
            type:"string",
            visibility:"xmlBinding",
            name:"tagName",
            validators:[
            ]
        },
        {
            type:"XPath",
            name:"recordXPath",
            validators:[
            ]
        },
        {
            type:"string",
            name:"recordName",
            validators:[
            ]
        },
        {
            type:"Object",
            name:"xmlNamespaces",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"dropExtraFields",
            validators:[
            ]
        },
        {
            type:"string",
            visibility:"internal",
            xmlAttribute:"true",
            name:"schemaNamespace",
            validators:[
            ]
        },
        {
            type:"boolean",
            visibility:"internal",
            name:"mustQualify",
            validators:[
            ]
        },
        {
            type:"boolean",
            visibility:"internal",
            name:"xsdSimpleContent",
            validators:[
            ]
        },
        {
            type:"boolean",
            visibility:"internal",
            name:"xsdAnyElement",
            validators:[
            ]
        },
        {
            type:"boolean",
            visibility:"internal",
            name:"xsdAbstract",
            validators:[
            ]
        },
        {
            title:"Title",
            type:"string",
            name:"title",
            validators:[
            ]
        },
        {
            title:"Title Field",
            type:"string",
            name:"titleField",
            validators:[
            ]
        },
        {
            title:"Plural Title",
            type:"string",
            name:"pluralTitle",
            validators:[
            ]
        },
        {
            title:"Client Only",
            type:"boolean",
            xmlAttribute:"true",
            name:"clientOnly",
            validators:[
            ]
        },
        {
            title:"Test File Name",
            type:"URL",
            xmlAttribute:"true",
            name:"testFileName",
            validators:[
            ]
        },
        {
            type:"URL",
            xmlAttribute:"true",
            name:"dbImportFileName",
            validators:[
            ]
        },
        {
            multiple:"true",
            skipSerialization:"true",
            type:"Object",
            name:"testData",
            validators:[
            ]
        },
        {
            multiple:"true",
            type:"Object",
            name:"cacheData",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"cacheAllData",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"cacheAcrossOperationIds",
            validators:[
            ]
        },
        {
            multiple:"true",
            hidden:"true",
            type:"SandboxedTestFile",
            name:"sandboxedTestFiles",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"mockMode",
            validators:[
            ]
        },
        {
            type:"integer",
            xmlAttribute:"true",
            name:"mockDataRows",
            validators:[
            ]
        },
        {
            multiple:"true",
            type:"Criterion",
            name:"mockDataCriteria",
            validators:[
            ]
        },
        {
            multiple:"true",
            propertiesOnly:"true",
            type:"DataSourceField",
            uniqueProperty:"ID",
            visibility:"internal",
            name:"types",
            validators:[
            ]
        },
        {
            multiple:"true",
            type:"string",
            visibility:"internal",
            name:"groups",
            validators:[
            ]
        },
        {
            multiple:"true",
            type:"MethodDeclaration",
            visibility:"internal",
            name:"methods",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"showSuperClassActions",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"createStandalone",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"useFlatFields",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"showLocalFieldsOnly",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"showSuperClassEvents",
            validators:[
            ]
        },
        {
            type:"Object",
            name:"globalNamespaces",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"autoDeriveSchema",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"autoDeriveFKs",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"useLocalValidators",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"autoDeriveTitles",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"qualifyColumnNames",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"validateRelatedRecords",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"requiresAuthentication",
            validators:[
            ]
        },
        {
            type:"string",
            name:"requiresRole",
            validators:[
            ]
        },
        {
            type:"string",
            name:"requires",
            validators:[
            ]
        },
        {
            type:"string",
            name:"ownerIdField",
            validators:[
            ]
        },
        {
            type:"string",
            name:"ownerIdNullRole",
            validators:[
            ]
        },
        {
            valueMap:{
                edit:"all users can fetch and update",
                none:"fetch, update disallowed",
                view:"all users can fetch"
            },
            type:"string",
            xmlAttribute:"true",
            name:"ownerIdNullAccess",
            validators:[
            ]
        },
        {
            type:"string",
            name:"guestUserId",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"beanClassName",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"autoJoinTransactions",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"useAnsiJoins",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"useSpringTransaction",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"sparseUpdates",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"noNullUpdates",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"canExport",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"progressiveLoading",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"autoConvertRelativeDates",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"allowAdvancedCriteria",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"dropUnknownCriteria",
            validators:[
            ]
        },
        {
            type:"string",
            name:"scriptImport",
            validators:[
            ]
        },
        {
            idAllowed:"true",
            type:"Object",
            name:"script",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"allowClientRequestedSummaries",
            validators:[
            ]
        },
        {
            multiple:"true",
            type:"string",
            name:"patternMultiWildcard",
            validators:[
            ]
        },
        {
            multiple:"true",
            type:"string",
            name:"patternSingleWildcard",
            validators:[
            ]
        },
        {
            type:"string",
            valueMap:{
                dropOnChange:"Drop on criteria change",
                dropOnShortening:"Drop if criteria became more restrictive"
            },
            name:"criteriaPolicy",
            validators:[
            ]
        },
        {
            type:"string",
            name:"substituteClasses",
            validators:[
            ]
        },
        {
            type:"string",
            name:"idClassName",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"supportTransactions",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"creatorOverrides",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"loadParents",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"loadID",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"audit",
            validators:[
            ]
        },
        {
            type:"string",
            name:"auditDataSourceID",
            validators:[
            ]
        },
        {
            type:"string",
            name:"auditDSConstructor",
            validators:[
            ]
        },
        {
            type:"string",
            name:"auditRevisionFieldName",
            validators:[
            ]
        },
        {
            type:"string",
            name:"auditTimeStampFieldName",
            validators:[
            ]
        },
        {
            type:"string",
            name:"auditTypeFieldName",
            validators:[
            ]
        },
        {
            type:"string",
            name:"auditUserFieldName",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"compareMetadataForAuditChangeStatus",
            validators:[
            ]
        },
        {
            type:"integer",
            name:"auditChangedFieldsFieldLength",
            validators:[
            ]
        },
        {
            type:"string",
            name:"auditedDataSourceID",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"autoCreateAuditTable",
            validators:[
            ]
        },
        {
            type:"string",
            valueMap:{
                full:"Inherit fields by copying them onto the inheriting DataSource's underlying table.",
                none:"Do not physically inherit fields onto the inheriting DataSource's SQL table"
            },
            name:"inheritanceMode",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"quoteColumnNames",
            validators:[
            ]
        },
        {
            type:"string",
            xmlAttribute:"true",
            name:"generatedBy",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"useUTCDateTimes",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"useOfflineStorage",
            validators:[
            ]
        },
        {
            type:"string",
            name:"enumOrdinalProperty",
            validators:[
            ]
        },
        {
            type:"string",
            valueMap:{
                bean:"Translates to/from a Javascript object containing one property for each property defined within the enum.",
                ordinal:"Translates to/from an integer matching the ordinal number of the constant within the enumeration",
                string:"Translates to/from a String matching the constant name."
            },
            name:"enumTranslateStrategy",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"xmlFromConfig",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"translatePatternOperators",
            validators:[
            ]
        },
        {
            type:"string",
            name:"fileNameField",
            validators:[
            ]
        },
        {
            type:"string",
            name:"fileTypeField",
            validators:[
            ]
        },
        {
            type:"string",
            name:"fileFormatField",
            validators:[
            ]
        },
        {
            type:"string",
            name:"fileSizeField",
            validators:[
            ]
        },
        {
            type:"string",
            name:"fileLastModifiedField",
            validators:[
            ]
        },
        {
            type:"string",
            name:"fileContentsField",
            validators:[
            ]
        },
        {
            type:"string",
            name:"fileVersionField",
            validators:[
            ]
        },
        {
            type:"string",
            name:"fileSourceExtensionFields",
            validators:[
            ]
        },
        {
            type:"string",
            name:"projectFileKey",
            validators:[
            ]
        },
        {
            childTagName:"location",
            multiple:"true",
            type:"string",
            name:"projectFileLocations",
            validators:[
            ]
        },
        {
            type:"string",
            name:"defaultTextMatchStyle",
            validators:[
            ]
        },
        {
            type:"boolean",
            name:"ignoreTextMatchStyleCaseSensitive",
            validators:[
            ]
        },
        {
            valueMap:{
                always:"PK always required no matter what",
                clientRequest:"PK required for client requests only",
                never:"PK never required",
                rpcManager:"PK required for requests associated with RPCManager"
            },
            type:"string",
            xmlAttribute:"true",
            name:"defaultMultiUpdatePolicy",
            validators:[
            ]
        },
        {
            type:"string",
            name:"maxFileSizeExceededMessage",
            validators:[
            ]
        },
        {
            title:"Required Message",
            type:"string",
            xmlAttribute:"true",
            name:"requiredMessage",
            validators:[
            ]
        },
        {
            type:"AdvancedCriteria",
            xmlAttribute:"true",
            name:"implicitCriteria",
            validators:[
            ]
        },
        {
            type:"boolean",
            visibility:"internal",
            xmlAttribute:"true",
            name:"immutableAcrossSandboxes",
            validators:[
            ]
        },
        {
            title:"Is a Reify sample DS?",
            type:"boolean",
            xmlAttribute:"true",
            name:"sample",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"readOnly",
            validators:[
            ]
        },
        {
            type:"boolean",
            xmlAttribute:"true",
            name:"canDelete",
            validators:[
            ]
        },
        {
            type:"text",
            xmlAttribute:"true",
            name:"unionOf",
            validators:[
            ]
        },
        {
            type:"text",
            xmlAttribute:"true",
            name:"unionFields",
            validators:[
            ]
        },
        {
            valueMap:{
                all:"Include all fields from all member databases",
                intersect:"Only include fields that appear on all member dataSources (match by name)",
                matching:"Only include fields that appear on two or more member dataSources (match by name)"
            },
            type:"text",
            xmlAttribute:"true",
            name:"defaultUnionFieldsStrategy",
            validators:[
            ]
        }
    ]
})
