if (env.equals("Production")){
 return ["Prod1"]
}else if (env.equals("QA")){
 return ["QA1","QA2"]
}else if ( env.equals("Development")){
 return ["Dev1","Dev2","Dev3"]
}else if(env.equals("Others")){
 return "<input name=\"value\" value=\"${server}\" type=\"text\">"
}else{
return ["Select a server from dropdown"]
}