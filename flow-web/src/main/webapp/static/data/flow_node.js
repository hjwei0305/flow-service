var _flownode = {
	event : [{
				name : "startEventText",//对应多语言里面的key
				type : "StartEvent",
				css : "flow-event-start"
			}, {
				name : "endEventText",
				type : "EndEvent",
				css : "flow-event-end"
			}],
	task : [{
				name : "serviceTaskText",
				type : "UserTask",
				css : "usertask"
			}, {
				name : "scriptTaskText",
				type : "ServiceTask",
				css : "servicetask"
			}, {
				name : "emailTaskText",
				type : "ScriptTask",
				css : "scripttask"
			}, {
				name : "manualTaskText",
				type : "mailTask",
				css : "emailtask"
			}, {
				name : "exclusiveGatewayText",
				type : "ManualTask",
				css : "manualtask"
			}],
	gateway : [{
				name : "exclusiveGatewayText",
				type : "ExclusiveGateway",
				css : "exclusivegateway"
			},{
				name : "parallelGatewayText",
				type : "ParallelGateway",
				css : "parallelgateway"
			},{
				name : "inclusiveGatewayText",
				type : "InclusiveGateway",
				css : "inclusivegateway"
			},{
				name : "eventGatewayText",
				type : "EventGateway",
				css : "eventgateway"
			}]
};