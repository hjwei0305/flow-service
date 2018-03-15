var _flownode = {
    event: [{
        name: "startEventText",//对应多语言里面的key
        type: "StartEvent",
        css: "flow-event-start"
    }, {
        name: "endEventText",
        type: "EndEvent",
        css: "flow-event-end"
    }, {
        name: "terminateEndEventText",
        type: "TerminateEndEvent",
        css: "flow-event-terminateend"
    }],
    task: [{
        name: "normalTaskText",
        type: "UserTask",
        css: "usertask",
        nodeType: "Normal"
    }, {
        name: "singleSignTaskText",
        type: "UserTask",
        css: "singletask",
        nodeType: "SingleSign"
    }, {
        name: "counterSignTaskText",
        type: "UserTask",
        css: "countertask",
        nodeType: "CounterSign"
    }, {
        name: "approveTaskText",
        type: "UserTask",
        css: "approvetask",
        nodeType: "Approve"
    }, {
        name: "parallelTaskText",
        type: "UserTask",
        css: "paralleltask",
        nodeType: "ParallelTask"
    }, {
        name: "serialTaskText",
        type: "UserTask",
        css: "serialtask",
        nodeType: "SerialTask"
    }
    , {
        name: "serviceTaskText",
        type: "ServiceTask",
        css: "servicetask ",
        nodeType: "ServiceTask"
    }
    , {
            name: "manualTaskText",
            type: "ManualTask",
            css: "manualtask ",
            nodeType: "ManualTask"
        }
        , {
            name: "receiveTaskText",
            type: "ReceiveTask",
            css: "receiveTask ",
            nodeType: "ReceiveTask"
        }
        , {
            name: "poolTaskText",
            type: "PoolTask",
            css: "poolTask ",
            nodeType: "PoolTask"
        }
        , {
            name: "callActivityText",
            type: "CallActivity",
            css: "callActivity ",
            nodeType: "CallActivity"
        }
    ],
    gateway: [{
        name: "exclusiveGatewayText",
        type: "ExclusiveGateway",
        css: "exclusivegateway",
        busType: "ExclusiveGateway"
    }, {
        name: "manualExclusiveGatewayText",
        type: "ExclusiveGateway",
        css: "manualExclusivegateway",
        busType: "ManualExclusiveGateway"
    }, {
        name: "parallelGatewayText",
        type: "ParallelGateway",
        css: "parallelgateway",
        busType: "ParallelGateway"
    }, {
        name: "inclusiveGatewayText",
        type: "InclusiveGateway",
        css: "inclusivegateway",
        busType: "InclusiveGateway"
    }, {
        name: "eventGatewayText",
        type: "EventGateway",
        css: "eventgateway",
        busType: "EventGateway"
    }]
};