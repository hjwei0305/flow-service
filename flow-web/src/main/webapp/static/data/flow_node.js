var _flownode = {
    event: [{
        name: "startEventText",//对应多语言里面的key
        type: "StartEvent",
        css: "flow-event-start"
    }, {
        name: "endEventText",
        type: "EndEvent",
        css: "flow-event-end"
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
    }],
    gateway: [{
        name: "exclusiveGatewayText",
        type: "ExclusiveGateway",
        css: "exclusivegateway",
        busType: "ExclusiveGateway"
    }, {
        name: "manualExclusiveGatewayText",
        type: "ExclusiveGateway",
        css: "exclusivegateway",
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