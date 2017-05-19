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
        css: "usertask",
        nodeType: "SingleSign"
    }, {
        name: "counterSignTaskText",
        type: "UserTask",
        css: "usertask",
        nodeType: "CounterSign"
    }],
    gateway: [{
        name: "exclusiveGatewayText",
        type: "ExclusiveGateway",
        css: "exclusivegateway"
    }, {
        name: "parallelGatewayText",
        type: "ParallelGateway",
        css: "parallelgateway"
    }, {
        name: "inclusiveGatewayText",
        type: "InclusiveGateway",
        css: "inclusivegateway"
    }, {
        name: "eventGatewayText",
        type: "EventGateway",
        css: "eventgateway"
    }]
};