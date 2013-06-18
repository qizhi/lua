var FIREBASE=FIREBASE||{};
var utf8=utf8||{};
FIREBASE=FIREBASE||{};
FIREBASE.ByteArray=function(array){var MAX_INT32=4294967295;
var MAX_SIGNED_INT32=2147483647;
var MAX_INT16=65535;
var MAX_SIGNED_INT16=32767;
var MAX_BYTE=255;
var MAX_SIGNED_BYTE=127;
this.buffer=[];
if(array){this.buffer=array
}else{this.buffer=[]
}this.position=0;
this.remaining=function(){return this.buffer.length-this.position
};
this.getBuffer=function(){return this.buffer
};
this.readBoolean=function(){this.checkBuffer(1);
return this.readByte()!==0
};
this.checkBuffer=function(size){if(this.remaining()<size){FIREBASE.FirebaseException.Throw(FIREBASE.ErrorCodes.BUFFER_UNDERRUN,"Buffer underrun")
}};
this.readInt=function(){this.checkBuffer(4);
var value=(this.buffer[this.position++]<<24);
value+=(this.buffer[this.position++]<<16);
value+=(this.buffer[this.position++]<<8);
value+=this.buffer[this.position++];
if(value>MAX_SIGNED_INT32){value-=MAX_INT32+1
}return value
};
this.readShort=function(){this.checkBuffer(2);
var value=(this.buffer[this.position++]<<8);
value+=this.buffer[this.position++];
if(value>MAX_SIGNED_INT16){value-=MAX_INT16+1
}return value
};
this.readByte=function(){this.checkBuffer(1);
var value=this.buffer[this.position++];
if(value>MAX_SIGNED_BYTE){value-=MAX_BYTE+1
}return value
};
this.readUnsignedInt=function(){var value=this.readInt();
if(value<0){value+=MAX_INT32+1
}return value
};
this.readUnsignedShort=function(){var value=this.readShort();
if(value<0){value+=MAX_INT16+1
}return value
};
this.readUnsignedByte=function(){var value=this.readByte();
if(value<0){value+=MAX_BYTE+1
}return value
};
this.readString=function(){this.checkBuffer(2);
var length=this.readShort();
var byteArray=this.buffer.splice(this.position,length);
return utf8.fromByteArray(byteArray)
};
this.writeBoolean=function(value){this.writeByte(value===true?1:0)
};
this.writeString=function(str){var i;
var byteArray=utf8.toByteArray(str);
this.writeUnsignedShort(byteArray.length);
for(i=0;
i<byteArray.length;
i++){this.writeUnsignedByte(byteArray[i])
}};
this.writeInt=function(value){this.buffer.push((value&4278190080)>>24);
this.buffer.push((value&16711680)>>16);
this.buffer.push((value&65280)>>8);
this.buffer.push(value&255)
};
this.writeShort=function(value){this.buffer.push(((value&65280)>>8));
this.buffer.push(value&255)
};
this.writeByte=function(value){this.buffer.push(value&255)
};
this.writeUnsignedInt=function(value){if(value<0){value+=MAX_INT32
}this.writeInt(value)
};
this.writeUnsignedShort=function(value){value&=65535;
if(value<0){value+=MAX_INT16
}this.writeShort(value)
};
this.writeUnsignedByte=function(value){value&=255;
if(value<0){value+=MAX_BYTE
}this.writeByte(value)
};
this.createServiceDataArray=function(classId){return this.createGameDataArray(classId)
};
this.createGameDataArray=function(classId){var header=new FIREBASE.ByteArray();
header.writeUnsignedInt(this.buffer.length);
header.writeUnsignedByte(classId);
var arrayToSend=header.buffer.concat(this.buffer);
return arrayToSend
};
this.writeArray=function(byteArray){var newBuffer=this.buffer.concat(byteArray.buffer);
this.buffer=newBuffer
}
};
FIREBASE.ByteArray.fromBase64String=function(input){var result=[];
var _keyStr="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
var chr1,chr2,chr3;
var enc1,enc2,enc3,enc4;
var i=0;
input=input.replace(/[^A-Za-z0-9\+\/\=]/g,"");
while(i<input.length){enc1=_keyStr.indexOf(input.charAt(i++));
enc2=_keyStr.indexOf(input.charAt(i++));
enc3=_keyStr.indexOf(input.charAt(i++));
enc4=_keyStr.indexOf(input.charAt(i++));
chr1=(enc1<<2)|(enc2>>4);
chr2=((enc2&15)<<4)|(enc3>>2);
chr3=((enc3&3)<<6)|enc4;
result.push(chr1);
if(enc3!==64){result.push(chr2)
}if(enc4!==64){result.push(chr3)
}}return result
};
FIREBASE.ByteArray.toBase64String=function(input){var output="";
var _keyStr="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
var i=0;
var current=0;
var previous=0;
var byteNum=0;
while(i<input.length){current=input[i];
byteNum=i%3;
switch(byteNum){case 0:output+=_keyStr.charAt(current>>2);
break;
case 1:output+=_keyStr.charAt((previous&3)<<4|(current>>4));
break;
case 2:output+=_keyStr.charAt((previous&15)<<2|(current>>6));
output+=_keyStr.charAt(current&63);
break
}previous=current;
i++
}if(byteNum===0){output+=_keyStr.charAt((previous&3)<<4);
output+="=="
}else{if(byteNum===1){output+=_keyStr.charAt((previous&15)<<2);
output+="="
}}return output
};var FIREBASE=FIREBASE||{};
var org=org||{};
org.cometd=org.cometd||{};
FIREBASE.CometdAdapter=function(hostname,port,endpoint,secure,cometdAccess){var _hostname=hostname;
var _secure=secure!==undefined?secure:false;
var _endpoint=endpoint;
var _port=port;
var _statusCallback;
var _dataCallback;
var _connected=false;
var _firstConnect=false;
this.cometd=cometdAccess();
this.protocol=_secure?"https://":"http://";
this.firebaseUrl=this.protocol+_hostname;
var _instance=this.cometd;
if(_port){this.firebaseUrl+=":"+_port.toString()
}if(_endpoint){if(_endpoint.charAt(0)==="/"){this.firebaseUrl+=_endpoint
}else{this.firebaseUrl+="/"+_endpoint
}}_instance.unregisterTransport("websocket");
_instance.configure({url:this.firebaseUrl});
var _reportConnected=function(){_statusCallback(FIREBASE.ConnectionStatus.CONNECTED);
_firstConnect=false
};
var _reportDisconnected=function(){_statusCallback(FIREBASE.ConnectionStatus.DISCONNECTED);
_firstConnect=true
};
var _subscribe=function(){_instance.subscribe("/service/client",function(message){_dataCallback({data:org.cometd.JSON.toJSON(message.data)})
})
};
var _connect=function(){_statusCallback(FIREBASE.ConnectionStatus.CONNECTING);
_instance.handshake()
};
_instance.addListener("/meta/handshake",function(message){if(message.failure){_reportDisconnected()
}});
_instance.addListener("/meta/connect",function(message){if(!_instance.isDisconnected()){var wasConnected=_connected;
_connected=message.successful;
if(!wasConnected&&_connected){_reportConnected();
_subscribe()
}else{if(wasConnected&&!_connected){_reportDisconnected()
}else{if(_connected){if(_firstConnect){_reportConnected();
_subscribe()
}}else{_reportDisconnected()
}}}}});
this.reconnect=function(){_connect()
};
this.connect=function(statusCallback,dataCallback){_statusCallback=statusCallback;
_dataCallback=dataCallback;
_connect()
};
this.send=function(message){_instance.publish("/service/client",org.cometd.JSON.fromJSON(message))
}
};var FIREBASE=FIREBASE||{};
FIREBASE.ConnectionStatus={CONNECTING:1,CONNECTED:2,DISCONNECTED:3,RECONNECTING:4,RECONNECTED:5,FAIL:6,CANCELLED:7,toString:function(status){var key;
for(key in this){if(this[key]===status){return key
}}}};var FIREBASE=FIREBASE||{};
var FB_PROTOCOL=FB_PROTOCOL||{};
var utf8=utf8||{};
FIREBASE.Connector=function(packetCallback,lobbyCallback,loginCallback,statusCallback){var _packetCallback=packetCallback;
var _lobbyCallback=lobbyCallback;
var _loginCallback=loginCallback;
var _statusCallback=statusCallback;
var _ioAdapter;
var _reconnecting=false;
var _reconnectAttempts=0;
var _reconnectInterval=FIREBASE.ReconnectStrategy.RECONNECT_START_INTERVAL;
var _reconnectTimer={};
var _instance=this;
var _cancelled=false;
var _reconnect=function(){clearTimeout(_reconnectTimer);
_ioAdapter.reconnect()
};
var getClass=function(className){var nameParts=className.split(".");
var classConstructor=window;
var i;
for(i=0;
i<nameParts.length;
i++){if(classConstructor[nameParts[i]]!==undefined){classConstructor=classConstructor[nameParts[i]]
}else{return undefined
}}return classConstructor
};
var _handleLoginResponse=function(loginResponse){if(_loginCallback){_loginCallback(loginResponse.status,loginResponse.pid,loginResponse.screenname)
}};
var _handleDisconnect=function(){if(_cancelled){return
}if(!_reconnecting){if(FIREBASE.ReconnectStrategy.MAX_ATTEMPTS===0){_statusCallback(FIREBASE.ConnectionStatus.DISCONNECTED,0,"Disconnected");
console.log("SKIPPED RECONNECTING");
return
}else{_statusCallback(FIREBASE.ConnectionStatus.DISCONNECTED,0,"Disconnected");
_reconnecting=true;
_reconnectAttempts=1;
_reconnectInterval=FIREBASE.ReconnectStrategy.RECONNECT_START_INTERVAL;
console.log("START RECONNECTING")
}}else{_reconnectAttempts++
}if(_reconnectAttempts>FIREBASE.ReconnectStrategy.MAX_ATTEMPTS){_statusCallback(FIREBASE.ConnectionStatus.FAIL,_reconnectAttempts,"Too many reconnect attempts");
_reconnecting=false;
_cancelled=true;
console.log("STOP RECONNECTING")
}else{if(_reconnectAttempts>=FIREBASE.ReconnectStrategy.INCREASE_THRESHOLD_COUNT){_reconnectInterval+=FIREBASE.ReconnectStrategy.INTERVAL_INCREMENT_STEP
}console.log("Reconnect attempt "+_reconnectAttempts);
_statusCallback(FIREBASE.ConnectionStatus.RECONNECTING,_reconnectAttempts,"Reconnecting");
_reconnectTimer=setTimeout(_reconnect,_reconnectInterval)
}};
var _handleConnect=function(){if(_reconnecting){_statusCallback(FIREBASE.ConnectionStatus.RECONNECTED,0,"Reconnected");
_reconnecting=false
}_statusCallback(FIREBASE.ConnectionStatus.CONNECTED,0,"Connected")
};
var _handlePacket=function(protocolObject){switch(protocolObject.classId){case FB_PROTOCOL.LoginResponsePacket.CLASSID:_handleLoginResponse(protocolObject);
break;
case FB_PROTOCOL.TableQueryResponsePacket.CLASSID:case FB_PROTOCOL.TableSnapshotPacket.CLASSID:case FB_PROTOCOL.TableUpdatePacket.CLASSID:case FB_PROTOCOL.TableRemovedPacket.CLASSID:case FB_PROTOCOL.TableSnapshotListPacket.CLASSID:case FB_PROTOCOL.TableUpdateListPacket.CLASSID:case FB_PROTOCOL.TournamentRemovedPacket.CLASSID:case FB_PROTOCOL.TournamentSnapshotPacket.CLASSID:case FB_PROTOCOL.TournamentUpdatePacket.CLASSID:case FB_PROTOCOL.TournamentSnapshotListPacket.CLASSID:case FB_PROTOCOL.TournamentUpdateListPacket.CLASSID:if(_lobbyCallback){_lobbyCallback(protocolObject)
}else{if(_packetCallback){_packetCallback(protocolObject)
}}break;
default:if(_packetCallback){_packetCallback(protocolObject)
}break
}};
this.getIOAdapter=function(){return _ioAdapter
};
this.cancel=function(){if(_reconnecting){clearTimeout(_reconnectTimer)
}_statusCallback(FIREBASE.ConnectionStatus.CANCELLED,0,"Cancelled");
_cancelled=true
};
this.connect=function(ioAdapterName,hostname,port,endpoint,secure,extraConfig){var i;
var IoAdapterClass;
_cancelled=false;
secure=secure||false;
try{if(window){IoAdapterClass=getClass(ioAdapterName);
if(IoAdapterClass===undefined){_statusCallback(FIREBASE.ConnectionStatus.FAIL,FIREBASE.ErrorCodes.INVALID_IO_ADAPTER,ioAdapterName);
return
}_ioAdapter=new IoAdapterClass(hostname,port,endpoint,secure,extraConfig)
}_ioAdapter.connect(function(status){if(status===FIREBASE.ConnectionStatus.DISCONNECTED){_handleDisconnect()
}else{if(status===FIREBASE.ConnectionStatus.CONNECTED){_handleConnect()
}else{if(!_reconnecting){_statusCallback(status)
}}}},function(message){var protocolObjects=JSON.parse(message.data);
if(typeof(protocolObjects)===Array){for(i=0;
i<protocolObjects.length;
i++){_handlePacket(protocolObjects[i])
}}else{_handlePacket(protocolObjects)
}})
}catch(error){_statusCallback(FIREBASE.ConnectionStatus.FAIL,FIREBASE.ErrorCodes.IO_ADAPTER_ERROR,error.message)
}};
this.send=function(packet){if(_ioAdapter){_ioAdapter.send(packet)
}};
this.login=function(user,pwd,operatorid,credentials){var loginRequest=new FB_PROTOCOL.LoginRequestPacket();
loginRequest.user=user;
loginRequest.password=pwd;
loginRequest.operatorid=operatorid === undefined ? 1 : operatorid;
loginRequest.credentials=credentials||[];
this.sendProtocolObject(loginRequest)
};
this.logout=function(leaveTables){this.cancel();
var logoutRequest=new FB_PROTOCOL.LogoutPacket();
logoutRequest.leaveTables=leaveTables;
this.sendProtocolObject(logoutRequest)
};
this.lobbySubscribe=function(gameId,address){var subscribeRequest=new FB_PROTOCOL.LobbySubscribePacket();
subscribeRequest.type=FB_PROTOCOL.LobbyTypeEnum.REGULAR;
subscribeRequest.gameid=gameId;
subscribeRequest.address=address;
this.sendProtocolObject(subscribeRequest)
};
this.watchTable=function(tableId){var watchRequest=new FB_PROTOCOL.WatchRequestPacket();
watchRequest.tableid=tableId;
this.sendProtocolObject(watchRequest)
};
this.joinTable=function(tableId,seatId){var joinRequest=new FB_PROTOCOL.JoinRequestPacket();
joinRequest.tableid=tableId;
joinRequest.seat=seatId;
this.sendProtocolObject(joinRequest)
};
this.leaveTable=function(tableId){var leaveRequest=new FB_PROTOCOL.LeaveRequestPacket();
leaveRequest.tableid=tableId;
this.sendProtocolObject(leaveRequest)
};
this.sendStyxGameData=function(pid,tableid,protocolObject){var transportPacket=FIREBASE.Styx.wrapInGameTransportPacket(pid,tableid,protocolObject);
this.sendProtocolObject(transportPacket)
};
this.sendStringGameData=function(pid,tableid,string){var bytes=utf8.toByteArray(string);
this.sendBinaryGameData(pid,tableid,bytes)
};
this.sendBinaryGameData=function(pid,tableId,bytearray){var gameTransportPacket=new FB_PROTOCOL.GameTransportPacket();
gameTransportPacket.tableid=tableId;
gameTransportPacket.pid=pid;
gameTransportPacket.gamedata=FIREBASE.ByteArray.toBase64String(bytearray);
this.sendProtocolObject(gameTransportPacket)
};
this.sendServiceTransportPacket=function(pid,gameId,classId,serviceContract,byteArray){var serviceTransportPacket=new FB_PROTOCOL.ServiceTransportPacket();
serviceTransportPacket.gameid=gameId;
serviceTransportPacket.seq=-1;
serviceTransportPacket.pid=pid;
serviceTransportPacket.service=serviceContract;
serviceTransportPacket.idtype=FB_PROTOCOL.ServiceIdentifierEnum.CONTRACT;
serviceTransportPacket.servicedata=FIREBASE.ByteArray.toBase64String(byteArray.createServiceDataArray(classId));
console.log("SERVICETRANSPORT: %o",serviceTransportPacket);
this.sendProtocolObject(serviceTransportPacket)
};
this.sendProtocolObject=function(protocolObject){var jsonString=FIREBASE.Styx.toJSON(protocolObject);
this.send(jsonString)
}
};var FIREBASE=FIREBASE||{};
FIREBASE.ErrorCodes={INVALID_IO_ADAPTER:1,BUFFER_UNDERRUN:2,RECONNECT_FAILED:3,IO_ADAPTER_ERROR:4};var FIREBASE=FIREBASE||{};
FIREBASE.FirebaseException=function(errorCode,errorMessage){this.name="FIREBASE.FirebaseException";
this.message=errorMessage;
this.code=errorCode
};
FIREBASE.FirebaseException.Throw=function(errorCode,errorMessage){throw new FIREBASE.FirebaseException(errorCode,errorMessage)
};var JSON;
if(!JSON){JSON={}
}(function(){function f(n){return n<10?"0"+n:n
}if(typeof Date.prototype.toJSON!=="function"){Date.prototype.toJSON=function(key){return isFinite(this.valueOf())?this.getUTCFullYear()+"-"+f(this.getUTCMonth()+1)+"-"+f(this.getUTCDate())+"T"+f(this.getUTCHours())+":"+f(this.getUTCMinutes())+":"+f(this.getUTCSeconds())+"Z":null
};
String.prototype.toJSON=Number.prototype.toJSON=Boolean.prototype.toJSON=function(key){return this.valueOf()
}
}var cx=/[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,escapable=/[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,gap,indent,meta={"\b":"\\b","\t":"\\t","\n":"\\n","\f":"\\f","\r":"\\r",'"':'\\"',"\\":"\\\\"},rep;
function quote(string){escapable.lastIndex=0;
return escapable.test(string)?'"'+string.replace(escapable,function(a){var c=meta[a];
return typeof c==="string"?c:"\\u"+("0000"+a.charCodeAt(0).toString(16)).slice(-4)
})+'"':'"'+string+'"'
}function str(key,holder){var i,k,v,length,mind=gap,partial,value=holder[key];
if(value&&typeof value==="object"&&typeof value.toJSON==="function"){value=value.toJSON(key)
}if(typeof rep==="function"){value=rep.call(holder,key,value)
}switch(typeof value){case"string":return quote(value);
case"number":return isFinite(value)?String(value):"null";
case"boolean":case"null":return String(value);
case"object":if(!value){return"null"
}gap+=indent;
partial=[];
if(Object.prototype.toString.apply(value)==="[object Array]"){length=value.length;
for(i=0;
i<length;
i+=1){partial[i]=str(i,value)||"null"
}v=partial.length===0?"[]":gap?"[\n"+gap+partial.join(",\n"+gap)+"\n"+mind+"]":"["+partial.join(",")+"]";
gap=mind;
return v
}if(rep&&typeof rep==="object"){length=rep.length;
for(i=0;
i<length;
i+=1){if(typeof rep[i]==="string"){k=rep[i];
v=str(k,value);
if(v){partial.push(quote(k)+(gap?": ":":")+v)
}}}}else{for(k in value){if(Object.prototype.hasOwnProperty.call(value,k)){v=str(k,value);
if(v){partial.push(quote(k)+(gap?": ":":")+v)
}}}}v=partial.length===0?"{}":gap?"{\n"+gap+partial.join(",\n"+gap)+"\n"+mind+"}":"{"+partial.join(",")+"}";
gap=mind;
return v
}}if(typeof JSON.stringify!=="function"){JSON.stringify=function(value,replacer,space){var i;
gap="";
indent="";
if(typeof space==="number"){for(i=0;
i<space;
i+=1){indent+=" "
}}else{if(typeof space==="string"){indent=space
}}rep=replacer;
if(replacer&&typeof replacer!=="function"&&(typeof replacer!=="object"||typeof replacer.length!=="number")){throw new Error("JSON.stringify")
}return str("",{"":value})
}
}if(typeof JSON.parse!=="function"){JSON.parse=function(text,reviver){var j;
function walk(holder,key){var k,v,value=holder[key];
if(value&&typeof value==="object"){for(k in value){if(Object.prototype.hasOwnProperty.call(value,k)){v=walk(value,k);
if(v!==undefined){value[k]=v
}else{delete value[k]
}}}}return reviver.call(holder,key,value)
}text=String(text);
cx.lastIndex=0;
if(cx.test(text)){text=text.replace(cx,function(a){return"\\u"+("0000"+a.charCodeAt(0).toString(16)).slice(-4)
})
}if(/^[\],:{}\s]*$/.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,"@").replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,"]").replace(/(?:^|:|,)(?:\s*\[)+/g,""))){j=eval("("+text+")");
return typeof reviver==="function"?walk({"":j},""):j
}throw new SyntaxError("JSON.parse")
}
}}());var requirejs,require,define;
(function(){var defined={},aps=[].slice,req;
if(typeof define==="function"){return
}function normalize(name,baseName){if(name&&name.charAt(0)==="."){if(baseName){baseName=baseName.split("/");
baseName=baseName.slice(0,baseName.length-1);
name=baseName.concat(name.split("/"));
var i,part;
for(i=0;
(part=name[i]);
i++){if(part==="."){name.splice(i,1);
i-=1
}else{if(part===".."){if(i===1&&(name[2]===".."||name[0]==="..")){break
}else{if(i>0){name.splice(i-1,2);
i-=2
}}}}}name=name.join("/")
}}return name
}function makeRequire(relName,forceSync){return function(){return req.apply(null,aps.call(arguments,0).concat([relName,forceSync]))
}
}function makeNormalize(relName){return function(name){return normalize(name,relName)
}
}function makeLoad(depName){return function(value){defined[depName]=value
}
}function makeMap(name,relName){var prefix,plugin,index=name.indexOf("!");
if(index!==-1){prefix=normalize(name.slice(0,index),relName);
name=name.slice(index+1);
plugin=defined[prefix];
if(plugin&&plugin.normalize){name=plugin.normalize(name,makeNormalize(relName))
}else{name=normalize(name,relName)
}}else{name=normalize(name,relName)
}return{f:prefix?prefix+"!"+name:name,n:name,p:plugin}
}function main(name,deps,callback,relName){var args=[],usingExports,cjsModule,depName,i,ret,map;
if(!relName){relName=name
}if(typeof callback==="function"){if(deps){for(i=0;
i<deps.length;
i++){map=makeMap(deps[i],relName);
depName=map.f;
if(depName==="require"){args[i]=makeRequire(name)
}else{if(depName==="exports"){args[i]=defined[name]={};
usingExports=true
}else{if(depName==="module"){cjsModule=args[i]={id:name,uri:"",exports:defined[name]}
}else{if(depName in defined){args[i]=defined[depName]
}else{if(map.p){map.p.load(map.n,makeRequire(relName,true),makeLoad(depName),{});
args[i]=defined[depName]
}}}}}}}ret=callback.apply(defined[name],args);
if(name){if(cjsModule&&cjsModule.exports!==undefined){defined[name]=cjsModule.exports
}else{if(!usingExports){defined[name]=ret
}}}}else{if(name){defined[name]=callback
}}}requirejs=req=function(deps,callback,relName,forceSync){if(typeof deps==="string"){return defined[makeMap(deps,callback).f]
}else{if(!deps.splice){if(callback.splice){deps=callback;
callback=arguments[2]
}else{deps=[]
}}}if(forceSync){main(null,deps,callback,relName)
}else{setTimeout(function(){main(null,deps,callback,relName)
},15)
}return req
};
req.config=function(){return req
};
if(!require){require=req
}define=function(name,deps,callback){if(!deps.splice){callback=deps;
deps=[]
}main(name,deps,callback)
};
define.amd={}
}());var FIREBASE=FIREBASE||{};
FIREBASE.ReconnectStrategy={MAX_ATTEMPTS:0,RECONNECT_START_INTERVAL:1000,INCREASE_THRESHOLD_COUNT:Infinity,INTERVAL_INCREMENT_STEP:200};var FIREBASE=FIREBASE||{};
var FB_PROTOCOL=FB_PROTOCOL||{};
FIREBASE.Styx=function(){};
FIREBASE.Styx.wrapInGameTransportPacket=function(pid,tid,protocolObject){var gameTransportPacket=new FB_PROTOCOL.GameTransportPacket();
gameTransportPacket.tableid=tid;
gameTransportPacket.pid=pid;
var byteArray=protocolObject.save();
gameTransportPacket.gamedata=FIREBASE.ByteArray.toBase64String(byteArray.createGameDataArray(protocolObject.classId()));
return gameTransportPacket
};
FIREBASE.Styx.isByteArray=function(arr){var i;
for(i=0;
i<arr.length;
i++){if(typeof(arr[i])!=="number"||arr[i]>256){return false
}}return true
};
FIREBASE.Styx.cloneObject=function(protocolObject){var i,name="",newObject={};
for(name in protocolObject){if(typeof(protocolObject[name])==="object"){if(protocolObject[name] instanceof Array){if(protocolObject[name].length===0){newObject[name]=[]
}else{if(this.isByteArray(protocolObject[name])){newObject[name]=FIREBASE.ByteArray.toBase64String(protocolObject[name])
}else{newObject[name]=[];
for(i=0;
i<protocolObject[name].length;
i++){if(typeof(protocolObject[name][i])==="object"){newObject[name].push(FIREBASE.Styx.cloneObject(protocolObject[name][i]))
}else{newObject[name].push(protocolObject[name][i])
}}}}}else{newObject[name]=FIREBASE.Styx.cloneObject(protocolObject[name])
}}else{if(typeof(protocolObject[name])!=="function"){newObject[name]=protocolObject[name]
}else{if(name==="classId"){newObject[name]=protocolObject[name]()
}}}}return newObject
};
FIREBASE.Styx.writeParam=function(param,key,value){var byteArray=new FIREBASE.ByteArray();
if(typeof(value)==="string"){param.type=FB_PROTOCOL.ParameterTypeEnum.STRING;
byteArray.writeString(value)
}else{if(typeof(value)==="number"){param.type=FB_PROTOCOL.ParameterTypeEnum.INT;
byteArray.writeInt(value)
}else{return
}}param.key=key;
param.value=byteArray.getBuffer()
};
FIREBASE.Styx.readParam=function(param){var byteArray=new FIREBASE.ByteArray(param.value);
if(param.type===FB_PROTOCOL.ParameterTypeEnum.STRING){return byteArray.readString()
}else{if(param.type===FB_PROTOCOL.ParameterTypeEnum.INT){return byteArray.readInt()
}}return null
};
FIREBASE.Styx.getParam=function(param){var retObject={};
retObject.name=param.key;
retObject.value=FIREBASE.Styx.readParam(param);
return retObject
};
FIREBASE.Styx.toJSON=function(protocolObject){var objectClone=FIREBASE.Styx.cloneObject(protocolObject);
return JSON.stringify(objectClone)
};var utf8=utf8||{};
utf8.toByteArray=function(str){var i,j;
var bytes=[];
for(i=0;
i<str.length;
i++){if(str.charCodeAt(i)<=127){bytes.push(str.charCodeAt(i))
}else{var h=encodeURIComponent(str.charAt(i)).substr(1).split("%");
for(j=0;
j<h.length;
j++){bytes.push(parseInt(h[j],16))
}}}return bytes
};
utf8.fromByteArray=function(bytes){var i;
var str="";
for(i=0;
i<bytes.length;
i++){if(bytes[i]<=127){if(bytes[i]===37){str+="%25"
}else{str+=String.fromCharCode(bytes[i])
}}else{str+="%"+bytes[i].toString(16).toUpperCase()
}}return decodeURIComponent(str)
};var FIREBASE=FIREBASE||{};
FIREBASE.WebSocketAdapter=function(hostname,port,endpoint,secure,config){var _hostname=hostname;
var _secure=secure!==undefined?secure:false;
var _endpoint=endpoint;
var _port=port;
var _statusCallback;
var _dataCallback;
var _instance=this;
var _socket={};
this.protocol=_secure?"wss://":"ws://";
this.url=this.protocol+hostname;
if(port){this.url+=":"+port.toString()
}if(endpoint){if(endpoint.charAt(0)==="/"){this.url+=endpoint
}else{this.url+="/"+endpoint
}}this.getSocket=function(){return _socket
};
var _connect=function(){_statusCallback(FIREBASE.ConnectionStatus.CONNECTING);
_socket=new WebSocket(_instance.url);
_socket.onopen=function(){_statusCallback(FIREBASE.ConnectionStatus.CONNECTED)
};
_socket.onmessage=function(msg){_dataCallback(msg)
};
_socket.onclose=function(){_statusCallback(FIREBASE.ConnectionStatus.DISCONNECTED)
}
};
this.connect=function(statusCallback,dataCallback){_statusCallback=statusCallback;
_dataCallback=dataCallback;
_connect()
};
this.reconnect=function(){_connect()
};
this.send=function(message){_socket.send(message)
}
};