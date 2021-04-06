if (sub_env.equals("Prod1")){
 return ["Prod-server-1","Prod-server2"]
}else if (sub_env.equals("QA1")){
 return ["QA_server1","QA_server2","QA_server3"]
}else if ( sub_env.equals("QA2")){
 return ["No Servers for this sub_env"]
}else if( sub_env.equals("Dev1") ){
 return ["Dev-server-1","Dev-server-2","Dev-server-3"]
}else if( sub_env.equals("Dev2") ){
return ["No Servers for this sub_env"]
}else if( sub_env.equals("Dev3") ){
 return ["Dev-server-1","Dev-server-2","Dev-server-3"]
}else{
return ["Select a server from dropdown"]
}